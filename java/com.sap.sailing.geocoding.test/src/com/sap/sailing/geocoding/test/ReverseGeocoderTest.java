package com.sap.sailing.geocoding.test;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.geocoding.Placemark;
import com.sap.sailing.geocoding.ReverseGeocoder;
import com.sap.sailing.geocoding.impl.PlacemarkImpl;

public class ReverseGeocoderTest {
    
    ReverseGeocoder geocoder = ReverseGeocoder.INSTANCE;
    
    @Test
    public void getPlacemarkSimpleTest() {
        //Simple Test in Kiel center to check the connection and the parsing from JSONObject to Placemark
        double latSmplKiel = 54.3231063453431;
        double lngSmplKiel = 10.12265682220459;
        Placemark kielCorrect = new PlacemarkImpl("Kiel", "DE", 54.32132926107913, 10.1348876953125, "P", 232758);
        
        try {
            Placemark kielReversed = geocoder.getPlacemark(latSmplKiel, lngSmplKiel);
            Assert.assertEquals(kielCorrect, kielReversed);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } catch (ParseException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test
    public void getPlacemarkNearSimpleTest() {
        double latDeg = 54.3231063453431;
        double lngDeg = 10.12265682220459;
        
        try {
            List<Placemark> placemarks = geocoder.getPlacemarkNear(latDeg, lngDeg, 20);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
