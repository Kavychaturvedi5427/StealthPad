package com.kavya.stealthpad.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoteSyncDTO {

    private Long id;

    private String title;

    private String content;

    private String category;

    private long timestamp;

    private long updatedAt;

    private long version;

    private boolean deleted;

    private boolean isVault;

}
