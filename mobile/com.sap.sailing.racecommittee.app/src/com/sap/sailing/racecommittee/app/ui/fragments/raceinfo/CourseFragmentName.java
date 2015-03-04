package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.ListView;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.CourseNameAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class CourseFragmentName extends ScheduleFragment implements CourseNameAdapter.CourseItemClick {

    private static final String TAG = CourseFragmentName.class.getName();
    private static final String STARTMODE = "startMode";

    private ListView mListView;
    
    public CourseFragmentName() {
        
    }

    public static CourseFragmentName newInstance(int startMode) {
        CourseFragmentName fragment = new CourseFragmentName();
        Bundle args = new Bundle();
        args.putInt(STARTMODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule_course_name, container, false);

        if (getArguments() != null) {
            switch (getArguments().getInt(STARTMODE, 0)) {
                case 1:
                    layout.findViewById(R.id.race_header).setVisibility(View.VISIBLE);
                    View header = layout.findViewById(R.id.header);
                    header.setVisibility(View.GONE);
                    break;

                default:
                    break;
            }
        }

        mListView = (ListView) layout.findViewById(R.id.listView);

        LinearLayout headerText = (LinearLayout) layout.findViewById(R.id.header_text);
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
            mListView.setAdapter(new CourseNameAdapter(getActivity(), preferences.getByNameCourseDesignerCourseNames(), this));
        }
    }

    @Override
    public void onClick(String course) {
        CourseBase courseLayout = new CourseDataImpl(course);
        getRaceState().setCourseDesign(MillisecondsTimePoint.now(), courseLayout);

        if (getArguments() != null && getArguments().getInt(STARTMODE, 0) == 0) {
            openMainScheduleFragment();
        } else {
            replaceFragment(RaceFlagViewerFragment.newInstance(), R.id.race_frame);
            sendIntent(R.string.intent_uncheck_all);
        }
    }
}
