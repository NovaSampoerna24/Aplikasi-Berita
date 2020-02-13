package com.bocahkoding.igame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bocahkoding.igame.data.Constant;
import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.model.News;
import com.bocahkoding.igame.model.type.NotifType;
import com.bocahkoding.igame.model.type.SourceType;
import com.bocahkoding.igame.room.AppDatabase;
import com.bocahkoding.igame.room.DAO;
import com.bocahkoding.igame.room.table.NotificationEntity;
import com.bocahkoding.igame.utils.Tools;

public class ActivityDialogNotification extends AppCompatActivity {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";
    private static final String EXTRA_FROM_NOTIF = "key.EXTRA_FROM_NOTIF";
    private static final String EXTRA_POSITION = "key.EXTRA_FROM_POSITION";

    // activity transition
    public static void navigate(Activity activity, NotificationEntity obj, Boolean from_notif, int position) {
        Intent i = navigateBase(activity, obj, from_notif);
        i.putExtra(EXTRA_POSITION, position);
        activity.startActivity(i);
    }

    public static Intent navigateBase(Context context, NotificationEntity obj, Boolean from_notif) {
        Intent i = new Intent(context, ActivityDialogNotification.class);
        i.putExtra(EXTRA_OBJECT, obj);
        i.putExtra(EXTRA_FROM_NOTIF, from_notif);
        return i;
    }

    private Boolean from_notif;
    private NotificationEntity notification;
    private Intent intent;
    private DAO dao;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_notification);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dao = AppDatabase.getDb(this).getDAO();

        notification = (NotificationEntity) getIntent().getSerializableExtra(EXTRA_OBJECT);
        from_notif = getIntent().getBooleanExtra(EXTRA_FROM_NOTIF, false);
        position = getIntent().getIntExtra(EXTRA_POSITION, -1);

        // set notification as read
        notification.read = true;
        dao.insertNotification(notification);

        initComponent();

        Tools.RTLMode(getWindow());
        ThisApp.get().saveClassLogEvent(getClass());
    }

    private void initComponent() {
        ((TextView) findViewById(R.id.title)).setText(notification.title);
        ((TextView) findViewById(R.id.content)).setText(notification.content);
        ((TextView) findViewById(R.id.date)).setText(Tools.getFormattedDateSimple(notification.created_at));
        ((TextView) findViewById(R.id.type)).setText(Tools.getNotificationType(this, notification.type));

        String image_url = null;
        String type = notification.type;
        intent = new Intent(this, ActivitySplash.class);
        if (type.equalsIgnoreCase(NotifType.NEWS.name())) {
            image_url = Constant.getURLimgNews(notification.image);
            News news = new News(notification.obj_id, SourceType.NOTIFICATION);
            intent = ActivityNewsDetails.navigateBase(this, news);

        } else if (type.equalsIgnoreCase(NotifType.LINK.name())) {
            intent = ActivityWebView.navigateBase(this, notification.link, from_notif);

        } else if (type.equalsIgnoreCase(NotifType.IMAGE.name())) {
            image_url = notification.image;

        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        }

        if (from_notif) {
            (findViewById(R.id.bt_delete)).setVisibility(View.GONE);
            if (ActivityMain.active && (type.equalsIgnoreCase(NotifType.NORMAL.name()) || type.equalsIgnoreCase(NotifType.IMAGE.name()))) {
                (findViewById(R.id.lyt_action)).setVisibility(View.GONE);
            }
        } else {
            if (type.equalsIgnoreCase(NotifType.NORMAL.name()) || type.equalsIgnoreCase(NotifType.IMAGE.name())) {
                (findViewById(R.id.bt_open)).setVisibility(View.GONE);
            }
            ((TextView) findViewById(R.id.dialog_title)).setText(null);
            (findViewById(R.id.logo)).setVisibility(View.GONE);
            (findViewById(R.id.view_space)).setVisibility(View.GONE);
        }

        (findViewById(R.id.lyt_image)).setVisibility(View.GONE);
        if (image_url != null) {
            (findViewById(R.id.lyt_image)).setVisibility(View.VISIBLE);
            Tools.displayImage(this, ((ImageView) findViewById(R.id.image)), image_url);
        }

        (findViewById(R.id.img_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        (findViewById(R.id.bt_open)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(intent);
            }
        });

        (findViewById(R.id.bt_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                if (!from_notif && position != -1) {
                    dao.deleteNotification(notification.id);
                    ActivityNotification.getInstance().adapter.removeItem(position);
                    Snackbar.make(ActivityNotification.getInstance().parent_view, R.string.delete_successfully, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}