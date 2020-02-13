package com.bocahkoding.igame;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bocahkoding.igame.data.Constant;
import com.bocahkoding.igame.data.SharedPref;
import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.model.User;
import com.bocahkoding.igame.utils.Tools;

public class ActivitySettings extends AppCompatActivity {

    // activity transition
    public static void navigate(Context context) {
        Intent i = new Intent(context, ActivitySettings.class);
        context.startActivity(i);
    }

    private boolean is_login = false;
    private User user = new User();
    private SharedPref sharedPref;
    private SwitchCompat switch_push_notif, switch_vibrate, switch_image_cache;
    private View parent_view;
    private TextView tv_theme;
    private String[] themes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPref = new SharedPref(this);

        initComponent();
        initToolbar();
        Tools.RTLMode(getWindow());
        ThisApp.get().saveClassLogEvent(getClass());
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorTextAction), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.title_activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.changeOverflowMenuIconColor(toolbar, getResources().getColor(R.color.colorTextAction));
        Tools.setSmartSystemBar(this);
    }

    private void initComponent() {
        themes = getResources().getStringArray(R.array.themes);
        tv_theme = findViewById(R.id.tv_theme);
        parent_view = findViewById(R.id.parent_view);
        switch_push_notif = findViewById(R.id.switch_push_notif);
        switch_vibrate = findViewById(R.id.switch_vibrate);
        switch_image_cache = findViewById(R.id.switch_image_cache);
        ((TextView) findViewById(R.id.build_version)).setText(Tools.getVersionName(this));
        TextView name = findViewById(R.id.name);
        TextView email = findViewById(R.id.email);
        TextView login_logout = findViewById(R.id.login_logout);
        TextView edit_profile = findViewById(R.id.edit_profile);
        ImageView avatar = findViewById(R.id.avatar);
        if (is_login) {
            login_logout.setText(R.string.logout_title);
            name.setText(user.name);
            email.setText(user.email);
            Tools.displayImageCircle(this, avatar, Constant.getURLimgUser(user.image));
        } else {
            login_logout.setText(R.string.login_title);
            avatar.setImageDrawable(null);
        }
        name.setVisibility(is_login ? View.VISIBLE : View.GONE);
        email.setVisibility(is_login ? View.VISIBLE : View.GONE);
        edit_profile.setVisibility(is_login ? View.VISIBLE : View.GONE);
        tv_theme.setText(themes[sharedPref.getSelectedTheme()]);

        switch_push_notif.setChecked(sharedPref.getPushNotification());
        switch_vibrate.setChecked(sharedPref.getVibration());
        switch_image_cache.setChecked(sharedPref.getImageCache());
        setRingtoneName();

        login_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginLogout();
            }
        });

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityRegisterProfile.navigate(ActivitySettings.this, user);
            }
        });

        switch_push_notif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPref.setPushNotification(isChecked);
            }
        });
        switch_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPref.setVibration(isChecked);
            }
        });
        switch_image_cache.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPref.setImageCache(isChecked);
            }
        });
    }

    public void onClickLayout(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.lyt_ringtone:
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(sharedPref.getRingtone()));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                startActivityForResult(intent, 999);
                break;
            case R.id.lyt_img_cache:
                showDialogClearImageCache();
                break;
            case R.id.lyt_contact_us:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.pref_title_contact_us), getString(R.string.developer_email));
                clipboard.setPrimaryClip(clip);
                Snackbar.make(parent_view, R.string.email_copied, Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.lyt_term_policies:
                Tools.openInAppBrowser(this, getString(R.string.privacy_policy_url), false);
                break;
            case R.id.lyt_rate_this:
                Tools.rateAction(this);
                break;
            case R.id.lyt_more_app:
                Tools.openInAppBrowser(this, Constant.MORE_APP_URL, false);
                break;
            case R.id.lyt_about:
                Tools.showDialogAbout(this);
                break;
            case R.id.lyt_theme:
                showDialogTheme();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri == null) return;
            sharedPref.setRingtone(uri.toString());
            setRingtoneName();
        }
    }

    private void setRingtoneName() {
        ((TextView) findViewById(R.id.ringtone)).setText(sharedPref.getRingtoneName());
    }

    public void loginLogout() {
        if (is_login) {
            showDialogLogout();
        } else {
            ActivityLogin.navigate(this);
        }
    }

    private void showDialogTheme() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.setting_title_theme);
        builder.setSingleChoiceItems(themes, sharedPref.getSelectedTheme(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                sharedPref.setSelectedTheme(i);
                tv_theme.setText(themes[sharedPref.getSelectedTheme()]);
                Tools.refreshTheme(ActivitySettings.this);
                dialog.dismiss();
                restartActivity();
            }
        });
        builder.show();
    }

    private void showDialogLogout() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.confirmation);
        dialog.setMessage(R.string.logout_confirmation_text);
        dialog.setNegativeButton(R.string.CANCEL, null);
        dialog.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                ThisApp.get().logout();
                onResume();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showDialogClearImageCache() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySettings.this);
        builder.setTitle(getString(R.string.dialog_confirm_title));
        builder.setMessage(getString(R.string.message_clear_image_cache));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Tools.clearImageCacheOnBackground(getApplicationContext());
                Snackbar.make(parent_view, getString(R.string.message_after_clear_image_cache), Snackbar.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        is_login = ThisApp.get().isLogin();
        user = ThisApp.get().getUser();
        initComponent();
    }

    private void restartActivity() {
        Intent intent = new Intent(getApplicationContext(), ActivitySplash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
