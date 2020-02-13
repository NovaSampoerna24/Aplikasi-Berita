package com.bocahkoding.igame.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bocahkoding.igame.ActivitySearch;
import com.bocahkoding.igame.R;
import com.bocahkoding.igame.adapter.AdapterTopic;
import com.bocahkoding.igame.connection.API;
import com.bocahkoding.igame.connection.RestAdapter;
import com.bocahkoding.igame.connection.response.ResponseTopic;
import com.bocahkoding.igame.data.Constant;
import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.model.SearchFilter;
import com.bocahkoding.igame.model.Topic;
import com.bocahkoding.igame.utils.NetworkCheck;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentTopic extends Fragment {

    private View root_view;
    private RecyclerView recycler_view;
    private SwipeRefreshLayout swipe_refresh;
    private ShimmerFrameLayout shimmer;

    private Call<ResponseTopic> callbackTopic;
    private AdapterTopic mAdapter;

    private int count_total = 0;
    private int failed_page = 0;

    public FragmentTopic() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_topic, container, false);
        initComponent();

        requestAction(1);
        ThisApp.get().saveClassLogEvent(getClass());
        return root_view;
    }

    private void initComponent() {
        recycler_view = root_view.findViewById(R.id.recycler_view);
        swipe_refresh = root_view.findViewById(R.id.swipe_refresh);
        shimmer = root_view.findViewById(R.id.shimmer_topic);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_view.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterTopic(getActivity(), recycler_view);
        recycler_view.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterTopic.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Topic obj, int position) {
                SearchFilter sf = new SearchFilter(obj);
                ActivitySearch.navigate(getActivity(), sf, getString(R.string.hint_topic) + obj.name);
            }
        });

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new AdapterTopic.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (count_total > mAdapter.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    mAdapter.setLoaded();
                }
            }
        });

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (callbackTopic != null && callbackTopic.isExecuted()) callbackTopic.cancel();
                count_total = 0;
                failed_page = 0;
                mAdapter.resetListData();
                requestAction(1);
            }
        });
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "", R.drawable.img_failed);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            mAdapter.setLoading();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestListTopic(page_no);
            }
        }, 1000);
    }

    private void requestListTopic(final int page_no) {
        API api = RestAdapter.createAPI();
        callbackTopic = api.getListTopic(page_no, Constant.TOPIC_PER_REQUEST, null);
        callbackTopic.enqueue(new Callback<ResponseTopic>() {
            @Override
            public void onResponse(Call<ResponseTopic> call, Response<ResponseTopic> response) {
                ResponseTopic resp = response.body();
                if (resp != null && resp.status.equals("success")) {
                    count_total = resp.count_total;
                    displayApiResult(resp.topics);
                    swipeProgress(false);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(Call<ResponseTopic> call, Throwable t) {
                Log.e("onFailure", t.getMessage());
                if (!call.isCanceled()) onFailRequest(page_no);
            }
        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        mAdapter.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text), R.drawable.img_failed);
        } else {
            showFailedView(true, getString(R.string.no_internet_text), R.drawable.img_no_internet);
        }
    }

    private void showFailedView(boolean show, String message, @DrawableRes int icon) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed);

        ((ImageView) root_view.findViewById(R.id.failed_icon)).setImageResource(icon);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recycler_view.setVisibility(View.INVISIBLE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recycler_view.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        (root_view.findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(failed_page);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_failed);
        (root_view.findViewById(R.id.failed_retry)).setVisibility(View.GONE);
        ((ImageView) root_view.findViewById(R.id.failed_icon)).setImageResource(R.drawable.img_no_item);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(getString(R.string.no_item));
        if (show) {
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(show);
            }
        });
        if (!show) {
            shimmer.setVisibility(View.GONE);
            shimmer.stopShimmer();
            return;
        }
        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();
    }

    private void displayApiResult(final List<Topic> items) {
        mAdapter.insertData(items);
        swipeProgress(false);
        if (items.size() == 0) showNoItemView(true);
    }

    @Override
    public void onDestroy() {
        if (callbackTopic != null && !callbackTopic.isCanceled()) callbackTopic.cancel();
        shimmer.stopShimmer();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
