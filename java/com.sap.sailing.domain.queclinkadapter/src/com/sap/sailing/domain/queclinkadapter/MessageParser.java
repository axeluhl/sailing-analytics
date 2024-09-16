package com.sap.sailing.domain.queclinkadapter;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import com.sap.sailing.domain.queclinkadapter.impl.QueclinkStreamParserImpl;

public interface MessageParser {
    MessageParser INSTANCE = new QueclinkStreamParserImpl();

    Message parse(String messageIncludingTailCharacter) throws ParseException;

    /**
     * Reads from the {@code reader} until EOF is reached and produces {@link Message}s based on {@link #parse(String)}
     * and the splitting of the input based on the termination character "$" (dollar sign).
     */
    Iterable<Message> parse(Reader reader) throws ParseException, IOException;

}
