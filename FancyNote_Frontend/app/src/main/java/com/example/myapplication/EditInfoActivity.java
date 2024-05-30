package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.UpdateInfoRequest;
import com.example.myapplication.network.UpdateInfoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditInfoActivity extends AppCompatActivity {
    private EditText etNickname;
    private EditText etEmail;
    private EditText etMotto;
    private Button btnSave;
    private Button btnCancel;
    private SharedPreferences sharedPreferences;

    private boolean isRequestInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        etNickname = findViewById(R.id.et_nickname);
        etEmail = findViewById(R.id.et_email);
        etMotto = findViewById(R.id.et_motto);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // Load existing user information from SharedPreferences
        loadUserInfo();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRequestInProgress) {
                    isRequestInProgress = true;
                    if (validateInputs()) {
                        updateUserInformation();
                    } else {
                        isRequestInProgress = false;
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Close the activity
            }
        });
    }

    private void loadUserInfo() {
        String nickname = sharedPreferences.getString("nickname", "");
        String email = sharedPreferences.getString("email", "");
        String motto = sharedPreferences.getString("motto", "");

        etNickname.setText(nickname);
        etEmail.setText(email);
        etMotto.setText(motto);
    }

    private boolean validateInputs() {
        String nickname = etNickname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String motto = etMotto.getText().toString().trim();

        if (TextUtils.isEmpty(nickname) || nickname.length() < 6 || nickname.length() > 10) {
            etNickname.setError("用户名应为6到10个字符");
            etNickname.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("请输入有效的邮箱地址");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(motto) || motto.length() < 5 || motto.length() > 30) {
            etMotto.setError("签名应为5到30个字符");
            etMotto.requestFocus();
            return false;
        }

        return true;
    }

    private void updateUserInformation() {
        String nickname = etNickname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String motto = etMotto.getText().toString().trim();

        String csrfToken = sharedPreferences.getString("csrf_token", null);
        String cookie = sharedPreferences.getString("cookie", null);

        if (csrfToken == null || cookie == null) {
            Toast.makeText(this, "CSRF token or cookie missing", Toast.LENGTH_SHORT).show();
            isRequestInProgress = false;
            return;
        }

        UpdateInfoRequest updateInfoRequest = new UpdateInfoRequest(nickname, email, motto);
        ApiService apiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);

        apiService.updateUserInfo(updateInfoRequest).enqueue(new Callback<UpdateInfoResponse>() {
            @Override
            public void onResponse(Call<UpdateInfoResponse> call, Response<UpdateInfoResponse> response) {
                isRequestInProgress = false;
                if (response.isSuccessful() && response.body() != null) {
                    UpdateInfoResponse updateInfoResponse = response.body();
                    if (updateInfoResponse.isSuccess()) {
                        // Save updated info to SharedPreferences
                        sharedPreferences.edit().putString("nickname", nickname).apply();
                        sharedPreferences.edit().putString("email", email).apply();
                        sharedPreferences.edit().putString("motto", motto).apply();
                        etNickname.setText(nickname);
                        etEmail.setText(email);
                        etMotto.setText(motto);
                        Toast.makeText(EditInfoActivity.this, "信息更新成功", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    } else {
                        Toast.makeText(EditInfoActivity.this, updateInfoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditInfoActivity.this, "信息更新失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateInfoResponse> call, Throwable t) {
                isRequestInProgress = false;
                Toast.makeText(EditInfoActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
