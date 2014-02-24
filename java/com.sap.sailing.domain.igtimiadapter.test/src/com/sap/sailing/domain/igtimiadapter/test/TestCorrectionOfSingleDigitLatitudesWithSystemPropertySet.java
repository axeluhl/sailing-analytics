package com.sap.sailing.domain.igtimiadapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.igtimiadapter.datatypes.GpsLatLong;
import com.sap.sailing.domain.igtimiadapter.impl.FixFactory;

/**
 * See bug 1794: http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=1794. Here is an offending string:
 * <p>
 * {"DD-EE-AAHE":{"1":{"t":[1392881700000,1392881732500,1392881772500,1392881812500,1392881852500,1392881892500,
 * 1392881932500
 * ,1392881972500,1392882012500,1392882052500,1392882092500,1392882132500,1392882172500,1392882212500,1392882252500
 * ,1392882292500
 * ,1392882332500,1392882372500,1392882412500,1392882452500,1392882492500,1392882532500,1392882572500,1392882612500
 * ,1392882652500
 * ,1392882692500,1392882732500,1392882772500,1392882812500,1392882852500,1392882892500,1392882932500,1392882972500
 * ,1392883012500
 * ,1392883052500,1392883092500,1392883132500,1392883172500,1392883212500,1392883252500,1392883292500,1392883332500
 * ,1392883359750
 * ,1392883360000,1392883360500,1392883400500,1392883462000],"1":[103.85452,103.854519,103.854514,103.854511333333
 * ,103.8545015
 * ,103.854510666667,103.854509833333,103.854512333333,103.854507833333,103.854507166667,103.854510833333,103.854509
 * ,103.854512
 * ,103.854511166667,103.854514333333,103.854517833333,103.854509833333,103.8545115,103.854516833333,103.854520833333
 * ,103.854525666667
 * ,103.854527166667,103.854529333333,103.854525166667,103.854525,103.854524333333,103.854520333333,103.854518166667
 * ,103.8545165
 * ,103.854516666667,103.854511666667,103.854515,103.854514666667,103.8545145,103.854514,103.854517,103.854527
 * ,103.854531166667
 * ,103.854531666667,103.8545335,103.8545335,103.854534,103.854534666667,103.854534833333,103.854537833333
 * ,103.854537166667
 * ,103.854540166667],"2":[11.1155463333333,11.1155478333333,11.1155546666667,11.1155586666667,11.1155595
 * ,11.1155543333333
 * ,11.115552,11.1155461666667,11.1155483333333,11.115549,11.1155618333333,11.1155745,11.1155771666667,11.1155746666667
 * ,11.1155708333333
 * ,11.1155701666667,11.1155716666667,11.1155733333333,11.1155781666667,11.1155795,11.1155785,11.1155735
 * ,11.1155711666667
 * ,11.1155741666667,11.1155763333333,11.1155748333333,11.1155756666667,11.1155768333333,11.1155791666667
 * ,11.1155798333333
 * ,11.1155795,11.1155746666667,11.1155781666667,11.1155813333333,11.1155773333333,11.115566,11.1155595,
 * 11.1155568333333,
 * 11.1155596666667,11.1155646666667,11.1155635,11.115557,11.1155571666667,11.1155571666667,11.1155565,11.115568
 * ,11.1155695]}}}
 * </p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class TestCorrectionOfSingleDigitLatitudesWithSystemPropertySet {
    @Before
    public void setSystemPropertyToEnableWorkaround() {
        System.setProperty(GpsLatLong.IGTIMI_ENABLE_WORKAROUND_FOR_SINGLE_DIGIT_LATITUDES, "true");
    }
    
    @After
    public void unsetSystemPropertyToDisableWorkaround() {
        System.clearProperty(GpsLatLong.IGTIMI_ENABLE_WORKAROUND_FOR_SINGLE_DIGIT_LATITUDES);
        assertNull(System.getProperty(GpsLatLong.IGTIMI_ENABLE_WORKAROUND_FOR_SINGLE_DIGIT_LATITUDES));
    }
    
    @Test
    public void testIntRounding() {
        assertEquals(1, (int) 1.1);
        assertEquals(1, (int) 1.0);
        assertEquals(1, (int) 1.9);
        assertEquals(-1, (int) -1.1);
        assertEquals(-1, (int) -1.0);
        assertEquals(-1, (int) -1.9);
    }
    
    @Test
    public void testSimpleLatLongConversionForTwoNorthLatDigitsWithTwoDigitMinutes() throws ParseException {
        double latitude = 11.0 + 17.0/60.0; // with two-digit minutes it cannot have been an incorrect parse
        JSONObject json = (JSONObject) new JSONParser().parse("{ \"AA-AA-AAA-5\":{ \"1\":{ \"t\":[ 1360618858970 ], \"1\":[ 172.04 ], \"2\":[ "+latitude+" ] } } }");
        GpsLatLong pos = (GpsLatLong) new FixFactory().createFixes(json).iterator().next();
        assertEquals(latitude, pos.getPosition().getLatDeg(), 0.0000001);
        assertEquals(172.04, pos.getPosition().getLngDeg(), 0.0000001);
    }

    @Test
    public void testSimpleLatLongConversionForTwoSouthLatDigitsWithTwoDigitMinutes() throws ParseException {
        double latitude = -11.0 - 17.0/60.0; // with two-digit minutes it cannot have been an incorrect parse
        JSONObject json = (JSONObject) new JSONParser().parse("{ \"AA-AA-AAA-5\":{ \"1\":{ \"t\":[ 1360618858970 ], \"1\":[ 172.04 ], \"2\":[ "+latitude+" ] } } }");
        GpsLatLong pos = (GpsLatLong) new FixFactory().createFixes(json).iterator().next();
        assertEquals(latitude, pos.getPosition().getLatDeg(), 0.0000001);
        assertEquals(172.04, pos.getPosition().getLngDeg(), 0.0000001);
    }

    @Test
    public void testSimpleLatLongConversionForTwoLatDigitsWithOneDigitMinutesFromOriginalSingaporeData() throws ParseException {
        // NMEA latitude was "116.9341N", meaning 1° 16.9341' North
        final double incorrectLatitude = 11. + 6.9341/60.;
        final double correctLatitude = 1.0 + 16.9341/60.;
        JSONObject json = (JSONObject) new JSONParser().parse("{\"DD-EE-AAHE\":{\"1\":{\"t\":[1392881700000], \"1\":[ 103.85452 ], \"2\":[ "+incorrectLatitude+" ] } } }");
        GpsLatLong pos = (GpsLatLong) new FixFactory().createFixes(json).iterator().next();
        assertEquals(correctLatitude, pos.getPosition().getLatDeg(), 0.0000001);
        assertEquals(103.85452, pos.getPosition().getLngDeg(), 0.0000001);
    }

    @Test
    public void testSimpleLatLongConversionForTwoSouthLatDigitsWithOneDigitMinutesFromOriginalSingaporeData() throws ParseException {
        // NMEA latitude was "116.9341N", meaning 1° 16.9341' North
        final double incorrectLatitude = -11. - 6.9341/60.;
        final double correctLatitude = -1.0 - 16.9341/60.;
        JSONObject json = (JSONObject) new JSONParser().parse("{\"DD-EE-AAHE\":{\"1\":{\"t\":[1392881700000], \"1\":[ 103.85452 ], \"2\":[ "+incorrectLatitude+" ] } } }");
        GpsLatLong pos = (GpsLatLong) new FixFactory().createFixes(json).iterator().next();
        assertEquals(correctLatitude, pos.getPosition().getLatDeg(), 0.0000001);
        assertEquals(103.85452, pos.getPosition().getLngDeg(), 0.0000001);
    }
}
