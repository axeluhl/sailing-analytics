package com.sap.sailing.domain.coursedesign;

public enum LeewardWindWardCourseLayouts implements CourseLayouts{
    windWardLeewardWindward("Windward/Leeward with Windward finish", "W", WindWardLeewardCourseDesignFactoryImpl.class),
    windWardLeewardLeeward("Windward/Leeward with Leeward finish", "L", WindWardLeewardCourseDesignFactoryImpl.class);
    
    private String displayName;
    private String shortName;
    private Class<? extends CourseDesignFactory> courseDesignFactoryClass;
    
    @Override
    public String getShortName() {
        return shortName;
    }

    private LeewardWindWardCourseLayouts(String displayName, String shortName, Class<? extends CourseDesignFactory> courseDesignFactoryClass){
        this.courseDesignFactoryClass = courseDesignFactoryClass;
        this.displayName = displayName;
        this.shortName = shortName;
    }

    @Override 
    public String toString(){
        return displayName;
    }

    @Override
    public Class<? extends CourseDesignFactory> getCourseDesignFactoryClass() {
        return this.courseDesignFactoryClass;
    }
}
