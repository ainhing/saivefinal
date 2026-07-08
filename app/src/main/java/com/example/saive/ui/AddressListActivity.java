package com.example.saive.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.adapters.AddressAdapter;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.Address;
import com.example.saive.utils.DialogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@android.annotation.SuppressLint("NotifyDataSetChanged")
public class AddressListActivity extends BaseActivity {

    private RecyclerView rvAddresses;
    private View emptyState;
    private AddressAdapter adapter;
    private List<Address> addressList = new ArrayList<>();
    private static final String PREFS_NAME = "address_prefs";
    private static final String ADDRESS_KEY = "saved_addresses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        if (getWindow() != null) {
            getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorHeaderBg));
            boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (isDarkMode) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        rvAddresses = findViewById(R.id.rvAddresses);
        emptyState = findViewById(R.id.emptyState);
        View btnBack = findViewById(R.id.btnBack);
        View btnAdd = findViewById(R.id.btnAddNewAddress);

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAddressActivity.class);
            startActivityForResult(intent, 100);
        });

        loadAddresses();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        adapter = new AddressAdapter(addressList, new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onEdit(Address address) {
                Intent intent = new Intent(AddressListActivity.this, AddAddressActivity.class);
                intent.putExtra("edit_address", address);
                startActivityForResult(intent, 101);
            }

            @Override
            public void onDelete(Address address) {
                DialogUtils.showCustomAlertDialog(
                        AddressListActivity.this,
                        "Delete Address",
                        "Are you sure you want to delete this address?",
                        "Delete",
                        "Cancel",
                        () -> {
                            addressList.remove(address);
                            saveAddresses();
                            adapter.notifyDataSetChanged();
                            updateVisibility();
                        }
                );
            }

            @Override
            public void onSetDefault(Address address, boolean isDefault) {
                if (isDefault) {
                    for (Address a : addressList) {
                        a.setDefault(false);
                    }
                    address.setDefault(true);
                    saveAddresses();
                    adapter.notifyDataSetChanged();
                }
            }
        });
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(adapter);
        updateVisibility();
    }

    private void loadAddresses() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(ADDRESS_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Address>>() {}.getType();
            List<Address> loadedAddresses = gson.fromJson(json, type);
            if (loadedAddresses != null) {
                addressList.clear();
                addressList.addAll(loadedAddresses);
            }
        } else if (addressList.isEmpty()) {
            // Add a dummy default address if list is empty for first time
            addressList.add(new Address("1", "Home", "Thao Nhi Huynh", "+84 901 234 567", "123 Le Loi Street", "Ben Nghe Ward", "District 1", "Ho Chi Minh City", true));
            saveAddresses();
        }
    }

    private void saveAddresses() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(addressList);
        editor.putString(ADDRESS_KEY, json);
        editor.apply();
    }

    private void updateVisibility() {
        if (addressList.isEmpty()) {
            rvAddresses.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvAddresses.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadAddresses();
            adapter.notifyDataSetChanged();
            updateVisibility();
        }
    }
}