package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class CourseFragmentHelper {

    public static RaceFragment newInstance(int startMode) {
        return CourseFragmentName.newInstance(startMode);
    }
}
