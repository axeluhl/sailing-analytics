package com.sap.sailing.domain.coursedesign;

public enum WindWardLeeWardCourseLayouts implements CourseLayouts{
    windWardLeewardWindward("Windward/Leeward with Windward finish", "W"),
    windWardLeewardLeeward("Windward/Leeward with Leeward finish", "L");
    
    private String displayName;
    private String shortName;
    
    @Override
    public String getShortName() {
        return shortName;
    }

    private WindWardLeeWardCourseLayouts(String displayName, String shortName){
        this.displayName = displayName;
        this.shortName = shortName;
    }

    @Override 
    public String toString(){
        return displayName;
    }

    @Override
    public Class<? extends CourseDesignFactory> getCourseDesignFactoryClass() {
        return WindWardLeeWardCourseDesignFactoryImpl.class;
    }
}
