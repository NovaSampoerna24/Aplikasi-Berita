package com.bocahkoding.igame.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Toast;

import com.bocahkoding.igame.ActivityMain;
import com.bocahkoding.igame.ActivityWebView;
import com.bocahkoding.igame.R;
import com.bocahkoding.igame.data.AppConfig;
import com.bocahkoding.igame.data.SharedPref;
import com.bocahkoding.igame.model.DeviceInfo;
import com.bocahkoding.igame.model.News;
import com.bocahkoding.igame.model.type.NotifType;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class Tools {

    public static void refreshTheme(Context ctx) {
        int index = new SharedPref(ctx).getSelectedTheme();
        switch (index) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
        }
    }

    public static boolean isdarkTheme(Context ctx){
        int night_mode = ctx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return night_mode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void RTLMode(Window window) {
        if (AppConfig.RTL_LAYOUT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }
    }

    public static boolean needRequestPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static void setSmartSystemBar(Activity act) {
        if(isdarkTheme(act)){
            setSystemBarColor(act, R.color.colorBackground);
        } else {
            setSystemBarColor(act, R.color.colorBackground);
            setSystemBarLight(act);
        }
    }


    public static void setSystemBarColor(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public static void setSystemBarColor(Activity act, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(color));
        }
    }

    public static void setSystemBarLight(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = act.findViewById(android.R.id.content);
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static void changeMenuIconColor(Menu menu, @ColorInt int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable == null) continue;
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void changeOverflowMenuIconColor(Toolbar toolbar, @ColorInt int color) {
        try {
            Drawable drawable = toolbar.getOverflowIcon();
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } catch (Exception e) {
        }
    }


    /**
     * For device_name info parameters
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE + "";
    }

    public static int getVersionCode(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }


    public static String getVersionName(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return ctx.getString(R.string.app_version) + " " + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return ctx.getString(R.string.version_unknown);
        }
    }

    public static String getVersionNamePlain(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return ctx.getString(R.string.version_unknown);
        }
    }

    public static DeviceInfo getDeviceInfo(Context context) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.device_name = Tools.getDeviceName();
        deviceInfo.os_version = Tools.getAndroidVersion();
        deviceInfo.app_version = Tools.getVersionCode(context) + " (" + Tools.getVersionNamePlain(context) + ")";
        deviceInfo.device_id = Tools.getDeviceID(context);
        return deviceInfo;
    }

    public static String getDeviceID(Context context) {
        String deviceID = Build.SERIAL;
        if (deviceID == null || deviceID.trim().isEmpty() || deviceID.equalsIgnoreCase("unknown") || deviceID.equals("0")) {
            try {
                deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
            }
        }
        return deviceID;
    }

    public static String getFormattedDateSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM yy hh:mm");
        return newFormat.format(new Date(dateTime));
    }

    public static void displayImage(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(new SharedPref(ctx).getImageCache() ? DiskCacheStrategy.ALL : DiskCacheStrategy.NONE)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageCircle(Context ctx, ImageView img, Bitmap bmp) {
        try {
            Glide.with(ctx.getApplicationContext()).load(bmp)
                    .transition(withCrossFade())
                    .diskCacheStrategy(new SharedPref(ctx).getImageCache() ? DiskCacheStrategy.ALL : DiskCacheStrategy.NONE)
                    .apply(RequestOptions.circleCropTransform())
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageCircle(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(new SharedPref(ctx).getImageCache() ? DiskCacheStrategy.ALL : DiskCacheStrategy.NONE)
                    .apply(RequestOptions.circleCropTransform())
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageCircle(Context ctx, ImageView img, String url, float thumb) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(new SharedPref(ctx).getImageCache() ? DiskCacheStrategy.ALL : DiskCacheStrategy.NONE)
                    .apply(RequestOptions.circleCropTransform())
                    .thumbnail(thumb)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageThumb(Context ctx, ImageView img, String url, float thumb) {
        try {
            Glide.with(ctx.getApplicationContext()).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(new SharedPref(ctx).getImageCache() ? DiskCacheStrategy.ALL : DiskCacheStrategy.NONE)
                    .thumbnail(thumb)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void clearImageCacheOnBackground(final Context ctx) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(ctx).clearDiskCache();
                }
            }).start();
        } catch (Exception e) {
        }
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static void bounceView(View view) {
        ScaleAnimation fade_in = new ScaleAnimation(0.0f, 1f, 0.0f, 1f, 1, 0.5f, 1, 0.5f);
        fade_in.setDuration(250);
        fade_in.setFillAfter(true);
        view.startAnimation(fade_in);
    }

    private static String appendQuery(String uri, String appendQuery) {
        try {
            URI oldUri = new URI(uri);
            String newQuery = oldUri.getQuery();
            if (newQuery == null) {
                newQuery = appendQuery;
            } else {
                newQuery += "&" + appendQuery;
            }
            URI newUri = new URI(
                    oldUri.getScheme(),
                    oldUri.getAuthority(),
                    oldUri.getPath(), newQuery, oldUri.getFragment()
            );
            return newUri.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return uri;
        }
    }

    public static void openInAppBrowser(Activity activity, String url, boolean from_notif) {
        url = appendQuery(url, "t=" + System.currentTimeMillis());
        if (!URLUtil.isValidUrl(url)) {
            Toast.makeText(activity, "Ops, Cannot open url", Toast.LENGTH_LONG).show();
            return;
        }
        ActivityWebView.navigate(activity, url, from_notif);
    }

    public static String getHostName(String url) {
        try {
            URI uri = new URI(url);
            String new_url = uri.getHost();
            if (!new_url.startsWith("www.")) new_url = "www." + new_url;
            return new_url;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return url;
        }
    }

    public static void directLinkToBrowser(Activity activity, String url) {
        if (!URLUtil.isValidUrl(url)) {
            Toast.makeText(activity, "Ops, Cannot open url", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(intent);
    }

    public static String extractYoutubeVideoId(String url) {
        String video_id = null;
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) video_id = matcher.group();
        return video_id;
    }

    public static String getEncodedString(String str) {
        for (int i = 0; i < 3; i++) {
            byte[] bytes = Base64.encode(str.getBytes(), Base64.NO_WRAP);
            str = new String(bytes);
        }
        return str;
    }

    public static boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
        return pattern.matcher(password).matches();
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "m");
        suffixes.put(1_000_000_000L, "g");
        suffixes.put(1_000_000_000_000L, "t");
    }

    public static String bigNumberFormat(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return bigNumberFormat(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + bigNumberFormat(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static void rateAction(Activity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    public static void showDialogAbout(Context ctx) {
        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_about);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void closeApplication(Activity ctx) {
        Intent intent = new Intent(ctx, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        ctx.startActivity(intent);
    }

    public static void backToMainActivity(Context ctx) {
        Intent intent = new Intent(ctx, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(intent);
    }

    public static String getNotificationType(Context ctx, String type) {
        if (type.equalsIgnoreCase(NotifType.NEWS.name())) {
            return ctx.getString(R.string.type_news);
        } else if (type.equalsIgnoreCase(NotifType.TOPIC.name())) {
            return ctx.getString(R.string.type_topic);
        } else if (type.equalsIgnoreCase(NotifType.LINK.name())) {
            return ctx.getString(R.string.type_link);
        } else if (type.equalsIgnoreCase(NotifType.IMAGE.name())) {
            return ctx.getString(R.string.type_image);
        } else if (type.equalsIgnoreCase(NotifType.NORMAL.name())) {
            return ctx.getString(R.string.type_normal);
        } else {
            return "";
        }
    }


    public static void methodShare(Activity act, News news) {
        try {
            if (news.isDraft()) {
                return;
            }
            // string to share
            String body = String.format(act.getString(R.string.share_text), news.title, act.getString(R.string.app_name));
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, act.getString(R.string.app_name));
            sharingIntent.putExtra(Intent.EXTRA_TEXT, body);
            act.startActivity(Intent.createChooser(sharingIntent, "Share Using"));
        } catch (Exception e) {
            Toast.makeText(act, "Failed when create share data", Toast.LENGTH_LONG).show();
        }
    }
}
