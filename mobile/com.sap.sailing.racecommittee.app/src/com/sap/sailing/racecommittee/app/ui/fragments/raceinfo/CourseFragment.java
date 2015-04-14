package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.View;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public abstract class CourseFragment extends BaseFragment {

    protected static final String START_MODE = "startMode";

    public static RaceFragment newInstance(int startMode, ManagedRace race) {
        RaceFragment fragment;
        RegattaConfiguration configuration = race.getState().getConfiguration();
        CourseDesignerMode mode = configuration.getDefaultCourseDesignerMode();

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

        if (getView() != null) {
            if (getArguments() != null) {
                View header = getView().findViewById(R.id.header);
                if (header != null) {
                    switch (getArguments().getInt(START_MODE, 0)) {
                    case 1:
                        header.setVisibility(View.GONE);
                        break;

                    default:
                        View text = header.findViewById(R.id.header_text);
                        if (text != null) {
                            text.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openMainScheduleFragment();
                                }
                            });
                        }
                        break;
                    }
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
