package com.example.saive.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.saive.R;
import com.example.saive.base.BaseActivity;
import com.example.saive.databinding.ActivityLoginBinding;
import com.example.saive.utils.ImageUtils;
import com.example.saive.utils.UserSession;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private int logoClickCount = 0;
    private long lastClickTime = 0;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean returnResult = getIntent().getBooleanExtra("return_result", false);

        // ── Kiểm tra session Admin (vẫn dùng SessionManager cho admin) ──
        com.example.saive.admin.util.SessionManager adminSession =
                new com.example.saive.admin.util.SessionManager(this);
        if (adminSession.isLoggedIn() && !returnResult) {
            startActivity(new Intent(LoginActivity.this,
                    com.example.saive.admin.ui.MainActivity.class));
            finish();
            return;
        }

        // ── Kiểm tra user đã đăng nhập qua Firebase Auth chưa ──
        if (UserSession.getInstance().isLoggedIn() && !returnResult) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Prepare Activity Result Launcher for Google Sign In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            showCustomToast("Google sign in failed: " + e.getMessage());
                        }
                    }
                }
        );

        ImageUtils.setSafeImage(binding.ivLogo, R.drawable.saive_logo);

        // Tap logo 5 lần → mở màn Admin Login
        binding.ivLogo.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 500) {
                logoClickCount++;
            } else {
                logoClickCount = 1;
            }
            lastClickTime = currentTime;

            if (logoClickCount >= 5) {
                logoClickCount = 0;
                Intent intent = new Intent(LoginActivity.this,
                        com.example.saive.admin.ui.login.LoginActivity.class);
                startActivity(intent);
                showCustomToast(getString(R.string.login_admin_unlocked));
            }
        });


        if (getWindow() != null) {
            getWindow().setStatusBarColor(
                    androidx.core.content.ContextCompat.getColor(this, R.color.colorAuthBg));
        }

        // Continue as Guest — không lưu gì vào DB hay SharedPreferences
        binding.btnContinueAsGuest.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });

        binding.btnLogin.setOnClickListener(v -> {
            if (validateInput()) performLogin();
        });

        binding.tvSignUpLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            showCustomToast(getString(R.string.login_forgot_pwd_clicked));
        });
    }

    // ──────────────────────────────────────────────────────────────────
    private boolean validateInput() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError(getString(R.string.login_error_email_req));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError(getString(R.string.login_error_email_invalid));
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError(getString(R.string.login_error_pwd_req));
            return false;
        }
        return true;
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
        binding.btnLogin.setAlpha(loading ? 0.6f : 1f);
    }

    // ──────────────────────────────────────────────────────────────────
    // ĐĂNG NHẬP — chỉ dùng Firebase Auth + Firebase Realtime Database
    // ──────────────────────────────────────────────────────────────────
    private void performLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        setLoading(true);

        com.google.firebase.auth.FirebaseAuth mAuth =
                com.google.firebase.auth.FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase Auth thành công → load user info từ DB
                        onFirebaseAuthSuccess(email);
                    } else {
                        // Firebase Auth thất bại → thử match trực tiếp từ DB
                        performDatabaseDirectLogin(email, password, task.getException());
                    }
                });
    }

    /**
     * Firebase Auth thành công — load thông tin user từ Realtime DB (theo email).
     */
    private void onFirebaseAuthSuccess(String email) {
        // Kiểm tra user có bị block không trước khi load
        com.example.saive.admin.connectors.FirebaseConnector
                .getDatabase()
                .getReference("Users")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {

                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            String dbEmail = getStringSafe(child.child("email"));
                            if (dbEmail == null)
                                dbEmail = getStringSafe(child.child("Email"));

                            if (email.equalsIgnoreCase(dbEmail)) {
                                // Kiểm tra block
                                Boolean isActive = child.child("IsActive").getValue(Boolean.class);
                                if (isActive == null)
                                    isActive = child.child("isActive").getValue(Boolean.class);
                                if (Boolean.FALSE.equals(isActive)) {
                                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                                    setLoading(false);
                                    showCustomToast(getString(R.string.login_error_acc_blocked));
                                    return;
                                }

                                // Load cache vào UserSession (in-memory, không persist)
                                String uid = getStringSafe(child.child("UserId"));
                                if (uid == null || uid.isEmpty()) {
                                    uid = child.getKey();
                                }

                                String name = getStringSafe(child.child("fullname"));
                                if (name == null) name = getStringSafe(child.child("DisplayName"));

                                String role = getStringSafe(child.child("role"));
                                if (role == null) role = getStringSafe(child.child("Role"));

                                String avatar = getStringSafe(child.child("avatarUrl"));
                                if (avatar == null) avatar = getStringSafe(child.child("AvatarUrl"));

                                String phone = getStringSafe(child.child("phone"));
                                if (phone == null) phone = getStringSafe(child.child("Phone"));

                                UserSession.getInstance().setCache(uid, dbEmail, name, role, avatar, phone);
                                navigateAfterLogin();
                                return;
                            }
                        }

                        // Không tìm thấy trong DB nhưng Auth thành công → vẫn cho vào
                        UserSession.getInstance().setCache(null, email, null, "customer", null, null);
                        navigateAfterLogin();
                    }

                    @Override
                    public void onCancelled(
                            @androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        // Auth đã OK, DB lỗi → vẫn cho vào (Auth state persist)
                        UserSession.getInstance().clearCache();
                        navigateAfterLogin();
                    }
                });
    }

    /**
     * Fallback: Firebase Auth thất bại (user chưa có Auth account) →
     * Tìm thẳng trong Realtime DB theo email+password.
     * Nếu tìm thấy, tạo Firebase Auth account cho họ rồi đăng nhập.
     */
    private void performDatabaseDirectLogin(String email, String password, Exception authException) {
        com.example.saive.admin.connectors.FirebaseConnector
                .getDatabase()
                .getReference("Users")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {

                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            String dbEmail = getStringSafe(child.child("email"));
                            if (dbEmail == null)
                                dbEmail = getStringSafe(child.child("Email"));
                            String dbPwd = getStringSafe(child.child("password"));
                            if (dbPwd == null)
                                dbPwd = getStringSafe(child.child("Password"));

                            if (!email.equalsIgnoreCase(dbEmail)) continue;

                            // Email khớp
                            if (!password.equals(dbPwd)) {
                                setLoading(false);
                                showCustomToast(getString(R.string.error_wrong_password));
                                return;
                            }

                            // Password khớp
                            Boolean isActive = child.child("IsActive").getValue(Boolean.class);
                            if (isActive == null)
                                isActive = child.child("isActive").getValue(Boolean.class);
                            if (Boolean.FALSE.equals(isActive)) {
                                setLoading(false);
                                showCustomToast(getString(R.string.login_error_acc_blocked));
                                return;
                            }

                            // Load info
                            String uid = getStringSafe(child.child("UserId"));
                            if (uid == null || uid.isEmpty()) {
                                uid = child.getKey();
                            }

                            String name = getStringSafe(child.child("fullname"));
                            if (name == null) name = getStringSafe(child.child("DisplayName"));
                            String role = getStringSafe(child.child("role"));
                            if (role == null) role = getStringSafe(child.child("Role"));
                            String avatar = getStringSafe(child.child("avatarUrl"));
                            if (avatar == null) avatar = getStringSafe(child.child("AvatarUrl"));
                            String phone = getStringSafe(child.child("phone"));
                            if (phone == null) phone = getStringSafe(child.child("Phone"));

                            final String finalName = name;
                            final String finalRole = role;
                            final String finalAvatar = avatar;
                            final String finalPhone = phone;
                            final String finalUid = uid;
                            final String finalDbEmail = dbEmail;

                            // Tạo Firebase Auth account để lần sau dùng được signInWithEmailAndPassword
                            com.google.firebase.auth.FirebaseAuth.getInstance()
                                    .createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(createTask -> {
                                        // Dù create thành công hay fail (account đã tồn tại),
                                        // ta vẫn đăng nhập bằng DB data
                                        UserSession.getInstance().setCache(
                                                finalUid, finalDbEmail, finalName,
                                                finalRole, finalAvatar, finalPhone);

                                        // Thử sign in lại với Auth để persist session
                                        com.google.firebase.auth.FirebaseAuth.getInstance()
                                                .signInWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(signInTask -> {
                                                    navigateAfterLogin();
                                                });
                                    });
                            return;
                        }

                        // Không tìm thấy email trong DB
                        setLoading(false);
                        String msg = authException != null ? authException.getMessage()
                                : "Sai tài khoản hoặc mật khẩu";
                        showCustomToast("Đăng nhập thất bại: " + msg);
                    }

                    @Override
                    public void onCancelled(
                            @androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        setLoading(false);
                        showCustomToast("Lỗi kết nối: " + error.getMessage());
                    }
                });
    }

    private void navigateAfterLogin() {
        setLoading(false);
        showCustomToast(getString(R.string.login_toast_success));
        boolean returnResult = getIntent().getBooleanExtra("return_result", false);
        if (returnResult) {
            setResult(RESULT_OK);
            finish();
        } else {
            startActivity(new Intent(LoginActivity.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            finish();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        com.google.firebase.auth.FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        com.google.firebase.auth.FirebaseUser user =
                                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            syncGoogleUserToDatabase(user);
                        }
                    } else {
                        setLoading(false);
                        showCustomToast("Firebase Auth with Google failed.");
                    }
                });
    }

    private void syncGoogleUserToDatabase(com.google.firebase.auth.FirebaseUser user) {
        String email = user.getEmail();
        com.google.firebase.database.DatabaseReference usersRef =
                com.example.saive.admin.connectors.FirebaseConnector
                        .getDatabase()
                        .getReference("Users");

        usersRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                boolean userExists = false;
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    String dbEmail = getStringSafe(child.child("email"));
                    if (dbEmail == null) dbEmail = getStringSafe(child.child("Email"));

                    if (email != null && email.equalsIgnoreCase(dbEmail)) {
                        userExists = true;
                        // User exists, check if blocked
                        Boolean isActive = child.child("IsActive").getValue(Boolean.class);
                        if (isActive == null) isActive = child.child("isActive").getValue(Boolean.class);
                        if (Boolean.FALSE.equals(isActive)) {
                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                            setLoading(false);
                            showCustomToast(getString(R.string.login_error_acc_blocked));
                            return;
                        }

                        // Update local cache
                        String uid = child.getKey();
                        String name = getStringSafe(child.child("fullname"));
                        if (name == null) name = getStringSafe(child.child("DisplayName"));
                        String role = getStringSafe(child.child("role"));
                        if (role == null) role = getStringSafe(child.child("Role"));
                        String avatar = getStringSafe(child.child("avatarUrl"));
                        if (avatar == null) avatar = getStringSafe(child.child("AvatarUrl"));
                        String phone = getStringSafe(child.child("phone"));
                        if (phone == null) phone = getStringSafe(child.child("Phone"));

                        UserSession.getInstance().setCache(uid, email, name, role, avatar, phone);
                        break;
                    }
                }

                if (!userExists) {
                    // Create new user in RTDB
                    String newUserId = usersRef.push().getKey();
                    java.util.HashMap<String, Object> userData = new java.util.HashMap<>();
                    userData.put("email", email);
                    userData.put("fullname", user.getDisplayName());
                    userData.put("avatarUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                    userData.put("role", "customer");
                    userData.put("isActive", true);
                    userData.put("phone", "");

                    if (newUserId != null) {
                        usersRef.child(newUserId).setValue(userData);
                        UserSession.getInstance().setCache(newUserId, email, user.getDisplayName(), "customer",
                                user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "", "");
                    }
                }
                navigateAfterLogin();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                setLoading(false);
                showCustomToast("Database error: " + error.getMessage());
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private String getStringSafe(com.google.firebase.database.DataSnapshot snapshot) {
        Object val = snapshot.getValue();
        return (val == null) ? null : String.valueOf(val);
    }
}