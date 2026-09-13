package com.kavya.stealthpad.ViewModel.AiViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kavya.stealthpad.data.DataModel.AiGenerateResDto;
import com.kavya.stealthpad.data.DataModel.AiKeyPointsResDto;
import com.kavya.stealthpad.data.DataModel.AiSummarizeResDto;
import com.kavya.stealthpad.data.DataModel.ApiError;
import com.kavya.stealthpad.data.repository.Ai.AiRepository;
import com.kavya.stealthpad.utils.ApiErrorHandler;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class AiViewModel extends ViewModel {

    private final AiRepository aiRepository;
    private final ApiErrorHandler errorHandler;

    private final MutableLiveData<AiState<AiSummarizeResDto>> summarizeResult = new MutableLiveData<>();
    private final MutableLiveData<AiState<AiGenerateResDto>> generateResult = new MutableLiveData<>();
    private final MutableLiveData<AiState<AiKeyPointsResDto>> keyPointsResult = new MutableLiveData<>();

    @Inject
    public AiViewModel(AiRepository aiRepository, ApiErrorHandler errorHandler) {
        this.aiRepository = aiRepository;
        this.errorHandler = errorHandler;
    }

    public LiveData<AiState<AiSummarizeResDto>> getSummarizeResult() {
        return summarizeResult;
    }

    public LiveData<AiState<AiGenerateResDto>> getGenerateResult() {
        return generateResult;
    }

    public LiveData<AiState<AiKeyPointsResDto>> getKeyPointsResult() {
        return keyPointsResult;
    }

    public void summarize(String text) {
        if (text == null || text.trim().isEmpty()) {
            summarizeResult.setValue(new AiState.Error<>("Text cannot be empty."));
            return;
        }
        if (text.length() > 10000) {
            summarizeResult.setValue(new AiState.Error<>("Text cannot exceed 10000 characters."));
            return;
        }

        summarizeResult.setValue(new AiState.Loading<>("Summarizing..."));
        aiRepository.summarize(text).enqueue(new Callback<AiSummarizeResDto>() {
            @Override
            public void onResponse(Call<AiSummarizeResDto> call, Response<AiSummarizeResDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    summarizeResult.setValue(new AiState.Success<>(response.body()));
                } else {
                    ApiError error = errorHandler.handleError(response);
                    summarizeResult.setValue(new AiState.Error<>(error.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<AiSummarizeResDto> call, Throwable t) {
                ApiError error = errorHandler.handleException(t);
                summarizeResult.setValue(new AiState.Error<>(error.getMessage()));
            }
        });
    }

    public void generate(String text) {
        if (text == null || text.trim().isEmpty()) {
            generateResult.setValue(new AiState.Error<>("Text cannot be empty."));
            return;
        }
        if (text.length() > 10000) {
            generateResult.setValue(new AiState.Error<>("Text cannot exceed 10000 characters."));
            return;
        }

        generateResult.setValue(new AiState.Loading<>("Generating..."));
        aiRepository.generate(text).enqueue(new Callback<AiGenerateResDto>() {
            @Override
            public void onResponse(Call<AiGenerateResDto> call, Response<AiGenerateResDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    generateResult.setValue(new AiState.Success<>(response.body()));
                } else {
                    ApiError error = errorHandler.handleError(response);
                    generateResult.setValue(new AiState.Error<>(error.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<AiGenerateResDto> call, Throwable t) {
                ApiError error = errorHandler.handleException(t);
                generateResult.setValue(new AiState.Error<>(error.getMessage()));
            }
        });
    }

    public void keyPoints(String text) {
        if (text == null || text.trim().isEmpty()) {
            keyPointsResult.setValue(new AiState.Error<>("Text cannot be empty."));
            return;
        }
        if (text.length() > 10000) {
            keyPointsResult.setValue(new AiState.Error<>("Text cannot exceed 10000 characters."));
            return;
        }

        keyPointsResult.setValue(new AiState.Loading<>("Extracting key points..."));
        aiRepository.keyPoints(text).enqueue(new Callback<AiKeyPointsResDto>() {
            @Override
            public void onResponse(Call<AiKeyPointsResDto> call, Response<AiKeyPointsResDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    keyPointsResult.setValue(new AiState.Success<>(response.body()));
                } else {
                    ApiError error = errorHandler.handleError(response);
                    keyPointsResult.setValue(new AiState.Error<>(error.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<AiKeyPointsResDto> call, Throwable t) {
                ApiError error = errorHandler.handleException(t);
                keyPointsResult.setValue(new AiState.Error<>(error.getMessage()));
            }
        });
    }

    public void resetStates() {
        summarizeResult.setValue(new AiState.Idle<>());
        generateResult.setValue(new AiState.Idle<>());
        keyPointsResult.setValue(new AiState.Idle<>());
    }
}
