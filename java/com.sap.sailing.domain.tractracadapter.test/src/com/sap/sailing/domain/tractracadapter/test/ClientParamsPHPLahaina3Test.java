package com.sap.sailing.domain.tractracadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.ControlPoint;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.Mark;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.Route;
import com.sap.sse.common.Util;

public class ClientParamsPHPLahaina3Test extends AbstractClientParamsPHPTest {
    
    @Before
    public void setUp() throws IOException {
        setUp("/clientparamsLahainaTest3.php");
    }
    
    @Test
    public void testRead() throws IOException {
        assertEquals("event_20110505_SailingTea", clientParams.getEvent().getDB());
    }

    @Test
    public void testRouteFromUUID() {
        Route route = clientParams.new Route(UUID.fromString("82be60fa-58cd-11e1-b933-406186cbf87c"));
        assertEquals("Lahaina-test3", route.getDescription());
    }
    
    @Test
    public void testRaceDefaultRoute() {
        assertEquals("Lahaina-test3", clientParams.getRace().getDefaultRoute().getDescription());
    }
    
    @Test
    public void testRaceDefaultRouteLength() {
        assertEquals(3, Util.size(clientParams.getRace().getDefaultRoute().getControlPoints()));
    }
    
    @Test
    public void testRouteControlPointMarkPositions() {
        Route route = clientParams.getRace().getDefaultRoute();
        Iterator<ControlPoint> controlPointIter = route.getControlPoints().iterator();
        ControlPoint cp1 = controlPointIter.next();
        ControlPoint cp2 = controlPointIter.next();
        ControlPoint cp3 = controlPointIter.next();
        assertEquals(cp1, cp3);
        assertEquals("Lahaina-Gate", cp1.getName());
        assertEquals("Lahaina-Windward", cp2.getName());
        final Iterator<Mark> cp1MarkIter = cp1.getMarks().iterator();
        Mark cp1Mark1 = cp1MarkIter.next();
        assertTrue(cp1MarkIter.hasNext());
        Mark cp1Mark2 = cp1MarkIter.next();
        assertEquals(20.884960, cp1Mark1.getPosition().getLatDeg(), 0.000000001);
        assertEquals(-156.686417, cp1Mark1.getPosition().getLngDeg(), 0.000000001);
        assertEquals(20.884958, cp1Mark2.getPosition().getLatDeg(), 0.000000001);
        assertEquals(-156.686402, cp1Mark2.getPosition().getLngDeg(), 0.000000001);
    }
}
