package com.sap.sailing.geocoding.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.PlacemarkImpl;
import com.sap.sailing.domain.common.impl.SerializablePositionImpl;
import com.sap.sailing.geocoding.ReverseGeocoder;

public class ReverseGeocoderTest {
    private ReverseGeocoder geocoder = ReverseGeocoder.INSTANCE;
    private static final Placemark KIEL = new PlacemarkImpl("Kiel", "DE", new SerializablePositionImpl(54.32132926107913, 10.1348876953125), 232758);
    private static final Position KIEL_POSITION = new DegreePosition(54.3231063453431, 10.12265682220459);
    
    @Test
    public void getPlacemarkSimpleTest() {
        //Simple Test in Kiel center to check the connection and the parsing from JSONObject to Placemark
        try {
            Placemark kielReversed = geocoder.getPlacemarkNearest(KIEL_POSITION);
            Assert.assertEquals(KIEL, kielReversed);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test
    public void getPlacemarkNearSimpleTest() {
        try {
            List<Placemark> placemarks = geocoder.getPlacemarksNear(KIEL_POSITION, 20);
            assertNotNull(placemarks);
            Assert.assertFalse(placemarks.isEmpty());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test
    public void getPlacemarkBestTest() {
        Position abroad = new DegreePosition(54.43334, 10.299999);
        Placemark firstByDistance = new PlacemarkImpl("Wendtorf", "DE", new SerializablePositionImpl(54.4166667, 10.3), 1139);
        
        try {
            Placemark p = geocoder.getPlacemarkLast(abroad, 20, new Placemark.ByPopulation());
            Assert.assertEquals(KIEL, p);
            
            p = geocoder.getPlacemarkFirst(abroad, 20, new Placemark.ByDistance(abroad));
            Assert.assertEquals(firstByDistance, p);
            
            p = geocoder.getPlacemarkLast(abroad, 20, new Placemark.ByPopulationDistanceRatio(abroad));
            Assert.assertEquals(KIEL, p);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test
    public void getPlacemarkNearWithOffshorePosition() {
        Position offshore = new DegreePosition(75.16330024622059, -0.087890625);
        long radius = 300; 
        try {
            List<Placemark> placemarks = geocoder.getPlacemarksNear(offshore, radius);
            Assert.assertNull(placemarks);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }
    }
}
