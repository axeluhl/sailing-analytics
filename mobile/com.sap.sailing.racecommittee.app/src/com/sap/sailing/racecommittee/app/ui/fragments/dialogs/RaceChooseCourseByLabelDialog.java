package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class RaceChooseCourseByLabelDialog extends RaceDialogFragment {

    public interface CourseByLabelSelectionListener {
        public void onCourseSelected();
    }

    private Spinner courseLayoutSpinner;
    private EditText numberOfRoundsEditText;
    private Button chooseButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_choose_running_course_view, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().setTitle(getString(R.string.set_course_layout));

        courseLayoutSpinner = (Spinner) getView().findViewById(R.id.routesSpinner);
        numberOfRoundsEditText = (EditText) getView().findViewById(R.id.numberOfRoundsEdit);
        
        String[] courseLayouts = getActivity().getResources().getStringArray(R.array.course_routes);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_layout, courseLayouts);
        courseLayoutSpinner.setAdapter(adapter);
        
        
        if (getRace().getCourseDesign() != null) {
            String[] currentCourseNameParts = getRace().getCourseDesign().getName().split(" ");
            if (currentCourseNameParts.length == 2) {

                int index = Util.indexOf(Arrays.asList(courseLayouts), currentCourseNameParts[0]);
                if (index > -1) {
                    courseLayoutSpinner.setSelection(index);
                }
                numberOfRoundsEditText.setText(currentCourseNameParts[1]);
            }
        }
        

        chooseButton = (Button) getDialog().findViewById(R.id.chooseRunningCourseButton);

        chooseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                onChooseClicked(v);
            }
        });
    }

    protected void onChooseClicked(View view) {
        try {
            int numberOfRounds = Integer.parseInt(numberOfRoundsEditText.getText().toString());
            String courseLayoutName = (String) courseLayoutSpinner.getAdapter().getItem(courseLayoutSpinner.getSelectedItemPosition());
            String courseName = courseLayoutName + " " + numberOfRounds;
            CourseBase courseLayout = new CourseDataImpl(courseName);
            getRace().getState().setCourseDesign(courseLayout);
            dismiss();
        } catch (NumberFormatException e) {
            ExLog.i(ExLog.RACE_SET_RACE_RUNNING_COURSE_FAIL, null, getActivity());
            Toast.makeText(getActivity(), "Please please enter the number of rounds.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void notifyTick() {

    }

}
