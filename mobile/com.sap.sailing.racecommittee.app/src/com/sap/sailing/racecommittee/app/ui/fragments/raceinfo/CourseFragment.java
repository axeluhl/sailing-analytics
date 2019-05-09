package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;

import android.os.Bundle;
import android.view.View;

public abstract class CourseFragment extends BaseFragment {

    public static RaceFragment newInstance(@START_MODE_VALUES int startMode, AppPreferences preferences) {
        CourseDesignerMode mode = preferences.getDefaultCourseDesignerMode();

        RaceFragment fragment;
        switch (mode) {
        case BY_NAME:
            fragment = CourseFragmentName.newInstance(startMode);
            break;

        case BY_MAP:
            fragment = CourseFragmentMap.newInstance(startMode);
            break;

        case BY_MARKS:
            fragment = CourseFragmentMarks.newInstance(startMode);
            break;

        default:
            throw new IllegalArgumentException("Invalid CourseDesignerMode");
        }

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null && getArguments() != null) {
            HeaderLayout header = (HeaderLayout) getView().findViewById(R.id.header);
            if (header != null) {
                header.setHeaderOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goHome();
                    }
                });
                switch (getArguments().getInt(START_MODE, START_MODE_PRESETUP)) {
                case START_MODE_PLANNED:
                    if (AppUtils.with(getActivity()).isLandscape() && AppUtils.with(getActivity()).is10inch()) {
                        header.setVisibility(View.GONE);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
    }

    @Override
    public void onPause() {
        super.onPause();

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }
}
