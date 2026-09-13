package com.kavya.stealthpad.data.DataModel;

public class AiRequestDto {
    private String text;

    public AiRequestDto(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
