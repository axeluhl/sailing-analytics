package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;

public class BoatImpl extends NamedImpl implements Boat {
    private final BoatClass boatClass;
    
    public BoatImpl(String name, BoatClass boatClass) {
        super(name);
        this.boatClass = boatClass;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

}
