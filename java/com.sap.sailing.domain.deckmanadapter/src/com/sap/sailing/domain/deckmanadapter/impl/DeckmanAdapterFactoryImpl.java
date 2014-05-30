package com.sap.sailing.domain.deckmanadapter.impl;

import com.sap.sailing.domain.deckmanadapter.DeckmanAdapter;
import com.sap.sailing.domain.deckmanadapter.DeckmanAdapterFactory;

public class DeckmanAdapterFactoryImpl implements DeckmanAdapterFactory {
    @Override
    public DeckmanAdapter createDeckmanAdapter() {
        return new DeckmanAdapterImpl();
    }
}
