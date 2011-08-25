package com.sap.sailing.domain.swisstimingadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.sap.sailing.domain.swisstimingadapter.SwissTimingFormatException;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingMessageParser;

public class ParseSingleSwissTimingPacketFromFile {
    @Test
    public void readSinglePacketFromFile() throws IOException, SwissTimingFormatException {
        byte[] message = new byte[65536];
        InputStream is = getClass().getResourceAsStream("/SwissTimingExampleTrack.bin");
        is.read(message);
        SwissTimingMessage swissTimingMessage = SwissTimingMessageParser.INSTANCE.parse(message);
        assertNotNull(swissTimingMessage);
        int lat = ((0x1e<<8 | 0x2c)<<8 | 0x31)<<8 | 0x14;
        int lng = ((0xfe << 8 | 0x97)<<8 | 0x1d)<<8 | 0x73;
        assertEquals((double) lat / 10000000., swissTimingMessage.getPosition().getLatDeg(), 0.0000000001);
        assertEquals((double) lng / 10000000., swissTimingMessage.getPosition().getLngDeg(), 0.0000000001);
    }
}
