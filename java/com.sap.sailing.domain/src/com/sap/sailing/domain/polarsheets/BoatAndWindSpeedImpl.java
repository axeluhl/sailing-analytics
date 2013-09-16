package com.sap.sailing.domain.polarsheets;

import com.sap.sailing.domain.common.Speed;

public class BoatAndWindSpeedImpl implements BoatAndWindSpeed{

    private Speed boatSpeed;
    private Speed windSpeed;
    
    

    public BoatAndWindSpeedImpl(Speed boatSpeed, Speed windSpeed) {
        this.boatSpeed = boatSpeed;
        this.windSpeed = windSpeed;
    }

    @Override
    public Speed getBoatSpeed() {
        return boatSpeed;
    }

    @Override
    public Speed getWindSpeed() {
        return windSpeed;
    }
    

}
