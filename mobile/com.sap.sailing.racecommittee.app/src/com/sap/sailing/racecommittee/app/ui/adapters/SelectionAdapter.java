package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.SelectionItem;

public class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.ViewHolder> {

    private Context mContext;
    private List<SelectionItem> mItems;
    private ItemClick mListener;

    public SelectionAdapter(Context context, List<SelectionItem> items, ItemClick listener) {
        mContext = context;
        mItems = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.selection_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final SelectionItem item = mItems.get(position);

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
        holder.drawable.setVisibility(View.GONE);
        if (item.getDrawable() != null) {
            holder.drawable.setImageDrawable(item.getDrawable());
            holder.drawable.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (mItems != null) ? mItems.size() : 0;
    }

    public interface ItemClick {
        void onItemClick(Runnable runnable);
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
}
