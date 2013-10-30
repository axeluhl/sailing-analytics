package com.sap.sailing.domain.polarsheets;

import com.sap.sailing.domain.common.Speed;

public interface BoatAndWindSpeedWithOriginInfo {
    
    public Speed getBoatSpeed();
    
    public Speed getWindSpeed();
    
    public String getWindGaugesIdString();
    
    public String getDayString();

}
