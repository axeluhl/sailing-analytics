package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public abstract class CourseFragment extends BaseFragment {

    public static RaceFragment newInstance(int startMode) {
        return CourseFragmentName.newInstance(startMode);
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
