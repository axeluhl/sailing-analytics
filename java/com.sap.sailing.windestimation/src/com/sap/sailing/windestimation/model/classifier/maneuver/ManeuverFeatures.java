package com.sap.sailing.windestimation.model.classifier.maneuver;

import java.io.Serializable;

public class ManeuverFeatures implements Serializable {

    private static final long serialVersionUID = -695419180697024150L;
    
    private static final ManeuverFeatures[] values = new ManeuverFeatures[8];
    
    static {
        int i = 0;
        for(int polars = 0; polars <= 1; polars++) {
            for(int scaledSpeed = 0; scaledSpeed <= 1; scaledSpeed++) {
                for(int marks = 0; marks <= 1; marks++) {
                    values[i++] = new ManeuverFeatures(polars == 0, scaledSpeed == 0, marks == 0);
                }
            }
        }
    }

    private final boolean polarsInformation;
    private final boolean scaledSpeed;
    private final boolean marksInformation;

    public ManeuverFeatures(boolean polarsInformation, boolean scaledSpeed, boolean marksInformation) {
        this.polarsInformation = polarsInformation;
        this.scaledSpeed = scaledSpeed;
        this.marksInformation = marksInformation;
    }

    public boolean isPolarsInformation() {
        return polarsInformation;
    }

    public boolean isScaledSpeed() {
        return scaledSpeed;
    }
    
    public boolean isMarksInformation() {
        return marksInformation;
    }

    public boolean isSubset(ManeuverFeatures superSet) {
        return (!polarsInformation || superSet.polarsInformation)
                && (!scaledSpeed || superSet.scaledSpeed) && (!marksInformation || superSet.marksInformation);
    }

    @Override
    public String toString() {
        if (polarsInformation || scaledSpeed || marksInformation) {
            StringBuilder str = new StringBuilder();
            if (polarsInformation) {
                str.append("Polars");
            }
            if (scaledSpeed) {
                str.append("ScaledSpeed");
            }
            if(marksInformation) {
                str.append("Marks");
            }
            return str.toString();
        }
        return "Basic";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (marksInformation ? 1231 : 1237);
        result = prime * result + (polarsInformation ? 1231 : 1237);
        result = prime * result + (scaledSpeed ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ManeuverFeatures other = (ManeuverFeatures) obj;
        if (marksInformation != other.marksInformation)
            return false;
        if (polarsInformation != other.polarsInformation)
            return false;
        if (scaledSpeed != other.scaledSpeed)
            return false;
        return true;
    }

    public static ManeuverFeatures[] values() {
        return values;
    }

}
