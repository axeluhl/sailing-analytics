package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.RoundingDirection;
import com.sap.sailing.racecommittee.app.utils.MarkImageHelper;

public class CourseElementListAdapter extends ArrayAdapter<CourseListDataElement> {

    public CourseElementListAdapter(Context context, int textViewResourceId, List<CourseListDataElement> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {

            LayoutInflater li = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            view = li.inflate(R.layout.welter_draggable_waypoint_item, null);
        }

        CourseListDataElement courseElement = getItem(position);

        TextView leftMarkText = (TextView) view
                .findViewById(R.id.Welter_Cell_Draggable_Waypoint_Item_columnOne_txtLeftMark);
        TextView roundingDirectionText = (TextView) view
                .findViewById(R.id.Welter_Cell_Draggable_Waypoint_Item_txtRoundingDirection);
        TextView rightMarkText = (TextView) view
                .findViewById(R.id.Welter_Cell_Draggable_Waypoint_Item_columnThree_txtRightMark);

        ImageView leftMarkImage = (ImageView) view.findViewById(R.id.Welter_Cell_Draggable_Waypoint_Item_columnOne_imgImage);
        ImageView rightMarkImage = (ImageView) view.findViewById(R.id.Welter_Cell_Draggable_Waypoint_Item_columnThree_imgImage);

        leftMarkText.setText(courseElement.getLeftMark().getName());
        leftMarkImage.setVisibility(View.INVISIBLE);

        if (courseElement.getLeftMark() != null) {
            int drawable = MarkImageHelper.INSTANCE.resolveMarkImage(courseElement.getLeftMark());
            leftMarkImage.setImageResource(drawable);
            leftMarkImage.setVisibility(View.VISIBLE);
        }

        if (courseElement.getRoundingDirection() != null)
            roundingDirectionText.setText(getDisplayValueForRounding(courseElement.getRoundingDirection()));
        else
            roundingDirectionText.setText(R.string.empty);

        if (courseElement.getRightMark() != null) {
            rightMarkText.setText(courseElement.getRightMark().getName());

            int drawable = MarkImageHelper.INSTANCE.resolveMarkImage(courseElement.getRightMark());
            rightMarkImage.setImageResource(drawable);
            rightMarkImage.setVisibility(View.VISIBLE);
        } else {
            rightMarkText.setText(R.string.empty);
            rightMarkImage.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    protected String getDisplayValueForRounding(RoundingDirection direction) {
        if (direction.equals(RoundingDirection.Gate))
            return "Gate";
        else if (direction.equals(RoundingDirection.Port))
            return "P";
        else if (direction.equals(RoundingDirection.Starboard))
            return "S";

        return"";
    }

}
