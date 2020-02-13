package com.bocahkoding.igame.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bocahkoding.igame.R;
import com.bocahkoding.igame.data.Constant;
import com.bocahkoding.igame.model.Topic;
import com.bocahkoding.igame.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class AdapterTopicHome extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Topic> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Topic obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.onItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterTopicHome(Context context, List<Topic> items) {
        this.items = items;
        ctx = context;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public ImageView icon;
        public ImageView lyt_color;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            icon = v.findViewById(R.id.icon);
            lyt_color = v.findViewById(R.id.lyt_color);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic_home, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Topic p = items.get(position);
            OriginalViewHolder v = (OriginalViewHolder) holder;
            v.title.setText(p.name);

            Tools.displayImage(ctx, v.icon, Constant.getURLimgTopic(p.icon));
            v.lyt_color.setColorFilter(Color.parseColor(p.color), android.graphics.PorterDuff.Mode.SRC_IN);
            v.icon.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
            v.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(view, p, position);
                    }
                }
            });
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

}