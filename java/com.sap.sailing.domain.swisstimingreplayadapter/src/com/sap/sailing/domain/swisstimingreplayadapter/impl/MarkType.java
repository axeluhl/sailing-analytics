package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.util.Arrays;

public class MarkType {

    // mark type ist maskiert: bit 7 = (isBuoy/isPin), 6 = (isBoat/noBoat), 5 = (isMeasure), 4 = (isStart,IsFinishOrCourse), 3 = (isFinish/isStartOrCourse), 2 = (isTurn/isStraight), 1 = (isCouple), 0 = (isVisible) 
    
    public static byte IS_VISIBLE = (byte) (1 << 0);
    public static byte IS_COUPLE = (byte) (1 << 1);
    public static byte IS_TURN = (byte) (1 << 2);
    public static byte IS_FINISH = (byte) (1 << 3);
    public static byte IS_START = (byte) (1 << 4);
    public static byte IS_MEASURE = (byte) (1 << 5);
    public static byte IS_BOAT = (byte) (1 << 6);
    public static byte IS_BUOY = (byte) (1 << 7);
    
    static String[] AS_STRING = new String[] {"VISIBLE", "COUPLE", "TURN", "FINISH", "START", "MEASURE", "BOAT", "BUOY"};
    
    private final byte bitCode;

    public MarkType(byte bitCode) {
        this.bitCode = bitCode;
    }
    
    public boolean isVisible() {
        return testFlag(IS_VISIBLE);
    }
    
    public boolean isCouple() {
        return testFlag(IS_COUPLE);
    }
    
    public boolean isTurn() {
        return testFlag(IS_TURN);
    }
    
    public boolean isStraight() {
        return ! isTurn();
    }
    
    public boolean isFinish() {
        return testFlag(IS_FINISH);
    }
    
    public boolean isStartOrCourse() {
        return ! isFinish();
    }
    
    public boolean isStart() {
        return testFlag(IS_START);
    }
    
    public boolean isFinishOrCourse() {
        return ! isStart();
    }
    
    public boolean isMeasure() {
        return testFlag(IS_MEASURE);
    }
    
    public boolean isBoat() {
        return testFlag(IS_BOAT);
    }
    
    public boolean isBuoy() {
        return testFlag(IS_BUOY);
    }
    
    public boolean isPin() {
        return ! isBuoy();
    }
    
    @Override
    public String toString() {
        String[] toString = new String[AS_STRING.length];
        for (int i = 0; i < toString.length; i++) {
            byte mask = (byte) (1 << i);
            if (testFlag(mask)) {
                toString[i] = AS_STRING[i];
            } else {
                toString[i] = AS_STRING[i].toLowerCase();
            }
        }
        return Arrays.toString(toString);
    }

    private boolean testFlag(byte mask) {
        return (bitCode & mask) == mask;
    }
    
}
