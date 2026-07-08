package com.example.saive.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saive.R;

public class PaymentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        Button btnContinueShopping = findViewById(R.id.btnContinueShopping);
        TextView btnViewOrder = findViewById(R.id.btnViewOrder);

        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnViewOrder.setOnClickListener(v -> {
            String orderId = getIntent().getStringExtra("ORDER_ID");
            Intent intent;
            if (orderId != null && !orderId.isEmpty()) {
                intent = new Intent(PaymentSuccessActivity.this, OrderTrackingActivity.class);
                intent.putExtra("ORDER_ID", orderId);
            } else {
                intent = new Intent(PaymentSuccessActivity.this, MyOrdersActivity.class);
            }
            startActivity(intent);
            finish();
        });
    }
}