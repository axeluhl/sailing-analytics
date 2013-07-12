package com.sap.sailing.domain.tractracadapter.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.Event;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.Race;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.Route;

public class ClientParamsPHP49erFX2013Test extends AbstractClientParamsPHPTest {
    
    @Before
    public void setUp() throws IOException {
        setUp("/event_20130703_erEuropean-49FX_Final_R2.txt");
    }
    
    @Test
    public void testRead() throws IOException {
        assertEquals("event_20130703_erEuropean", clientParams.getEvent().getDB());
    }

    @Test
    public void testRouteFromUUID() {
        Route route = clientParams.new Route(UUID.fromString("01aeaae6-e680-11e2-a60b-60a44ce903c3"));
        assertEquals("49FX Final R2-A-L2", route.getDescription());
    }
    
    @Test
    public void testRaceDefaultRoute() {
        assertEquals("49FX Final R2-A-L2", clientParams.getRace().getDefaultRoute().getDescription());
    }
    
    @Test
    public void testRaceDefaultRouteLength() {
        assertEquals(5, Util.size(clientParams.getRace().getDefaultRoute().getControlPoints()));
    }
    
    @Test
    public void testRouteControlPointRaceContents() {
        Race race = clientParams.getRace();
        assertEquals("49FX Final R2", race.getName());
        assertEquals("SIDELINE1=SL-S\nSIDELINE2=SL-P", race.getMetadata());
    }

    @Test
    public void testRouteControlPointEventContents() {
        Event event = clientParams.getEvent();
        assertEquals("49er European Championship 2013", event.getName());
        assertEquals("event_20130703_erEuropean", event.getDB());
    }
}
