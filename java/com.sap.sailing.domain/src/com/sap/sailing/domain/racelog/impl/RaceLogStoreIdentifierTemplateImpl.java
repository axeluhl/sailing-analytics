package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;


public class RaceLogStoreIdentifierTemplateImpl implements RaceLogIdentifierTemplate {

    private final Named parentObject;
    
    public RaceLogStoreIdentifierTemplateImpl(Named parentObject) {
        this.parentObject = parentObject;
    }

    @Override
    public RaceLogIdentifier compile(RaceColumn column, Fleet fleet) {
        return new RaceLogIdentifierImpl(parentObject, column, fleet);
    }

}
