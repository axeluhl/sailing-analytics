package com.sap.sailing.domain.common.racelog.courseDesigner;

public enum NumberOfRounds {
    TWO(2),
    THREE(3),
    FOUR(4);
    
    private final Integer numberOfRounds;
    
    public Integer getNumberOfRounds() {
        return numberOfRounds;
    }

    private NumberOfRounds(Integer numberOfRounds){
        this.numberOfRounds = numberOfRounds;
    }
    
    @Override
    public
    String toString(){
        return this.numberOfRounds.toString();
    }
}
