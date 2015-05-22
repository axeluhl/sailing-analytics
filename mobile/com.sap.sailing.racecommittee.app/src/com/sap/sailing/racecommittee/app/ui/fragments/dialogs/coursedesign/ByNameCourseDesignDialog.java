package com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.Collections;
import java.util.List;

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
        return inflater.inflate(R.layout.race_choose_by_name_course_design_view, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setTitle(getString(R.string.set_course_layout));

        GridView grid = (GridView) getView().findViewById(R.id.byNameCourseDesignGrid);
        final List<String> courses = preferences.getByNameCourseDesignerCourseNames();
        Collections.sort(courses, new NaturalComparator());

        ListAdapter buttonAdapter = new BaseAdapter() {
            public int getCount() {
                return courses.size();
            }

            public Object getItem(int position) {
                return null;
            }

            public long getItemId(int position) {
                return 0;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                Button button;
                if (convertView == null) { // if it's not recycled, initialize some attributes
                    Context ctx = ByNameCourseDesignDialog.this.getView().getContext();
                    button = new Button(ctx);
                    button.setTextSize(ctx.getResources().getDimension(R.dimen.Welter_BigTitleTextSize));
                    button.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onCourseSelectionButtonClicked(v);
                        }
                    });
                } else {
                    button = (Button) convertView;
                }

                button.setText(courses.get(position));
                return button;
            }
        };

        grid.setAdapter(buttonAdapter);
    }

    protected void onCourseSelectionButtonClicked(View v) {
        Button selectedButton = (Button) v;
        String internalCourseName = (String) selectedButton.getText();

        CourseBase courseLayout = new CourseDataImpl(internalCourseName);

        getRaceState().setCourseDesign(MillisecondsTimePoint.now(), courseLayout);
        dismiss();
    }

}
