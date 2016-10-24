package com.sap.sailing.domain.common.racelog.tracking;

import java.io.Serializable;

public class CompetitorRegistrationOnRaceLogDisabledException extends Exception implements Serializable {
    private static final long serialVersionUID = 963948633131929332L;

    public CompetitorRegistrationOnRaceLogDisabledException() {
        super("Competitor registration not allowed on race");
    }

    public CompetitorRegistrationOnRaceLogDisabledException(String message) {
        super(message);
    }
}
