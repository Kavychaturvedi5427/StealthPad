package com.kavya.stealthpad.data.Local.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.kavya.stealthpad.utils.SyncStatus;

@Entity(tableName = "notes")
public class NotesModel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "last_updated", defaultValue = "0")
    private long lastUpdated;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "user_email")
    private String userEmail;

    @ColumnInfo(name = "sync_status")
    private int syncStatus;         // 0 for pending, 1 for synced, 2 for failed...

    @ColumnInfo(name = "server_id")
    private Long serverId;      // this will later help in the updation at the backend...

    @ColumnInfo(name = "is_vault", defaultValue = "0")
    private boolean isVault;

    // Default constructor for Room
    public NotesModel() {
    }

    public boolean isVault() {
        return isVault;
    }

    public void setVault(boolean vault) {
        isVault = vault;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    // Constructor
    public NotesModel(String title, String content, long timestamp, String category, String userEmail) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.lastUpdated = timestamp;
        this.category = category;
        this.userEmail = userEmail;
        this.syncStatus = SyncStatus.PENDING;
    }

    // 3-argument constructor for convenience (e.g. in NotesRepository)
    @Ignore
    public NotesModel(String title, String content, long timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.lastUpdated = timestamp;
        this.category = "General";
        this.syncStatus = SyncStatus.PENDING;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getLastUpdated() {
        return lastUpdated != 0 ? lastUpdated : timestamp;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }
}