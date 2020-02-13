package com.bocahkoding.igame.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bocahkoding.igame.R;
import com.bocahkoding.igame.data.Constant;
import com.bocahkoding.igame.model.News;
import com.bocahkoding.igame.room.table.NewsEntity;
import com.bocahkoding.igame.utils.TimeAgo;
import com.bocahkoding.igame.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class AdapterNews extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private List<News> items = new ArrayList<>();

    private int pagination = 0;
    private boolean loading;
    private AdapterNews.OnLoadMoreListener onLoadMoreListener;

    private Context ctx;
    private AdapterNews.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, News obj, int position);
    }

    public void setOnItemClickListener(final AdapterNews.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterNews(Context context, RecyclerView view, int pagination) {
        ctx = context;
        this.pagination = pagination;
        lastItemViewDetector(view);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public TextView date;
        public TextView featured;
        public ImageView image;
        public ImageView img_type;
        public TextView txt_type;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            date = v.findViewById(R.id.date);
            featured = v.findViewById(R.id.featured);
            image = v.findViewById(R.id.image);
            img_type = v.findViewById(R.id.img_type);
            txt_type = v.findViewById(R.id.txt_type);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progress_loading);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
            vh = new AdapterNews.OriginalViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            vh = new AdapterNews.ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterNews.OriginalViewHolder) {
            final News news = items.get(position);
            AdapterNews.OriginalViewHolder v = (AdapterNews.OriginalViewHolder) holder;
            v.title.setText(news.title);
            v.date.setText(TimeAgo.get(ctx, news.date));
            Tools.displayImage(ctx, v.image, Constant.getURLimgNews(news.image));
            v.featured.setVisibility(news.featured == 1 ? View.VISIBLE : View.GONE);
            if (news.type.equalsIgnoreCase("GALLERY")) {
                v.img_type.setImageResource(R.drawable.ic_type_gallery);
                v.txt_type.setText(R.string.news_type_gallery);
            } else if (news.type.equalsIgnoreCase("VIDEO")) {
                v.img_type.setImageResource(R.drawable.ic_type_video);
                v.txt_type.setText(R.string.news_type_video);
            } else {
                v.img_type.setImageResource(R.drawable.ic_type_article);
                v.txt_type.setText(R.string.news_type_article);
            }
            v.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, news, position);
                    }
                }
            });
        } else {
            ((AdapterNews.ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.items.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    public void insertData(List<News> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void insertEntityData(List<NewsEntity> entities) {
        List<News> items = new ArrayList<>();
        for (NewsEntity e : entities) {
            items.add(e.original());
        }
        insertData(items);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(AdapterNews.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (onLoadMoreListener != null) {
                            int current_page = getItemCount() / pagination;
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

}
