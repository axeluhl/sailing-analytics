package com.sap.sse.pairinglist;

import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;

public interface PairingListTemplateFactory {
    static PairingListTemplateFactory INSTANCE = new PairingListTemplateFactoryImpl();

    PairingListTemplate getOrCreatePairingListTemplate(PairingFrameProvider pairingFrameProvider);
   
}