package com.example.saive.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;

import java.util.List;

public class BottomSheetOptionAdapter extends RecyclerView.Adapter<BottomSheetOptionAdapter.ViewHolder> {

    private final List<String> options;
    private final String currentSelection;
    private final OnOptionClickListener listener;

    public interface OnOptionClickListener {
        void onOptionClick(String option);
    }

    public BottomSheetOptionAdapter(List<String> options, String currentSelection, OnOptionClickListener listener) {
        this.options = options;
        this.currentSelection = currentSelection;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom_sheet_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String option = options.get(position);
        holder.tvOptionName.setText(option);
        
        if (option.equals(currentSelection)) {
            holder.viewSelectedIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.viewSelectedIndicator.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onOptionClick(option));
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOptionName;
        View viewSelectedIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            tvOptionName = itemView.findViewById(R.id.tvOptionName);
            viewSelectedIndicator = itemView.findViewById(R.id.viewSelectedIndicator);
        }
    }
}