package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.MarkImageHelper;

public class MarkGridAdapter extends ArrayAdapter<Mark> {

    private final MarkImageHelper markImageHelper;

    public MarkGridAdapter(Context context, List<Mark> marks, MarkImageHelper imageHelper) {
        super(context, 0, marks);
        markImageHelper = imageHelper;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            view = inflater.inflate(R.layout.welter_grid_mark_cell, parent, false);
        }

        Mark mark = getItem(position);

        ImageView image = (ImageView) view.findViewById(R.id.Welter_Grid_Mark_Cell_imgImage);

        int drawable = markImageHelper.resolveMarkImage(mark);
        image.setImageResource(drawable);
        image.setVisibility(View.VISIBLE);

        TextView title = (TextView) view.findViewById(R.id.Welter_Grid_Mark_Cell_txtTitle);
        title.setText(mark.getName());

        return view;
    }

}
