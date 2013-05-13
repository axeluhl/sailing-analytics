package com.sap.sailing.racecommittee.app.domain;

public enum RoundingDirection {
    None, 
    Port, 
    Starboard,
    Gate;

    public static RoundingDirection[] relevantValues(){
        RoundingDirection[] uiValues = new RoundingDirection[RoundingDirection.values().length-1];

        int i = 0;
        for(RoundingDirection direction : RoundingDirection.values()){
            if(direction != RoundingDirection.None)
                uiValues[i++] = direction;
        }
        return uiValues;

    }
}