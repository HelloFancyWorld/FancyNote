package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.CsrfTokenResponse;
import com.example.myapplication.network.LoginRequest;
import com.example.myapplication.network.LoginResponse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private SharedPreferences sharedPreferences;

    private boolean isRequestInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRequestInProgress) {
                    isRequestInProgress = true;
                    String csrfToken = sharedPreferences.getString("csrf_token", null);
                    String cookie = sharedPreferences.getString("cookie", null);
                    if (csrfToken == null || cookie == null) {
                        fetchCsrfTokenAndLogin();
                    } else {
                        ApiService apiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);
                        loginUser(apiService);
                    }
                }
            }
        });
    }

    private void fetchCsrfTokenAndLogin() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getCsrfToken().enqueue(new Callback<CsrfTokenResponse>() {
            @Override
            public void onResponse(Call<CsrfTokenResponse> call, Response<CsrfTokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String csrfToken = response.body().getCsrfToken();
                    String cookie = extractCookieFromHeaders(response.headers().values("Set-Cookie"));
                    saveCsrfTokenAndCookieToStorage(csrfToken, cookie);
                    ApiService updatedApiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);
                    loginUser(updatedApiService);
                } else {
                    isRequestInProgress = false;
                    Toast.makeText(LoginActivity.this, "Failed to get CSRF token", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CsrfTokenResponse> call, Throwable t) {
                isRequestInProgress = false;
                Log.d("Debug message", t.getMessage());
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(ApiService apiService) {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username required");
            usernameEditText.requestFocus();
            isRequestInProgress = false;
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password required");
            passwordEditText.requestFocus();
            isRequestInProgress = false;
            return;
        }

        LoginRequest loginRequest = new LoginRequest(username, password);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                isRequestInProgress = false;
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String newCookie = extractCookieFromHeaders(response.headers().values("Set-Cookie"));
                    String newCsrfToken = extractCsrfTokenFromCookie(newCookie);
                    saveCsrfTokenAndCookieToStorage(newCsrfToken, newCookie);
                    if (loginResponse.isSuccess()) {
                        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply();    // 保存登录状态
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // 传递用户信息到 MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                        // 保存登录信息
                        sharedPreferences.edit().putString("username", loginResponse.getUsername()).apply();
                        sharedPreferences.edit().putString("email", loginResponse.getEmail()).apply();
                        sharedPreferences.edit().putString("avatar", loginResponse.getAvatar()).apply();
                        sharedPreferences.edit().putString("motto", loginResponse.getMotto()).apply();

                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                isRequestInProgress = false;
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCsrfTokenAndCookieToStorage(String csrfToken, String cookie) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("csrf_token", csrfToken);
        editor.putString("cookie", cookie);
        editor.apply();
    }

    private void saveCookieToStorage(String cookie) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cookie", cookie);
        editor.apply();
    }

    private String extractCookieFromHeaders(List<String> cookies) {
        // 提取并返回所有Set-Cookie头部的值，通常用于会话cookie（例如sessionid）
        StringBuilder cookieBuilder = new StringBuilder();
        for (String cookie : cookies) {
            if (cookieBuilder.length() > 0) {
                cookieBuilder.append("; ");
            }
            cookieBuilder.append(cookie.split(";", 2)[0]);
        }
        return cookieBuilder.toString();
    }
    private String extractCsrfTokenFromCookie(String cookie) {
        Pattern pattern = Pattern.compile("csrftoken=([^;]+)");
        Matcher matcher = pattern.matcher(cookie);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
