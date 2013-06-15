package com.sap.sailing.domain.coursedesign;

public class WindwardLeewardCourseDesignImpl implements CourseDesign{
    
    PositionedMark pinEnd;

    @Override
    public PositionedMark getPinEnd() {
        return pinEnd;
    }
    @Override
    public void setPinEnd(PositionedMark pinEnd) {
        this.pinEnd = pinEnd;
    }
    
}
