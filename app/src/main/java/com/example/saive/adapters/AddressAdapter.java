package com.example.saive.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.models.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onEdit(Address address);
        void onDelete(Address address);
        void onSetDefault(Address address, boolean isDefault);
    }

    public AddressAdapter(List<Address> addressList, OnAddressActionListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.tvLabel.setText(address.getLabel());
        holder.tvName.setText(address.getFullName());
        holder.tvPhone.setText(address.getPhoneNumber());
        holder.tvFullAddress.setText(address.getFullDisplayAddress());
        holder.tvDefaultBadge.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);
        holder.cbDefault.setChecked(address.isDefault());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(address));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(address));
        
        holder.cbDefault.setOnCheckedChangeListener(null);
        holder.cbDefault.setChecked(address.isDefault());
        holder.cbDefault.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                listener.onSetDefault(address, true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel, tvName, tvPhone, tvFullAddress, tvDefaultBadge;
        ImageView btnEdit, btnDelete;
        CheckBox cbDefault;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvAddressLabel);
            tvName = itemView.findViewById(R.id.tvReceiverName);
            tvPhone = itemView.findViewById(R.id.tvPhoneNumber);
            tvFullAddress = itemView.findViewById(R.id.tvFullAddress);
            tvDefaultBadge = itemView.findViewById(R.id.tvDefaultBadge);
            btnEdit = itemView.findViewById(R.id.btnEditAddress);
            btnDelete = itemView.findViewById(R.id.btnDeleteAddress);
            cbDefault = itemView.findViewById(R.id.cbSetDefault);
        }
    }
}