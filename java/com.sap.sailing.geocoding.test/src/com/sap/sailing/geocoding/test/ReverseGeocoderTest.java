package com.sap.sailing.geocoding.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.PlacemarkImpl;
import com.sap.sailing.geocoding.ReverseGeocoder;
import com.sap.sailing.geocoding.impl.ReverseGeocoderImpl;

import org.junit.Assert;

public class ReverseGeocoderTest {
    private ReverseGeocoder geocoder;
    private static final Placemark KIEL = new PlacemarkImpl("Kiel", "DE", new DegreePosition(54.32132926107913, 10.1348876953125), 232758);
    private static final Position KIEL_POSITION = new DegreePosition(54.3231063453431, 10.12265682220459);
    
    @Before
    public void setUp() {
        geocoder = new ReverseGeocoderImpl(); // ensure we don't see any caching effects across test case executions
    }
    
    @Test
    public void getPlacemarkSimpleTest() {
        //Simple Test in Kiel center to check the connection and the parsing from JSONObject to Placemark
        try {
            Placemark kielReversed = geocoder.getPlacemarkNearest(KIEL_POSITION);
            Assert.assertEquals(KIEL.getName(), kielReversed.getName());
            Assert.assertEquals(KIEL.getCountryCode(), kielReversed.getCountryCode());
            Assert.assertEquals(KIEL.getPosition().getLatDeg(), kielReversed.getPosition().getLatDeg(), 0.0001);
            Assert.assertEquals(KIEL.getPosition().getLngDeg(), kielReversed.getPosition().getLngDeg(), 0.0001);
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
        Position abroad = new DegreePosition(54.429758, 10.289335);
        Placemark firstByDistance = new PlacemarkImpl("Wendtorf", "DE", new DegreePosition(54.41212, 10.28952), 1139);
        try {
            Placemark p = geocoder.getPlacemarkLast(abroad, 20, new Placemark.ByPopulation());
            Assert.assertEquals(KIEL.getName(), p.getName());
            Assert.assertEquals(KIEL.getCountryCode(), p.getCountryCode());
            Assert.assertEquals(KIEL.getPosition().getLatDeg(), p.getPosition().getLatDeg(), 0.0001);
            Assert.assertEquals(KIEL.getPosition().getLngDeg(), p.getPosition().getLngDeg(), 0.0001);
            
            p = geocoder.getPlacemarkFirst(abroad, 20, new Placemark.ByDistance(abroad));
            Assert.assertEquals(firstByDistance.getName(), p.getName());
            Assert.assertEquals(firstByDistance.getCountryCode(), p.getCountryCode());
            Assert.assertEquals(firstByDistance.getPosition().getLatDeg(), p.getPosition().getLatDeg(), 0.005);
            Assert.assertEquals(firstByDistance.getPosition().getLngDeg(), p.getPosition().getLngDeg(), 0.012);
            
            p = geocoder.getPlacemarkLast(abroad, 20, new Placemark.ByPopulationDistanceRatio(abroad));
            Assert.assertEquals(KIEL.getName(), p.getName());
            Assert.assertEquals(KIEL.getCountryCode(), p.getCountryCode());
            Assert.assertEquals(KIEL.getPosition().getLatDeg(), p.getPosition().getLatDeg(), 0.005);
            Assert.assertEquals(KIEL.getPosition().getLngDeg(), p.getPosition().getLngDeg(), 0.005);
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
