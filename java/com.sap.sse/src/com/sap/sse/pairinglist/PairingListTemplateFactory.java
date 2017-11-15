package com.sap.sse.pairinglist;

import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;

public interface PairingListTemplateFactory {
    static PairingListTemplateFactory INSTANCE = new PairingListTemplateFactoryImpl();

    /**
     * Returns new pairing list template if there is no existing pairing list template with 
     * the same parameters.
     * 
     * @param pairingFrameProvider 
     * @return PairingListTemplate Object
     */
    PairingListTemplate getOrCreatePairingListTemplate(PairingFrameProvider pairingFrameProvider);
   
}