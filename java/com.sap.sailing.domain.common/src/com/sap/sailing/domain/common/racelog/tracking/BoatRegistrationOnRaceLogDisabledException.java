package com.sap.sailing.domain.common.racelog.tracking;

import java.io.Serializable;

public class BoatRegistrationOnRaceLogDisabledException extends Exception implements Serializable {
    private static final long serialVersionUID = -6656554635954928251L;

    public BoatRegistrationOnRaceLogDisabledException() {
        super("Boat registration not allowed on race");
    }

    public BoatRegistrationOnRaceLogDisabledException(String message) {
        super(message);
    }
}
