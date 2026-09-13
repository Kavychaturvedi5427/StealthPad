package com.kavya.stealthpad.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kavya.stealthpad.Dto.AiGenerateReqDto;
import com.kavya.stealthpad.Dto.AiGenerateResDto;
import com.kavya.stealthpad.Dto.AiKeyPointsReqDto;
import com.kavya.stealthpad.Dto.AiKeyPointsResDto;
import com.kavya.stealthpad.Dto.AiSummarizeReqDto;
import com.kavya.stealthpad.Dto.AiSummarizeResDto;
import com.kavya.stealthpad.service.AiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class StealthAiController {

    private final AiService aiService;

    // Summary endpoint....
    @PostMapping("/summarize")
    public ResponseEntity<AiSummarizeResDto> summarizeNote(@Valid @RequestBody AiSummarizeReqDto requestDto) {

        AiSummarizeResDto aiResponseDto = aiService.summarize(requestDto.getText());

        return ResponseEntity.ok(aiResponseDto);
    }

    // note generation endpoint...
    @PostMapping("/generate")
    public ResponseEntity<AiGenerateResDto> generateNote(@Valid @RequestBody AiGenerateReqDto requestDto) {
        AiGenerateResDto resDto = aiService.generateNote(requestDto.getText());
        return ResponseEntity.ok(resDto);
    }

    // key points extraction endpoints...
    @PostMapping("/key-points")
    public ResponseEntity<AiKeyPointsResDto> generateKeyPoints(@Valid @RequestBody AiKeyPointsReqDto reqDto) {
        AiKeyPointsResDto resDto = aiService.extractKeyPoints(reqDto.getText());
        return ResponseEntity.ok(resDto);
    }
    

}
