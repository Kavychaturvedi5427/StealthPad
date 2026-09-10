package com.kavya.stealthpad.synchronization;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.kavya.stealthpad.data.DataModel.NoteRequestDTO;
import com.kavya.stealthpad.data.DataModel.NoteResponseDTO;
import com.kavya.stealthpad.data.Local.Dao.NotesDao;
import com.kavya.stealthpad.data.Local.model.NotesModel;
import com.kavya.stealthpad.data.api.NotesApi;
import com.kavya.stealthpad.utils.EncryptionManager;
import com.kavya.stealthpad.utils.SyncStatus;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

@Singleton
public class SyncManager {

    private static final String TAG = "SyncManager";

    private final NotesDao notesDao;
    private final NotesApi notesApi;
    private final EncryptionManager encryptionManager;

    @Inject
    public SyncManager(
            NotesDao notesDao,
            NotesApi notesApi,
            EncryptionManager encryptionManager
    ) {
        this.notesDao = notesDao;
        this.notesApi = notesApi;
        this.encryptionManager = encryptionManager;
    }


    /*
     * ======================================
     * COMPLETE SYNCHRONIZATION
     * ======================================
     *
     * This method performs ONE synchronization.
     *
     * WorkManager will decide WHEN this method
     * should be executed.
     */

    public void sync(String userEmail) throws Exception {
        // First push local changes
        pushLocalChanges();

        // Then pull latest notes from server
        pullRemoteNotes(userEmail);
    }

    public static void scheduleSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                15, java.util.concurrent.TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "StealthPadSync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
    }


    /*
     * ======================================
     * PUSH LOCAL CHANGES
     * ======================================
     */

    private void pushLocalChanges() {
        List<NotesModel> pendingNotes =
                notesDao.getNotesPendingSync();

        if (pendingNotes == null || pendingNotes.isEmpty()) {
            return;
        }

        for (NotesModel note : pendingNotes) {
            try {
                switch (note.getSyncStatus()) {
                    case SyncStatus.PENDING:
                    case SyncStatus.PENDING_CREATE:
                        createNote(note);
                        break;

                    case SyncStatus.PENDING_UPDATE:
                        updateNote(note);
                        break;

                    case SyncStatus.PENDING_DELETE:
                        deleteNote(note);
                        break;

                    case SyncStatus.FAILED:
                        if (note.getServerId() == null) {
                            createNote(note);
                        } else {
                            updateNote(note);
                        }
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                Log.e(
                        TAG,
                        "Error syncing note: "
                                + note.getId(),
                        e
                );
            }
        }
    }


    /*
     * ======================================
     * CREATE NOTE
     * ======================================
     */

    private void createNote(NotesModel localNote)
            throws Exception {
        NoteRequestDTO request =
                createRequest(localNote);

        Response<NoteResponseDTO> response =
                notesApi
                        .createNote(request)
                        .execute();


        if (!response.isSuccessful()
                || response.body() == null) {

            Log.e(
                    TAG,
                    "Create failed. HTTP: "
                            + response.code()
            );

            return;
        }


        NoteResponseDTO serverNote =
                response.body();


        Long serverId =
                serverNote.getId();


        if (serverId == null) {

            Log.e(
                    TAG,
                    "Server did not return server ID"
            );

            return;
        }


        /*
         * Save server ID against local note
         * and mark synchronization successful.
         */

        notesDao.updateServerIdAndSyncStatus(
                localNote.getId(),
                serverId,
                SyncStatus.SUCCESS
        );
    }


    /*
     * ======================================
     * UPDATE NOTE
     * ======================================
     */

    private void updateNote(NotesModel localNote)
            throws Exception {

        Long serverId =
                localNote.getServerId();


        /*
         * If there is no server ID,
         * the note has never been uploaded.
         */

        if (serverId == null) {
            createNote(localNote);
            return;
        }


        NoteRequestDTO request =
                createRequest(localNote);


        Response<NoteResponseDTO> response =
                notesApi
                        .updateNote(
                                serverId,
                                request
                        )
                        .execute();


        if (response.isSuccessful()) {

            notesDao.updateSyncStatus(
                    localNote.getId(),
                    SyncStatus.SUCCESS
            );

        } else {

            Log.e(
                    TAG,
                    "Update failed. HTTP: "
                            + response.code()
            );
        }
    }


    /*
     * ======================================
     * DELETE NOTE
     * ======================================
     */

    private void deleteNote(NotesModel localNote)
            throws Exception {
        Long serverId =
                localNote.getServerId();


        /*
         * If there is no server ID, this note
         * only exists locally.
         *
         * Therefore there is nothing to delete
         * on the backend.
         */

        if (serverId == null) {
            notesDao.deleteNoteAfterSync(
                    localNote.getId()
            );
            return;
        }


        Response<Void> response =
                notesApi
                        .deleteById(serverId)
                        .execute();


        if (response.isSuccessful()) {

            /*
             * Delete from Room ONLY after the
             * backend confirms deletion.
             */

            notesDao.deleteNoteAfterSync(
                    localNote.getId()
            );

        } else {

            Log.e(
                    TAG,
                    "Delete failed. HTTP: "
                            + response.code()
            );

            /*
             * Keep PENDING_DELETE.
             *
             * The next synchronization will retry it.
             */
        }
    }


    /*
     * ======================================
     * PULL REMOTE NOTES
     * ======================================
     */

    private void pullRemoteNotes(String userEmail)
            throws Exception {
        Response<List<NoteResponseDTO>> response =
                notesApi
                        .getAllNotes()
                        .execute();


        if (!response.isSuccessful()
                || response.body() == null) {

            Log.e(
                    TAG,
                    "Failed to fetch server notes. HTTP: "
                            + response.code()
            );

            return;
        }


        List<NoteResponseDTO> remoteNotes =
                response.body();


        for (NoteResponseDTO remoteNote :
                remoteNotes) {

            if (remoteNote.getId() == null) {
                continue;
            }


            /*
             * Check whether this server note
             * already exists locally.
             */

            NotesModel localNote =
                    notesDao.getNoteByServerId(
                            remoteNote.getId()
                    );


            if (localNote == null) {

                /*
                 * Server note does not exist locally.
                 * Insert it into Room.
                 */

                NotesModel newNote =
                        convertToLocalNote(remoteNote, userEmail);

                notesDao.insert(newNote);

            } else {

                /*
                 * Do NOT overwrite local changes that
                 * haven't reached the server yet.
                 */

                if (localNote.getSyncStatus()
                        == SyncStatus.PENDING
                        || localNote.getSyncStatus()
                        == SyncStatus.PENDING_CREATE
                        || localNote.getSyncStatus()
                        == SyncStatus.PENDING_UPDATE
                        || localNote.getSyncStatus()
                        == SyncStatus.PENDING_DELETE) {
                    continue;
                }


                /*
                 * Server version can safely update
                 * the local version.
                 */

                updateLocalNote(
                        localNote,
                        remoteNote
                );

                notesDao.update(localNote);
            }
        }
    }


    /*
     * ======================================
     * CREATE REQUEST DTO
     * ======================================
     */

    private NoteRequestDTO createRequest(
            NotesModel note
    ) {

        NoteRequestDTO request =
                new NoteRequestDTO();

        try {
            /*
             * Local database stores ENCRYPTED data.
             * The backend expects PLAIN TEXT.
             */
            request.setTitle(
                    encryptionManager.decrypt(note.getTitle())
            );

            request.setContent(
                    encryptionManager.decrypt(note.getContent())
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to decrypt note for sync: " + note.getId(), e);
            // Fallback to sending as-is or handle error
            request.setTitle(note.getTitle());
            request.setContent(note.getContent());
        }


        request.setCategory(
                note.getCategory()
        );


        request.setTimestamp(
                note.getTimestamp()
        );

        request.setVault(
                note.isVault()
        );

        return request;
    }


    /*
     * ======================================
     * REMOTE -> LOCAL
     * ======================================
     */

    private NotesModel convertToLocalNote(
            NoteResponseDTO remoteNote,
            String userEmail
    ) {

        NotesModel localNote = new NotesModel();

        try {
            /*
             * Backend sends PLAIN TEXT.
             * Local database requires ENCRYPTION.
             */
            localNote.setTitle(encryptionManager.encrypt(remoteNote.getTitle()));
            localNote.setContent(encryptionManager.encrypt(remoteNote.getContent()));
        } catch (Exception e) {
            Log.e(TAG, "Failed to encrypt remote note: " + remoteNote.getId(), e);
            localNote.setTitle(remoteNote.getTitle());
            localNote.setContent(remoteNote.getContent());
        }

        localNote.setCategory(remoteNote.getCategory());
        localNote.setTimestamp(remoteNote.getTimestamp());
        localNote.setVault(remoteNote.isVault());

        // Comes from logged-in user, not NoteResponseDTO
        localNote.setUserEmail(userEmail);

        localNote.setServerId(remoteNote.getId());
        localNote.setSyncStatus(SyncStatus.SUCCESS);

        return localNote;
    }


    /*
     * ======================================
     * UPDATE LOCAL NOTE
     * ======================================
     */

    private void updateLocalNote(
            NotesModel localNote,
            NoteResponseDTO remoteNote
    ) {

        try {
            /*
             * Backend sends PLAIN TEXT.
             * Local database requires ENCRYPTION.
             */
            localNote.setTitle(
                    encryptionManager.encrypt(remoteNote.getTitle())
            );

            localNote.setContent(
                    encryptionManager.encrypt(remoteNote.getContent())
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to encrypt updated remote note: " + remoteNote.getId(), e);
            localNote.setTitle(remoteNote.getTitle());
            localNote.setContent(remoteNote.getContent());
        }


        localNote.setCategory(
                remoteNote.getCategory()
        );


        localNote.setTimestamp(
                remoteNote.getTimestamp()
        );

        localNote.setVault(
                remoteNote.isVault()
        );

        localNote.setSyncStatus(
                SyncStatus.SUCCESS
        );
    }
}