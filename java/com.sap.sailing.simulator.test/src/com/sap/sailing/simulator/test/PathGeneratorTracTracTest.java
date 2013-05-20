package com.sap.sailing.simulator.test;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.impl.PathGeneratorTracTrac;

public class PathGeneratorTracTracTest {

    private static final double WIND_SCALE = 4.5;

    /** * proxy configuration */
    private static final String LIVE_URI = "tcp://10.18.22.156:1520";

    /** * proxy configuration */
    private static final String STORED_URI = "tcp://10.18.22.156:1521";

    /** * Internationale Deutche Meisterschaft, 49er Race4 */
    private static final String RACE_URL = "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=d1f521fa-ec52-11e0-a523-406186cbf87c";

    private static PathGeneratorTracTrac pathGenerator = null;

    @BeforeClass
    public static void initialize() {

        System.setProperty("mongo.port", "10200");
        System.setProperty("http.proxyHost", "proxy.wdf.sap.corp");
        System.setProperty("http.proxyPort", "8080");

        PathGeneratorTracTracTest.pathGenerator = new PathGeneratorTracTrac(null);
        PathGeneratorTracTracTest.pathGenerator.setEvaluationParameters(RACE_URL, LIVE_URI, STORED_URI, WIND_SCALE);
    }

    @Test
    public void testGetLeg() {

        PathGeneratorTracTracTest.pathGenerator.setSelectionParameters(0, 0);
        Path leg0 = PathGeneratorTracTracTest.pathGenerator.getPath();
        Assert.assertEquals(99, leg0.getPathPoints().size());

        PathGeneratorTracTracTest.pathGenerator.setSelectionParameters(1, 0);
        Path leg1 = PathGeneratorTracTracTest.pathGenerator.getPath();
        Assert.assertEquals(110, leg1.getPathPoints().size());

        PathGeneratorTracTracTest.pathGenerator.setSelectionParameters(2, 0);
        Path leg2 = PathGeneratorTracTracTest.pathGenerator.getPath();
        Assert.assertEquals(151, leg2.getPathPoints().size());

        PathGeneratorTracTracTest.pathGenerator.setSelectionParameters(3, 0);
        Path leg3 = PathGeneratorTracTracTest.pathGenerator.getPath();
        Assert.assertEquals(98, leg3.getPathPoints().size());
    }

    @Test
    public void testGetLegPolyline() {

        Distance maxDistance = new MeterDistance(4.88);

        PathGeneratorTracTracTest.pathGenerator.setSelectionParameters(0, 0);
        Path legPolyline0 = PathGeneratorTracTracTest.pathGenerator.getPathPolyline(maxDistance);
        Assert.assertEquals(10, legPolyline0.getPathPoints().size());

        PathGeneratorTracTracTest.pathGenerator.setSelectionParameters(1, 0);
        Path legPolyline1 = PathGeneratorTracTracTest.pathGenerator.getPathPolyline(maxDistance);
        Assert.assertEquals(7, legPolyline1.getPathPoints().size());

        PathGeneratorTracTracTest.pathGenerator.setSelectionParameters(2, 0);
        Path legPolyline2 = PathGeneratorTracTracTest.pathGenerator.getPathPolyline(maxDistance);
        Assert.assertEquals(8, legPolyline2.getPathPoints().size());

        PathGeneratorTracTracTest.pathGenerator.setSelectionParameters(3, 0);
        Path legPolyline3 = PathGeneratorTracTracTest.pathGenerator.getPathPolyline(maxDistance);
        Assert.assertEquals(7, legPolyline3.getPathPoints().size());
    }

    @Test
    public void testGetLegsNames() {

        List<String> legsNames = PathGeneratorTracTracTest.pathGenerator.getLegsNames();

        Assert.assertEquals(4, legsNames.size());
        Assert.assertEquals("G1 Start-Finish -> G1 Mark 1", legsNames.get(0));
        Assert.assertEquals("G1 Mark 1 -> G1 Mark 4", legsNames.get(1));
        Assert.assertEquals("G1 Mark 4 -> G1 Mark 1", legsNames.get(2));
        Assert.assertEquals("G1 Mark 1 -> G1 Start-Finish", legsNames.get(3));
    }
}
