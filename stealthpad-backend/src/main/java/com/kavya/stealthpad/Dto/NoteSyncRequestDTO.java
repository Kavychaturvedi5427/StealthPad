package com.kavya.stealthpad.Dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NoteSyncRequestDTO {

    private long lastSyncAt;

    private List<NoteSyncDTO> changes;
}