package com.kavya.stealthpad.utils;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class AttachmentStorageManager {

    private final Context context;
    private static final String ATTACHMENTS_DIR = "attachments";

    @Inject
    public AttachmentStorageManager(@ApplicationContext Context context) {
        this.context = context;
    }

    public File importImage(Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new Exception("Could not open input stream");

        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
        if (extension == null) extension = "jpg";

        String fileName = UUID.randomUUID().toString() + "." + extension;
        File attachmentsDir = new File(context.getFilesDir(), ATTACHMENTS_DIR);
        if (!attachmentsDir.exists()) attachmentsDir.mkdirs();

        File targetFile = new File(attachmentsDir, fileName);
        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            inputStream.close();
        }

        return targetFile;
    }

    public void deleteAttachment(String localPath) {
        if (localPath == null) return;
        File file = new File(localPath);
        if (file.exists()) {
            file.delete();
        }
    }

    public void deleteAllAttachments() {
        File attachmentsDir = new File(context.getFilesDir(), ATTACHMENTS_DIR);
        if (attachmentsDir.exists() && attachmentsDir.isDirectory()) {
            File[] files = attachmentsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }

    public String getMimeType(Uri uri) {
        return context.getContentResolver().getType(uri);
    }

    public long getFileSize(Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            return is != null ? is.available() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
