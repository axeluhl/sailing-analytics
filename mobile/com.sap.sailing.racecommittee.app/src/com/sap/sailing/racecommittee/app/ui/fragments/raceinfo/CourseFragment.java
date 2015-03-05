package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public abstract class CourseFragment extends ScheduleFragment {

    public static RaceFragment newInstance(int startMode) {
        return CourseFragmentName.newInstance(startMode);
    }
}
