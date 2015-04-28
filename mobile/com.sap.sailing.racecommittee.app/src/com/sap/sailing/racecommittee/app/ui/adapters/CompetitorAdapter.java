package com.sap.sailing.racecommittee.app.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
    private CompetitorClick mListener;

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

        if (competitor != null) {
            String name = "";
            if (competitor.getBoat() != null) {
                name = competitor.getBoat().getSailID() + " - ";
            }
            name += competitor.getName();
            if (holder.competitor != null) {
                holder.competitor.setText(name);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setListener(CompetitorClick listener) {
        mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public View container;
        public TextView vesselId;
        public TextView competitor;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            container = itemView.findViewById(R.id.container);
            vesselId = (TextView) itemView.findViewById(R.id.vessel_id);
            competitor = (TextView) itemView.findViewById(R.id.competitor);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                Competitor competitor = mData.get(getAdapterPosition());
                if (competitor != null) {
                    mListener.onCompetitorClick(competitor);
                }
            }
        }
    }

    public interface CompetitorClick {
        void onCompetitorClick(Competitor competitor);
    }
}
