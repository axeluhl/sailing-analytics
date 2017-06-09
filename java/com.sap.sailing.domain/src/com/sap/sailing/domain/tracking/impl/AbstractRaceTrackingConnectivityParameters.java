package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;

public abstract class AbstractRaceTrackingConnectivityParameters implements RaceTrackingConnectivityParameters {
    private boolean trackWind;
    private final boolean correctWindDirectionByMagneticDeclination;
    
    public AbstractRaceTrackingConnectivityParameters(boolean trackWind, boolean correctWindDirectionByMagneticDeclination) {
        super();
        this.trackWind = trackWind;
        this.correctWindDirectionByMagneticDeclination = correctWindDirectionByMagneticDeclination;
    }

    @Override
    public boolean isTrackWind() {
        return trackWind;
    }

    @Override
    public void setTrackWind(boolean trackWind) {
        this.trackWind = trackWind;
    }

    @Override
    public boolean isCorrectWindDirectionByMagneticDeclination() {
        return correctWindDirectionByMagneticDeclination;
    }
}
