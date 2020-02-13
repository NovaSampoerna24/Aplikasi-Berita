package com.bocahkoding.igame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bocahkoding.igame.adapter.AdapterComment;
import com.bocahkoding.igame.connection.API;
import com.bocahkoding.igame.connection.RestAdapter;
import com.bocahkoding.igame.connection.response.ResponseCommentAdd;
import com.bocahkoding.igame.connection.response.ResponseCommentList;
import com.bocahkoding.igame.data.Constant;
import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.model.Comment;
import com.bocahkoding.igame.model.CommentBody;
import com.bocahkoding.igame.model.News;
import com.bocahkoding.igame.model.User;
import com.bocahkoding.igame.utils.NetworkCheck;
import com.bocahkoding.igame.utils.Tools;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityComment extends AppCompatActivity {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";

    // activity transition
    public static void navigate(Activity activity, News news) {
        Intent i = new Intent(activity, ActivityComment.class);
        i.putExtra(EXTRA_OBJECT, news);
        activity.startActivity(i);
    }

    private News news = null;
    private User user = new User();
    private boolean is_login = false;
    private long comment_count = 0;

    private EditText et_comment;
    private TextView total_comment;
    private View btn_add_comment, progress_add_comment, progress_bar;
    private RecyclerView recycler_view;

    private API api = RestAdapter.createAPI();
    private Call<ResponseCommentList> callbackList;
    private Call<ResponseCommentAdd> callbackAdd;
    private AdapterComment mAdapter;

    private int count_total = 0;
    private int failed_page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        news = (News) getIntent().getSerializableExtra(EXTRA_OBJECT);

        initComponent();
        initToolbar();
        requestAction(1);
        Tools.RTLMode(getWindow());
        ThisApp.get().saveClassLogEvent(getClass());
    }

    private void initToolbar() {
        Tools.setSmartSystemBar(this);
    }

    private void initComponent() {
        total_comment = findViewById(R.id.total_comment);
        et_comment = findViewById(R.id.et_comment);
        btn_add_comment = findViewById(R.id.btn_add_comment);
        progress_add_comment = findViewById(R.id.progress_add_comment);
        progress_bar = findViewById(R.id.progress_bar);
        recycler_view = findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterComment(this, recycler_view);
        recycler_view.setAdapter(mAdapter);

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new AdapterComment.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (count_total > mAdapter.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                }
            }
        });
        comment_count = news.total_comment;
        setCommentCount();

        btn_add_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!is_login) {
                    ActivityLogin.navigate(ActivityComment.this);
                    return;
                }
                final String comment = et_comment.getText().toString().trim();
                if (comment.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.comment_input_warning, Toast.LENGTH_SHORT).show();
                    return;
                }
                loadingAddComment(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addCommentAction(comment);
                    }
                }, 1000);
            }
        });

        (findViewById(R.id.btn_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setCommentCount() {
        total_comment.setText("(" + Tools.bigNumberFormat(comment_count) + ")");
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "", R.drawable.img_failed);
        if (page_no == 1) {
            firstProgress(true);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestListComment(page_no);
            }
        }, 1000);
    }

    private void requestListComment(final int page_no) {
        callbackList = api.getListComment(page_no, Constant.COMMENT_PER_REQUEST, news.id);
        callbackList.enqueue(new Callback<ResponseCommentList>() {
            @Override
            public void onResponse(Call<ResponseCommentList> call, Response<ResponseCommentList> response) {
                ResponseCommentList resp = response.body();
                if (resp != null && resp.status.equals("success")) {
                    count_total = resp.count_total;
                    displayApiResult(resp.comments, page_no);
                    firstProgress(false);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(Call<ResponseCommentList> call, Throwable t) {
                Log.e("onFailure", t.getMessage());
                if (!call.isCanceled()) onFailRequest(page_no);
            }
        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        firstProgress(false);
        if (NetworkCheck.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text), R.drawable.img_failed);
        } else {
            showFailedView(true, getString(R.string.no_internet_text), R.drawable.img_no_internet);
        }
    }

    private void showFailedView(boolean show, String message, @DrawableRes int icon) {
        View lyt_failed = findViewById(R.id.lyt_failed);

        ((ImageView) findViewById(R.id.failed_icon)).setImageResource(icon);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recycler_view.setVisibility(View.INVISIBLE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recycler_view.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        (findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(failed_page);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_failed);
        (findViewById(R.id.failed_retry)).setVisibility(View.GONE);
        ((ImageView) findViewById(R.id.failed_icon)).setImageResource(R.drawable.img_no_item);
        ((TextView) findViewById(R.id.failed_message)).setText(getString(R.string.no_item));
        if (show) {
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void firstProgress(final boolean show) {
        progress_bar.setVisibility(show ? View.VISIBLE : View.GONE);
        recycler_view.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void displayApiResult(final List<Comment> items, int page_no) {
        total_comment.setText("(" + Tools.bigNumberFormat(count_total) + ")");
        mAdapter.hideLoadMore();
        mAdapter.insertData(items);
        if (count_total > mAdapter.getItemCount()) {
            mAdapter.showLoadMore();
        }
        firstProgress(false);
        if (items.size() == 0) {
            showNoItemView(true);
        } else if (page_no > 1) {
            recycler_view.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    private void addCommentAction(String comment) {
        CommentBody body = new CommentBody(news.id, user.id, comment);
        callbackAdd = api.addComment(body);
        callbackAdd.enqueue(new Callback<ResponseCommentAdd>() {
            @Override
            public void onResponse(Call<ResponseCommentAdd> call, Response<ResponseCommentAdd> response) {
                ResponseCommentAdd resp = response.body();
                onFinishAddComment(resp);
            }

            @Override
            public void onFailure(Call<ResponseCommentAdd> call, Throwable t) {
                Log.e("onFailure", t.getMessage());
                if (!call.isCanceled()) onFinishAddComment(null);
            }
        });
    }

    private void loadingAddComment(boolean show) {
        btn_add_comment.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        progress_add_comment.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        et_comment.setEnabled(!show);
    }

    private void onFinishAddComment(ResponseCommentAdd resp) {
        loadingAddComment(false);
        String message = getString(R.string.no_internet_text);
        if (NetworkCheck.isConnect(this)) {
            if (resp == null) {
                message = getString(R.string.failed_text);
            } else if (resp.code.equalsIgnoreCase("EXIST")) {
                message = getString(R.string.comment_exist_warning);
            } else if (resp.code.equalsIgnoreCase("SUCCESS")) {
                message = getString(R.string.comment_add_info);
                et_comment.setText("");
                resp.comment.name = user.name;
                resp.comment.image = user.image;
                mAdapter.addFirst(resp.comment);
                recycler_view.scrollToPosition(0);
                showFailedView(false, "", R.drawable.img_failed);
                comment_count++;
                setCommentCount();
            }
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroy() {
        if (callbackList != null && !callbackList.isCanceled()) callbackList.cancel();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        is_login = ThisApp.get().isLogin();
        user = ThisApp.get().getUser();
    }

}