package com.sap.sailing.domain.deckmanadapter;

import com.sap.sailing.domain.deckmanadapter.impl.DeckmanAdapterFactoryImpl;

public interface DeckmanAdapterFactory {
    DeckmanAdapterFactory INSTANCE = new DeckmanAdapterFactoryImpl();
    
    DeckmanAdapter createDeckmanAdapter();
}
