package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.MarkImageHelper;

public class DraggableCourseElementListAdapter extends ArrayAdapter<CourseListDataElement> {

    private final MarkImageHelper markImageHelper;

    public DraggableCourseElementListAdapter(Context context, List<CourseListDataElement> objects, MarkImageHelper imageHelper) {
        super(context, 0, objects);
        markImageHelper = imageHelper;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            view = inflater.inflate(R.layout.welter_draggable_waypoint_item, parent, false);
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
            int drawable = markImageHelper.resolveMarkImage(courseElement.getLeftMark());
            leftMarkImage.setImageResource(drawable);
            leftMarkImage.setVisibility(View.VISIBLE);
        }

        if (courseElement.getPassingInstructions() != null)
            roundingDirectionText.setText(getDisplayValueForRounding(courseElement.getPassingInstructions()));
        else
            roundingDirectionText.setText(R.string.empty);

        if (courseElement.getRightMark() != null) {
            rightMarkText.setText(courseElement.getRightMark().getName());

            int drawable = markImageHelper.resolveMarkImage(courseElement.getRightMark());
            rightMarkImage.setImageResource(drawable);
            rightMarkImage.setVisibility(View.VISIBLE);
        } else {
            rightMarkText.setText(R.string.empty);
            rightMarkImage.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    protected String getDisplayValueForRounding(PassingInstruction direction) {
        if (direction.equals(PassingInstruction.Gate))
            return "Gate";
        else if (direction.equals(PassingInstruction.Port))
            return "P";
        else if (direction.equals(PassingInstruction.Starboard))
            return "S";
        else if (direction.equals(PassingInstruction.Line))
            return "Line";
        else if (direction.equals(PassingInstruction.Offset))
            return "Offset";


        return"";
    }

}
