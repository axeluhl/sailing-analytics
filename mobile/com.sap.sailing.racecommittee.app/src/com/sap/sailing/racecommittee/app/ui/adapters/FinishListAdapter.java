package com.sap.sailing.racecommittee.app.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sap.sailing.racecommittee.app.R;

public class FinishListAdapter extends RecyclerView.Adapter<FinishListAdapter.ViewHolder> {

    private Context mContext;

    public FinishListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.race_positioning_draggable_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View container;
        public TextView position;
        public TextView competitor;

        public ViewHolder(View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.container);
            position = (TextView) itemView.findViewById(R.id.position);

        }
    }
}
