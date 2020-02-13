package com.bocahkoding.igame.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bocahkoding.igame.ActivityNewsDetails;
import com.bocahkoding.igame.R;
import com.bocahkoding.igame.adapter.AdapterNews;
import com.bocahkoding.igame.data.Constant;
import com.bocahkoding.igame.data.ThisApp;
import com.bocahkoding.igame.model.News;
import com.bocahkoding.igame.model.type.SourceType;
import com.bocahkoding.igame.room.AppDatabase;
import com.bocahkoding.igame.room.DAO;
import com.bocahkoding.igame.room.table.NewsEntity;

import java.util.List;

public class FragmentSaved extends Fragment {

    private View root_view;
    private View parent_view;
    private RecyclerView recycler_view;
    private DAO dao;

    public AdapterNews adapter;

    public FragmentSaved() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_saved, container, false);
        dao = AppDatabase.getDb(getActivity()).getDAO();

        ThisApp.get().saveClassLogEvent(getClass());
        return root_view;
    }

    private void initComponent() {
        parent_view = root_view.findViewById(android.R.id.content);
        recycler_view = root_view.findViewById(R.id.recycler_view);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));

        //set data and list adapter
        adapter = new AdapterNews(getActivity(), recycler_view, Constant.SAVED_PAGE);
        recycler_view.setAdapter(adapter);

        adapter.setOnItemClickListener(new AdapterNews.OnItemClickListener() {
            @Override
            public void onItemClick(View view, News obj, int pos) {
                obj.source_type = SourceType.SAVED;
                ActivityNewsDetails.navigate(getActivity(), obj);
            }
        });

        startLoadMoreAdapter();
    }

    private void startLoadMoreAdapter() {
        adapter.resetListData();
        List<NewsEntity> items = dao.getAllNewsByPage(Constant.SAVED_PAGE, 0);
        adapter.insertEntityData(items);
        showNoItemView();
        final int item_count = dao.getNewsCount();
        // detect when scroll reach bottom
        adapter.setOnLoadMoreListener(new AdapterNews.OnLoadMoreListener() {
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
                List<NewsEntity> items = dao.getAllNewsByPage(Constant.SAVED_PAGE, (next_page * Constant.SAVED_PAGE));
                adapter.insertEntityData(items);
                showNoItemView();
            }
        }, 1000);
    }

    private void showNoItemView() {
        View lyt_no_item = root_view.findViewById(R.id.lyt_failed);
        (root_view.findViewById(R.id.failed_retry)).setVisibility(View.GONE);
        ((ImageView) root_view.findViewById(R.id.failed_icon)).setImageResource(R.drawable.img_no_item);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(R.string.no_item);
        if (adapter.getItemCount() == 0) {
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initComponent();
    }
}
