package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CheckedItemListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CheckedListItem;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CourseItem;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class CourseFragmentName extends CourseFragment{

    private ListView mListView;

    public static CourseFragmentName newInstance(int startMode) {
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

        LinearLayout headerText = ViewHelper.get(layout, R.id.header_text);
        if (headerText != null) {
            headerText.setOnClickListener(new View.OnClickListener() {

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
            Collections.sort(courses, new NaturalComparator());
            String courseName = "";

            CourseBase courseDesign = getRaceState().getCourseDesign();
            if (courseDesign != null) {
                courseName = courseDesign.getName();
            }
            List<CheckedListItem> items = new ArrayList<>();
            for (String course : courses) {
                CourseItem item = new CourseItem();
                item.setText(course);
                item.setChecked(course.equals(courseName));
                items.add(item);
            }
            final CheckedItemListAdapter checkedItemListAdapter = new CheckedItemListAdapter(getActivity(), items);
            mListView.setAdapter(checkedItemListAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    handleSelection(checkedItemListAdapter.getItem(position).getText());
                }
            });
        }
    }

    public void handleSelection(String course) {
        CourseBase courseLayout = new CourseDataImpl(course);
        getRaceState().setCourseDesign(MillisecondsTimePoint.now(), courseLayout);

        if (getArguments() != null && getArguments().getInt(START_MODE, 0) == 0) {
            openMainScheduleFragment();
        } else {
            sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
        }
    }
}
