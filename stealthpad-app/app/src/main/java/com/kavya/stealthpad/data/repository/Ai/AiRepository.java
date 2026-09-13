package com.kavya.stealthpad.data.repository.Ai;

import com.kavya.stealthpad.data.DataModel.AiGenerateResDto;
import com.kavya.stealthpad.data.DataModel.AiKeyPointsResDto;
import com.kavya.stealthpad.data.DataModel.AiRequestDto;
import com.kavya.stealthpad.data.DataModel.AiSummarizeResDto;
import com.kavya.stealthpad.data.api.AiApi;

import javax.inject.Inject;

import retrofit2.Call;

public class AiRepository {

    private final AiApi aiApi;

    @Inject
    public AiRepository(AiApi aiApi) {
        this.aiApi = aiApi;
    }

    public Call<AiSummarizeResDto> summarize(String text) {
        return aiApi.summarize(new AiRequestDto(text));
    }

    public Call<AiGenerateResDto> generate(String text) {
        return aiApi.generate(new AiRequestDto(text));
    }

    public Call<AiKeyPointsResDto> keyPoints(String text) {
        return aiApi.keyPoints(new AiRequestDto(text));
    }
}
