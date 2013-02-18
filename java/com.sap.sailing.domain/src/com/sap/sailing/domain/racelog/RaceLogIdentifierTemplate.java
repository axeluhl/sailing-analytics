package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;

public interface RaceLogIdentifierTemplate {

    RaceLogIdentifier compile(RaceColumn column, Fleet fleet);

}
