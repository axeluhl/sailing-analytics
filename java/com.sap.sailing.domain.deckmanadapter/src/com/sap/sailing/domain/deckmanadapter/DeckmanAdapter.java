package com.sap.sailing.domain.deckmanadapter;

import java.io.IOException;
import java.io.Reader;

public interface DeckmanAdapter {
    LogFile parseLogFile(Reader r) throws IOException;
}
