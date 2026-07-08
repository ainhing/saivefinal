package com.example.saive.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.models.PaymentCard;
import java.util.List;

@android.annotation.SuppressLint("NotifyDataSetChanged")
public class PaymentCardAdapter extends RecyclerView.Adapter<PaymentCardAdapter.ViewHolder> {

    private List<PaymentCard> cardList;
    private PaymentCard selectedCard;
    private OnCardSelectedListener listener;

    public interface OnCardSelectedListener {
        void onCardSelected(PaymentCard card);
    }

    public PaymentCardAdapter(List<PaymentCard> cardList, OnCardSelectedListener listener) {
        this.cardList = cardList;
        this.listener = listener;
    }

    public void setSelectedCard(PaymentCard card) {
        this.selectedCard = card;
        notifyDataSetChanged();
    }

    public PaymentCard getSelectedCard() {
        return selectedCard;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentCard card = cardList.get(position);
        holder.tvCardNumber.setText(card.getMaskedNumber());
        holder.tvCardHolder.setText(card.getCardHolderName());
        holder.tvExpiryDate.setText(card.getExpiryDate());
        holder.tvCardType.setText(card.getCardType());

        boolean isSelected = selectedCard != null && selectedCard.getId().equals(card.getId());
        holder.cardContainer.setStrokeWidth(isSelected ? 6 : 0);
        holder.cardContainer.setStrokeColor(isSelected ? 
                holder.itemView.getContext().getResources().getColor(R.color.colorRatingGold) : 0);

        holder.itemView.setOnClickListener(v -> {
            selectedCard = card;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onCardSelected(card);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardNumber, tvCardHolder, tvExpiryDate, tvCardType;
        com.google.android.material.card.MaterialCardView cardContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardNumber = itemView.findViewById(R.id.tvCardNumber);
            tvCardHolder = itemView.findViewById(R.id.tvCardHolder);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
            tvCardType = itemView.findViewById(R.id.tvCardType);
            cardContainer = (com.google.android.material.card.MaterialCardView) itemView;
        }
    }
}
