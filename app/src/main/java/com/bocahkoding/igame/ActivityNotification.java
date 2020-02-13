package com.bocahkoding.igame;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bocahkoding.igame.adapter.AdapterNotification;
import com.bocahkoding.igame.data.AppConfig;
import com.bocahkoding.igame.data.Constant;
import com.bocahkoding.igame.data.GDPR;
import com.bocahkoding.igame.data.SharedPref;
import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.room.AppDatabase;
import com.bocahkoding.igame.room.DAO;
import com.bocahkoding.igame.room.table.NotificationEntity;
import com.bocahkoding.igame.utils.NetworkCheck;
import com.bocahkoding.igame.utils.Tools;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class ActivityNotification extends AppCompatActivity {
    public static void navigate(Activity activity) {
        Intent i = new Intent(activity, ActivityNotification.class);
        activity.startActivity(i);
    }

    private AdView mAdView;
    public View parent_view;
    private RecyclerView recyclerView;
    private DAO dao;
    private SharedPref sharedPref;

    public AdapterNotification adapter;
    static ActivityNotification activityNotification;

    public static ActivityNotification getInstance() {
        return activityNotification;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        activityNotification = this;
        dao = AppDatabase.getDb(this).getDAO();
        sharedPref = new SharedPref(this);

        initToolbar();
        iniComponent();
        prepareAds();

        Tools.RTLMode(getWindow());
        ThisApp.get().saveClassLogEvent(getClass());
    }

    private void initToolbar() {
        ActionBar actionBar;
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorTextAction), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_activity_notification);
        Tools.changeOverflowMenuIconColor(toolbar, getResources().getColor(R.color.colorTextAction));
        Tools.setSmartSystemBar(this);
    }

    private void iniComponent() {
        parent_view = findViewById(android.R.id.content);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set data and list adapter
        adapter = new AdapterNotification(this, recyclerView, new ArrayList<NotificationEntity>());
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new AdapterNotification.OnItemClickListener() {
            @Override
            public void onItemClick(View view, NotificationEntity obj, int pos) {
                obj.read = true;
                ActivityDialogNotification.navigate(ActivityNotification.this, obj, false, pos);
            }
        });

        startLoadMoreAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_notification, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.colorTextAction));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item_id == R.id.action_delete) {
            if (adapter.getItemCount() == 0) {
                Snackbar.make(parent_view, R.string.msg_notif_empty, Snackbar.LENGTH_SHORT).show();
                return true;
            }
            dialogDeleteConfirmation();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        if (mAdView != null) mAdView.resume();
    }

    public void dialogDeleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_delete_confirm);
        builder.setMessage(getString(R.string.content_delete_confirm) + getString(R.string.title_activity_notification));
        builder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface di, int i) {
                di.dismiss();
                dao.deleteAllNotification();
                startLoadMoreAdapter();
                Snackbar.make(parent_view, R.string.delete_success, Snackbar.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    private void startLoadMoreAdapter() {
        adapter.resetListData();
        List<NotificationEntity> items = dao.getNotificationByPage(Constant.NOTIFICATION_PAGE, 0);
        adapter.insertData(items);
        showNoItemView();
        final int item_count = (int) dao.getNotificationCount();
        // detect when scroll reach bottom
        adapter.setOnLoadMoreListener(new AdapterNotification.OnLoadMoreListener() {
            @Override
            public void onLoadMore(final int current_page) {
                if (item_count > adapter.getItemCount() && current_page != 0) {
                    displayDataByPage(current_page);
                } else {
                    adapter.setLoaded();
                }
            }
        });
    }

    private void displayDataByPage(final int next_page) {
        adapter.setLoading();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<NotificationEntity> items = dao.getNotificationByPage(Constant.NOTIFICATION_PAGE, (next_page * Constant.NOTIFICATION_PAGE));
                adapter.insertData(items);
                showNoItemView();
            }
        }, 500);
    }

    private void showNoItemView() {
        View lyt_no_item = findViewById(R.id.lyt_failed);
        (findViewById(R.id.failed_retry)).setVisibility(View.GONE);
        ((ImageView) findViewById(R.id.failed_icon)).setImageResource(R.drawable.img_no_item);
        ((TextView) findViewById(R.id.failed_message)).setText(R.string.no_item);
        if (adapter.getItemCount() == 0) {
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void prepareAds() {
        if (!AppConfig.ADS_NOTIFICATION_PAGE || !NetworkCheck.isConnect(getApplicationContext()))
            return;

        // banner
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(this)).build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdView != null) mAdView.pause();
    }

}
