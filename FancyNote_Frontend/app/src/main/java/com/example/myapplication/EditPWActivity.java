package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
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
import com.example.myapplication.network.UpdatePWRequest;
import com.example.myapplication.network.UpdatePWResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPWActivity extends AppCompatActivity {
    private EditText etPW;
    private EditText etNewPW;
    private EditText etConfirmPW;
    private Button btnSave;
    private Button btnCancel;
    private SharedPreferences sharedPreferences;

    private boolean isRequestInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        etPW = findViewById(R.id.password);
        etNewPW = findViewById(R.id.newpassword);
        etConfirmPW = findViewById(R.id.confirm_password);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new android.app.AlertDialog.Builder(EditPWActivity.this)
                        .setTitle("确认修改")
                        .setMessage("确定要修改密码吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (!isRequestInProgress) {
                                    isRequestInProgress = true;
                                    if (validateInputs()) {
                                        updatePW();
                                    } else {
                                        isRequestInProgress = false;
                                    }
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Close the activity
            }
        });
    }


    private boolean validateInputs() {
        String password = etPW.getText().toString().trim();
        String newpassword = etNewPW.getText().toString().trim();
        String confirmpassword = etConfirmPW.getText().toString().trim();

        // 检查密码格式：8-16个字符，允许字母和数字
        if (TextUtils.isEmpty(newpassword) || !newpassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")) {
            etNewPW.setError("密码应为8-16个字符的字母数字组合");
            etNewPW.requestFocus();
            isRequestInProgress = false;
            return false;
        }

        // 检查确认密码是否与密码匹配
        if (TextUtils.isEmpty(confirmpassword) || !confirmpassword.equals(newpassword)) {
            etConfirmPW.setError("密码不匹配");
            etConfirmPW.requestFocus();
            isRequestInProgress = false;
            return false;
        }

        return true;
    }

    private void updatePW() {
        String password = etPW.getText().toString().trim();
        String newpassword = etConfirmPW.getText().toString().trim();

        String csrfToken = sharedPreferences.getString("csrf_token", null);
        String cookie = sharedPreferences.getString("cookie", null);

        if (csrfToken == null || cookie == null) {
            Toast.makeText(this, "CSRF token or cookie missing", Toast.LENGTH_SHORT).show();
            isRequestInProgress = false;
            return;
        }

        UpdatePWRequest updatePWRequest = new UpdatePWRequest(password, newpassword);
        ApiService apiService = ApiClient.updateCsrfTokenAndCookie(csrfToken, cookie).create(ApiService.class);

        apiService.updatePassword(updatePWRequest).enqueue(new Callback<UpdatePWResponse>() {
            @Override
            public void onResponse(Call<UpdatePWResponse> call, Response<UpdatePWResponse> response) {
                isRequestInProgress = false;
                if (response.isSuccessful() && response.body() != null) {
                    UpdatePWResponse updatePWResponse = response.body();
                    if (updatePWResponse.isSuccess()) {
                        Toast.makeText(EditPWActivity.this, updatePWResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        sharedPreferences.edit().remove("isLoggedIn").apply();
                        sharedPreferences.edit().remove("nickname").apply();
                        sharedPreferences.edit().remove("email").apply();
                        sharedPreferences.edit().remove("avatar").apply();
                        sharedPreferences.edit().remove("motto").apply();
                        // 刷新页面
                        Intent intent = new Intent(EditPWActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(EditPWActivity.this,  updatePWResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditPWActivity.this, "密码修改失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdatePWResponse> call, Throwable t) {
                isRequestInProgress = false;
                Toast.makeText(EditPWActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
