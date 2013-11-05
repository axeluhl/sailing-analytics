package com.sap.sailing.domain.common;

public enum PassingInstruction {
    None, 
    Port, 
    Starboard,
    Gate,
    Line,
    Offset;

    public static PassingInstruction[] relevantValues(){
        PassingInstruction[] uiValues = new PassingInstruction[PassingInstruction.values().length-1];

        int i = 0;
        for(PassingInstruction direction : PassingInstruction.values()){
            if(direction != PassingInstruction.None)
                uiValues[i++] = direction;
        }
        return uiValues;

    }
}