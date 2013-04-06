package com.sap.sailing.racecommittee.app.ui.adapters.finishing;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.racecommittee.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CompetitorPositioningListAdapter extends ArrayAdapter<Pair<Competitor, MaxPointsReason>> {
    
    public CompetitorPositioningListAdapter(Context context, int textViewResourceId, List<Pair<Competitor, MaxPointsReason>> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {

            LayoutInflater li = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            view = li.inflate(R.layout.welter_positioning_item, null);
        }

        Pair<Competitor, MaxPointsReason> competitorMaxPoint = getItem(position);

        TextView positionText = (TextView) view.findViewById(R.id.Welter_Cell_Positioning_columnOne_txtTitle);

        TextView title = (TextView) view.findViewById(R.id.Welter_Cell_Positioning_columnTwo_txtTitle);

        if (competitorMaxPoint.getB().equals(MaxPointsReason.NONE)) {
            positionText.setText(String.valueOf(position + 1));
        } else {
            positionText.setText(competitorMaxPoint.getB().name());
        }
        
        title.setText(competitorMaxPoint.getA().getName());

        return view;
    }

}
