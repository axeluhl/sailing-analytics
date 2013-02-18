package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;


import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;

public class RaceLogIdentifierImpl implements RaceLogIdentifier {

    private final Named parentObject;
    private final RaceColumn raceColumn;
    private final Fleet fleet;
    
    public RaceLogIdentifierImpl(Named parentObject, RaceColumn raceColumn, Fleet fleet) {
        this.parentObject = parentObject;
        this.raceColumn = raceColumn;
        this.fleet = fleet;
    }

    @Override
    public Serializable getIdentifier() {
        return String.format("%s.%s.%s", parentObject.getName(), raceColumn.getName(), fleet.getName());
    }

}
