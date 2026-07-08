package com.example.saive.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.Address;
import com.example.saive.utils.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddAddressActivity extends BaseActivity {

    private EditText etName, etPhone, etStreet;
    private TextView tvSelectedCity, tvSelectedDistrict, tvSelectedWard;
    private TextView chipHome, chipOffice, chipOther, tvTitle;
    private View btnCitySelector, btnDistrictSelector, btnWardSelector;
    private CheckBox cbDefault;
    private String selectedLabel = "Home";
    private String selectedCountry = "Vietnam";
    private Address editAddress;
    
    private static final String PREFS_NAME = "address_prefs";
    private static final String ADDRESS_KEY = "saved_addresses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.saive.utils.LocationProvider.init(this, () -> {
            // Data loaded, if we want to refresh UI we can do it here
        });
        setContentView(R.layout.activity_add_address);

        if (getWindow() != null) {
            getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorHeaderBg));
            boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            if (isDarkMode) {
                getWindow().getDecorView().setSystemUiVisibility(
                        getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                        getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }

        tvTitle = findViewById(R.id.tvTitle);
        etName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etStreet = findViewById(R.id.etStreet);
        tvSelectedCity = findViewById(R.id.tvSelectedCity);
        tvSelectedDistrict = findViewById(R.id.tvSelectedDistrict);
        tvSelectedWard = findViewById(R.id.tvSelectedWard);
        btnCitySelector = findViewById(R.id.btnCitySelector);
        btnDistrictSelector = findViewById(R.id.btnDistrictSelector);
        btnWardSelector = findViewById(R.id.btnWardSelector);
        cbDefault = findViewById(R.id.cbDefault);
        
        chipHome = findViewById(R.id.chipHome);
        chipOffice = findViewById(R.id.chipOffice);
        chipOther = findViewById(R.id.chipOther);

        setupSelectors();

        editAddress = (Address) getIntent().getSerializableExtra("edit_address");
        if (editAddress != null) {
            tvTitle.setText(R.string.address_edit_title);
            fillData(editAddress);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnOpenMap).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, LocationPermissionActivity.class), 1002);
        });
        
        chipHome.setOnClickListener(v -> selectLabel("Home"));
        chipOffice.setOnClickListener(v -> selectLabel("Office"));
        chipOther.setOnClickListener(v -> selectLabel("Other"));

        findViewById(R.id.btnSaveAddress).setOnClickListener(v -> saveAddress());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1002 && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("selected_address");
            if (address != null) {
                autoFillLocation(address);
            }
        }
    }

    private void autoFillLocation(String fullAddress) {
        if (!com.example.saive.utils.LocationProvider.isLoaded()) {
            etStreet.setText(fullAddress);
            return;
        }
        
        List<String> provinces = com.example.saive.utils.LocationProvider.getProvinces(this);
        String matchedCity = null;
        String matchedDistrict = null;
        String matchedWard = null;
        
        // Find City
        for (String city : provinces) {
            if (fullAddress.contains(city) || fullAddress.contains(city.replace("Thành phố ", "").replace("Tỉnh ", ""))) {
                matchedCity = city;
                break;
            }
        }
        
        if (matchedCity != null) {
            tvSelectedCity.setText(matchedCity);
            List<String> districts = com.example.saive.utils.LocationProvider.getDistricts(this, matchedCity);
            
            // Find District
            for (String district : districts) {
                if (fullAddress.contains(district) || fullAddress.contains(district.replace("Quận ", "").replace("Huyện ", "").replace("Thành phố ", "").replace("Thị xã ", ""))) {
                    matchedDistrict = district;
                    break;
                }
            }
            
            // If District not found directly, try to find it by searching all Wards in the City
            if (matchedDistrict == null) {
                for (String district : districts) {
                    List<String> wards = com.example.saive.utils.LocationProvider.getWards(this, matchedCity, district);
                    for (String ward : wards) {
                        String cleanWard = ward.replace("Phường ", "").replace("Xã ", "").replace("Thị trấn ", "");
                        if (fullAddress.contains(ward) || (!cleanWard.isEmpty() && fullAddress.contains(cleanWard))) {
                            matchedDistrict = district;
                            matchedWard = ward;
                            break;
                        }
                    }
                    if (matchedDistrict != null) break;
                }
            }
            
            if (matchedDistrict != null) {
                tvSelectedDistrict.setText(matchedDistrict);
                List<String> wards = com.example.saive.utils.LocationProvider.getWards(this, matchedCity, matchedDistrict);
                
                // Find Ward (if not already found in fallback)
                if (matchedWard == null) {
                    for (String ward : wards) {
                        String cleanWard = ward.replace("Phường ", "").replace("Xã ", "").replace("Thị trấn ", "");
                        if (fullAddress.contains(ward) || (!cleanWard.isEmpty() && fullAddress.contains(cleanWard))) {
                            matchedWard = ward;
                            break;
                        }
                    }
                }
                
                if (matchedWard != null) {
                    tvSelectedWard.setText(matchedWard);
                } else {
                    tvSelectedWard.setText(R.string.hint_choose_ward);
                }
            } else {
                tvSelectedDistrict.setText(R.string.hint_choose_district);
                tvSelectedWard.setText(R.string.hint_choose_ward);
            }
        } else {
            tvSelectedCity.setText(R.string.hint_choose_city);
            tvSelectedDistrict.setText(R.string.hint_choose_district);
            tvSelectedWard.setText(R.string.hint_choose_ward);
        }
        
        // Clean up the street address by removing matched parts
        String streetOnly = fullAddress;
        if (matchedWard != null) streetOnly = streetOnly.replace(matchedWard, "");
        if (matchedDistrict != null) streetOnly = streetOnly.replace(matchedDistrict, "");
        if (matchedCity != null) streetOnly = streetOnly.replace(matchedCity, "");
        
        // Remove "Việt Nam", "Vietnam", and clean up commas
        streetOnly = streetOnly.replaceAll("Việt Nam", "").replaceAll("Vietnam", "");
        String[] parts = streetOnly.split(",");
        StringBuilder cleanStreet = new StringBuilder();
        for (String part : parts) {
            String p = part.trim();
            if (!p.isEmpty() && !p.equals("Ward") && !p.equals("District") && !p.equals("City")) {
                if (cleanStreet.length() > 0) cleanStreet.append(", ");
                cleanStreet.append(p);
            }
        }
        
        etStreet.setText(cleanStreet.length() > 0 ? cleanStreet.toString() : fullAddress);
    }

    private void setupSelectors() {
        btnCitySelector.setOnClickListener(v -> showLocationDialog("City", com.example.saive.utils.LocationProvider.getProvinces(this)));

        btnDistrictSelector.setOnClickListener(v -> {
            String city = tvSelectedCity.getText().toString();
            if (city.equals(getString(R.string.hint_choose_city))) {
                ToastUtils.showCustomToast(this, getString(R.string.error_select_city_first));
                return;
            }
            showLocationDialog("District", com.example.saive.utils.LocationProvider.getDistricts(this, city));
        });

        btnWardSelector.setOnClickListener(v -> {
            String city = tvSelectedCity.getText().toString();
            String district = tvSelectedDistrict.getText().toString();
            if (district.equals(getString(R.string.hint_choose_district))) {
                ToastUtils.showCustomToast(this, getString(R.string.error_select_district_first));
                return;
            }
            showLocationDialog("Ward", com.example.saive.utils.LocationProvider.getWards(this, city, district));
        });
    }

    @android.annotation.SuppressLint("InflateParams")
    private void showLocationDialog(String type, List<String> options) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_menu, null, false);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        if (type.equals("City")) tvTitle.setText(R.string.hint_choose_city);
        else if (type.equals("District")) tvTitle.setText(R.string.hint_choose_district);
        else tvTitle.setText(R.string.hint_choose_ward);

        EditText etSearch = view.findViewById(R.id.etSearchOption);
        etSearch.setVisibility(View.VISIBLE);

        RecyclerView rvOptions = view.findViewById(R.id.rvSheetOptions);
        rvOptions.setLayoutManager(new LinearLayoutManager(this));

        List<String> filteredOptions = new ArrayList<>(options);

        RecyclerView.Adapter<OptionViewHolder> adapter = new RecyclerView.Adapter<OptionViewHolder>() {
            @androidx.annotation.NonNull
            @Override
            public OptionViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
                View itemView = getLayoutInflater().inflate(R.layout.item_bottom_sheet_option, parent, false);
                return new OptionViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(@androidx.annotation.NonNull OptionViewHolder holder, int position) {
                String option = filteredOptions.get(position);
                holder.tvName.setText(option);
                holder.itemView.setOnClickListener(v -> {
                    if (type.equals("City")) {
                        tvSelectedCity.setText(option);
                        tvSelectedDistrict.setText(R.string.hint_choose_district);
                        tvSelectedWard.setText(R.string.hint_choose_ward);
                    } else if (type.equals("District")) {
                        tvSelectedDistrict.setText(option);
                        tvSelectedWard.setText(R.string.hint_choose_ward);
                    } else {
                        tvSelectedWard.setText(option);
                    }
                    bottomSheetDialog.dismiss();
                });
            }

            @Override
            public int getItemCount() {
                return filteredOptions.size();
            }
        };

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @android.annotation.SuppressLint("NotifyDataSetChanged")
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                filteredOptions.clear();
                for (String opt : options) {
                    if (opt.toLowerCase().contains(query)) filteredOptions.add(opt);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        rvOptions.setAdapter(adapter);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private static class OptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        OptionViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvOptionName);
        }
    }

    private void fillData(Address address) {
        etName.setText(address.getFullName());
        
        // Handle +84 prefix
        String phone = address.getPhoneNumber();
        if (phone != null && phone.startsWith("+84")) {
            etPhone.setText(phone.substring(3));
        } else {
            etPhone.setText(phone);
        }

        etStreet.setText(address.getStreetAddress());
        tvSelectedCity.setText(address.getCity());
        tvSelectedDistrict.setText(address.getDistrict());
        tvSelectedWard.setText(address.getWard());
        cbDefault.setChecked(address.isDefault());
        selectLabel(address.getLabel());
    }

    private void selectLabel(String label) {
        selectedLabel = label;
        updateChips();
    }

    private void updateChips() {
        int activeBg = androidx.core.content.ContextCompat.getColor(this, R.color.colorAccentBrand);
        int inactiveBg = androidx.core.content.ContextCompat.getColor(this, R.color.colorLinen);
        int activeText = androidx.core.content.ContextCompat.getColor(this, R.color.colorButtonText);
        int inactiveText = androidx.core.content.ContextCompat.getColor(this, R.color.colorNoirBlack);

        chipHome.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedLabel.equalsIgnoreCase("Home") ? activeBg : inactiveBg));
        chipHome.setTextColor(selectedLabel.equalsIgnoreCase("Home") ? activeText : inactiveText);

        chipOffice.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedLabel.equalsIgnoreCase("Office") ? activeBg : inactiveBg));
        chipOffice.setTextColor(selectedLabel.equalsIgnoreCase("Office") ? activeText : inactiveText);

        chipOther.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedLabel.equalsIgnoreCase("Other") ? activeBg : inactiveBg));
        chipOther.setTextColor(selectedLabel.equalsIgnoreCase("Other") ? activeText : inactiveText);
    }

    private void saveAddress() {
        String name = etName.getText().toString().trim();
        String phonePart = etPhone.getText().toString().trim();
        String street = etStreet.getText().toString().trim();
        String city = tvSelectedCity.getText().toString().trim();
        String district = tvSelectedDistrict.getText().toString().trim();
        String ward = tvSelectedWard.getText().toString().trim();
        boolean isDefault = cbDefault.isChecked();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_required_field));
            return;
        }
        if (TextUtils.isEmpty(phonePart)) {
            etPhone.setError(getString(R.string.error_required_field));
            return;
        }
        if (phonePart.length() < 9) {
            etPhone.setError(getString(R.string.error_phone_length));
            return;
        }
        if (city.equals(getString(R.string.hint_choose_city))) {
            ToastUtils.showCustomToast(this, getString(R.string.error_select_city));
            return;
        }
        if (district.equals(getString(R.string.hint_choose_district))) {
            ToastUtils.showCustomToast(this, getString(R.string.error_select_district));
            return;
        }
        if (ward.equals(getString(R.string.hint_choose_ward))) {
            ToastUtils.showCustomToast(this, getString(R.string.error_select_ward));
            return;
        }
        if (TextUtils.isEmpty(street)) {
            etStreet.setError(getString(R.string.error_required_field));
            return;
        }

        String fullPhone = "+84" + phonePart;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(ADDRESS_KEY, null);
        List<Address> addressList = new ArrayList<>();
        Gson gson = new Gson();
        
        if (json != null) {
            Type type = new TypeToken<ArrayList<Address>>() {}.getType();
            addressList = gson.fromJson(json, type);
        }

        if (isDefault) {
            for (Address a : addressList) {
                a.setDefault(false);
            }
        }

        if (editAddress != null) {
            // Update existing
            for (int i = 0; i < addressList.size(); i++) {
                if (addressList.get(i).getId().equals(editAddress.getId())) {
                    Address updated = new Address(editAddress.getId(), selectedLabel, name, fullPhone, street, ward, district, city, isDefault);
                    updated.setCountry(selectedCountry);
                    addressList.set(i, updated);
                    break;
                }
            }
        } else {
            // Add new
            String id = UUID.randomUUID().toString();
            Address newAddress = new Address(id, selectedLabel, name, fullPhone, street, ward, district, city, isDefault);
            newAddress.setCountry(selectedCountry);
            addressList.add(newAddress);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ADDRESS_KEY, gson.toJson(addressList));
        editor.apply();

        setResult(RESULT_OK);
        finish();
    }
}