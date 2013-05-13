package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;

public interface CourseDesignListener {
    void onCourseDesignPublish();
    ReadonlyDataManager getDataManager();
}
