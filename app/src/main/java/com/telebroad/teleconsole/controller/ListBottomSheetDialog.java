package com.telebroad.teleconsole.controller;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.telebroad.teleconsole.R;

import java.util.List;

public abstract class ListBottomSheetDialog<T> extends BottomSheetDialogFragment {

    protected List<T> items;
    private RecyclerView phoneRecyclerView;

    protected abstract RecyclerView.Adapter getAdapter();

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.contact_bottom_dialog, null);
        phoneRecyclerView = view.findViewById(R.id.phone_number_recycler);
        if (items == null) {
            return d;
        }
        phoneRecyclerView.setAdapter(getAdapter());
        phoneRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        phoneRecyclerView.invalidate();
        d.setContentView(view);
        return d;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissAllowingStateLoss();
    }
}
