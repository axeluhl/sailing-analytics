package com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;

public class ByNameCourseDesignDialog extends RaceDialogFragment {

    public interface CourseByLabelSelectionListener {
        public void onCourseSelected();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TableLayout holder = new TableLayout(getActivity());
        String[] courseLayouts = getActivity().getResources().getStringArray(R.array.course_routes);
        AppPreferences preferences = AppPreferences.on(getActivity());
        int maxNumberOfRounds = preferences.getMaxRounds();
        int minNumberOfRounds = preferences.getMinRounds();
        
        for (int layout = 0; layout < courseLayouts.length; layout++) {
            String[] courseNumberAndName = courseLayouts[layout].split(",");
            TableRow rowLayout = new TableRow(getActivity());
            holder.addView(rowLayout);
            for (int round = minNumberOfRounds; round <= maxNumberOfRounds; round++) {
                Button courseButton = new Button(getActivity());
                courseButton.setText(courseNumberAndName[0] + " " + round);
                courseButton.setOnClickListener( new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCourseSelectionButtonClicked(v);
                    }
                });
                rowLayout.addView(courseButton);
            }
            TextView courseName = new TextView(getActivity());
            courseName.setText(courseNumberAndName[1]);
            rowLayout.addView(courseName);
        }
        return holder;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().setTitle(getString(R.string.set_course_layout));
    }
    
    protected void onCourseSelectionButtonClicked(View v) {
        Button selectedButton = (Button) v;
        String internalCourseName = (String) selectedButton.getText();
        
        CourseBase courseLayout = new CourseDataImpl(internalCourseName);
        
        getRaceState().setCourseDesign(MillisecondsTimePoint.now(), courseLayout);
        dismiss();
    }

}
