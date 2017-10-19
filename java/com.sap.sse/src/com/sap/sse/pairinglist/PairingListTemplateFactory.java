package com.sap.sse.pairinglist;

import com.sap.sse.pairinglist.impl.PairingListImpl;
import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;

public interface PairingListTemplateFactory {
    
    public PairingListTemplateFactoryImpl INSTANCE = new PairingListTemplateFactoryImpl();
    
    public PairingListTemplate createPairingListTemplate(int competitors, int flights);
}