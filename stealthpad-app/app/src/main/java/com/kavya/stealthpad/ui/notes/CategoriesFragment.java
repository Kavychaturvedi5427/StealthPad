package com.kavya.stealthpad.ui.notes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kavya.stealthpad.R;
import com.kavya.stealthpad.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private RecyclerView recyclerView;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        recyclerView = view.findViewById(R.id.recycler_categories);

        List<CategoryAdapter.CategoryItem> categories = new ArrayList<>();
        categories.add(new CategoryAdapter.CategoryItem("Personal", R.drawable.home));
        categories.add(new CategoryAdapter.CategoryItem("Work", android.R.drawable.ic_menu_agenda));
        categories.add(new CategoryAdapter.CategoryItem("Ideas", R.drawable.idea));
        categories.add(new CategoryAdapter.CategoryItem("Important", R.drawable.ic_star));

        CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
            if (sessionManager.isLoggedIn()) {
                CategoryNotesBottomSheet bottomSheet = CategoryNotesBottomSheet.newInstance(category);
                bottomSheet.show(getChildFragmentManager(), "CATEGORY_NOTES");
            } else {
                Toast.makeText(requireContext(), "Please login to view notes", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(adapter);
    }
}
