package com.sap.sailing.domain.common.racelog;

public enum CourseLayout {
    windWardLeewardWindward("Windward/Leeward with Windward finish", "W"),
    windWardLeewardLeeward("Windward/Leeward with Leeward finish", "W"),
    innerLoopTrapezoid("Trapezoid Inner Loop", "I"),
    outerLoopTrapezoid("Trapezoid Outer Loop", "O");
    
    private String displayName;
    private String shortName;

    public String getShortName() {
        return shortName;
    }

    private CourseLayout(String displayName, String shortName){
        this.displayName = displayName;
        this.shortName = shortName;
    }

    @Override 
    public String toString(){
        return displayName;
    }
}
