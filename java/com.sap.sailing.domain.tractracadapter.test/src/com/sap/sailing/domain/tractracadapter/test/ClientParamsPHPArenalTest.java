package com.sap.sailing.domain.tractracadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.tractracadapter.TracTracControlPoint;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.ControlPoint;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.Route;
import com.sap.sailing.domain.tractracadapter.impl.ControlPointAdapter;
import com.sap.sse.common.Util;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.setup.KeyValue;

public class ClientParamsPHPArenalTest extends AbstractClientParamsPHPTest {
    
    @Before
    public void setUp() throws IOException {
        setUp("/clientparamsArenalEmptyCourse.php");
    }
    
    @Test
    public void testRouteControlPointMarkPositions() {
        Route route = clientParams.getRace().getDefaultRoute();
        Iterator<ControlPoint> controlPointIter = route.getControlPoints().iterator();
        ControlPoint cp1 = controlPointIter.next();
        assertNotNull(cp1);
        ControlPoint cp2 = controlPointIter.next();
        assertNotNull(cp2);
        ControlPoint cp3 = controlPointIter.next();
        assertNotNull(cp3);
        assertEquals("P1.Color=Green\nP1.Type=Conical", cp1.getMetadata());
    }
    
    @Test
    public void testReadWithTracTracKeyValue() throws MalformedURLException, IOException {
        Event event = KeyValue.setup(getClass().getResource("/clientparamsArenalEmptyCourse.php"));
        com.tractrac.clientmodule.ControlPoint controlPoint2 = event.getControlPointById(UUID.fromString("59d18d06-6ec7-11e1-b933-406186cbf87c"));
        assertEquals("P1.Color=Green\nP1.Type=Conical", controlPoint2.getMetadata().getText());
    }

    @Test
    public void compareReadWithTracTracKeyValueWithLocalClientParamsPHP() throws MalformedURLException, IOException {
        Event event = KeyValue.setup(getClass().getResource("/clientparamsArenalEmptyCourse.php"));
        assertEquals(3, Util.size(clientParams.getRace().getDefaultRoute().getControlPoints()));
        for (TracTracControlPoint ttcp : clientParams.getRace().getDefaultRoute().getControlPoints()) {
            ControlPointAdapter eventControlPoint = new ControlPointAdapter(event.getControlPointById(ttcp.getId()));
            assertEquals(ttcp, eventControlPoint); // assert that comparison by UUID matches
            assertEquals(eventControlPoint.getName(), ttcp.getName());
            assertEquals(eventControlPoint.getHasTwoPoints(), ttcp.getHasTwoPoints());
            assertEquals(eventControlPoint.getMark1Position(), ttcp.getMark1Position());
            assertEquals(eventControlPoint.getMark2Position(), ttcp.getMark2Position());
        }
    }
}
