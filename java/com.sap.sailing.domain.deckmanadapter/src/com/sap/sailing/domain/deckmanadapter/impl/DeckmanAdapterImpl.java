package com.sap.sailing.domain.deckmanadapter.impl;

import java.io.IOException;
import java.io.Reader;

import com.sap.sailing.domain.deckmanadapter.DeckmanAdapter;
import com.sap.sailing.domain.deckmanadapter.LogFile;

public class DeckmanAdapterImpl implements DeckmanAdapter {
    @Override
    public LogFile parseLogFile(Reader r) throws IOException {
        return new LogFileImpl(r);
    }
}
