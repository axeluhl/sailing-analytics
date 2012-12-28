package com.sap.sailing.domain.tractracadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.ControlPoint;
import com.sap.sailing.domain.tractracadapter.impl.ClientParamsPHP.Route;

public class ClientParamsPHPArenalTest extends AbstractClientParamsPHPTest {
    
    @Before
    public void setUp() throws IOException {
        setUp("/clientparamsArenalEmptyCourse.php");
    }
    
    @Test
    public void testRouteControlPointMarkPositions() {
        Route route = clientParams.getRaceDefaultRoute();
        Iterator<ControlPoint> controlPointIter = route.getControlPoints().iterator();
        ControlPoint cp1 = controlPointIter.next();
        assertNotNull(cp1);
        ControlPoint cp2 = controlPointIter.next();
        assertNotNull(cp2);
        ControlPoint cp3 = controlPointIter.next();
        assertNotNull(cp3);
        assertEquals("P1.Color=Green\nP1.Type=Conical", cp1.getMetadata());
    }
}
