package com.kavya.stealthpad.ui.dashboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.kavya.stealthpad.R;
import com.kavya.stealthpad.ViewModel.AiViewModel.AiState;
import com.kavya.stealthpad.ViewModel.AiViewModel.AiViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AiActionBottomSheet extends BottomSheetDialogFragment {

    public enum ActionType {
        SUMMARIZE, GENERATE, KEY_POINTS
    }

    public interface OnResultAppliedListener {
        void onResultApplied(String result);
    }

    private OnResultAppliedListener resultAppliedListener;

    public void setOnResultAppliedListener(OnResultAppliedListener listener) {
        this.resultAppliedListener = listener;
    }

    private static final String ARG_ACTION_TYPE = "action_type";
    private static final String ARG_INITIAL_TEXT = "initial_text";

    private ActionType actionType;
    private String initialText;
    private AiViewModel aiViewModel;

    // UI Components
    private ImageView mascotImg;
    private TextView headerTitle, headerSubtitle, charCountText, resultHeader;
    private TextInputEditText inputEdit;
    private MaterialButton actionBtn, copyBtn, useNoteBtn, retryBtn;
    private View resultContainer, textResultCard;
    private TextView resultTextView;
    private LinearLayout keyPointsList;
    private ChipGroup suggestionsGroup;
    private ImageView inputTypeIcon;
    private TextView inputTypeLabel;

    public static AiActionBottomSheet newInstance(ActionType type, @Nullable String initialText) {
        AiActionBottomSheet fragment = new AiActionBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACTION_TYPE, type);
        args.putString(ARG_INITIAL_TEXT, initialText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
        if (getArguments() != null) {
            actionType = (ActionType) getArguments().getSerializable(ARG_ACTION_TYPE);
            initialText = getArguments().getString(ARG_INITIAL_TEXT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_ai_action_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        aiViewModel = new ViewModelProvider(this).get(AiViewModel.class);

        // Bind Views
        mascotImg = view.findViewById(R.id.ai_mascot_img);
        headerTitle = view.findViewById(R.id.text_ai_header);
        headerSubtitle = view.findViewById(R.id.text_ai_subtitle);
        charCountText = view.findViewById(R.id.text_char_count);
        resultHeader = view.findViewById(R.id.text_result_header);
        inputEdit = view.findViewById(R.id.edit_ai_input);
        actionBtn = view.findViewById(R.id.btn_ai_action);
        copyBtn = view.findViewById(R.id.btn_copy_result);
        useNoteBtn = view.findViewById(R.id.btn_use_note);
        retryBtn = view.findViewById(R.id.btn_retry);
        resultContainer = view.findViewById(R.id.container_ai_result);
        textResultCard = view.findViewById(R.id.card_text_result);
        resultTextView = view.findViewById(R.id.text_ai_result);
        keyPointsList = view.findViewById(R.id.list_key_points);
        suggestionsGroup = view.findViewById(R.id.chip_group_suggestions);
        inputTypeIcon = view.findViewById(R.id.icon_input_type);
        inputTypeLabel = view.findViewById(R.id.label_input_type);

        setupModeUI();
        setupListeners();
        observeViewModel();
    }

    private void setupModeUI() {
        switch (actionType) {
            case SUMMARIZE:
                headerTitle.setText("Summarize your note");
                headerSubtitle.setText("Turn your thoughts into a clear, concise summary.");
                inputTypeLabel.setText("Your note");
                inputTypeIcon.setImageResource(R.drawable.category); // Fallback
                actionBtn.setText("✨ Summarize with Stealth AI");
                addSuggestions(new String[]{"Meeting notes", "Long article", "Study notes"});
                break;
            case GENERATE:
                headerTitle.setText("Create with Stealth AI");
                headerSubtitle.setText("Describe what you want to write and let AI build the note.");
                inputTypeLabel.setText("What would you like to write?");
                inputTypeIcon.setImageResource(R.drawable.ic_ai);
                actionBtn.setText("✨ Create Note");
                inputEdit.setHint("e.g. Create study notes about the solar system...");
                addSuggestions(new String[]{"Study notes", "Meeting summary", "Project plan", "To-do list"});
                break;
            case KEY_POINTS:
                headerTitle.setText("Find the key points");
                headerSubtitle.setText("Pull out the most important ideas from your note.");
                inputTypeLabel.setText("Your note");
                inputTypeIcon.setImageResource(R.drawable.ic_star);
                actionBtn.setText("✨ Extract Key Points");
                addSuggestions(new String[]{"Main ideas", "Important facts", "Study revision"});
                break;
        }

        if (initialText != null) {
            inputEdit.setText(initialText);
            updateCharCount(initialText.length());
        }
    }

    private void addSuggestions(String[] suggestions) {
        suggestionsGroup.removeAllViews();
        for (String suggestion : suggestions) {
            Chip chip = new Chip(requireContext());
            chip.setText(suggestion);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.ai_chip_bg)));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.ai_text_secondary));
            chip.setChipStrokeWidth(0);
            chip.setOnClickListener(v -> inputEdit.setText(suggestion));
            suggestionsGroup.addView(chip);
        }
    }

    private void setupListeners() {
        inputEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharCount(s.length());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        actionBtn.setOnClickListener(v -> performAiAction());
        retryBtn.setOnClickListener(v -> performAiAction());

        copyBtn.setOnClickListener(v -> copyToClipboard());
        copyBtn.setIconResource(R.drawable.accsetting); // Temporary placeholder icon
        useNoteBtn.setOnClickListener(v -> {
            String resultText = "";
            if (textResultCard.getVisibility() == View.VISIBLE) {
                resultText = resultTextView.getText().toString();
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < keyPointsList.getChildCount(); i++) {
                    TextView tv = keyPointsList.getChildAt(i).findViewById(R.id.text_point_content);
                    sb.append("• ").append(tv.getText()).append("\n");
                }
                resultText = sb.toString().trim();
            }

            if (resultAppliedListener != null) {
                resultAppliedListener.onResultApplied(resultText);
            }
            dismiss();
        });
    }

    private void performAiAction() {
        String text = inputEdit.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter some text", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (actionType) {
            case SUMMARIZE: aiViewModel.summarize(text); break;
            case GENERATE: aiViewModel.generate(text); break;
            case KEY_POINTS: aiViewModel.keyPoints(text); break;
        }
    }

    private void expandBottomSheet() {
        if (getDialog() != null) {
            View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
    }

    private void updateCharCount(int count) {
        charCountText.setText(String.format("%d / 10,000", count));
    }

    private void observeViewModel() {
        aiViewModel.getSummarizeResult().observe(getViewLifecycleOwner(), state -> {
            if (actionType == ActionType.SUMMARIZE) handleState(state, data -> {
                showTextResult("YOUR SUMMARY", data.getSummary());
            });
        });

        aiViewModel.getGenerateResult().observe(getViewLifecycleOwner(), state -> {
            if (actionType == ActionType.GENERATE) handleState(state, data -> {
                showTextResult("YOUR NEW NOTE", data.getGeneratedNote());
            });
        });

        aiViewModel.getKeyPointsResult().observe(getViewLifecycleOwner(), state -> {
            if (actionType == ActionType.KEY_POINTS) handleState(state, data -> {
                showKeyPointsResult(data.getKeyPoints());
            });
        });
    }

    private <T> void handleState(AiState<T> state, DataHandler<T> handler) {
        if (state instanceof AiState.Loading) {
            setUiLoading(true);
            updateMascotState("thinking");
        } else if (state instanceof AiState.Success) {
            setUiLoading(false);
            updateMascotState("success");
            resultContainer.setVisibility(View.VISIBLE);
            handler.onData(((AiState.Success<T>) state).getData());
            expandBottomSheet();
        } else if (state instanceof AiState.Error) {
            setUiLoading(false);
            updateMascotState("error");
            Toast.makeText(requireContext(), ((AiState.Error<T>) state).getError(), Toast.LENGTH_LONG).show();
        }
    }

    private void setUiLoading(boolean isLoading) {
        actionBtn.setEnabled(!isLoading);
        actionBtn.setAlpha(isLoading ? 0.6f : 1.0f);
        actionBtn.setText(isLoading ? "Stealth AI is thinking..." : getDefaultActionText());
        inputEdit.setEnabled(!isLoading);
    }

    private String getDefaultActionText() {
        switch (actionType) {
            case SUMMARIZE: return "✨ Summarize with Stealth AI";
            case GENERATE: return "✨ Create Note";
            case KEY_POINTS: return "✨ Extract Key Points";
            default: return "Process";
        }
    }

    private void showTextResult(String header, String content) {
        resultHeader.setText(header);
        textResultCard.setVisibility(View.VISIBLE);
        keyPointsList.setVisibility(View.GONE);
        resultTextView.setText(content);
    }

    private void showKeyPointsResult(List<String> points) {
        resultHeader.setText("KEY POINTS");
        textResultCard.setVisibility(View.GONE);
        keyPointsList.setVisibility(View.VISIBLE);
        keyPointsList.removeAllViews();

        if (points != null) {
            for (int i = 0; i < points.size(); i++) {
                addKeyPointView(i + 1, points.get(i));
            }
        }
    }

    private void addKeyPointView(int index, String text) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_key_point, keyPointsList, false);
        TextView numText = view.findViewById(R.id.text_point_number);
        TextView contentText = view.findViewById(R.id.text_point_content);
        
        numText.setText(String.format("%02d", index));
        contentText.setText(text);
        
        keyPointsList.addView(view);
    }

    private void updateMascotState(String state) {
        // We've switched from Lottie to a static mascot drawable.
        // In the future, we can add subtle fade animations here if needed.
        if (mascotImg != null) {
            mascotImg.setImageResource(R.drawable.stealthmascot);
        }
    }

    private void copyToClipboard() {
        String textToCopy = "";
        if (textResultCard.getVisibility() == View.VISIBLE) {
            textToCopy = resultTextView.getText().toString();
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < keyPointsList.getChildCount(); i++) {
                TextView tv = keyPointsList.getChildAt(i).findViewById(R.id.text_point_content);
                sb.append("• ").append(tv.getText()).append("\n");
            }
            textToCopy = sb.toString().trim();
        }

        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Stealth AI Result", textToCopy);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private interface DataHandler<T> {
        void onData(T data);
    }
}
