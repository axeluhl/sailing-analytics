package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.racelog.RaceLog;

public interface RaceLogStore {

    RaceLog getRaceLog(RaceLogIdentifier identifier);

}
