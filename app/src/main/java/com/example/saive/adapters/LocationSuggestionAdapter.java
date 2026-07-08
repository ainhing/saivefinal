package com.example.saive.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.models.LocationSuggestion;
import java.util.ArrayList;
import java.util.List;

public class LocationSuggestionAdapter extends RecyclerView.Adapter<LocationSuggestionAdapter.ViewHolder> {

    private List<LocationSuggestion> suggestions = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LocationSuggestion suggestion);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void setSuggestions(List<LocationSuggestion> suggestions) {
        this.suggestions = suggestions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationSuggestion suggestion = suggestions.get(position);
        holder.tvMainText.setText(suggestion.getMainText());
        holder.tvSecondaryText.setText(suggestion.getSecondaryText());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(suggestion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMainText, tvSecondaryText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMainText = itemView.findViewById(R.id.tvMainText);
            tvSecondaryText = itemView.findViewById(R.id.tvSecondaryText);
        }
    }
}
