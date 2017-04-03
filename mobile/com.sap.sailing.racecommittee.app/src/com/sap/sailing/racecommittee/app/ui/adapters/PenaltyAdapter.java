package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.List;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultWithIdImpl;

public class PenaltyAdapter extends RecyclerView.Adapter<PenaltyAdapter.ViewHolder> {

    private List<CompetitorResultWithIdImpl> mCompetitor;
    private ItemListener mListener;

    public PenaltyAdapter(@NonNull ItemListener listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.race_penalty_row_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CompetitorResultWithIdImpl competitor = mCompetitor.get(position);

        holder.mItemText.setText(competitor.getCompetitorDisplayName());

        final boolean hasReason = !MaxPointsReason.NONE.equals(competitor.getMaxPointsReason());
        holder.mItemPenalty.setVisibility(hasReason ? View.VISIBLE : View.GONE);
        if (hasReason) {
            holder.mItemPenalty.setText(competitor.getMaxPointsReason().name());
        }

        holder.mItemCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mListener != null) {
                    mListener.onCheckedChanged(competitor, isChecked);
                }
            }
        });

        holder.mItemEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onEditClicked(competitor);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mCompetitor != null) ? mCompetitor.size() : 0;
    }

    public void setCompetitor(List<CompetitorResultWithIdImpl> competitor) {
        mCompetitor = competitor;
        notifyDataSetChanged();
    }

    public interface ItemListener {

        void onCheckedChanged(CompetitorResultWithIdImpl competitor, boolean isChecked);

        void onEditClicked(CompetitorResultWithIdImpl competitor);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CheckBox mItemCheck;
        private TextView mItemText;
        private TextView mItemPenalty;
        private View mItemEdit;

        public ViewHolder(View itemView) {
            super(itemView);

            mItemCheck = ViewHelper.get(itemView, R.id.item_check);
            mItemText = ViewHelper.get(itemView, R.id.item_text);
            mItemPenalty = ViewHelper.get(itemView, R.id.item_penalty);
            mItemEdit = ViewHelper.get(itemView, R.id.item_edit);
        }
    }
}
