package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplateResolver;

public class RaceLogOnRegattaIdentifier implements RaceLogIdentifierTemplate {

    private final String regattaName;
    
    public RaceLogOnRegattaIdentifier(Regatta regatta) {
        this.regattaName = regatta.getName();
    }
    
    @Override
    public RaceLogIdentifier compile(RaceColumn column, Fleet fleet) {
        return new RaceLogIdentifierImpl(this, column, fleet);
    }

    @Override
    public void resolve(RaceLogIdentifierTemplateResolver resolver) {
        resolver.resolveOnRegattaIdentifier(this);
    }

    @Override
    public String getHostName() {
        return regattaName;
    }

}
