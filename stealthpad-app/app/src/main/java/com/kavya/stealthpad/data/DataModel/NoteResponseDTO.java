package com.kavya.stealthpad.data.DataModel;

public class NoteResponseDTO {
    private Long id;

    private String title;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    private String content;

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
