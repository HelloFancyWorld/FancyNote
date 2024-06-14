package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.CsrfTokenResponse;
import com.example.myapplication.network.SignupRequest;
import com.example.myapplication.network.SignupResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private SharedPreferences sharedPreferences;

    private boolean isRequestInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.email);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.register_button);

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRequestInProgress) {
                    isRequestInProgress = true;
                    String csrfToken = sharedPreferences.getString("csrf_token", null);
                    String cookie = sharedPreferences.getString("cookie", null);
                    if (csrfToken == null || cookie == null) {
                        fetchCsrfTokenAndRegister();
                    } else {
                        ApiService apiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);
                        registerUser(apiService);
                    }
                }
            }
        });
    }

    private void fetchCsrfTokenAndRegister() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getCsrfToken().enqueue(new Callback<CsrfTokenResponse>() {
            @Override
            public void onResponse(Call<CsrfTokenResponse> call, Response<CsrfTokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String csrfToken = response.body().getCsrfToken();
                    String cookie = response.headers().get("Set-Cookie");
                    saveCsrfTokenAndCookieToStorage(csrfToken, cookie);
                    ApiService updatedApiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);
                    registerUser(updatedApiService);
                } else {
                    isRequestInProgress = false;
                    Toast.makeText(RegisterActivity.this, "获取CSRF令牌失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CsrfTokenResponse> call, Throwable t) {
                isRequestInProgress = false;
                Log.d("Debug message", t.getMessage());
                Toast.makeText(RegisterActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(ApiService apiService) {
        String email = emailEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("邮箱格式不合法");
            emailEditText.requestFocus();
            isRequestInProgress = false;
            return;
        }

        // 检查用户名格式：5-20个字符，允许字母和数字
        if (TextUtils.isEmpty(username) || !username.matches("^[a-zA-Z0-9]{5,20}$")) {
            usernameEditText.setError("用户名应为5-20个字符的字母数字组合");
            usernameEditText.requestFocus();
            isRequestInProgress = false;
            return;
        }

        // 检查密码格式：8-16个字符，允许字母和数字
        if (TextUtils.isEmpty(password) || !password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")) {
            passwordEditText.setError("密码应为8-16个字符的字母数字组合");
            passwordEditText.requestFocus();
            isRequestInProgress = false;
            return;
        }


        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("需要用户名");
            usernameEditText.requestFocus();
            isRequestInProgress = false;
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("需要密码");
            passwordEditText.requestFocus();
            isRequestInProgress = false;
            return;
        }

        if (TextUtils.isEmpty(confirmPassword) || !password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("密码不匹配");
            confirmPasswordEditText.requestFocus();
            isRequestInProgress = false;
            return;
        }

        SignupRequest signupRequest = new SignupRequest(email, username, password);

        apiService.signup(signupRequest).enqueue(new Callback<SignupResponse>() {
            @Override
            public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                isRequestInProgress = false;
                if (response.isSuccessful() && response.body() != null) {
                    SignupResponse signupResponse = response.body();
                    if (signupResponse.isSuccess()) {
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, signupResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SignupResponse> call, Throwable t) {
                isRequestInProgress = false;
                Toast.makeText(RegisterActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCsrfTokenAndCookieToStorage(String csrfToken, String cookie) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("csrf_token", csrfToken);
        editor.putString("cookie", cookie);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearCsrfTokenAndCookie();
    }

    private void clearCsrfTokenAndCookie() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("csrf_token");
        editor.remove("cookie");
        editor.apply();
    }
}
