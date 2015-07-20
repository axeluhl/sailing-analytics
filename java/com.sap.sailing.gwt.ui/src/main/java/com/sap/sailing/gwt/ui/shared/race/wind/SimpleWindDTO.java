package com.sap.sailing.gwt.ui.shared.race.wind;


public class SimpleWindDTO extends AbstractWindDTO {
    private Double trueWindSpeedInKnots;

    protected SimpleWindDTO() {
        super();
    }

    public SimpleWindDTO(Double trueWindFromDeg, Double trueWindSpeedInKnots) {
        super(trueWindFromDeg);
        this.trueWindSpeedInKnots = trueWindSpeedInKnots;
    }

    public Double getTrueWindSpeedInKnots() {
        return trueWindSpeedInKnots;
    }
}
