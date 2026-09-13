package com.kavya.stealthpad.data.api;

import com.kavya.stealthpad.data.DataModel.AiGenerateResDto;
import com.kavya.stealthpad.data.DataModel.AiKeyPointsResDto;
import com.kavya.stealthpad.data.DataModel.AiRequestDto;
import com.kavya.stealthpad.data.DataModel.AiSummarizeResDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AiApi {

    @POST("api/ai/summarize")
    Call<AiSummarizeResDto> summarize(@Body AiRequestDto request);

    @POST("api/ai/generate")
    Call<AiGenerateResDto> generate(@Body AiRequestDto request);

    @POST("api/ai/key-points")
    Call<AiKeyPointsResDto> keyPoints(@Body AiRequestDto request);
}
