package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.ArrayList;

import com.sap.sailing.domain.swisstimingadapter.Course;
import com.sap.sailing.domain.swisstimingadapter.Mark;

public class CourseImpl implements Course {
    private final String raceId;
    private final Iterable<Mark> marks;
    
    public CourseImpl(String raceId, Iterable<Mark> marks) {
        super();
        this.raceId = raceId;
        ArrayList<Mark> l = new ArrayList<Mark>();
        for (Mark mark : marks) {
            l.add(mark);
        }
        this.marks = l;
    }

    @Override
    public String getRaceID() {
        return raceId;
    }

    @Override
    public Iterable<Mark> getMarks() {
        return marks;
    }

}
