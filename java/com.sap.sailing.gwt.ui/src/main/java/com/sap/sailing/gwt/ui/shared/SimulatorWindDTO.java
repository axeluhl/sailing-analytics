package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimulatorWindDTO implements IsSerializable {
    public boolean isTurn;
    //public Double trueWindSpeedInMetersPerSecond;
    public Double trueWindSpeedInKnots;
    public Double trueWindBearingDeg;
    //public Double trueWindFromDeg;
    //public Double dampenedTrueWindSpeedInMetersPerSecond;
    //public Double dampenedTrueWindSpeedInKnots;
    //public Double dampenedTrueWindBearingDeg;
    //public Double dampenedTrueWindFromDeg;
    public PositionDTO position;
    public Long timepoint;
    //public Long originTimepoint;
    //public Double confidence;

    public SimulatorWindDTO() {
    }
}
