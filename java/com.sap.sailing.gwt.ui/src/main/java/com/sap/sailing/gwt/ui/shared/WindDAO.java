package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindDAO implements IsSerializable {
    public Double trueWindSpeedInMetersPerSecond;
    public Double trueWindSpeedInKnots;
    public Double trueWindBearingDeg;
    public Double trueWindFromDeg;
    public Double dampenedTrueWindSpeedInMetersPerSecond;
    public Double dampenedTrueWindSpeedInKnots;
    public Double dampenedTrueWindBearingDeg;
    public Double dampenedTrueWindFromDeg;
    public PositionDAO position;
    public Long timepoint;
    
    public WindDAO() {}
}
