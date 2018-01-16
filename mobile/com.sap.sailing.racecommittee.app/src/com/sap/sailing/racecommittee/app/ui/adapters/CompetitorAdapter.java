package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.List;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.racecommittee.app.R;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CompetitorAdapter extends RecyclerView.Adapter<CompetitorAdapter.ViewHolder> {

    private static final String TAG = CompetitorAdapter.class.getName();

    private Context mContext;
    private List<Competitor> mData;
    private CompetitorClick mListener;

    public CompetitorAdapter(Context context, List<Competitor> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.race_tracking_list_competitor_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Competitor competitor = mData.get(position);
        if (competitor != null) {
            String name = "";
            if (competitor.hasBoat()) {
                CompetitorWithBoat competitorWithBoat = (CompetitorWithBoat) competitor;
                if (competitorWithBoat.getBoat() != null) {
                    name = competitorWithBoat.getBoat().getSailID() + " - ";
                }
            }
            name += competitor.getName();
            if (holder.competitor != null) {
                holder.competitor.setText(name);
            }
        } else {
            ExLog.e(mContext, TAG, "Competitor at position " + position + " was unexpected null");
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
