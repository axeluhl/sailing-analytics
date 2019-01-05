package com.sap.sailing.simulator.test;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;

import org.junit.Assert;

import org.junit.Test;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.impl.PolarDiagram49;
import com.sap.sailing.simulator.impl.PolarDiagram49Bethwaite;
import com.sap.sailing.simulator.impl.PolarDiagram49ORC;
import com.sap.sailing.simulator.impl.PolarDiagram49STG;
import com.sap.sailing.simulator.impl.PolarDiagram505STG;
import com.sap.sailing.simulator.impl.PolarDiagramCSV;
import com.sap.sse.common.AbstractBearing;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class PolarDiagramCSVTest {
    private static double DELTA_PERCENTAGE = 0.54;
    private static double BEARING_STEP = 10.0;
    private static double VELOCITY_STEP = 1.0;

    private static String POLAR_DIAGRAM_49_CSV_FILE_PATH = "PolarDiagram49.csv";
    private static String POLAR_DIAGRAM_49_BETHWAITE_CSV_FILE_PATH = "PolarDiagram49Bethwaite.csv";
    private static String POLAR_DIAGRAM_49_ORC_CSV_FILE_PATH = "PolarDiagram49ORC.csv";
    private static String POLAR_DIAGRAM_49_STG_CSV_FILE_PATH = "PolarDiagram49STG.csv";
    private static String POLAR_DIAGRAM_505_STG_CSV_FILE_PATH = "PolarDiagram505STG.csv";

    @Test
    public void test_PolarDiagram49_internals() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_CSV_FILE_PATH);

        testInternals(expectedPD, actualPD, "PolarDiagram49");
    }

    @Test
    public void test_PolarDiagram49_getSpeedAtBearing() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_CSV_FILE_PATH);

        testSpeedsAtBearingsOfPolarDiagrams(expectedPD, actualPD, "PolarDiagram49");
    }

    @Test
    public void test_PolarDiagram49_polarDiagramPlot() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_CSV_FILE_PATH);

        testPolarDiagramPlot(expectedPD, actualPD, "PolarDiagram49");
    }

    @Test
    public void test_PolarDiagram49Bethwaite_internals() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49Bethwaite();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_BETHWAITE_CSV_FILE_PATH);

        testInternals(expectedPD, actualPD, "PolarDiagram49Bethwaite");
    }

    @Test
    public void test_PolarDiagram49Bethwaite_getSpeedAtBearing() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49Bethwaite();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_BETHWAITE_CSV_FILE_PATH);

        testSpeedsAtBearingsOfPolarDiagrams(expectedPD, actualPD, "PolarDiagram49Bethwaite");
    }

    @Test
    public void test_PolarDiagram49Bethwaite_polarDiagramPlot() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49Bethwaite();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_BETHWAITE_CSV_FILE_PATH);

        testPolarDiagramPlot(expectedPD, actualPD, "PolarDiagram49Bethwaite");
    }

    @Test
    public void test_PolarDiagram49ORC_internals() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49ORC();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_ORC_CSV_FILE_PATH);

        testInternals(expectedPD, actualPD, "PolarDiagram49ORC");
    }

    @Test
    public void test_PolarDiagram49ORC_getSpeedAtBearing() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49ORC();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_ORC_CSV_FILE_PATH);

        testSpeedsAtBearingsOfPolarDiagrams(expectedPD, actualPD, "PolarDiagram49ORC");
    }

    @Test
    public void test_PolarDiagram49ORC_polarDiagramPlot() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49ORC();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_ORC_CSV_FILE_PATH);

        testPolarDiagramPlot(expectedPD, actualPD, "PolarDiagram49ORC");
    }

    @Test
    public void test_PolarDiagram49STG_internals() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49STG();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_STG_CSV_FILE_PATH);

        testInternals(expectedPD, actualPD, "PolarDiagram49STG");
    }

    @Test
    public void test_PolarDiagram49STG_getSpeedAtBearing() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49STG();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_STG_CSV_FILE_PATH);

        testSpeedsAtBearingsOfPolarDiagrams(expectedPD, actualPD, "PolarDiagram49STG");
    }

    @Test
    public void test_PolarDiagram49STG_polarDiagramPlot() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram49STG();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_49_STG_CSV_FILE_PATH);

        testPolarDiagramPlot(expectedPD, actualPD, "PolarDiagram49STG");
    }

    @Test
    public void test_PolarDiagram505STG_internals() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram505STG();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_505_STG_CSV_FILE_PATH);

        testInternals(expectedPD, actualPD, "PolarDiagram505STG");
    }

    @Test
    public void test_PolarDiagram505STG_getSpeedAtBearing() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram505STG();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_505_STG_CSV_FILE_PATH);

        testSpeedsAtBearingsOfPolarDiagrams(expectedPD, actualPD, "PolarDiagram505STG");
    }

    @Test
    public void test_PolarDiagram505STG_polarDiagramPlot() throws IOException {
        PolarDiagram expectedPD = new PolarDiagram505STG();
        PolarDiagram actualPD = new PolarDiagramCSV(POLAR_DIAGRAM_505_STG_CSV_FILE_PATH);

        testPolarDiagramPlot(expectedPD, actualPD, "PolarDiagram505STG");
    }

    private static void testInternals(PolarDiagram expectedPD, PolarDiagram actualPD, String typeName) {
        testMapOfSpeedsAndMapsOfBearingAndSpeeds(expectedPD.getSpeedTable(), actualPD.getSpeedTable(), "speedTable",
                typeName);

        testMapOfSpeedsAndBearings(expectedPD.getBeatAngles(), actualPD.getBeatAngles(), "beatAngles", typeName);

        testMapOfSpeedsAndBearings(expectedPD.getJibeAngles(), actualPD.getJibeAngles(), "jibeAngles", typeName);

        testMapOfSpeedsAndSpeeds(expectedPD.getBeatSOG(), actualPD.getBeatSOG(), "beatSOG", typeName);

        testMapOfSpeedsAndSpeeds(expectedPD.getJibeSOG(), actualPD.getJibeSOG(), "jibeSOG", typeName);
    }

    private static void testSpeedsAtBearingsOfPolarDiagrams(PolarDiagram expectedPD, PolarDiagram actualPD,
            String typeName) {
        AbstractBearing testBearing = null;
        Speed actualSpeed = null;
        Speed expectedSpeed = null;
        SpeedWithBearing wind = null;
        String debugMessage = "";

        for (double velocity = 6; velocity < 21; velocity += VELOCITY_STEP) {
            for (double degreeBearing = 0; degreeBearing < 360; degreeBearing += BEARING_STEP) {
                wind = new KnotSpeedWithBearingImpl(velocity, new DegreeBearingImpl(degreeBearing));

                actualPD.setWind(wind);
                expectedPD.setWind(wind);

                for (double bearing = 0; bearing < 360; bearing += BEARING_STEP) {
                    debugMessage = " (TypeName = " + typeName + ", Bearing = " + bearing + ", Velocity =  " + velocity
                            + ", DegreeBearing = " + degreeBearing + ")";

                    testBearing = new DegreeBearingImpl(bearing);
                    expectedSpeed = expectedPD.getSpeedAtBearing(testBearing);
                    actualSpeed = actualPD.getSpeedAtBearing(testBearing);

                    testSpeeds(expectedSpeed, actualSpeed, debugMessage);
                }
            }
        }
    }

    private static void testPolarDiagramPlot(PolarDiagram expectedPD, PolarDiagram actualPD, String typeName) {
        for (double bearingStep = 1; bearingStep < 360; bearingStep += BEARING_STEP) {
            testMapOfSpeedsAndMapsOfBearingAndSpeeds(expectedPD.polarDiagramPlot(bearingStep),
                    actualPD.polarDiagramPlot(bearingStep), "" + bearingStep, typeName);
        }
    }

    private static void testMapOfSpeedsAndSpeeds(NavigableMap<Speed, Speed> expectedMap,
            NavigableMap<Speed, Speed> actualMap, String mapName, String polarDiagramType) {
        Assert.assertEquals("Incorrect size for actual map object.", expectedMap.size(), actualMap.size());
        Set<Speed> expectedKeys = expectedMap.keySet();
        String debugMessage = "";

        for (Speed speedKey : expectedKeys) {
            debugMessage = " (mapName = " + mapName + ", polarDiagramType = " + polarDiagramType
                    + ", speedKey.getKnots() = " + speedKey.getKnots() + ")";
            Assert.assertTrue("Actual map does not contain key " + speedKey.getKnots(), actualMap.containsKey(speedKey));

            testSpeeds(expectedMap.get(speedKey), actualMap.get(speedKey), debugMessage);
        }
    }

    private static void testMapOfSpeedsAndBearings(NavigableMap<Speed, Bearing> expectedMap,
            NavigableMap<Speed, Bearing> actualMap, String mapName, String polarDiagramType) {
        Assert.assertEquals("Incorrect size for actual map object.", expectedMap.size(), actualMap.size());
        Set<Speed> expectedKeys = expectedMap.keySet();
        String debugMessage = "";

        for (Speed speedKey : expectedKeys) {
            debugMessage = " (mapName = " + mapName + ", polarDiagramType = " + polarDiagramType
                    + ", speedKey.getKnots() = " + speedKey.getKnots() + ")";
            Assert.assertTrue("Actual map does not contain key " + speedKey.getKnots(), actualMap.containsKey(speedKey));

            testBearing(expectedMap.get(speedKey), actualMap.get(speedKey), debugMessage);
        }
    }

    private static String convertToString(Speed speed) {
        return "[Speed(knots=" + speed.getKnots() + ")]";
    }

    private static String convertToString(Bearing bearing) {
        return "[Bearing(degrees=" + bearing.getDegrees() + ")]";
    }

    private static String convertToString2(NavigableMap<Bearing, Speed> map) {
        String result = "";

        for (Entry<Bearing, Speed> entry : map.entrySet()) {
            result += convertToString(entry.getKey()) + " || " + convertToString(entry.getValue()) + "\r\n";
        }

        return result;
    }

    private static String convertToString(NavigableMap<Speed, NavigableMap<Bearing, Speed>> map) {
        String result = "";

        for (Entry<Speed, NavigableMap<Bearing, Speed>> entry : map.entrySet()) {
            result += " --- " + convertToString(entry.getKey()) + " --- \r\n" + convertToString2(entry.getValue());
        }

        return result;
    }

    private static void testMapOfSpeedsAndMapsOfBearingAndSpeeds(
            NavigableMap<Speed, NavigableMap<Bearing, Speed>> expectedMap,
            NavigableMap<Speed, NavigableMap<Bearing, Speed>> actualMap, String mapName, String polarDiagramType) {
        NavigableMap<Bearing, Speed> expectedPDPValues = null;
        NavigableMap<Bearing, Speed> actualPDPValues = null;
        Set<Speed> expectedPDPKeys = null;
        Set<Bearing> expectedPDPValuesKeys = null;
        String debugMessage = "";

        Assert.assertEquals("Incorrect size for actual Polar Diagram Plot object.\r\n" + "Expected: \r\n"
                + convertToString(expectedMap) + "\r\n" + "Actual: \r\n" + convertToString(actualMap),
                expectedMap.size(), actualMap.size());

        expectedPDPKeys = expectedMap.keySet();

        for (Speed speedKey : expectedPDPKeys) {
            debugMessage = "(speedKey = " + speedKey.getKnots() + ", typeName = " + polarDiagramType + ", mapName = "
                    + mapName + ")";

            Assert.assertTrue("\r\nActual Polar Diagram Plot does not contain key! \r\n" + debugMessage + " \r\n",
                    actualMap.containsKey(speedKey));

            expectedPDPValues = expectedMap.get(speedKey);
            actualPDPValues = actualMap.get(speedKey);

            Assert.assertEquals("\r\nIncorrect size for actual Polar Diagram Plot Values object! \r\n" + debugMessage
                    + " \r\n", expectedPDPValues.size(), actualPDPValues.size());

            expectedPDPValuesKeys = expectedPDPValues.keySet();

            for (Bearing bearingKey : expectedPDPValuesKeys) {
                Assert.assertTrue("\r\nActual Polar Diagram Plot Values does not contain key: " + bearingKey + "\r\n"
                        + debugMessage + " \r\n", actualPDPValues.containsKey(bearingKey));

                debugMessage = " (typeName = " + polarDiagramType + ", bearingKey = " + bearingKey + ", speedKey = "
                        + speedKey.getKnots() + ")";
                testSpeeds(expectedPDPValues.get(bearingKey), actualPDPValues.get(bearingKey), debugMessage);
            }
        }
    }

    private static void testBearing(Bearing expectedBearing, Bearing actualBearing, String debugMessage) {
        if (expectedBearing == null) {
            Assert.assertNull("Actual bearing value must be NULL!" + debugMessage, actualBearing);
        } else {
            Assert.assertNotNull("Actual bearing value must not be NULL!" + debugMessage, actualBearing);

            Assert.assertEquals("Beaufort values do not match!" + debugMessage, expectedBearing.getDegrees(),
                    actualBearing.getDegrees(), expectedBearing.getDegrees() * DELTA_PERCENTAGE);

            Assert.assertEquals("KilometersPerHour values do not match!" + debugMessage, expectedBearing.getRadians(),
                    actualBearing.getRadians(), expectedBearing.getRadians() * DELTA_PERCENTAGE);
        }
    }

    private static void testSpeeds(Speed expectedSpeed, Speed actualSpeed, String debugMessage) {
        if (expectedSpeed == null) {
            Assert.assertNull("Actual speed value must be NULL!" + debugMessage, actualSpeed);
        } else {
            Assert.assertNotNull("Actual speed value must not be NULL!" + debugMessage, actualSpeed);

            Assert.assertEquals("Beaufort values do not match!" + debugMessage, expectedSpeed.getBeaufort(),
                    actualSpeed.getBeaufort(), expectedSpeed.getBeaufort() * DELTA_PERCENTAGE);

            Assert.assertEquals("KilometersPerHour values do not match!" + debugMessage,
                    expectedSpeed.getKilometersPerHour(), actualSpeed.getKilometersPerHour(),
                    expectedSpeed.getKilometersPerHour() * DELTA_PERCENTAGE);

            Assert.assertEquals("Knots values do not match!" + debugMessage, expectedSpeed.getKnots(),
                    actualSpeed.getKnots(), expectedSpeed.getKnots() * DELTA_PERCENTAGE);

            Assert.assertEquals("MetersPerSecond values do not match!" + debugMessage,
                    expectedSpeed.getMetersPerSecond(), actualSpeed.getMetersPerSecond(),
                    expectedSpeed.getMetersPerSecond() * DELTA_PERCENTAGE);
        }
    }
}
