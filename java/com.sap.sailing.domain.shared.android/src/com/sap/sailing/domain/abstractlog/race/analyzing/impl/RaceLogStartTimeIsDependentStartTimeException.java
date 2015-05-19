package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

public class RaceLogStartTimeIsDependentStartTimeException extends Exception {
    private static final long serialVersionUID = 1812091995418381241L;

    public RaceLogStartTimeIsDependentStartTimeException() {
        super();
    }

    public RaceLogStartTimeIsDependentStartTimeException(String fleetName) {
        super("StartTime depends on " + fleetName);
    }
}
