package com.sap.sailing.domain.polarsheets;

import com.sap.sailing.domain.common.Speed;

public class BoatAndWindSpeedWithOriginInfoImpl implements BoatAndWindSpeedWithOriginInfo {

    private Speed boatSpeed;
    private Speed windSpeed;
    private String windGaugesIdString;

    public BoatAndWindSpeedWithOriginInfoImpl(Speed boatSpeed, Speed windSpeed, String windGaugesIdString) {
        this.boatSpeed = boatSpeed;
        this.windSpeed = windSpeed;
        this.windGaugesIdString = windGaugesIdString;
    }

    @Override
    public Speed getBoatSpeed() {
        return boatSpeed;
    }

    @Override
    public Speed getWindSpeed() {
        return windSpeed;
    }

    @Override
    public String getWindGaugesIdString() {
        return windGaugesIdString;
    }

}
