package com.sap.sailing.domain.common.racelog.tracking;

import java.io.Serializable;

public class CompetitorAndBoatRegistrationOnRaceLogDisabledException extends Exception implements Serializable {
    private static final long serialVersionUID = -6656554635954928251L;

    public CompetitorAndBoatRegistrationOnRaceLogDisabledException() {
        super("Competitor AND Boat registration not allowed on race");
    }

    public CompetitorAndBoatRegistrationOnRaceLogDisabledException(String message) {
        super(message);
    }
}
