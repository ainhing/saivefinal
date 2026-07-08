package com.example.saive.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.PaymentCard;
import com.example.saive.utils.DataManager;

@android.annotation.SuppressLint("SetTextI18n")
public class AddPaymentCardActivity extends BaseActivity {

    private EditText etCardNumber, etCardHolder, etExpiry;
    private TextView tvPreviewNumber, tvPreviewHolder, tvPreviewExpiry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment_card);

        if (getWindow() != null) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMaroon));
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etCardNumber = findViewById(R.id.etCardNumber);
        etCardHolder = findViewById(R.id.etCardHolder);
        etExpiry = findViewById(R.id.etExpiry);

        tvPreviewNumber = findViewById(R.id.tvCardNumber);
        tvPreviewHolder = findViewById(R.id.tvCardHolder);
        tvPreviewExpiry = findViewById(R.id.tvExpiryDate);

        setupPreviewSync();

        findViewById(R.id.btnSaveCard).setOnClickListener(v -> saveCard());
    }

    private void setupPreviewSync() {
        etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = s.toString().replaceAll(" ", "");
                if (val.isEmpty()) {
                    tvPreviewNumber.setText(R.string.card_preview_number);
                } else {
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < val.length(); i++) {
                        if (i > 0 && i % 4 == 0) formatted.append(" ");
                        formatted.append(val.charAt(i));
                    }
                    tvPreviewNumber.setText(formatted.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
        // No-op
    }
        });

        etCardHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvPreviewHolder.setText(s.toString().isEmpty() ? "CARD HOLDER" : s.toString().toUpperCase(java.util.Locale.getDefault()));
            }
            @Override
            public void afterTextChanged(Editable s) {
        // No-op
    }
        });

        etExpiry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.length() == 2 && before < count && !input.contains("/")) {
                    etExpiry.setText(input + "/");
                    etExpiry.setSelection(etExpiry.getText().length());
                }
                tvPreviewExpiry.setText(s.toString().isEmpty() ? "MM/YY" : s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
        // No-op
    }
        });
    }

    private void saveCard() {
        String number = etCardNumber.getText().toString().trim();
        String holder = etCardHolder.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();

        if (number.length() < 12) {
            showCustomToast(getString(R.string.error_invalid_card_num));
            return;
        }
        if (holder.isEmpty()) {
            showCustomToast(getString(R.string.error_enter_card_name));
            return;
        }

        PaymentCard card = new PaymentCard(
                String.valueOf(System.currentTimeMillis()),
                number,
                holder,
                expiry,
                "VISA" // Simplified
        );

        DataManager.getInstance(this).addPaymentCard(card);
        showCustomToast(getString(R.string.toast_card_saved_success));
        finish();
    }
}
