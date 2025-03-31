package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.udpconnector.UDPMessageParser;

public interface SwissTimingMessageParser extends UDPMessageParser<SwissTimingMessage> {
    SwissTimingMessage parse(byte[] message) throws SwissTimingFormatException;

    SwissTimingMessage parse(byte[] message, int offset, int length) throws SwissTimingFormatException;
}
