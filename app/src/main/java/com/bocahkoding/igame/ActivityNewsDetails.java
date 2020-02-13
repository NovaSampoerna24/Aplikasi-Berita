package com.bocahkoding.igame;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bocahkoding.igame.connection.API;
import com.bocahkoding.igame.connection.RestAdapter;
import com.bocahkoding.igame.connection.response.ResponseNewsDetails;
import com.bocahkoding.igame.data.AppConfig;
import com.bocahkoding.igame.data.Constant;
import com.bocahkoding.igame.data.GDPR;
import com.bocahkoding.igame.data.SharedPref;
import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.model.News;
import com.bocahkoding.igame.model.type.SourceType;
import com.bocahkoding.igame.room.AppDatabase;
import com.bocahkoding.igame.room.DAO;
import com.bocahkoding.igame.room.table.NewsEntity;
import com.bocahkoding.igame.utils.NetworkCheck;
import com.bocahkoding.igame.utils.TimeAgo;
import com.bocahkoding.igame.utils.Tools;
import com.bocahkoding.igame.utils.ViewAnimation;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityNewsDetails extends AppCompatActivity {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";

    // activity transition
    public static void navigate(Activity activity, News news) {
        Intent i = navigateBase(activity, news);
        activity.startActivity(i);
    }

    public static Intent navigateBase(Context context, News news) {
        Intent i = new Intent(context, ActivityNewsDetails.class);
        i.putExtra(EXTRA_OBJECT, news);
        return i;
    }

    // extra obj
    private boolean is_saved;
    boolean lyt_navigation_hide = false;

    private DAO dao;
    private News news = null;
    private List<String> topics = new ArrayList<>();
    private List<String> gallery = new ArrayList<>();

    private Call<ResponseNewsDetails> callbackCall = null;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private NestedScrollView nested_scroll_view;
    private View parent_view;
    private ShimmerFrameLayout shimmer;
    private ImageView image, img_type;
    private View lyt_main_content, featured, lyt_bottom_bar, lyt_toolbar;
    private WebView web_view;
    private TextView date, type, total_view, total_comment;
    private MenuItem menu_refresh;
    private boolean is_running;
    private boolean is_activity_active = false;
    private Intent header_intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);

        news = (News) getIntent().getSerializableExtra(EXTRA_OBJECT);
        dao = AppDatabase.getDb(this).getDAO();

        iniComponent();
        initToolbar();
        if (news.source_type == SourceType.SAVED) {
            displayNewsData();
        } else {
            requestAction();
        }

        prepareBannerAds();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                prepareIntersAds();
            }
        }, 1000 * AppConfig.ADS_INTERS_DETAILS_FIRST_INTERVAL);

        Tools.RTLMode(getWindow());
        ThisApp.get().saveClassLogEvent(getClass());
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorTextAction), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSmartSystemBar(this);
    }

    private void iniComponent() {
        lyt_bottom_bar = findViewById(R.id.lyt_bottom_bar);
        lyt_toolbar = findViewById(R.id.lyt_toolbar);
        nested_scroll_view = findViewById(R.id.nested_scroll_view);
        image = findViewById(R.id.image);
        img_type = findViewById(R.id.img_type);
        total_view = findViewById(R.id.total_view);
        total_comment = findViewById(R.id.total_comment);
        shimmer = findViewById(R.id.shimmer);

        parent_view = findViewById(android.R.id.content);
        lyt_main_content = findViewById(R.id.lyt_main_content);
        featured = findViewById(R.id.featured);
        date = findViewById(R.id.date);
        type = findViewById(R.id.type);

        featured.setVisibility(View.GONE);
        date.setText("");
        type.setText("");

        nested_scroll_view.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY >= oldScrollY) { // down
                    if (lyt_navigation_hide) return;
                    ViewAnimation.hideBottomBar(lyt_bottom_bar);
                    ViewAnimation.hideToolbar(lyt_toolbar);
                    lyt_navigation_hide = true;
                } else {
                    if (!lyt_navigation_hide) return;
                    ViewAnimation.showBottomBar(lyt_bottom_bar);
                    ViewAnimation.showToolbar(lyt_toolbar);
                    lyt_navigation_hide = false;
                }
            }
        });
    }

    private void requestAction() {
        if (is_running) return;
        showFailedView(false, "", R.drawable.img_failed);
        showLoading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestVideoDetailsApi();
            }
        }, 1000);
    }

    private void onFailRequest() {
        showLoading(false);
        if (NetworkCheck.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text), R.drawable.img_failed);
        } else {
            showFailedView(true, getString(R.string.no_internet_text), R.drawable.img_no_internet);
        }
    }

    private void requestVideoDetailsApi() {
        int viewed = ThisApp.get().isEligibleViewed(news.id) ? 1 : 0;
        API api = RestAdapter.createAPI();
        callbackCall = api.getNewsDetails(news.id, viewed);
        callbackCall.enqueue(new Callback<ResponseNewsDetails>() {
            @Override
            public void onResponse(Call<ResponseNewsDetails> call, Response<ResponseNewsDetails> response) {
                ResponseNewsDetails resp = response.body();
                if (resp != null && resp.status.equals("success")) {
                    news = resp.news;
                    topics = resp.topics;
                    gallery = resp.gallery;
                    displayNewsData();
                    displayTopicData();
                    showLoading(false);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<ResponseNewsDetails> call, Throwable t) {
                Log.e("onFailure", t.getMessage());
                if (!call.isCanceled()) onFailRequest();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void displayNewsData() {
        ((TextView) findViewById(R.id.title)).setText(news.title);
        web_view = findViewById(R.id.web_view);
        String html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> ";
        if (new SharedPref(this).getSelectedTheme() == 1) {
            html_data += "<style>body{color: #f2f2f2;}</style> ";
        }
        html_data += news.content;
        web_view.getSettings().setJavaScriptEnabled(true);
        web_view.getSettings();
        web_view.getSettings().setBuiltInZoomControls(true);
        web_view.setBackgroundColor(Color.TRANSPARENT);
        web_view.setWebChromeClient(new WebChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            web_view.loadDataWithBaseURL(null, html_data, "text/html; charset=UTF-8", "utf-8", null);
        } else {
            web_view.loadData(html_data, "text/html; charset=UTF-8", null);
        }
        // disable scroll on touch
        web_view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        // override url direct
        web_view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return true;
            }
        });

        date.setText(TimeAgo.get(this, news.date));
        if (news.featured == 1) {
            featured.setVisibility(View.VISIBLE);
        }

        lyt_main_content.setVisibility(View.VISIBLE);

        if (news.type.equalsIgnoreCase("GALLERY")) {
            img_type.setImageResource(R.drawable.ic_type_gallery_large);
            type.setText(R.string.news_type_gallery);
            ArrayList<String> images = new ArrayList<>();
            images.add(Constant.getURLimgNews(news.image));
            if (gallery != null && gallery.size() > 0) {
                for (String i : gallery) images.add(Constant.getURLimgNews(i));
            }
            header_intent = ActivityGallery.navigateBase(this, images);

        } else if (news.type.equalsIgnoreCase("VIDEO")) {
            img_type.setImageResource(R.drawable.ic_type_video_large);
            type.setText(R.string.news_type_video);
            header_intent = ActivityWebView.navigateBase(this, news.url, false);
        } else {
            img_type.setVisibility(View.GONE);
            type.setText(R.string.news_type_article);
        }

        Tools.displayImage(this, image, Constant.getURLimgNews(news.image));

        total_view.setText(Tools.bigNumberFormat(news.total_view));
        total_comment.setText(Tools.bigNumberFormat(news.total_comment));

        (findViewById(R.id.lyt_image)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (header_intent == null) return;
                startActivity(header_intent);
            }
        });


        (findViewById(R.id.lyt_comment)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityComment.navigate(ActivityNewsDetails.this, news);
            }
        });


        (findViewById(R.id.btn_share)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.methodShare(ActivityNewsDetails.this, news);
            }
        });
    }


    private void displayTopicData() {
        FlexboxLayout tags_flex_box = findViewById(R.id.topic_flex_box);
        tags_flex_box.removeAllViews();
        for (String t : topics) {
            TextView text = new TextView(this);
            text.setText(t);
            text.setTextColor(getResources().getColor(R.color.grey_40));
            text.setTextSize(10);
            text.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_rect_grey));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = Tools.dpToPx(this, 2);
            int padding = Tools.dpToPx(this, 1);
            layoutParams.setMargins(margin, margin, margin, margin);
            text.setLayoutParams(layoutParams);
            text.setPadding(padding * 4, padding, padding * 4, padding * 2);
            tags_flex_box.addView(text);
        }
    }

    private void showFailedView(boolean show, String message, @DrawableRes int icon) {
        View lyt_failed = findViewById(R.id.lyt_failed);

        ((ImageView) findViewById(R.id.failed_icon)).setImageResource(icon);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_main_content.setVisibility(View.INVISIBLE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_main_content.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        (findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction();
            }
        });
    }

    private void showLoading(final boolean show) {
        is_running = show;
        if (menu_refresh != null) menu_refresh.setVisible(!show);
        if (!show) {
            shimmer.setVisibility(View.GONE);
            shimmer.stopShimmer();
            lyt_main_content.setVisibility(View.VISIBLE);
        } else {
            shimmer.setVisibility(View.VISIBLE);
            shimmer.startShimmer();
            lyt_main_content.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_news_details, menu);
        MenuItem menu_saved = menu.findItem(R.id.action_saved);
        menu_refresh = menu.findItem(R.id.action_refresh);

        NewsEntity ns = dao.getNews(news.id);
        is_saved = ns != null;
        if (is_saved) {
            menu_saved.setIcon(R.drawable.ic_bookmark);
            if (news.source_type == SourceType.SAVED) {
                topics = ns.getTopicsList();
                gallery = ns.getGalleryList();
                displayTopicData();
            }
        } else {
            menu_saved.setIcon(R.drawable.ic_bookmark_border);
        }
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.colorTextAction));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            finish();
        } else if (item_id == R.id.action_refresh) {
            requestAction();
        } else if (item_id == R.id.action_saved) {
            if (news.isDraft()) return true;
            String str;
            if (is_saved) {
                dao.deleteNews(news.id);
                str = getString(R.string.remove_from_saved);
            } else {
                NewsEntity entity = NewsEntity.entity(news);
                entity.setTopicsList(new ArrayList<>(topics));
                entity = setImageGallery(entity);
                dao.insertNews(entity);
                str = getString(R.string.added_to_saved);
            }
            invalidateOptionsMenu();
            Snackbar.make(parent_view, str, Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle() + " clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    private NewsEntity setImageGallery(NewsEntity entity) {
        if (news.type.equalsIgnoreCase("GALLERY") && gallery != null && gallery.size() > 0) {
            entity.setGalleryList(new ArrayList<>(gallery));
        }
        return entity;
    }

    @Override
    public void onDestroy() {
        if (callbackCall != null && !callbackCall.isCanceled()) callbackCall.cancel();
        shimmer.stopShimmer();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) mAdView.resume();
        is_activity_active = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdView != null) mAdView.pause();
        is_activity_active = false;
    }

    private void prepareBannerAds() {
        if (!AppConfig.ADS_DETAILS_ALL || !NetworkCheck.isConnect(getApplicationContext())) return;

        // banner
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(this)).build();
        if (AppConfig.ADS_DETAILS_BANNER) mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void prepareIntersAds() {
        if (!AppConfig.ADS_DETAILS_ALL || !NetworkCheck.isConnect(getApplicationContext())) return;

        // interstitial
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(this)).build();
        if (AppConfig.ADS_DETAILS_INTERS) mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (!is_activity_active) return;
                mInterstitialAd.show();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                // delay for next ads
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        prepareIntersAds();
                    }
                }, 1000 * AppConfig.ADS_INTERS_DETAILS_NEXT_INTERVAL);
            }
        });
    }

}
