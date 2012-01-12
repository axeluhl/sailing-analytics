package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class BoatImpl extends NamedImpl implements Boat {
    private final BoatClass boatClass;
    private final String sailID;
    
    public BoatImpl(String name, BoatClass boatClass, String sailID) {
        super(name);
        this.boatClass = boatClass;
        this.sailID = sailID;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public String getSailID() {
        return sailID;
    }
}
