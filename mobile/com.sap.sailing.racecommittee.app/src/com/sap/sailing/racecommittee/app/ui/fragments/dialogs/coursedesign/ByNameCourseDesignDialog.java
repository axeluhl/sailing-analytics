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

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
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
        for (String courseName : preferences.getByNameCourseDesignerCourseNames()) {
            TableRow rowLayout = new TableRow(getActivity());
            Button courseButton = new Button(getActivity());
            courseButton.setText(courseName);
            courseButton.setOnClickListener( new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCourseSelectionButtonClicked(v);
                }
            });
            rowLayout.addView(courseButton);
            holder.addView(rowLayout);
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
