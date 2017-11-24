package com.sap.sse.pairinglist.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.pairinglist.AbstractPairingFrameProvider;
import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;

public class PairingListTemplateFactoryImpl implements PairingListTemplateFactory {
    private final Map<Integer, PairingListTemplate> pairingListTemplates;

    public PairingListTemplateFactoryImpl() {
        this(new HashMap<>());
    }

    public PairingListTemplateFactoryImpl(Map<Integer, PairingListTemplate> existingPairingListTemplates) {
        this.pairingListTemplates = existingPairingListTemplates;
    }

    @Override
    public PairingListTemplate getOrCreatePairingListTemplate(PairingFrameProvider pairingFrameProvider) {
        PairingListTemplate result = pairingListTemplates.get(pairingFrameProvider.getHashCode());
        if (result == null) {
            result = generatePairingList(pairingFrameProvider);
            pairingListTemplates.put(pairingFrameProvider.getHashCode(), result);
        }
        return result;
    }
     /**
      * Creates a new ParingListTemplate which depends on the parameters of the given pairingFrameProvider.
      * @param pairingFrameProvider
      * @return new PariningListTemplate object
      */
    protected PairingListTemplate generatePairingList(PairingFrameProvider pairingFrameProvider) {
        return new PairingListTemplateImpl(pairingFrameProvider);
    }
}
