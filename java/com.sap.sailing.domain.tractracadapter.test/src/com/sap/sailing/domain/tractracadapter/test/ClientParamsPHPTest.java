package com.sap.sailing.domain.tractracadapter.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;

import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP;

public class ClientParamsPHPTest {
    @Test
    public void testRead() throws IOException {
        InputStream is = getClass().getResourceAsStream("/clientparamsLahainaTest3.php");
        Reader r = new InputStreamReader(is);
        ClientParamsPHP clientParams = new ClientParamsPHP(r);
        assertEquals("event_20110505_SailingTea", clientParams.getEventDB());
    }
}
