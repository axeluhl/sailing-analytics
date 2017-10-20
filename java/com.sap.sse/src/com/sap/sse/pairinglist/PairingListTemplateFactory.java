package com.sap.sse.pairinglist;

import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;

public interface PairingListTemplateFactory {
    
    public PairingListTemplateFactoryImpl INSTANCE = new PairingListTemplateFactoryImpl();
    
    public PairingListTemplate createPairingListTemplate(PairingFrameProvider<?, ?, ?> pPFP);
}