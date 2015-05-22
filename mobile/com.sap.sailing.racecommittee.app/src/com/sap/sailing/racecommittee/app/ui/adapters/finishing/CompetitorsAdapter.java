package com.sap.sailing.racecommittee.app.ui.adapters.finishing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.racecommittee.app.R;

import java.util.List;

public class CompetitorsAdapter extends ArrayAdapter<Competitor> {

    public CompetitorsAdapter(Context context, int textViewResourceId, List<Competitor> competitors) {
        super(context, textViewResourceId, competitors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            view = inflater.inflate(R.layout.welter_grid_competitor_cell, parent, false);
        }

        Competitor competitor = getItem(position);

        TextView title = (TextView) view.findViewById(R.id.Welter_Grid_Competitor_Cell_txtTitle);
        title.setText(competitor.getName());

        return view;
    }
}
