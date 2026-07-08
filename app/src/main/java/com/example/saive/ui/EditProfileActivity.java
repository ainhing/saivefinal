package com.example.saive.ui;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.adapters.BottomSheetOptionAdapter;
import com.example.saive.base.BaseActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class EditProfileActivity extends BaseActivity {

    private static final String USER_PREFS = "user_prefs";

    private ShapeableImageView ivAvatar;
    private TextView tvDob, tvGender;
    private android.widget.EditText etName;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {
                    }

                    selectedImageUri = uri;

                    try {
                        com.example.saive.utils.ImageUtils.setSafeImage(ivAvatar, uri);
                    } catch (Exception ignored) {
                    }

                    getSharedPreferences(USER_PREFS, MODE_PRIVATE)
                            .edit()
                            .putString("avatar_uri", uri.toString())
                            .apply();
                }
            });

    @android.annotation.SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerContainer), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(),
                    sys.top + (int) (14 * getResources().getDisplayMetrics().density),
                    v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        ivAvatar = findViewById(R.id.ivAvatar);
        tvDob = findViewById(R.id.tvDob);
        tvGender = findViewById(R.id.tvGender);
        etName = findViewById(R.id.etName);

        try {
            com.example.saive.utils.ImageUtils.setSafeImage(ivAvatar, R.drawable.model1);
        } catch (Exception ignored) {
        }
        loadSavedData();

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            finish();
        });

        // Save bottom — đồng bộ DOB/Gender lên server, sau đó finish
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            syncProfileToServer();
        });

        // Avatar
        android.view.View.OnClickListener openGallery = v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            pickImageLauncher.launch("image/*");
        };
        findViewById(R.id.btnEditAvatar).setOnClickListener(openGallery);
        findViewById(R.id.tvChangePhoto).setOnClickListener(openGallery);

        // ── MaterialDatePicker thay DatePickerDialog ──────
        findViewById(R.id.btnPickDob).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            // Tính selection mặc định
            long selectedMs = System.currentTimeMillis();
            String saved = getSharedPreferences(USER_PREFS, MODE_PRIVATE).getString("dob", "");
            if (!saved.isEmpty()) {
                try {
                    String[] p = saved.split("/");
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.set(Integer.parseInt(p[2]),
                            Integer.parseInt(p[1]) - 1,
                            Integer.parseInt(p[0]), 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    selectedMs = cal.getTimeInMillis();
                } catch (Exception ignored) {
                }
            }

            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Birthday")
                    .setSelection(selectedMs)
                    .build();

            picker.addOnPositiveButtonClickListener(selection -> {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(selection);
                String dob = String.format(java.util.Locale.ROOT, "%02d/%02d/%d",
                        cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.YEAR));

                // Cập nhật UI
                tvDob.setText(dob);
                tvDob.setTextColor(getColor(R.color.colorNoirBlack));

                // Lưu ngay
                getSharedPreferences(USER_PREFS, MODE_PRIVATE)
                        .edit()
                        .putString("dob", dob)
                        .apply();

                Toast.makeText(this, getString(R.string.birthday_saved_format, dob), Toast.LENGTH_SHORT).show();
            });

            picker.show(getSupportFragmentManager(), "DOB_PICKER");
        });

        // Gender Picker
        findViewById(R.id.btnPickGender).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            List<String> options = Arrays.asList("Male", "Female", "Other", "Prefer not to say");
            String current = tvGender.getText().toString();

            BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
            android.view.View sheet = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_menu, null);
            dialog.setContentView(sheet);

            ((TextView) sheet.findViewById(R.id.tvSheetTitle)).setText(getString(R.string.profile_gender));

            RecyclerView rv = sheet.findViewById(R.id.rvSheetOptions);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(new BottomSheetOptionAdapter(options, current, option -> {
                tvGender.setText(option);
                tvGender.setTextColor(getColor(R.color.colorNoirBlack));

                getSharedPreferences(USER_PREFS, MODE_PRIVATE)
                        .edit()
                        .putString("gender", option)
                        .apply();

                dialog.dismiss();
            }));

            dialog.show();
        });
    }

    private void loadSavedData() {
        com.example.saive.utils.UserSession session = com.example.saive.utils.UserSession.getInstance();

        String name = session.getDisplayName();
        if (name != null && !name.isEmpty()) {
            etName.setText(name);
        }

        // DOB & Gender: thử lấy từ Firebase DB (nếu có)
        String userId = session.getUserId();
        if (userId != null && !userId.isEmpty()) {
            com.example.saive.admin.connectors.FirebaseConnector.getDatabase()
                    .getReference("Users").child(userId)
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            String dob = com.example.saive.utils.DataManager.getStringSafe(snapshot.child("dob"));
                            if (dob.isEmpty()) dob = com.example.saive.utils.DataManager.getStringSafe(snapshot.child("DateOfBirth"));
                            if (!dob.isEmpty()) {
                                tvDob.setText(dob);
                                tvDob.setTextColor(getColor(R.color.colorNoirBlack));
                            }
                            String gender = com.example.saive.utils.DataManager.getStringSafe(snapshot.child("gender"));
                            if (gender.isEmpty()) gender = com.example.saive.utils.DataManager.getStringSafe(snapshot.child("Gender"));
                            if (!gender.isEmpty()) {
                                tvGender.setText(gender);
                                tvGender.setTextColor(getColor(R.color.colorNoirBlack));
                            }
                            String avatar = com.example.saive.utils.DataManager.getStringSafe(snapshot.child("avatarUrl"));
                            if (avatar.isEmpty()) avatar = com.example.saive.utils.DataManager.getStringSafe(snapshot.child("AvatarUrl"));
                            if (!avatar.isEmpty()) {
                                try {
                                    com.example.saive.utils.ImageUtils.setSafeImage(ivAvatar, android.net.Uri.parse(avatar));
                                } catch (Exception ignored) {}
                            }
                        }
                        @Override
                        public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
                    });
        }
    }

    // ────────────────────────────────────────────────────────────
    // Đồng bộ đế DOB & Gender lên Firebase DB
    // ────────────────────────────────────────────────────────────
    private void syncProfileToServer() {
        com.example.saive.utils.UserSession session = com.example.saive.utils.UserSession.getInstance();
        String userId = session.getUserId();

        String dob = tvDob.getText().toString();
        String gender = tvGender.getText().toString();
        String name = etName.getText().toString().trim();

        // Avatar: nếu người dùng vừa chọn ảnh mới thì dùng URI đó
        String avatarUrl = selectedImageUri != null
                ? selectedImageUri.toString()
                : session.getAvatarUrl();

        if (userId == null || userId.isEmpty()) {
            // Chưa đăng nhập → chỉ cập nhật cache in-memory
            session.setCache(null, session.getEmail(), name, session.getRole(),
                    avatarUrl, session.getPhone());
            Toast.makeText(this, R.string.toast_update_success, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cập nhật cache in-memory
        session.setCache(userId, session.getEmail(), name, session.getRole(),
                avatarUrl, session.getPhone());

        // Cập nhật lên Firebase DB
        findViewById(R.id.btnSaveProfile).setEnabled(false);

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("fullname", name);
        updates.put("DisplayName", name);
        updates.put("dob", dob);
        updates.put("DateOfBirth", dob);
        updates.put("gender", gender);
        updates.put("Gender", gender);
        updates.put("avatarUrl", avatarUrl);
        updates.put("AvatarUrl", avatarUrl);

        com.example.saive.admin.connectors.FirebaseConnector.getDatabase().getReference("Users")
                .child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    findViewById(R.id.btnSaveProfile).setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfileActivity.this,
                                "Lỗi cập nhật: " + (task.getException() != null ? task.getException().getMessage() : "?"),
                                Toast.LENGTH_SHORT).show();
                    }
                    finish();
                });
    }

    protected void navigateToMain(String sectionExtra) {
    }
}