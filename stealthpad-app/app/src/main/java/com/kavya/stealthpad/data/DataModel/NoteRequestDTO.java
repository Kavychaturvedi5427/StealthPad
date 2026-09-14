package com.kavya.stealthpad.data.DataModel;

import com.google.gson.annotations.SerializedName;

public class NoteRequestDTO {
    private String title;
    private String content;
    private String category;
    private long timestamp;
    
    @SerializedName("updated_at")
    private long lastUpdated;

    @SerializedName("is_vault")
    private boolean isVault;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isVault() {
        return isVault;
    }

    public void setVault(boolean vault) {
        isVault = vault;
    }
}
