package com.kavya.stealthpad.data.DataModel;

import java.util.List;

public class AiKeyPointsResDto {
    private List<String> keyPoints;
    private String generatedAt;

    public List<String> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<String> keyPoints) {
        this.keyPoints = keyPoints;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}
