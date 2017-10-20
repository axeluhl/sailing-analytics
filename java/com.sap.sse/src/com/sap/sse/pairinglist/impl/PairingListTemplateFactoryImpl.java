package com.sap.sse.pairinglist.impl;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;

public class PairingListTemplateFactoryImpl<Flight,Group,Competitor> implements PairingListTemplateFactory<Flight,Group,Competitor>{
    
    
    public PairingListTemplateFactoryImpl() {
        
    }
    @Override
    public PairingListTemplate<Flight, Group, Competitor> createPairingListTemplate(
            PairingFrameProvider<Flight, Group, Competitor> pPFP) {
        // TODO Auto-generated method stub
        return null;
    }
}
