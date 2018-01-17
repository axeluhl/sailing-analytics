package com.sap.sse.pairinglist;

import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;

public interface PairingListTemplateFactory {
    static PairingListTemplateFactory INSTANCE = new PairingListTemplateFactoryImpl();

    /**
     * Returns a new calculated {@link PairingListTemplate}
     * 
     * @param pairingFrameProvider interface that has all necessary parameters
     * @param flightMultiplier specifies how often the flights will be repeated within a single {@link PairingList}
     * @return PairingListTemplate correlates with the new {@link PairingListTemplate}
     */
    PairingListTemplate createPairingListTemplate(PairingFrameProvider pairingFrameProvider, int flightMultiplier);
}