package com.bocahkoding.igame;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bocahkoding.igame.connection.API;
import com.bocahkoding.igame.connection.RestAdapter;
import com.bocahkoding.igame.connection.response.ResponseCode;
import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.model.User;
import com.bocahkoding.igame.utils.NetworkCheck;
import com.bocahkoding.igame.utils.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLogin extends AppCompatActivity {

    // activity transition
    public static void navigate(Context context) {
        Intent i = new Intent(context, ActivityLogin.class);
        context.startActivity(i);
    }

    private View parent_view;
    private EditText et_email, et_password;
    private Button btn_login;
    private ProgressBar progress_bar;

    private Call<ResponseCode> callback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponent();

        Tools.setSmartSystemBar(this);
        Tools.RTLMode(getWindow());
        ThisApp.get().saveClassLogEvent(getClass());
    }

    private void initComponent() {
        parent_view = findViewById(android.R.id.content);
        progress_bar = findViewById(R.id.progress_bar);
        btn_login = findViewById(R.id.btn_login);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        (findViewById(R.id.show_pass)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
                if (v.isActivated()) {
                    et_password.setTransformationMethod(null);
                } else {
                    et_password.setTransformationMethod(new PasswordTransformationMethod());
                }
                et_password.setSelection(et_password.getText().length());
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateValue(et_email.getText().toString().trim(), et_password.getText().toString().trim());
            }
        });

        (findViewById(R.id.register)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                ActivityRegisterProfile.navigate(ActivityLogin.this, null);
            }
        });

        (findViewById(R.id.forgot_password)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForgotPassword();
            }
        });
    }

    private void validateValue(final String email, final String password) {
        if (email.trim().equals("")) {
            Snackbar.make(parent_view, R.string.login_email_warning, Snackbar.LENGTH_SHORT).show();
            return;
        } else if (!Tools.isEmailValid(email)) {
            Snackbar.make(parent_view, R.string.login_email_invalid, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (password.trim().equals("")) {
            Snackbar.make(parent_view, R.string.login_password_warning, Snackbar.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                processLogin(email, password);
            }
        }, 1000);
    }

    private void processLogin(String email, String password) {
        API api = RestAdapter.createAPI();
        callback = api.login(email, Tools.getEncodedString(password), Tools.getDeviceID(this));
        callback.enqueue(new Callback<ResponseCode>() {
            @Override
            public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                ResponseCode resp = response.body();
                if (resp == null) {
                    onFailRequest(null);
                } else if (resp.code.equalsIgnoreCase("SUCCESS")) {
                    saveLoginData(resp.user);
                    showDialogSuccess(getString(R.string.login_success_info));
                } else {
                    onFailRequest(resp.code);
                }
                showLoading(false);
            }

            @Override
            public void onFailure(Call<ResponseCode> call, Throwable t) {
                Log.e("onFailure", t.getMessage());
                if (!call.isCanceled()) onFailRequest(null);
                showLoading(false);
            }
        });
    }

    private void onFailRequest(String code) {
        if (NetworkCheck.isConnect(this)) {
            if (TextUtils.isEmpty(code)) {
                showDialogFailed(getString(R.string.failed_text));
            } else if (code.equalsIgnoreCase("DISABLED")) {
                showDialogFailed(getString(R.string.account_disabled));
            } else if (code.equalsIgnoreCase("NOT_FOUND")) {
                showDialogFailed(getString(R.string.invalid_email_password));
            }
        } else {
            showDialogFailed(getString(R.string.no_internet_text));
        }
    }

    private void showDialogFailed(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(message);
        dialog.setPositiveButton(R.string.OK, null);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void saveLoginData(User user) {
        ThisApp.get().setUser(user);
    }

    private void showDialogSuccess(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(message);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showLoading(final boolean show) {
        btn_login.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        progress_bar.setVisibility(!show ? View.INVISIBLE : View.VISIBLE);
    }

    private void showDialogForgotPassword() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_forgot_password);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);

        final View lyt_content = dialog.findViewById(R.id.lyt_content);
        final View progress_loading = dialog.findViewById(R.id.progress_loading);
        final EditText et_email = dialog.findViewById(R.id.et_email);
        final TextView tv_message = dialog.findViewById(R.id.tv_message);
        tv_message.setVisibility(View.GONE);

        final ForgotPasswordListener listener = new ForgotPasswordListener() {
            @Override
            public void onFinish(String code) {
                lyt_content.setVisibility(View.VISIBLE);
                tv_message.setVisibility(View.VISIBLE);
                progress_loading.setVisibility(View.GONE);
                String message = getString(R.string.email_sent_info);
                if (NetworkCheck.isConnect(ActivityLogin.this)) {
                    if (code == null || code.equalsIgnoreCase("FAILED")) {
                        message = getString(R.string.failed_text);
                    } else if (code.equalsIgnoreCase("DISABLED")) {
                        message = getString(R.string.account_disabled);
                    } else if (code.equalsIgnoreCase("NOT_FOUND")) {
                        message = getString(R.string.email_not_registered);
                    }
                } else {
                    message = getString(R.string.no_internet_text);
                }
                tv_message.setText(message);
            }
        };

        (dialog.findViewById(R.id.bt_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_str = et_email.getText().toString();
                if (email_str.equals("")) {
                    tv_message.setVisibility(View.VISIBLE);
                    tv_message.setText(R.string.profile_email_empty);
                    return;
                } else if (!Tools.isEmailValid(email_str)) {
                    tv_message.setVisibility(View.VISIBLE);
                    tv_message.setText(R.string.profile_email_invalid);
                    return;
                }
                lyt_content.setVisibility(View.INVISIBLE);
                progress_loading.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        forgotPasswordAction(et_email.getText().toString(), listener);
                    }
                }, 1000);
            }
        });

        (dialog.findViewById(R.id.btn_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void forgotPasswordAction(String email, final ForgotPasswordListener listener) {
        API api = RestAdapter.createAPI();
        Call<ResponseCode> callbackCallRating = api.forgotPassword(email);
        callbackCallRating.enqueue(new Callback<ResponseCode>() {
            @Override
            public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                ResponseCode rsp = response.body();
                listener.onFinish(rsp != null ? rsp.code : null);
            }

            @Override
            public void onFailure(Call<ResponseCode> call, Throwable t) {
                listener.onFinish(null);
            }

        });
    }

    private interface ForgotPasswordListener {
        void onFinish(String code);
    }
}

