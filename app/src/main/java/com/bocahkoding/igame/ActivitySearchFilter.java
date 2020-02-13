package com.bocahkoding.igame;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bocahkoding.igame.adapter.AdapterTopicPick;
import com.bocahkoding.igame.connection.API;
import com.bocahkoding.igame.connection.RestAdapter;
import com.bocahkoding.igame.connection.response.ResponseTopic;
import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.model.SearchFilter;
import com.bocahkoding.igame.model.SortBy;
import com.bocahkoding.igame.model.Topic;
import com.bocahkoding.igame.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySearchFilter extends AppCompatActivity {

    public static final int REQUEST_CODE = 500;
    public static final String RESULT_DATA = "RESULT_DATA";

    public static void navigate(Activity activity, SearchFilter searchFilter) {
        Intent i = new Intent(activity, ActivitySearchFilter.class);
        i.putExtra(RESULT_DATA, searchFilter);
        activity.startActivityForResult(i, REQUEST_CODE);
    }

    private ActionBar actionBar;
    private RadioGroup rg_topic_1, rg_topic_2;
    private RadioGroup rg_sort_1, rg_sort_2;
    private RadioGroup.OnCheckedChangeListener listener_topic, listener_sort_by;
    private ThisApp application;
    private SearchFilter searchFilter = new SearchFilter();
    private List<Topic> displayed_topic = new ArrayList<>();
    private List<SortBy> sort_by_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filter);
        application = ThisApp.get();
        if (application.getFeaturedTopic() != null) {
            displayed_topic = new ArrayList<>(application.getFeaturedTopic());
        }

        if (getIntent().getSerializableExtra(RESULT_DATA) != null) {
            searchFilter = (SearchFilter) getIntent().getSerializableExtra(RESULT_DATA);
        }

        sort_by_list = ThisApp.get().getSorts();

        initToolbar();
        initComponent();
        displayed_topic.add(0, new Topic(-1, getString(R.string.all_topic)));
        initTopic();
        initSortBy();

        Tools.RTLMode(getWindow());
        ThisApp.get().saveClassLogEvent(getClass());
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(R.string.title_activity_search_filter);
        Tools.changeOverflowMenuIconColor(toolbar, getResources().getColor(R.color.colorTextAction));
        Tools.setSmartSystemBar(this);
    }

    private void initComponent() {
        rg_topic_1 = findViewById(R.id.rg_topic_1);
        rg_topic_2 = findViewById(R.id.rg_topic_2);
        rg_sort_1 = findViewById(R.id.rg_sort_1);
        rg_sort_2 = findViewById(R.id.rg_sort_2);
        (findViewById(R.id.btn_reset)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rg_topic_1.check(0);
                rg_sort_1.check(0);
            }
        });

        (findViewById(R.id.btn_apply)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(RESULT_DATA, searchFilter);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        (findViewById(R.id.see_all_topic)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogTopic();
            }
        });

    }

    private void initTopic() {
        rg_topic_1.removeAllViews();
        rg_topic_2.removeAllViews();
        refreshTopicRadioGroup(rg_topic_1.getId());
        refreshTopicRadioGroup(rg_topic_2.getId());

        int div_size = displayed_topic.size() / 2;
        int mod_size = displayed_topic.size() % 2;

        for (int i = 0; i < div_size + mod_size; i++) {
            Topic c = displayed_topic.get(i);
            rg_topic_1.addView(getTopicRadioTemplate(i, c));
            if (c.id == searchFilter.topic.id) {
                rg_topic_1.check(i);
            }
        }

        for (int i = div_size + mod_size; i < displayed_topic.size(); i++) {
            Topic c = displayed_topic.get(i);
            rg_topic_2.addView(getTopicRadioTemplate(i, c));
            if (c.id == searchFilter.topic.id) {
                rg_topic_2.check(i);
            }
        }

        listener_topic = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == -1) return;
                refreshTopicRadioGroup(group.getId());
                Topic p = displayed_topic.get(checkedId);
                searchFilter.topic = new Topic(p.id, p.name);
            }
        };
        rg_topic_1.setOnCheckedChangeListener(listener_topic);
        rg_topic_2.setOnCheckedChangeListener(listener_topic);
    }

    private void initSortBy() {
        rg_sort_1.removeAllViews();
        rg_sort_2.removeAllViews();
        refreshTopicRadioGroup(rg_sort_1.getId());
        refreshTopicRadioGroup(rg_sort_2.getId());

        int div_size = sort_by_list.size() / 2;
        int mod_size = sort_by_list.size() % 2;

        for (int i = 0; i < div_size + mod_size; i++) {
            SortBy c = sort_by_list.get(i);
            rg_sort_1.addView(getSortByRadioTemplate(i, c));
            if (c.type == searchFilter.sort_by.type) {
                rg_sort_1.check(i);
            }
        }

        for (int i = div_size + mod_size; i < sort_by_list.size(); i++) {
            SortBy c = sort_by_list.get(i);
            rg_sort_2.addView(getSortByRadioTemplate(i, c));
            if (c.type == searchFilter.sort_by.type) {
                rg_sort_2.check(i);
            }
        }

        listener_sort_by = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == -1) return;
                refreshSortByRadioGroup(group.getId());
                searchFilter.sort_by = sort_by_list.get(checkedId);
            }
        };
        rg_sort_1.setOnCheckedChangeListener(listener_sort_by);
        rg_sort_2.setOnCheckedChangeListener(listener_sort_by);
    }

    private void refreshTopicRadioGroup(int groupId) {
        if (groupId == R.id.rg_topic_1) {
            rg_topic_2.setOnCheckedChangeListener(null);
            rg_topic_2.clearCheck();
            rg_topic_2.setOnCheckedChangeListener(listener_topic);
        } else if (groupId == R.id.rg_topic_2) {
            rg_topic_1.setOnCheckedChangeListener(null);
            rg_topic_1.clearCheck();
            rg_topic_1.setOnCheckedChangeListener(listener_topic);
        }
    }

    private void refreshSortByRadioGroup(int groupId) {
        if (groupId == R.id.rg_sort_1) {
            rg_sort_2.setOnCheckedChangeListener(null);
            rg_sort_2.clearCheck();
            rg_sort_2.setOnCheckedChangeListener(listener_sort_by);
        } else if (groupId == R.id.rg_sort_2) {
            rg_sort_1.setOnCheckedChangeListener(null);
            rg_sort_1.clearCheck();
            rg_sort_1.setOnCheckedChangeListener(listener_sort_by);
        }
    }

    private AppCompatRadioButton getTopicRadioTemplate(int idx, Topic c) {
        AppCompatRadioButton rb = new AppCompatRadioButton(this);
        rb.setId(idx);
        rb.setText(c.name);
        rb.setMaxLines(1);
        rb.setSingleLine(true);
        rb.setEllipsize(TextUtils.TruncateAt.END);
        rb.setTextAppearance(this, R.style.TextAppearance_AppCompat_Body1);
        rb.setHighlightColor(getResources().getColor(R.color.colorTextAction));
        rb.setTextColor(getResources().getColor(R.color.grey_60));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rb.setLayoutParams(params);
        rb.setPadding(0, 15, 0, 15);
        return rb;
    }

    private AppCompatRadioButton getSortByRadioTemplate(int idx, SortBy c) {
        AppCompatRadioButton rb = new AppCompatRadioButton(this);
        rb.setId(idx);
        rb.setText(c.label);
        rb.setMaxLines(1);
        rb.setSingleLine(true);
        rb.setEllipsize(TextUtils.TruncateAt.END);
        rb.setTextAppearance(this, R.style.TextAppearance_AppCompat_Body1);
        rb.setHighlightColor(getResources().getColor(R.color.colorTextAction));
        rb.setTextColor(getResources().getColor(R.color.grey_60));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rb.setLayoutParams(params);
        rb.setPadding(0, 15, 0, 15);
        return rb;
    }


    private Call<ResponseTopic> callbackTopic;

    private void populateTopic(final RequestTopicListener listener) {
        API api = RestAdapter.createAPI();
        callbackTopic = api.getListTopicName();
        callbackTopic.enqueue(new Callback<ResponseTopic>() {
            @Override
            public void onResponse(Call<ResponseTopic> call, Response<ResponseTopic> response) {
                ResponseTopic resp = response.body();
                if (resp != null && resp.status.equals("success")) {
                    listener.onSuccess(new ArrayList<Topic>(resp.topics));
                } else {
                    listener.onFailed();
                }
            }

            @Override
            public void onFailure(Call<ResponseTopic> call, Throwable t) {
                listener.onFailed();
            }

        });
    }

    private boolean isTopicDisplayed(long id) {
        for (Topic c : displayed_topic) if (c.id == id) return true;
        return false;
    }

    private void showDialogTopic() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_topic_pick);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        final View progress_loading = dialog.findViewById(R.id.progress_loading);
        progress_loading.setVisibility(View.VISIBLE);
        EditText et_search = dialog.findViewById(R.id.et_search);
        et_search.setHint(R.string.hint_search_topic);

        RecyclerView recycler = dialog.findViewById(R.id.recyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        //set data and list adapter
        final AdapterTopicPick mAdapter = new AdapterTopicPick(this);
        recycler.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterTopicPick.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Topic obj) {
                dialog.dismiss();
                if (!isTopicDisplayed(obj.id)) {
                    displayed_topic.add(obj);
                }
                searchFilter.topic.id = obj.id;
                initTopic();
            }
        });
        RequestTopicListener listener = new RequestTopicListener() {
            @Override
            public void onSuccess(List<Topic> topics) {
                progress_loading.setVisibility(View.GONE);
                mAdapter.setItems(topics);
            }

            @Override
            public void onFailed() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ActivitySearchFilter.this, R.string.failed_text, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }, 1000);
            }
        };
        populateTopic(listener);


        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        (dialog.findViewById(R.id.btn_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_search_filter, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.colorTextAction));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_close) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private interface RequestTopicListener {
        void onSuccess(List<Topic> topics);

        void onFailed();
    }

}
