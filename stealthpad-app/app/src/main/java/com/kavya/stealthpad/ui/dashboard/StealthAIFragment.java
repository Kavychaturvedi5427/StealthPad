package com.kavya.stealthpad.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kavya.stealthpad.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StealthAIFragment extends BottomSheetDialogFragment {

    public interface OnResultAppliedListener {
        void onResultApplied(String result);
    }

    private OnResultAppliedListener resultAppliedListener;

    public void setOnResultAppliedListener(OnResultAppliedListener listener) {
        this.resultAppliedListener = listener;
    }

    private static final String ARG_INITIAL_TEXT = "initial_text";
    private String initialText;

    public static StealthAIFragment newInstance(@Nullable String initialText) {
        StealthAIFragment fragment = new StealthAIFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_TEXT, initialText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
        if (getArguments() != null) {
            initialText = getArguments().getString(ARG_INITIAL_TEXT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stealth_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Main Button
        view.findViewById(R.id.btn_start_ai).setOnClickListener(v -> {
            openAiAction(AiActionBottomSheet.ActionType.GENERATE);
        });

        // Action Cards
        view.findViewById(R.id.card_summarize).setOnClickListener(v -> {
            openAiAction(AiActionBottomSheet.ActionType.SUMMARIZE);
        });

        view.findViewById(R.id.card_key_points).setOnClickListener(v -> {
            openAiAction(AiActionBottomSheet.ActionType.KEY_POINTS);
        });

        view.findViewById(R.id.card_generate).setOnClickListener(v -> {
            openAiAction(AiActionBottomSheet.ActionType.GENERATE);
        });

        // Disabled actions
        view.findViewById(R.id.card_improve).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Improve Note is coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void openAiAction(AiActionBottomSheet.ActionType type) {
        AiActionBottomSheet bottomSheet = AiActionBottomSheet.newInstance(type, initialText);
        bottomSheet.setOnResultAppliedListener(result -> {
            if (resultAppliedListener != null) {
                resultAppliedListener.onResultApplied(result);
            }
            dismiss();
        });
        bottomSheet.show(getParentFragmentManager(), "AI_ACTION");
    }
}
