package com.kavya.stealthpad.data.DataModel;

import com.google.gson.annotations.SerializedName;

public class NoteResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String category;
    
    @SerializedName("timestamp")
    private Long timestamp;
    
    @SerializedName("updated_at")
    private Long lastUpdated;

    @SerializedName("is_vault")
    private Boolean vault;

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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Boolean getVault() {
        return vault;
    }

    public void setVault(Boolean vault) {
        this.vault = vault;
    }
    
    public boolean isVault() {
        return vault != null && vault;
    }
}
