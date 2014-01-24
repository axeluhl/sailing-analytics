package com.sap.sailing.domain.common;

/**
 * There are multiple ways a boat can pass a Waypoint. Two marks are either a Line that has to be crossed or a Gate,
 * where either of the the marks can be rounded. A single mark usually has to be passed on Port or Starboard. On rare
 * occasions an Offset mark can be laid out close to another mark to avoid collisions around that mark. The Offset mark
 * does not count as a Waypoint and does not start an own Leg. Sometimes a single mark has an absolute Bearing, leading
 * to a line that has to be crossed.
 * 
 * @author Nicolas Klose
 * 
 */

public enum PassingInstruction {
    None, 
    Port, 
    Starboard,
    Gate,
    Line,
    Offset,
    FixedBearing;

    public static PassingInstruction[] relevantValues() {
        PassingInstruction[] uiValues = new PassingInstruction[PassingInstruction.values().length - 1];

        int i = 0;
        for (PassingInstruction p : PassingInstruction.values()) {
            if (p != PassingInstruction.None)
                uiValues[i++] = p;
        }
        return uiValues;
    }

    public static PassingInstruction valueOfIgnoringCase(String value) {
        for (PassingInstruction p : PassingInstruction.values()) {
            if (value.toLowerCase().equals(p.toString().toLowerCase())) {
                return p;
            }
        }
        return null;
    }
    
}