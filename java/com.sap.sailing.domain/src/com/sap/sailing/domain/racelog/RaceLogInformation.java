package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;


public interface RaceLogInformation {
    
    RaceLogStore getStore();
    RaceLogIdentifierTemplate getIdentifierTemplate();
    RaceLog getRaceLog(RaceColumn raceColumn, Fleet fleet);

}
