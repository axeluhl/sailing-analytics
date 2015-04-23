package com.sap.sailing.domain.common.racelog;

import java.util.ArrayList;
import java.util.List;

public enum Flags {
    NONE,
    ALPHA,
    BRAVO,
    CLASS,
    FOXTROTT,
    GOLF,
    HOTEL,
    INDIA,
    INDIA_ZULU, // the flag combination of I+Z flown at the same time
    NOVEMBER,
    PAPA,
    UNIFORM,
    XRAY,
    ZULU,
    FIRSTSUBSTITUTE,
    BLACK,
    BLUE,
    JURY,
    AP,
    ESSONE,
    ESSTWO,
    ESSTHREE;
    
    public static Flags[] validValues() {
        List<Flags> validValues = new ArrayList<Flags>();
        for (Flags flag : values()) {
            if (flag != NONE) {
                validValues.add(flag);
            }
        }
        return validValues.toArray(new Flags[validValues.size()]);
    }
}
