package com.sap.sailing.geocoding.test;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.domain.common.DegreePosition;
import com.sap.sailing.geocoding.Placemark;
import com.sap.sailing.geocoding.impl.PlacemarkImpl;

public class PlacemarkTest {

    @Test
    public void placemarkEqualsTest() {
        Placemark p1 = new PlacemarkImpl("Kiel", "DE", new DegreePosition(55, 10), "P");
        Placemark p2 = new PlacemarkImpl("Kiel", "DE", new DegreePosition(55, 10), "P");
        Assert.assertEquals(p1, p2);
    }
    
}
