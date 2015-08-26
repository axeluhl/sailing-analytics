package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.MainScheduleItem;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;

public class MainScheduleAdapter extends RecyclerView.Adapter<MainScheduleAdapter.ViewHolder> {

    private Context mContext;
    private List<MainScheduleItem> mItems;
    private ItemClick mListener;

    public MainScheduleAdapter(Context context, List<MainScheduleItem> items, ItemClick listener) {
        mContext = context;
        mItems = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.race_schedule_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MainScheduleItem item = mItems.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(item.getRunnable());
                }
            }
        });
        holder.caption.setText(item.getCaption());
        holder.value.setText(item.getValue());
        holder.drawable.setImageDrawable(null);
        if (item.getDrawable() != null) {
            holder.drawable.setImageDrawable(item.getDrawable());
        }
    }

    @Override
    public int getItemCount() {
        return (mItems != null) ? mItems.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView caption;
        public ImageView drawable;
        public TextView value;

        public ViewHolder(View itemView) {
            super(itemView);

            caption = ViewHelper.get(itemView, R.id.item_caption);
            drawable = ViewHelper.get(itemView, R.id.item_flag);
            value = ViewHelper.get(itemView, R.id.item_value);
        }
    }

    public interface ItemClick {
        void onItemClick(Runnable runnable);
    }
}
