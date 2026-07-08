package com.example.saive.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saive.R;
import com.example.saive.adapters.CouponAdapter;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.Coupon;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.example.saive.utils.DataManager;
import java.util.ArrayList;
import java.util.List;

public class CouponActivity extends BaseActivity {

    private RecyclerView rvCoupons;
    private CouponAdapter adapter;
    private List<Coupon> couponList;
    private EditText etSearch;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        dataManager = DataManager.getInstance(this);

        if (getWindow() != null) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMaroon));
            getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        etSearch = findViewById(R.id.etSearch);

        rvCoupons = findViewById(R.id.rvCoupons);
        rvCoupons.setLayoutManager(new LinearLayoutManager(this));

        loadCoupons();

        adapter = new CouponAdapter(couponList, this::showCouponDetail);
        rvCoupons.setAdapter(adapter);

        setupSearch();
    }

    @android.annotation.SuppressLint("InflateParams")
    private void showCouponDetail(Coupon coupon) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_coupon_detail, null);

        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvDiscount = view.findViewById(R.id.tvDetailDiscount);
        TextView tvCode = view.findViewById(R.id.tvDetailCode);
        MaterialButton btnCopy = view.findViewById(R.id.btnCopyDetail);

        tvTitle.setText(coupon.getTitle());
        tvDiscount.setText(getString(R.string.coupon_detail_discount_format, coupon.getDiscount()));
        tvCode.setText(coupon.getCode());

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Coupon Code", coupon.getCode());
            clipboard.setPrimaryClip(clip);
            
            showCustomToast(getString(R.string.toast_coupon_copied, coupon.getCode()));
            
            if (getCallingActivity() != null) {
                // Trả kết quả về cho CartActivity nếu được gọi bằng startActivityForResult
                Intent resultIntent = new Intent();
                resultIntent.putExtra("COUPON_CODE", coupon.getCode());
                setResult(RESULT_OK, resultIntent);
            } else {
                // Chuyển trực tiếp sang CartActivity nếu mở từ Profile
                Intent intent = new Intent(this, CartActivity.class);
                intent.putExtra("COUPON_CODE", coupon.getCode());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
            
            dialog.dismiss();
            finish();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
        // No-op
    }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadCoupons() {
        couponList = dataManager.getCoupons();
        fetchCouponsFromServer();
    }

    private void fetchCouponsFromServer() {
        try {
            com.google.firebase.database.DatabaseReference couponsRef = 
                com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference("Vouchers");
            
            couponsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        List<Coupon> serverCoupons = new ArrayList<>();
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            try {
                                String title = DataManager.getStringSafe(child.child("Title"));
                                if (title.isEmpty()) title = DataManager.getStringSafe(child.child("discountType"));
                                
                                String desc = DataManager.getStringSafe(child.child("Description"));
                                if (desc.isEmpty()) desc = DataManager.getStringSafe(child.child("description"));

                                // Lấy Discount an toàn (chấp nhận cả String và Number)
                                String discount = DataManager.getStringSafe(child.child("Discount"));
                                if (discount.isEmpty()) discount = DataManager.getStringSafe(child.child("discount"));
                                if (discount.isEmpty()) discount = "0";
                                
                                // Fix: Try both EndDate (used by Admin) and ExpiryDate (legacy)
                                String expiryDate = DataManager.getStringSafe(child.child("EndDate"));
                                if (expiryDate.isEmpty()) expiryDate = DataManager.getStringSafe(child.child("endDate"));
                                if (expiryDate.isEmpty()) expiryDate = DataManager.getStringSafe(child.child("ExpiryDate"));
                                
                                String code = DataManager.getStringSafe(child.child("Code"));
                                if (code.isEmpty()) code = DataManager.getStringSafe(child.child("code"));
                                
                                String status = DataManager.getStringSafe(child.child("Status"));
                                if (status.isEmpty()) status = DataManager.getStringSafe(child.child("status"));

                                String type = DataManager.getStringSafe(child.child("Type"));
                                if (type.isEmpty()) type = DataManager.getStringSafe(child.child("type"));

                                Integer points = child.child("PointsRequired").getValue(Integer.class);
                                if (points == null) points = 0;

                                String descEn = DataManager.getStringSafe(child.child("Description_en"));
                                if (descEn.isEmpty()) descEn = DataManager.getStringSafe(child.child("description_en"));
                                String descZh = DataManager.getStringSafe(child.child("Description_zh"));
                                if (descZh.isEmpty()) descZh = DataManager.getStringSafe(child.child("description_zh"));

                                Coupon coupon = new Coupon(title, desc, discount, expiryDate, code, status, points, type);
                                coupon.setDescriptionEn(descEn);
                                coupon.setDescriptionZh(descZh);
                                serverCoupons.add(coupon);
                            } catch (Exception e) {
                                android.util.Log.e("CouponActivity", "Error parsing coupon", e);
                            }
                        }
                        if (!serverCoupons.isEmpty()) {
                            couponList.clear();
                            couponList.addAll(serverCoupons);
                            dataManager.saveCoupons(serverCoupons);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                    android.util.Log.e("CouponActivity", "Firebase Coupons Cancelled", error.toException());
                }
            });
        } catch (Exception e) {
            android.util.Log.e("CouponActivity", "Error initiating coupons fetch", e);
        }
    }
}
