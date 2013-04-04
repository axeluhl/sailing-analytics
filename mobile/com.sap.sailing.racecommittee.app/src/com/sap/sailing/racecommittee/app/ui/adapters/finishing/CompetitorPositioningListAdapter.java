package com.sap.sailing.racecommittee.app.ui.adapters.finishing;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.racecommittee.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CompetitorPositioningListAdapter extends ArrayAdapter<Competitor> {
    
    public CompetitorPositioningListAdapter(Context context, int textViewResourceId, List<Competitor> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {

            LayoutInflater li = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            view = li.inflate(R.layout.welter_one_row_two_columns, null);
        }

        Competitor competitor = getItem(position);

        TextView positionText = (TextView) view.findViewById(R.id.Welter_Cell_OneRowTwoColumns_columnOne_txtTitle);

        TextView title = (TextView) view.findViewById(R.id.Welter_Cell_OneRowTwoColumns_columnTwo_txtTitle);

        positionText.setText(String.valueOf(position + 1));
        title.setText(competitor.getName());

        return view;
    }

}
