package com.sap.sailing.domain.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sse.common.Distance;

public class DistanceTest {
    @Test
    public void testDistances() {
        final Distance laserHullLength = BoatClassMasterdata.LASER_INT.getHullLength();
        final Distance laserHullBeam = BoatClassMasterdata.LASER_INT.getHullBeam(); 
        final Position pos = new DegreePosition(35.29989440459763, 139.4847256889834);
        final Position x = new DegreePosition(35.299894404588855, 139.4847718595392);
        final Position y = new DegreePosition(35.29990690516795, 139.48472568898336);
        assertTrue(x.getDistance(pos).scale(-1).add(laserHullLength).getMeters() < 0.1);
        assertTrue(y.getDistance(pos).scale(-1).add(laserHullBeam).getMeters() < 0.1);
    }
}
