package com.sap.sailing.simulator.impl;

public class SparseSimulationDataException extends Exception {

    private static final long serialVersionUID = 4134116458606258315L;

    public SparseSimulationDataException() {
        super("Not enough simulation data available (sparse wind data or sparse polar data for beat/jibe angles/speeds).");
    }

    public SparseSimulationDataException(String string) {
        super(string);
    }

}
