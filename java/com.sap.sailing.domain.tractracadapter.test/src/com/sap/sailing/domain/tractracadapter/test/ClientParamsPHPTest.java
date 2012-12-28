package com.sap.sailing.domain.tractracadapter.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.Route;

public class ClientParamsPHPTest {
    private ClientParamsPHP clientParams;
    
    @Before
    public void setUp() throws IOException {
        InputStream is = getClass().getResourceAsStream("/clientparamsLahainaTest3.php");
        Reader r = new InputStreamReader(is);
        clientParams = new ClientParamsPHP(r);
    }
    
    @Test
    public void testRead() throws IOException {
        assertEquals("event_20110505_SailingTea", clientParams.getEventDB());
    }

    @Test
    public void testRouteFromUUID() {
        Route route = clientParams.new Route(UUID.fromString("82be60fa-58cd-11e1-b933-406186cbf87c"));
        assertEquals("Lahaina-test3", route.getDescription());
    }
}
