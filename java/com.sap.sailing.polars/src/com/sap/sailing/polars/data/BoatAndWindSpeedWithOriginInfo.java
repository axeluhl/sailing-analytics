package com.sap.sailing.polars.data;

import com.sap.sailing.domain.common.Speed;

public interface BoatAndWindSpeedWithOriginInfo {
    
    public Speed getBoatSpeed();
    
    public Speed getWindSpeed();
    
    public String getWindGaugesIdString();
    
    public String getDayString();

}
