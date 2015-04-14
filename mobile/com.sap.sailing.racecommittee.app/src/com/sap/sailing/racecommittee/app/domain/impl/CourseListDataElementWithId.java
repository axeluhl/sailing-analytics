package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CourseListDataElement;

public class CourseListDataElementWithId extends CourseListDataElement {

    private long mId;

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }
}
