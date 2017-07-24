package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItem;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItemAdapter;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class CourseFragmentName extends CourseFragment {

    private ListView mListView;

    public static CourseFragmentName newInstance(@START_MODE_VALUES int startMode) {
        CourseFragmentName fragment = new CourseFragmentName();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule_course_name, container, false);

        mListView = (ListView) layout.findViewById(R.id.listView);

        HeaderLayout header = ViewHelper.get(layout, R.id.header);
        if (header != null) {
            header.setHeaderOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    openMainScheduleFragment();
                }
            });
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mListView != null) {
            final List<String> courses = preferences.getByNameCourseDesignerCourseNames();
            Collections.sort(courses);
            String courseName = "";

            CourseBase courseDesign = getRaceState().getCourseDesign();
            if (courseDesign != null) {
                courseName = courseDesign.getName();
            }
            List<CheckedItem> items = new ArrayList<>();
            int position = 0;
            int selected = -1;
            for (String course : courses) {
                CheckedItem item = new CheckedItem();
                item.setText(course);
                if (course.equals(courseName)) {
                    selected = position;
                }
                items.add(item);
                position++;
            }
            final CheckedItemAdapter checkedItemAdapter = new CheckedItemAdapter(getActivity(), items);
            checkedItemAdapter.setCheckedPosition(selected);
            mListView.setAdapter(checkedItemAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    handleSelection(checkedItemAdapter.getItem(position).getText());
                }
            });
        }
    }

    public void handleSelection(String course) {
        CourseBase courseLayout = new CourseDataImpl(course);
        getRaceState().setCourseDesign(MillisecondsTimePoint.now(), courseLayout, CourseDesignerMode.BY_NAME);

        if (getArguments() != null && getArguments().getInt(START_MODE, START_MODE_PRESETUP) == START_MODE_PRESETUP) {
            openMainScheduleFragment();
        } else {
            sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
        }
    }
}
