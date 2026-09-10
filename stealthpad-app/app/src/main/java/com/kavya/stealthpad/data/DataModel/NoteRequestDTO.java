package com.kavya.stealthpad.data.DataModel;

public class NoteRequestDTO {
    private String title;
    private String content;

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

    private String category;
    private long timestamp;
    private boolean isVault;

    public boolean isVault() {
        return isVault;
    }

    public void setVault(boolean vault) {
        isVault = vault;
    }
}
