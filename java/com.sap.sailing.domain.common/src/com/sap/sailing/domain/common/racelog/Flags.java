package com.sap.sailing.domain.common.racelog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Flags for different purposes. Flags may qualify as start mode flags. See {@link #getStartModeFlags}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum Flags {
    NONE(/* start mode flag */ false),
    ALPHA(/* start mode flag */ false),
    BRAVO(/* start mode flag */ false),
    CLASS(/* start mode flag */ false),
    FOXTROTT(/* start mode flag */ false),
    GOLF(/* start mode flag */ false),
    HOTEL(/* start mode flag */ false),
    INDIA(/* start mode flag */ true),
    INDIA_ZULU(/* start mode flag */ true), // the flag combination of I+Z flown at the same time
    NOVEMBER(/* start mode flag */ false),
    OSCAR(/* start mode flag */ false),
    PAPA(/* start mode flag */ true),
    UNIFORM(/* start mode flag */ true),
    XRAY(/* start mode flag */ false),
    ZULU(/* start mode flag */ true),
    FIRSTSUBSTITUTE(/* start mode flag */ false),
    BLACK(/* start mode flag */ true),
    BLUE(/* start mode flag */ false),
    JURY(/* start mode flag */ false),
    AP(/* start mode flag */ false),
    ESSONE(/* start mode flag */ false),
    ESSTWO(/* start mode flag */ false),
    ESSTHREE(/* start mode flag */ false),
    SWC_ZERO(/* start mode flag */ false),
    SWC_ONE(/* start mode flag */ false),
    SWC_TWO(/* start mode flag */ false),
    SWC_THREE(/* start mode flag */ false),
    SWC_FOUR(/* start mode flag */ false),
    SWC_FIVE(/* start mode flag */ false);
    
    public static Flags[] validValues() {
        List<Flags> validValues = new ArrayList<Flags>();
        for (Flags flag : values()) {
            if (flag != NONE) {
                validValues.add(flag);
            }
        }
        return validValues.toArray(new Flags[validValues.size()]);
    }
    
    private static Set<Flags> startModeFlags;
    
    static {
        startModeFlags = new HashSet<>();
        for (final Flags flag : Flags.values()) {
            if (flag.isStartModeFlag()) {
                startModeFlags.add(flag);
            }
        }
    }
    
    private Flags(boolean isStartModeFlag) {
        this.isStartModeFlag = isStartModeFlag;
    }
    
    public static Flags[] getStartModeFlags() {
        return startModeFlags.toArray(new Flags[startModeFlags.size()]);
    }

    public boolean isStartModeFlag() {
        return isStartModeFlag;
    }
    
    private final boolean isStartModeFlag;
}
