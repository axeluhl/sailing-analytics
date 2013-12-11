package com.sap.sailing.polars.data.impl;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.polars.data.BoatAndWindSpeedWithOriginInfo;

public class BoatAndWindSpeedWithOriginInfoImpl implements BoatAndWindSpeedWithOriginInfo {

    private Speed boatSpeed;
    private Speed windSpeed;
    private String windGaugesIdString;
    private String dayString;

    public BoatAndWindSpeedWithOriginInfoImpl(Speed boatSpeed, Speed windSpeed, String windGaugesIdString,
            String dayString) {
        this.boatSpeed = boatSpeed;
        this.windSpeed = windSpeed;
        this.windGaugesIdString = windGaugesIdString;
        this.dayString = dayString;
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

    @Override
    public String getDayString() {
        return dayString;
    }

}
