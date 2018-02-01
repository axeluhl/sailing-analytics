package com.sap.sailing.domain.swisstimingadapter;

public interface Course {
    String getRaceID();
    
    Iterable<Mark> getMarks();
}
