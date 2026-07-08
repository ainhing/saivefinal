package com.example.saive.adapters;

import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.models.Address;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CheckoutAddressAdapter extends RecyclerView.Adapter<CheckoutAddressAdapter.ViewHolder> {

    private List<Address> addresses;
    private Address selectedAddress;
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onSelected(Address address);
    }

    public CheckoutAddressAdapter(List<Address> addresses, Address defaultAddress, OnAddressSelectedListener listener) {
        this.addresses = addresses;
        this.selectedAddress = defaultAddress;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = addresses.get(position);
        holder.tvLabel.setText(address.getLabel());
        holder.tvFullName.setText(address.getFullName());
        holder.tvAddressDetail.setText(address.getFullDisplayAddress());
        holder.tvPhone.setText(address.getPhoneNumber());

        boolean isSelected = selectedAddress != null && selectedAddress.getId().equals(address.getId());
        
        holder.ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.cardAddress.setStrokeColor(isSelected ? 
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorMaroon) : 
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorLightGray));
        holder.cardAddress.setStrokeWidth(isSelected ? 4 : 2);

        holder.itemView.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            int prevPos = -1;
            if (selectedAddress != null) {
                for (int i = 0; i < addresses.size(); i++) {
                    if (addresses.get(i).getId().equals(selectedAddress.getId())) {
                        prevPos = i;
                        break;
                    }
                }
            }
            selectedAddress = address;
            int currentPos = holder.getAdapterPosition();
            if (prevPos != -1) {
                notifyItemChanged(prevPos);
            }
            if (currentPos != RecyclerView.NO_POSITION) {
                notifyItemChanged(currentPos);
            }
            if (listener != null) {
                listener.onSelected(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public Address getSelectedAddress() {
        return selectedAddress;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel, tvFullName, tvAddressDetail, tvPhone;
        ImageView ivCheck;
        MaterialCardView cardAddress;

        ViewHolder(View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvAddressDetail = itemView.findViewById(R.id.tvAddressDetail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            cardAddress = itemView.findViewById(R.id.cardAddress);
        }
    }
}