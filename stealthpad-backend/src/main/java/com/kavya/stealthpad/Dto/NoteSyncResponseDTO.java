package com.kavya.stealthpad.Dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NoteSyncResponseDTO {

    private long serverTime;

    private List<NoteSyncDTO> changes;
}