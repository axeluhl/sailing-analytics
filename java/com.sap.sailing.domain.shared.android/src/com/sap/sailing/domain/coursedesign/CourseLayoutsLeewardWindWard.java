package com.sap.sailing.domain.coursedesign;

public enum CourseLayoutsLeewardWindWard implements CourseLayout{
    windWardLeewardWindward("Windward/Leeward with Windward finish", "W"),
    windWardLeewardLeeward("Windward/Leeward with Leeward finish", "L");
    
    private String displayName;
    private String shortName;
    
    @Override
    public String getShortName() {
        return shortName;
    }

    private CourseLayoutsLeewardWindWard(String displayName, String shortName){
        this.displayName = displayName;
        this.shortName = shortName;
    }

    @Override 
    public String toString(){
        return displayName;
    }
}
