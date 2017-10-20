package com.sap.sse.pairinglist.impl;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;

public class PairingListTemplateFactoryImpl implements PairingListTemplateFactory{
    
    
    public PairingListTemplateFactoryImpl() {
        
    }
    public PairingListTemplate createPairingListTemplate(PairingFrameProvider<?,?,?> pPFP) {
 
        return new PairingListTemplateImpl();
    }
}
