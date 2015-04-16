package com.sap.sailing.racecommittee.app.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.racecommittee.app.R;

import java.util.ArrayList;

public class CompetitorAdapter extends RecyclerView.Adapter<CompetitorAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Competitor> mData;

    public CompetitorAdapter(Context context, ArrayList<Competitor> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.race_positioning_competitor_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Competitor competitor = mData.get(position);

        holder.competitor.setText(competitor.getName());
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View container;
        public TextView competitor;

        public ViewHolder(View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.container);
            competitor = (TextView) itemView.findViewById(R.id.competitor);
        }
    }
}
