package com.sap.sailing.nmeaconnector.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class ReadWindVaneDataTest {
    @Test
    public void readWindVaneFile() throws IOException {
        InputStream is = getClass().getResourceAsStream("/windvane.nmea");
        assertNotNull(is);
        is.close();
    }
}
