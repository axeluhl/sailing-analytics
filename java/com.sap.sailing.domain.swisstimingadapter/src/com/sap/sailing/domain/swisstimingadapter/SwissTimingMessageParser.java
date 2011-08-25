package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.swisstimingadapter.impl.SwissTimingMessageParserImpl;

public interface SwissTimingMessageParser {
    SwissTimingMessageParser INSTANCE = new SwissTimingMessageParserImpl();
    
    SwissTimingMessage parse(byte[] message) throws SwissTimingFormatException;

    SwissTimingMessage parse(byte[] message, int offset) throws SwissTimingFormatException;
}
