package com.sap.sse.pairinglist.impl;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;

public class PairingListTemplateFactoryImpl implements PairingListTemplateFactory {

    public PairingListTemplateFactoryImpl() { }

    
    @Override
    public PairingListTemplate createPairingListTemplate(PairingFrameProvider pairingFrameProvider, int flightMultiplier) {
        return new PairingListTemplateImpl(pairingFrameProvider, flightMultiplier);
    }
    
    public PairingListTemplate createPairingListTemplate(PairingFrameProvider pairingFrameProvider, int iterations, int flightMultiplier) {
        return new PairingListTemplateImpl(pairingFrameProvider, iterations, flightMultiplier);
    }
    
    public PairingListTemplate createPairingListTemplate(PairingFrameProvider pairingFrameProvider) {
        return new PairingListTemplateImpl(pairingFrameProvider);
    }
}
