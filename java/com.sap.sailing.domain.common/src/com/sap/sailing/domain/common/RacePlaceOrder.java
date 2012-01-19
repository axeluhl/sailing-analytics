package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface RacePlaceOrder extends Serializable {

    Placemark getStartPlace();
    Placemark getFinishPlace();
    
    /**
     * @return The start placemark as String in the format 'Country-Code, Name'
     */
    String startToString();
    /**
     * @return The finish placemark as String in the format 'Country-Code, Name'
     */
    String finishToString();
    
    /**
     * @return true if the start Placemark and the finish Placemark are equal, false if not
     */
    boolean startEqualsFinish();
    
}
