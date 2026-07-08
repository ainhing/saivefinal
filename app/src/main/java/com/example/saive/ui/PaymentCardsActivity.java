package com.example.saive.ui;

import android.os.Bundle;
import android.view.View;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.adapters.PaymentCardAdapter;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.PaymentCard;
import com.example.saive.utils.DataManager;

import java.util.List;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class PaymentCardsActivity extends BaseActivity {

    private RecyclerView rvPaymentCards;
    private View emptyState;
    private PaymentCardAdapter adapter;
    private List<PaymentCard> cardList;

    @Override
    protected void onResume() {
        super.onResume();
        loadCards();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_cards);

        if (getWindow() != null) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMaroon));
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvPaymentCards = findViewById(R.id.rvPaymentCards);
        emptyState = findViewById(R.id.emptyState);

        // Nếu chưa có card nào, thêm 1 card demo
        if (DataManager.getInstance(this).getPaymentCards().isEmpty()) {
            PaymentCard demoCard = new PaymentCard(
                    "demo",
                    "4242 4242 4242 4242",
                    "HUYNH THAO NHI",
                    "12/26",
                    "VISA"
            );
            DataManager.getInstance(this).addPaymentCard(demoCard);
        }

        loadCards();

        findViewById(R.id.fabAddCard).setOnClickListener(v -> showAddCardBottomSheet());
    }

    @android.annotation.SuppressLint("InflateParams")
    private void showAddCardBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_add_card_bottom_sheet, null);
        bottomSheetDialog.setContentView(view);

        EditText etCardNumber = view.findViewById(R.id.etCardNumber);
        EditText etCardHolder = view.findViewById(R.id.etCardHolder);
        EditText etExpiry = view.findViewById(R.id.etExpiry);
        
        TextView tvPreviewNumber = view.findViewById(R.id.tvCardNumber);
        TextView tvPreviewHolder = view.findViewById(R.id.tvCardHolder);
        TextView tvPreviewExpiry = view.findViewById(R.id.tvExpiryDate);

        // Preview Logic
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
                tvPreviewHolder.setText(s.toString().isEmpty() ? getString(R.string.card_preview_holder) : s.toString().toUpperCase(java.util.Locale.getDefault()));
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
                tvPreviewExpiry.setText(s.toString().isEmpty() ? getString(R.string.card_preview_expiry) : s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
        // No-op
    }
        });

        view.findViewById(R.id.btnSaveCard).setOnClickListener(v -> {
            String number = etCardNumber.getText().toString().trim();
            String holder = etCardHolder.getText().toString().trim();
            String expiry = etExpiry.getText().toString().trim();

            if (number.length() < 12) {
                showCustomToast(getString(R.string.error_invalid_card_number));
                return;
            }

            PaymentCard card = new PaymentCard(
                    String.valueOf(System.currentTimeMillis()),
                    number,
                    holder,
                    expiry,
                    "VISA"
            );

            DataManager.getInstance(this).addPaymentCard(card);
            loadCards();
            bottomSheetDialog.dismiss();
            showCustomToast(getString(R.string.toast_card_added));
        });

        bottomSheetDialog.show();
    }

    private void loadCards() {
        cardList = DataManager.getInstance(this).getPaymentCards();
        if (cardList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvPaymentCards.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvPaymentCards.setVisibility(View.VISIBLE);
            adapter = new PaymentCardAdapter(cardList, card -> {
                // Handle card selection if needed in this activity
            });
            rvPaymentCards.setLayoutManager(new LinearLayoutManager(this));
            rvPaymentCards.setAdapter(adapter);
        }
    }
}
