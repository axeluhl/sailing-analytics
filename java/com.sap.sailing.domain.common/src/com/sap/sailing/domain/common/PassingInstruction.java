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
        for(PassingInstruction p : PassingInstruction.values()){
            if(p != PassingInstruction.None)
                uiValues[i++] = p;
        }
        return uiValues;

    }
    public static PassingInstruction valueOfWithoutCase(String value){
        for(PassingInstruction p : PassingInstruction.values()){
            if(value.toLowerCase().equals(p.toString().toLowerCase())){
                return p;
            }
        }
        throw new IllegalArgumentException("No enum constant of PassingInstructions matches " + value);
    }
    
}