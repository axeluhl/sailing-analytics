package com.sap.sse.pairinglist.impl;

import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;

public class PairingListTemplateFactoryImpl implements PairingListTemplateFactory{
    
    public PairingListTemplateFactoryImpl() {
        
    }

    public PairingListTemplate createPairingListTemplate(int competitors, int flights) {
        return new PairingListTemplateImpl();
    }

}
