package com.example.saive.admin.ui.login;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.saive.admin.util.SessionManager;

public class LoginViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final SessionManager sessionManager;

    // Hardcoded Admin Credentials
    private static final String ADMIN_EMAIL = "thuy.dao@saive.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_DISPLAY_NAME = "Administrator";

    public LoginViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void login(String email, String password) {
        isLoading.setValue(true);
        
        // Simulating network delay for consistent UX
        new android.os.Handler().postDelayed(() -> {
            isLoading.setValue(false);
            if (ADMIN_EMAIL.equalsIgnoreCase(email) && ADMIN_PASSWORD.equals(password)) {
                sessionManager.createLoginSession("ADMIN_001", ADMIN_DISPLAY_NAME, "admin");
                loginSuccess.setValue(true);
            } else {
                errorMessage.setValue(getApplication().getString(com.example.saive.R.string.admin_error_login_failed));
            }
        }, 1000);
    }
}
