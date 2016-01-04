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
    None(1, 2),
    Port(1),
    Starboard(1),
    Gate(2),
    Line(2),
    Offset(2),
    FixedBearing(1);

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
        final String lowerCaseValue = value.toLowerCase();
        for (PassingInstruction p : PassingInstruction.values()) {
            if (lowerCaseValue.equals(p.toString().toLowerCase())) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Denotes for which types of {@code ControlPoints} (defined by the number of marks they are made up of)
     * this passing instruction is applicable.<p>
     * E.g. {@code [1,2]} denotes that the passing instruction is applicable for {@code ControlPoints} with either one
     * or two marks.
     */
    public final int[] applicability;
    
    private PassingInstruction(int... applicability) {
        this.applicability = applicability;
    }
}