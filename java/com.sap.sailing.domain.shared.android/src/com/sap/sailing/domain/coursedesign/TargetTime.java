package com.sap.sailing.domain.coursedesign;

public enum TargetTime {
    thirty(30),
    sixty(60);
    
    private final Integer timeInMinutes;
    
    public Integer getTimeInMinutes() {
        return timeInMinutes;
    }

    private TargetTime(Integer timeInMinutes){
        this.timeInMinutes = timeInMinutes;
    }
    
    @Override
    public
    String toString(){
        return this.timeInMinutes.toString();
    }
}
