package com.sap.sailing.domain.common;

public enum LegType { UPWIND, DOWNWIND, REACHING;
    
    public static double UPWIND_DOWNWIND_TOLERANCE_IN_DEG=45.; // TracTrac does 22.5, Marcus Baur suggest 40; Nils Schröder suggests 60
}