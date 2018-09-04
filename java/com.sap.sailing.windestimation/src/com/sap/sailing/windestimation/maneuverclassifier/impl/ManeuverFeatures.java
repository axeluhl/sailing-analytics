package com.sap.sailing.windestimation.maneuverclassifier.impl;

public class ManeuverFeatures {

    private final boolean polarsInformation;
    private final boolean scaledSpeed;

    public ManeuverFeatures(boolean polarsInformation, boolean scaledSpeed) {
        this.polarsInformation = polarsInformation;
        this.scaledSpeed = scaledSpeed;
    }

    public boolean isPolarsInformation() {
        return polarsInformation;
    }

    public boolean isScaledSpeed() {
        return scaledSpeed;
    }

    public boolean isSubset(ManeuverFeatures superSet) {
        return equals(superSet) && (!polarsInformation || superSet.polarsInformation)
                && (!scaledSpeed || superSet.scaledSpeed);
    }

    @Override
    public String toString() {
        if (polarsInformation || scaledSpeed) {
            StringBuilder str = new StringBuilder();
            if (polarsInformation) {
                str.append("Polars");
            }
            if (scaledSpeed) {
                str.append("ScaledSpeed");
            }
            return str.toString();
        }
        return "Basic";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        if (polarsInformation != other.polarsInformation)
            return false;
        if (scaledSpeed != other.scaledSpeed)
            return false;
        return true;
    }

    public static ManeuverFeatures[] values() {
        return new ManeuverFeatures[] { new ManeuverFeatures(true, true), new ManeuverFeatures(true, false),
                new ManeuverFeatures(false, true), new ManeuverFeatures(false, false) };
    }

}
