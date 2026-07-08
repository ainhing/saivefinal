package com.example.saive.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.adapters.CheckoutAddressAdapter;
import com.example.saive.base.BaseActivity;
import com.example.saive.databinding.ActivityCheckoutBinding;
import com.example.saive.models.Address;
import com.example.saive.utils.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@android.annotation.SuppressLint("SetTextI18n")
public class CheckoutActivity extends BaseActivity {

    private ActivityCheckoutBinding binding;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_NAME = "full_name";
    private static final String KEY_PHONE = "phone_number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_DISTRICT = "district";
    private static final String KEY_ADDRESS = "address";

    private String couponCode = "";
    private List<Address> addressList = new ArrayList<>();
    private List<com.example.saive.models.PaymentCard> savedCards = new ArrayList<>();
    private Address selectedAddress;
    private com.example.saive.models.PaymentCard selectedCard;
    private boolean isPaymentStep = false;
    private String totalPrice, selectedSize, couponType;
    private double discountRate, discountValue;
    private static final String ADDRESS_PREFS = "address_prefs";
    private static final String ADDRESS_KEY = "saved_addresses";
    private static final int MAP_REQUEST_CODE = 1002;

    private ActivityResultLauncher<Intent> loginLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loginLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        setupUI();
                    } else {
                        finish();
                    }
                }
        );
        super.onCreate(savedInstanceState);

        updateStatusBar();
        if (!com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.putExtra("return_result", true);
            loginLauncher.launch(loginIntent);
            return;
        }

        setupUI();
        com.example.saive.utils.LocationProvider.init(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("selected_address");
            if (address != null) {
                binding.etAddress.setText(address);
            }
        }
    }

    private void setupUI() {
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        loadIntentData();
        loadAddresses();
        checkExistingAddress();
        setupListeners();
    }

    private void updateStatusBar() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorHeaderBg));
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        if (isDarkMode) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private void loadIntentData() {
        totalPrice = getIntent().getStringExtra("total_price");
        selectedSize = getIntent().getStringExtra("selected_size");
        discountRate = getIntent().getDoubleExtra("discount_rate", 0);
        discountValue = getIntent().getDoubleExtra("discount_value", 0);
        couponType = getIntent().getStringExtra("discount_type");
        couponCode = getIntent().getStringExtra("coupon_code");

        if (totalPrice != null) {
            binding.btnAction.setText(getString(R.string.btn_continue_payment_format, totalPrice));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding == null) return;
        loadAddresses();
        if (!addressList.isEmpty()) {
            checkExistingAddress();
        }
    }

    private void loadAddresses() {
        SharedPreferences prefs = getSharedPreferences(ADDRESS_PREFS, MODE_PRIVATE);
        String json = prefs.getString(ADDRESS_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Address>>() {}.getType();
            List<Address> loaded = gson.fromJson(json, type);
            if (loaded != null) {
                addressList.clear();
                addressList.addAll(loaded);
            }
        }
    }

    private void checkExistingAddress() {
        boolean isLoggedIn = com.example.saive.utils.UserSession.getInstance().isLoggedIn();

        if (isLoggedIn && !addressList.isEmpty()) {
            selectedAddress = addressList.get(0);
            for (Address a : addressList) {
                if (a.isDefault()) {
                    selectedAddress = a;
                    break;
                }
            }
            showAddressSummaryStep();
        } else {
            showShippingStep();
            binding.sectionTitle.setVisibility(View.VISIBLE);
            binding.sectionTitle.setText(R.string.checkout_shipping_title);

            if (!isLoggedIn) {
                clearShippingFields();
            } else {
                loadSavedInfo();
            }
        }
    }

    private void clearShippingFields() {
        binding.etFullName.setText("");
        binding.etPhone.setText("");
        binding.etEmail.setText("");
        binding.etAddress.setText("");
        binding.tvSelectedCity.setText(R.string.hint_choose_city);
        binding.tvSelectedDistrict.setText(R.string.hint_choose_district);
        binding.tvSelectedWard.setText(R.string.hint_choose_ward);
    }

    private void showShippingStep() {
        isPaymentStep = false;
        binding.containerShipping.setVisibility(View.VISIBLE);
        binding.containerPayment.setVisibility(View.GONE);
        binding.containerAddressSelection.setVisibility(View.GONE);
        binding.layoutDefaultAddress.setVisibility(View.GONE);
        binding.sectionTitle.setVisibility(View.VISIBLE);
        binding.sectionTitle.setText(R.string.checkout_shipping_title);
        binding.btnAction.setVisibility(View.VISIBLE);
        if (totalPrice != null) {
            binding.btnAction.setText(getString(R.string.btn_continue_payment_format, totalPrice));
        } else {
            binding.btnAction.setText(R.string.btn_continue_payment);
        }
    }

    private void showAddressSelectionStep() {
        isPaymentStep = false;
        binding.layoutDefaultAddress.setVisibility(View.GONE);
        binding.containerShipping.setVisibility(View.GONE);
        binding.containerPayment.setVisibility(View.GONE);
        binding.containerAddressSelection.setVisibility(View.VISIBLE);
        binding.sectionTitle.setVisibility(View.GONE);

        binding.rvCheckoutAddresses.setLayoutManager(new LinearLayoutManager(this));
        CheckoutAddressAdapter adapter = new CheckoutAddressAdapter(addressList, selectedAddress, address -> {
            selectedAddress = address;
            showAddressSummaryStep();
        });
        binding.rvCheckoutAddresses.setAdapter(adapter);

        binding.btnAction.setVisibility(View.GONE);
    }

    private void showAddressSummaryStep() {
        isPaymentStep = false;
        binding.containerAddressSelection.setVisibility(View.GONE);
        binding.containerShipping.setVisibility(View.GONE);
        binding.containerPayment.setVisibility(View.GONE);
        binding.layoutDefaultAddress.setVisibility(View.VISIBLE);
        binding.btnAction.setVisibility(View.VISIBLE);

        updateDefaultAddressUI();
        binding.sectionTitle.setVisibility(View.GONE);

        if (totalPrice != null) {
            binding.btnAction.setText(getString(R.string.btn_continue_payment_format, totalPrice));
        } else {
            binding.btnAction.setText(R.string.btn_continue_payment);
        }
    }

    private void updateDefaultAddressUI() {
        if (selectedAddress != null) {
            binding.tvDefaultAddressLabel.setText(selectedAddress.getLabel().toUpperCase(java.util.Locale.getDefault()));
            binding.tvDefaultName.setText(selectedAddress.getFullName());
            binding.tvDefaultAddress.setText(selectedAddress.getFullDisplayAddress());
            binding.tvDefaultPhone.setText(selectedAddress.getPhoneNumber());
        }
    }

    private void showAddCardBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        com.example.saive.databinding.LayoutAddCardBottomSheetBinding sheetBinding =
                com.example.saive.databinding.LayoutAddCardBottomSheetBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.etCardNumber.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = s != null ? s.toString().replaceAll(" ", "") : "";
                if (val.isEmpty()) {
                    sheetBinding.cardPreview.tvCardNumber.setText(R.string.card_preview_number);
                } else {
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < val.length(); i++) {
                        if (i > 0 && i % 4 == 0) formatted.append(" ");
                        formatted.append(val.charAt(i));
                    }
                    sheetBinding.cardPreview.tvCardNumber.setText(formatted.toString());
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {
        // No-op
    }
        });

        sheetBinding.etCardHolder.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                sheetBinding.cardPreview.tvCardHolder.setText(s != null && !s.toString().isEmpty() ? s.toString().toUpperCase(java.util.Locale.getDefault()) : "CARD HOLDER");
            }
            @Override public void afterTextChanged(android.text.Editable s) {
        // No-op
    }
        });

        sheetBinding.etExpiry.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s != null ? s.toString() : "";
                if (input.length() == 2 && before < count && !input.contains("/")) {
                    sheetBinding.etExpiry.setText(input + "/");
                    sheetBinding.etExpiry.setSelection(sheetBinding.etExpiry.getText().length());
                }
                sheetBinding.cardPreview.tvExpiryDate.setText(input.isEmpty() ? "MM/YY" : input);
            }
            @Override public void afterTextChanged(android.text.Editable s) {
        // No-op
    }
        });

        sheetBinding.btnSaveCard.setOnClickListener(v -> {
            String number = sheetBinding.etCardNumber.getText().toString().trim();
            String holder = sheetBinding.etCardHolder.getText().toString().trim();
            String expiry = sheetBinding.etExpiry.getText().toString().trim();

            if (number.length() < 12) {
                ToastUtils.showCustomToast(this, getString(R.string.checkout_invalid_card));
                return;
            }

            com.example.saive.models.PaymentCard card = new com.example.saive.models.PaymentCard(
                    String.valueOf(System.currentTimeMillis()),
                    number,
                    holder,
                    expiry,
                    "VISA"
            );

            com.example.saive.utils.DataManager.getInstance(this).addPaymentCard(card);
            bottomSheetDialog.dismiss();
            ToastUtils.showCustomToast(this, getString(R.string.checkout_card_added));
            loadSavedCards();
        });

        bottomSheetDialog.show();
    }

    private void initViews() {
        setupSelectors();

        binding.btnBack.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (isPaymentStep) {
                if (binding.containerAddressSelection.getVisibility() == View.VISIBLE) {
                    showAddressSummaryStep();
                } else {
                    showShippingStep();
                }
            } else {
                finish();
            }
        });
    }

    private void setupSelectors() {
        binding.btnCitySelector.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            List<String> cities = com.example.saive.utils.LocationProvider.getProvinces(this);
            showOptionsBottomSheet(getString(R.string.hint_choose_city),
                    cities.toArray(new String[0]),
                    selection -> {
                        binding.tvSelectedCity.setText(selection);
                        binding.tvSelectedDistrict.setText(R.string.hint_choose_district);
                        binding.tvSelectedWard.setText(R.string.hint_choose_ward);
                    });
        });
        binding.tvSelectedCity.setOnClickListener(v -> binding.btnCitySelector.performClick());

        binding.btnDistrictSelector.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            String selectedCity = binding.tvSelectedCity.getText().toString();
            List<String> districts = com.example.saive.utils.LocationProvider.getDistricts(this, selectedCity);

            if (districts.isEmpty()) {
                districts = new ArrayList<>();
                districts.add(selectedCity + " District 1");
                districts.add(selectedCity + " District 2");
            }

            showOptionsBottomSheet(getString(R.string.hint_choose_district),
                    districts.toArray(new String[0]),
                    selection -> {
                        binding.tvSelectedDistrict.setText(selection);
                        binding.tvSelectedWard.setText(R.string.hint_choose_ward);
                    });
        });
        binding.tvSelectedDistrict.setOnClickListener(v -> binding.btnDistrictSelector.performClick());

        binding.btnWardSelector.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            String selectedCity = binding.tvSelectedCity.getText().toString();
            String selectedDistrict = binding.tvSelectedDistrict.getText().toString();
            List<String> wards = com.example.saive.utils.LocationProvider.getWards(this, selectedCity, selectedDistrict);

            if (wards.isEmpty()) {
                wards = new ArrayList<>();
                wards.add(selectedDistrict + " Ward 1");
                wards.add(selectedDistrict + " Ward 2");
            }

            showOptionsBottomSheet(getString(R.string.hint_choose_ward),
                    wards.toArray(new String[0]),
                    selection -> binding.tvSelectedWard.setText(selection));
        });
        binding.tvSelectedWard.setOnClickListener(v -> binding.btnWardSelector.performClick());
    }

    private interface OnOptionSelected {
        void onSelected(String selection);
    }

    private void showOptionsBottomSheet(String title, String[] options, OnOptionSelected callback) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        com.example.saive.databinding.LayoutBottomSheetMenuBinding sheetBinding =
                com.example.saive.databinding.LayoutBottomSheetMenuBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvSheetTitle.setText(title);
        sheetBinding.etSearchOption.setVisibility(View.VISIBLE);
        sheetBinding.rvSheetOptions.setLayoutManager(new LinearLayoutManager(this));

        List<String> originalOptions = java.util.Arrays.asList(options);
        List<String> filteredOptions = new ArrayList<>(originalOptions);

        RecyclerView.Adapter<OptionViewHolder> adapter = new RecyclerView.Adapter<>() {
            @NonNull
            @Override
            public OptionViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
                com.example.saive.databinding.ItemBottomSheetOptionBinding itemBinding =
                        com.example.saive.databinding.ItemBottomSheetOptionBinding.inflate(getLayoutInflater(), parent, false);
                return new OptionViewHolder(itemBinding);
            }

            @Override
            public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
                String option = filteredOptions.get(position);
                holder.binding.tvOptionName.setText(option);
                holder.itemView.setOnClickListener(v -> {
                    callback.onSelected(option);
                    bottomSheetDialog.dismiss();
                });
            }

            @Override
            public int getItemCount() {
                return filteredOptions.size();
            }
        };

        sheetBinding.rvSheetOptions.setAdapter(adapter);

        sheetBinding.etSearchOption.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }
            @android.annotation.SuppressLint("NotifyDataSetChanged")
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase(java.util.Locale.getDefault()).trim();
                filteredOptions.clear();
                for (String option : originalOptions) {
                    if (option.toLowerCase(java.util.Locale.getDefault()).contains(query)) {
                        filteredOptions.add(option);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void afterTextChanged(android.text.Editable s) {
        // No-op
    }
        });

        bottomSheetDialog.show();
    }

    private static class OptionViewHolder extends RecyclerView.ViewHolder {
        com.example.saive.databinding.ItemBottomSheetOptionBinding binding;
        OptionViewHolder(com.example.saive.databinding.ItemBottomSheetOptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void loadSavedInfo() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        binding.etFullName.setText(prefs.getString(KEY_NAME, ""));
        binding.etPhone.setText(prefs.getString(KEY_PHONE, ""));
        binding.etEmail.setText(prefs.getString(KEY_EMAIL, ""));

        String savedCity = prefs.getString("city", "");
        if (!TextUtils.isEmpty(savedCity)) binding.tvSelectedCity.setText(savedCity);

        String savedDistrict = prefs.getString(KEY_DISTRICT, "");
        if (!TextUtils.isEmpty(savedDistrict)) binding.tvSelectedDistrict.setText(savedDistrict);

        String savedWard = prefs.getString("ward", "");
        if (!TextUtils.isEmpty(savedWard)) binding.tvSelectedWard.setText(savedWard);

        binding.etAddress.setText(prefs.getString(KEY_ADDRESS, ""));
        if (!TextUtils.isEmpty(binding.etFullName.getText())) binding.cbSaveInfo.setChecked(true);
    }

    private void setupListeners() {
        binding.btnAddAddress.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            startActivity(new Intent(this, AddAddressActivity.class));
        });

        binding.btnChangeAddress.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            showAddressSelectionStep();
        });

        binding.btnAddPaymentCard.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            showAddCardBottomSheet();
        });

        binding.btnChangePaymentCard.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            showCardSelectionBottomSheet();
        });

        binding.layoutAddCard.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            showAddCardBottomSheet();
        });

        binding.layoutDefaultPaymentCard.setOnClickListener(v -> selectPaymentMethod(binding.rbDefaultCard));
        binding.rbDefaultCard.setOnClickListener(v -> selectPaymentMethod(binding.rbDefaultCard));

        binding.layoutCod.setOnClickListener(v -> selectPaymentMethod(binding.rbCod));
        binding.layoutBank.setOnClickListener(v -> selectPaymentMethod(binding.rbBank));
        binding.layoutVietQR.setOnClickListener(v -> selectPaymentMethod(binding.rbVietQR));

        binding.rbCod.setOnClickListener(v -> selectPaymentMethod(binding.rbCod));
        binding.rbBank.setOnClickListener(v -> selectPaymentMethod(binding.rbBank));
        binding.rbVietQR.setOnClickListener(v -> selectPaymentMethod(binding.rbVietQR));

        binding.btnAddFromCheckout.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            binding.containerAddressSelection.setVisibility(View.GONE);
            showShippingStep();
        });

        binding.btnOpenMap.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            startActivityForResult(new Intent(this, LocationPermissionActivity.class), MAP_REQUEST_CODE);
        });

        binding.cardOrderSummary.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (addressList != null && !addressList.isEmpty()) {
                showAddressSelectionStep();
            } else {
                showShippingStep();
            }
        });

        binding.btnAction.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (!isPaymentStep) {
                if (binding.layoutDefaultAddress.getVisibility() == View.VISIBLE) {
                    showPaymentStep();
                } else if (validateShippingInfo()) {
                    if (binding.cbSaveInfo.isChecked()) saveInfo();
                    showPaymentStep();
                }
            } else {
                processOrder();
            }
        });
    }

    private void selectPaymentMethod(RadioButton selectedRb) {
        selectedRb.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        binding.rbCod.setChecked(selectedRb == binding.rbCod);
        binding.rbBank.setChecked(selectedRb == binding.rbBank);
        binding.rbVietQR.setChecked(selectedRb == binding.rbVietQR);
        binding.rbDefaultCard.setChecked(selectedRb == binding.rbDefaultCard);
    }

    private boolean validateShippingInfo() {
        if (TextUtils.isEmpty(binding.etFullName.getText())) {
            binding.etFullName.setError(getString(R.string.error_required_field));
            return false;
        }
        if (TextUtils.isEmpty(binding.etPhone.getText())) {
            binding.etPhone.setError(getString(R.string.error_required_field));
            return false;
        }
        if (TextUtils.isEmpty(binding.etEmail.getText())) {
            binding.etEmail.setError(getString(R.string.error_required_field));
            return false;
        }
        if (binding.tvSelectedCity.getText().toString().contains("Choose")) {
            ToastUtils.showCustomToast(this, getString(R.string.checkout_error_city));
            return false;
        }
        if (binding.tvSelectedDistrict.getText().toString().contains("Choose")) {
            ToastUtils.showCustomToast(this, getString(R.string.checkout_error_district));
            return false;
        }
        if (binding.tvSelectedWard.getText().toString().contains("Choose")) {
            ToastUtils.showCustomToast(this, getString(R.string.checkout_error_ward));
            return false;
        }
        if (TextUtils.isEmpty(binding.etAddress.getText())) {
            binding.etAddress.setError(getString(R.string.error_required_field));
            return false;
        }
        return true;
    }

    private void saveInfo() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NAME, binding.etFullName.getText().toString());
        editor.putString(KEY_PHONE, binding.etPhone.getText().toString());
        editor.putString(KEY_EMAIL, binding.etEmail.getText().toString());
        editor.putString("city", binding.tvSelectedCity.getText().toString());
        editor.putString(KEY_DISTRICT, binding.tvSelectedDistrict.getText().toString());
        editor.putString("ward", binding.tvSelectedWard.getText().toString());
        editor.putString(KEY_ADDRESS, binding.etAddress.getText().toString());
        editor.apply();
    }

    private void showPaymentStep() {
        isPaymentStep = true;
        binding.containerShipping.setVisibility(View.GONE);
        binding.containerAddressSelection.setVisibility(View.GONE);
        binding.layoutDefaultAddress.setVisibility(View.GONE);
        binding.containerPayment.setVisibility(View.VISIBLE);

        loadSavedCards();
        binding.sectionTitle.setVisibility(View.GONE);

        if (selectedAddress != null) {
            binding.tvSummaryFullName.setText(selectedAddress.getFullName());
            binding.tvSummaryAddress.setText(selectedAddress.getFullDisplayAddress());
            binding.tvSummaryPhone.setText(selectedAddress.getPhoneNumber());
        } else {
            binding.tvSummaryFullName.setText(binding.etFullName.getText().toString());
            String addressText = binding.etAddress.getText().toString() + ", " +
                    binding.tvSelectedWard.getText().toString() + ", " +
                    binding.tvSelectedDistrict.getText().toString() + ", " +
                    binding.tvSelectedCity.getText().toString();
            binding.tvSummaryAddress.setText(addressText);
            binding.tvSummaryPhone.setText(binding.etPhone.getText().toString());
        }

        if (selectedSize != null && !selectedSize.isEmpty()) {
            binding.tvSummarySize.setVisibility(View.VISIBLE);
            binding.tvSummarySize.setText(getString(R.string.format_summary_size, selectedSize));
        } else {
            binding.tvSummarySize.setVisibility(View.GONE);
        }

        updatePriceSummary();

        if (totalPrice != null) {
            binding.btnAction.setText(getString(R.string.btn_place_order_format, totalPrice));
        } else {
            binding.btnAction.setText(R.string.btn_place_order);
        }
    }

    private void showCardSelectionBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        com.example.saive.databinding.LayoutAddressSelectionBottomSheetBinding sheetBinding =
                com.example.saive.databinding.LayoutAddressSelectionBottomSheetBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvBottomSheetTitle.setText(R.string.payment_card_select_title);
        sheetBinding.rvBottomSheetAddresses.setLayoutManager(new LinearLayoutManager(this));

        com.example.saive.adapters.PaymentCardAdapter adapter = new com.example.saive.adapters.PaymentCardAdapter(savedCards, card -> {
            selectedCard = card;
            updateDefaultCardUI();
            selectPaymentMethod(binding.rbDefaultCard);
            bottomSheetDialog.dismiss();
        });
        adapter.setSelectedCard(selectedCard);
        sheetBinding.rvBottomSheetAddresses.setAdapter(adapter);

        sheetBinding.btnAddNewAddress.setText(R.string.payment_method_add_card);
        sheetBinding.btnAddNewAddress.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showAddCardBottomSheet();
        });

        bottomSheetDialog.show();
    }

    private void updateDefaultCardUI() {
        if (selectedCard != null) {
            binding.layoutDefaultPaymentCard.setVisibility(View.VISIBLE);
            binding.layoutAddCard.setVisibility(View.GONE);
            binding.btnChangePaymentCard.setVisibility(View.VISIBLE);
            binding.tvDefaultCardNumber.setText(selectedCard.getCardNumber());
            binding.tvDefaultCardHolder.setText(selectedCard.getCardHolderName());
        } else {
            binding.layoutDefaultPaymentCard.setVisibility(View.GONE);
            binding.layoutAddCard.setVisibility(View.VISIBLE);
            binding.btnChangePaymentCard.setVisibility(View.GONE);
        }
    }

    private void loadSavedCards() {
        savedCards = com.example.saive.utils.DataManager.getInstance(this).getPaymentCards();
        if (savedCards != null && !savedCards.isEmpty()) {
            if (selectedCard == null) selectedCard = savedCards.get(0);
            updateDefaultCardUI();
        } else {
            binding.layoutDefaultPaymentCard.setVisibility(View.GONE);
            binding.layoutAddCard.setVisibility(View.VISIBLE);
            binding.btnChangePaymentCard.setVisibility(View.GONE);
        }
    }

    private void updatePriceSummary() {
        double subtotal = com.example.saive.utils.CartManager.getInstance(this).getTotalPrice();
        double discountAmount = 0;

        if ("Fixed".equalsIgnoreCase(couponType)) {
            discountAmount = discountValue;
        } else {
            discountAmount = subtotal * discountRate;
        }

        double finalTotal = Math.max(0, subtotal - discountAmount);

        binding.tvSummarySubtotal.setText(com.example.saive.utils.PriceFormatter.formatPrice(subtotal));

        if (discountAmount > 0) {
            binding.layoutSummaryDiscount.setVisibility(View.VISIBLE);
            String discountText = getString(R.string.label_discount);
            if (discountText.endsWith(":")) discountText = discountText.substring(0, discountText.length() - 1);
            binding.tvSummaryDiscountLabel.setText(getString(R.string.format_discount_label, discountText, couponCode));
            binding.tvSummaryDiscountValue.setText(getString(R.string.format_negative_price, com.example.saive.utils.PriceFormatter.formatPrice(discountAmount)));
        } else {
            binding.layoutSummaryDiscount.setVisibility(View.GONE);
        }

        String formattedTotal = com.example.saive.utils.PriceFormatter.formatPrice(finalTotal);
        binding.tvSummaryTotal.setText(formattedTotal);
        totalPrice = formattedTotal;
    }

    private void processOrder() {
        if (!com.example.saive.utils.UserSession.getInstance().isLoggedIn()) {
            ToastUtils.showCustomToast(this, getString(R.string.msg_guest_checkout_prompt));
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        if (!binding.rbCod.isChecked() && !binding.rbBank.isChecked() && !binding.rbVietQR.isChecked() &&
            !binding.rbDefaultCard.isChecked()) {
            ToastUtils.showCustomToast(this, getString(R.string.checkout_error_payment));
            return;
        }

        com.example.saive.utils.CartManager cartManager = com.example.saive.utils.CartManager.getInstance(this);
        List<com.example.saive.models.Product> cartItems = cartManager.getCartItems();
        if (cartItems.isEmpty()) return;

        List<com.example.saive.models.OrderItem> orderItems = new ArrayList<>();
        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append(cartItems.size()).append(" Items: ");

        for (int i = 0; i < cartItems.size(); i++) {
            com.example.saive.models.Product p = cartItems.get(i);
            String itemSize = p.getSelectedSize();
            if (itemSize == null || itemSize.isEmpty()) {
                itemSize = (p.getCategory() != null && p.getCategory().toLowerCase(java.util.Locale.ROOT).contains("glasses")) ? "One Size" : "M";
            }

            orderItems.add(new com.example.saive.models.OrderItem(p.getName(), itemSize, p.getSelectedColor() != null ? p.getSelectedColor() : "—", p.getQuantity(), p.getPrice(), p.getImageResId(), p.getImageUrl()));
            summaryBuilder.append(p.getName());
            if (i < cartItems.size() - 1) summaryBuilder.append(", ");
        }


        String customerName = selectedAddress != null ? selectedAddress.getFullName() : binding.etFullName.getText().toString();
        String shippingAddr = selectedAddress != null ? selectedAddress.getFullDisplayAddress() :
                (binding.etAddress.getText().toString() + ", " + binding.tvSelectedWard.getText() + ", " +
                 binding.tvSelectedDistrict.getText() + ", " + binding.tvSelectedCity.getText());

        String method = "COD";
        if (binding.rbBank.isChecked()) method = "Bank Transfer";
        else if (binding.rbVietQR.isChecked()) method = "VietQR";
        else if (binding.rbDefaultCard.isChecked() && selectedCard != null)
            method = "Card (**** " + selectedCard.getCardNumber().substring(Math.max(0, selectedCard.getCardNumber().length() - 4)) + ")";
        final String paymentMethod = method;

        String finalPrice = (totalPrice != null) ? totalPrice : binding.tvSummaryTotal.getText().toString();
        String itemSizeLegacy = (selectedSize != null && !selectedSize.isEmpty()) ? selectedSize : cartItems.get(0).getSelectedSize();
        if (itemSizeLegacy == null || itemSizeLegacy.isEmpty()) itemSizeLegacy = "M";
        String itemColorLegacy = cartItems.get(0).getSelectedColor();
        if (itemColorLegacy == null || itemColorLegacy.isEmpty()) itemColorLegacy = "—";

        com.example.saive.models.AdminOrder newOrder = new com.example.saive.models.AdminOrder(
                "", customerName, summaryBuilder.toString(), finalPrice, "PENDING", "Just now",
                cartItems.get(0).getImageResId(), cartItems.get(0).getImageUrl(), itemSizeLegacy, itemColorLegacy, cartManager.getItemCount(), paymentMethod, shippingAddr
        );
        newOrder.setItems(orderItems);

        String uId = com.example.saive.utils.UserSession.getInstance().getUserId();
        final String userId = (uId == null) ? "" : uId;
        com.example.saive.utils.DataManager.getInstance(this).addOrder(newOrder, userId);

        // --- PREPARE DATA FOR FIREBASE & API ---
        double subTotalNumeric = com.example.saive.utils.CartManager.getInstance(this).getTotalPrice();
        double discountAmountNumeric = 0;
        if ("Fixed".equalsIgnoreCase(couponType)) {
            discountAmountNumeric = discountValue;
        } else {
            discountAmountNumeric = subTotalNumeric * discountRate;
        }
        double finalTotalNumericValue = Math.max(0, subTotalNumeric - discountAmountNumeric);
        String email = binding.etEmail.getText().toString().trim();
        if (email.isEmpty() && selectedAddress != null) email = "customer@saive.vn"; // fallback
        final String customerEmail = email;
        String customerPhone = selectedAddress != null ? selectedAddress.getPhoneNumber() : binding.etPhone.getText().toString().trim();

        // --- WRITE TO FIREBASE REALTIME DATABASE ---
        com.google.firebase.database.DatabaseReference firebaseRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://saive-403f7-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Orders");

        // ── LOGIC TẠO ID TỰ TĂNG CHO ORDER (ORDxxx) ──
        firebaseRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                int maxNum = 0;
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key != null && key.startsWith("ORD")) {
                        try {
                            int num = Integer.parseInt(key.substring(3));
                            if (num > maxNum) maxNum = num;
                        } catch (NumberFormatException e) { }
                    }
                }
                String nextOrderId = String.format(java.util.Locale.US, "ORD%03d", maxNum + 1);
                String displayOrderId = "#" + nextOrderId;
                newOrder.setOrderId(displayOrderId);

                Map<String, Object> firebaseOrder = new HashMap<>();
                firebaseOrder.put("OrderId", displayOrderId);
                firebaseOrder.put("FullName", customerName);
                firebaseOrder.put("Email", customerEmail.isEmpty() ? "customer@saive.vn" : customerEmail);
                firebaseOrder.put("Phone", customerPhone);
                firebaseOrder.put("ShippingAddress", shippingAddr);
                firebaseOrder.put("PaymentMethod", paymentMethod);
                firebaseOrder.put("TotalAmount", finalTotalNumericValue);
                firebaseOrder.put("Status", "pending");
                firebaseOrder.put("CreatedAt", com.google.firebase.database.ServerValue.TIMESTAMP);
                firebaseOrder.put("UserId", userId);

                List<Map<String, Object>> firebaseItems = new ArrayList<>();
                for (com.example.saive.models.OrderItem item : orderItems) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("ProductName", item.getName());
                    itemMap.put("Quantity", item.getQuantity());
                    itemMap.put("SelectedSize", item.getSize());
                    itemMap.put("SelectedColor", item.getColor() != null && !item.getColor().equals("—") ? item.getColor() : "—");
                    itemMap.put("size", item.getSize());
                    itemMap.put("color", item.getColor() != null && !item.getColor().equals("—") ? item.getColor() : "—");
                    String priceStr = item.getPrice() != null ? item.getPrice().replaceAll("[^\\d]", "") : "0";
                    itemMap.put("Price", priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr));
                    itemMap.put("Image", item.getImageUrl());
                    firebaseItems.add(itemMap);
                }
                firebaseOrder.put("Items", firebaseItems);

                firebaseRef.child(nextOrderId).setValue(firebaseOrder);

                com.google.firebase.database.DatabaseReference orderDetailsRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://saive-403f7-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("OrderDetails");

                for (int i = 0; i < cartItems.size(); i++) {
                    com.example.saive.models.Product p = cartItems.get(i);
                    com.example.saive.models.OrderItem item = orderItems.get(i);

                    Map<String, Object> detailMap = new HashMap<>();
                    detailMap.put("OrderId", nextOrderId);
                    detailMap.put("ProductId", p.getProductId());
                    detailMap.put("ProductName", item.getName());
                    detailMap.put("Quantity", item.getQuantity());
                    detailMap.put("Size", item.getSize());
                    String detailPriceStr = item.getPrice() != null ? item.getPrice().replaceAll("[^\\d]", "") : "0";
                    detailMap.put("UnitPrice", detailPriceStr.isEmpty() ? 0.0 : Double.parseDouble(detailPriceStr));
                    detailMap.put("Image", item.getImageUrl());

                    orderDetailsRef.child(nextOrderId + "_" + (i + 1)).setValue(detailMap);
                }

                // --- PHẦN CẬP NHẬT TỒN KHO ---
                com.google.firebase.database.DatabaseReference productsRef = com.google.firebase.database.FirebaseDatabase.getInstance("https://saive-403f7-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("Products");
                for (com.example.saive.models.Product p : cartItems) {
                    String productId = p.getProductId();
                    if (productId == null || productId.isEmpty()) continue;

                    String size = p.getSelectedSize();
                    String color = p.getSelectedColor();
                    int qty = p.getQuantity();

                    productsRef.child(productId).runTransaction(new com.google.firebase.database.Transaction.Handler() {
                        @androidx.annotation.NonNull
                        @Override
                        public com.google.firebase.database.Transaction.Result doTransaction(@androidx.annotation.NonNull com.google.firebase.database.MutableData currentData) {
                            if (currentData.getValue() == null) return com.google.firebase.database.Transaction.success(currentData);

                            // Decrease total stock
                            Integer currentStock = currentData.child("StockQuantity").getValue(Integer.class);
                            if (currentStock != null) {
                                currentData.child("StockQuantity").setValue(Math.max(0, currentStock - qty));
                            }

                            // Decrease variant stock
                            // Hỗ trợ cả node "Stock" và "Variants" (nếu có)
                            com.google.firebase.database.MutableData stockNode = currentData.child("Stock");
                            if (size != null && color != null && stockNode.getValue() != null) {
                                com.google.firebase.database.MutableData sizeNode = stockNode.child(size);
                                if (sizeNode.getValue() != null) {
                                    boolean found = false;
                                    for (com.google.firebase.database.MutableData colorNode : sizeNode.getChildren()) {
                                        if (colorNode.getKey() != null && colorNode.getKey().equalsIgnoreCase(color)) {
                                            Integer variantStock = colorNode.getValue(Integer.class);
                                            if (variantStock != null) {
                                                colorNode.setValue(Math.max(0, variantStock - qty));
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!found) {
                                        com.google.firebase.database.MutableData exactColorNode = sizeNode.child(color);
                                        Integer exactStock = exactColorNode.getValue(Integer.class);
                                        if (exactStock != null) {
                                            exactColorNode.setValue(Math.max(0, exactStock - qty));
                                        }
                                    }
                                }
                            }
                            return com.google.firebase.database.Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@androidx.annotation.Nullable com.google.firebase.database.DatabaseError error, boolean committed, @androidx.annotation.Nullable com.google.firebase.database.DataSnapshot currentData) {}
                    });
                }

                // Kết thúc checkout
                com.example.saive.utils.CartManager.getInstance(CheckoutActivity.this).clearCart();
                if (binding.rbBank.isChecked() || binding.rbVietQR.isChecked()) {
                    Intent intent = new Intent(CheckoutActivity.this, BankTransferActivity.class);
                    intent.putExtra("isVietQR", binding.rbVietQR.isChecked());
                    intent.putExtra("order_id", nextOrderId);
                    intent.putExtra("total_amount", finalTotalNumericValue);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(CheckoutActivity.this, PaymentSuccessActivity.class);
                    intent.putExtra("ORDER_ID", nextOrderId);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                ToastUtils.showCustomToast(CheckoutActivity.this, "Error: " + error.getMessage());
            }
        });
    }
}
