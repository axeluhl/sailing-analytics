package com.sap.sailing.simulator.impl;

public class SparsePolarDataException extends Exception {

    private static final long serialVersionUID = 4134116458606258315L;

    public SparsePolarDataException() {
        super("Not enough polar data to represent beat/jibe angles/speeds for simulation.");
    }

    public SparsePolarDataException(String string) {
        super(string);
    }

}
