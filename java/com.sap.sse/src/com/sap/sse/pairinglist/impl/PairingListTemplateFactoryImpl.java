package com.sap.sse.pairinglist.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;

public class PairingListTemplateFactoryImpl implements PairingListTemplateFactory {
    private final Map<PairingFrameProvider, PairingListTemplate> pairingListTemplates;
    
    public PairingListTemplateFactoryImpl() {
        this(new HashMap<>());
    }
    
    public PairingListTemplateFactoryImpl(Map<PairingFrameProvider, PairingListTemplate> existingPairingListTemplates) {
        this.pairingListTemplates = existingPairingListTemplates;
    }

    @Override
    public PairingListTemplate getOrCreatePairingListTemplate(PairingFrameProvider pairingFrameProvider) {
        PairingListTemplate result = pairingListTemplates.get(pairingFrameProvider);
        if (result == null) {
            result = new PairingListTemplateImpl(pairingFrameProvider);
            pairingListTemplates.put(pairingFrameProvider, result);
        }
        return result;
    }
}
