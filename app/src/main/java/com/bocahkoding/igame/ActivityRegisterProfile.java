package com.bocahkoding.igame;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bocahkoding.igame.connection.API;
import com.bocahkoding.igame.connection.RestAdapter;
import com.bocahkoding.igame.connection.response.ResponseCode;
import com.bocahkoding.igame.data.Constant;
import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.model.User;
import com.bocahkoding.igame.utils.AvatarUtils;
import com.bocahkoding.igame.utils.NetworkCheck;
import com.bocahkoding.igame.utils.Tools;

import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityRegisterProfile extends AppCompatActivity {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";

    // activity transition
    public static void navigate(Context context, User user) {
        Intent i = new Intent(context, ActivityRegisterProfile.class);
        i.putExtra(EXTRA_OBJECT, user);
        context.startActivity(i);
    }

    private static final int REQUEST_CODE_PICTURE = 500;
    private boolean is_profile = false;
    private ImageView avatar;
    private Bitmap bitmap = null;
    private User user = null;
    private String old_password = null;

    private Call<ResponseCode> callback = null;
    private View parent_view;
    private Button btn_register;
    private ProgressBar progress_bar;
    private EditText et_name, et_email, et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_profile);

        user = (User) getIntent().getSerializableExtra(EXTRA_OBJECT);
        is_profile = user != null;
        initComponent();

        Tools.setSmartSystemBar(this);
        Tools.RTLMode(getWindow());
        ThisApp.get().saveClassLogEvent(getClass());
    }

    private void initComponent() {
        parent_view = findViewById(android.R.id.content);
        avatar = findViewById(R.id.avatar);
        btn_register = findViewById(R.id.btn_register);
        progress_bar = findViewById(R.id.progress_bar);
        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);

        if (is_profile) {
            findViewById(R.id.lyt_term).setVisibility(View.INVISIBLE);
            findViewById(R.id.lyt_login).setVisibility(View.INVISIBLE);
            btn_register.setText(R.string.form_action_update);
            ((TextView) findViewById(R.id.page_title)).setText(R.string.form_title_update);
            et_name.setText(user.name);
            et_email.setText(user.email);
            Tools.displayImageCircle(this, avatar, Constant.getURLimgUser(user.image));
            old_password = user.password;
        } else {
            findViewById(R.id.lyt_term).setVisibility(View.VISIBLE);
            findViewById(R.id.lyt_login).setVisibility(View.VISIBLE);
            btn_register.setText(R.string.form_action_register);
            ((TextView) findViewById(R.id.page_title)).setText(R.string.form_title_register);
        }

        (findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                ActivityLogin.navigate(ActivityRegisterProfile.this);
            }
        });

        (findViewById(R.id.lyt_avatar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_PICTURE);
            }
        });

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

        (findViewById(R.id.lyt_term_policies)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.openInAppBrowser(ActivityRegisterProfile.this, getString(R.string.privacy_policy_url), false);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateValue();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICTURE && resultCode == RESULT_OK) {
            Uri image_uri = data.getData();
            bitmap = AvatarUtils.getBitmapFormUri(this, image_uri);
            if (bitmap != null) {
                Tools.displayImageCircle(this, avatar, bitmap);
            }
        }
    }

    private void validateValue() {
        if (!is_profile) user = new User();
        user.name = et_name.getText().toString();
        user.email = et_email.getText().toString();
        user.password = et_password.getText().toString();

        if (user.name.trim().equals("")) {
            Snackbar.make(parent_view, R.string.profile_name_empty, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (user.email.trim().equals("")) {
            Snackbar.make(parent_view, R.string.profile_email_empty, Snackbar.LENGTH_SHORT).show();
            return;
        } else if (!Tools.isEmailValid(user.email)) {
            Snackbar.make(parent_view, R.string.profile_email_invalid, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!is_profile && user.password.trim().equals("")) {
            Snackbar.make(parent_view, R.string.profile_password_empty, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!is_profile && !Tools.isPasswordValid(user.password)) {
            Snackbar.make(parent_view, R.string.profile_password_invalid, Snackbar.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestRegisterApi();
            }
        }, 1000);
    }

    private void requestRegisterApi() {
        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("id", createPartFromString(user.id + ""));
        map.put("name", createPartFromString(user.name));
        map.put("email", createPartFromString(user.email));
        map.put("notif_device", createPartFromString(Tools.getDeviceID(this)));
        String pass = Tools.getEncodedString(user.password);
        if (is_profile && user.password.trim().equals("")) pass = old_password;
        map.put("password", createPartFromString(pass));

        MultipartBody.Part body = null;

        if (bitmap != null) {
            File file = AvatarUtils.createTempFile(this, bitmap);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            body = MultipartBody.Part.createFormData("avatar", file.getName(), reqFile);
        }

        API api = RestAdapter.createAPI();
        callback = api.register(body, map);
        callback.enqueue(new Callback<ResponseCode>() {
            @Override
            public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                ResponseCode resp = response.body();
                if (resp == null) {
                    onFailRequest(null);
                } else if (resp.code.equalsIgnoreCase("SUCCESS")) {
                    user = resp.user;
                    showDialogSuccess(getString(R.string.register_success));
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
            if (TextUtils.isEmpty(code) || code.equalsIgnoreCase("FAILED")) {
                showDialogFailed(getString(R.string.failed_text));
            } else if (code.equalsIgnoreCase("EXIST")) {
                showDialogFailed(getString(R.string.email_in_use));
            } else if (code.equalsIgnoreCase("NOT_FOUND")) {
                showDialogFailed(getString(R.string.account_not_found));
            }
        } else {
            showDialogFailed(getString(R.string.no_internet_text));
        }
    }

    private RequestBody createPartFromString(String value) {
        return RequestBody.create(okhttp3.MultipartBody.FORM, value);
    }

    private void showLoading(final boolean show) {
        btn_register.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        progress_bar.setVisibility(!show ? View.INVISIBLE : View.VISIBLE);
    }

    private void showDialogFailed(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(message);
        dialog.setPositiveButton(R.string.OK, null);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void showDialogSuccess(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(message);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                if (is_profile) {
                    ThisApp.get().setUser(user);
                } else {
                    ActivityLogin.navigate(ActivityRegisterProfile.this);
                }
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }
}
