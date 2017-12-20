package com.sap.sse.pairinglist;

import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;

public interface PairingListTemplateFactory {
    static PairingListTemplateFactory INSTANCE = new PairingListTemplateFactoryImpl();

    /**
     * Returns a new {@link PairingListTemplate}
     * 
     * @param pairingFrameProvider 
     * @return PairingListTemplate correlates with the new {@link PairingListTemplate}
     */
    PairingListTemplate createPairingListTemplate(PairingFrameProvider pairingFrameProvider, int flightCount);
}