package com.sap.sailing.simulator.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.Util.Quadruple;
import com.sap.sailing.simulator.impl.ConfigurationManager;

public class ConfigurationManagerTest {

    private final List<Quadruple<String, Double, String, Integer>> _boatClassesInfo = new ArrayList<Quadruple<String, Double, String, Integer>>();

    private final List<Quadruple<String, String, String, Integer>> _racesInfo = new ArrayList<Quadruple<String, String, String, Integer>>();

    @Before
    public void initialize() {
        this._boatClassesInfo.add(new Quadruple<String, Double, String, Integer>("49er", 4.995, "resources/PolarDiagram49.csv", 0));
        this._boatClassesInfo.add(new Quadruple<String, Double, String, Integer>("49er Bethwaite", 4.876, "resources/PolarDiagram49Bethwaite.csv", 1));
        this._boatClassesInfo.add(new Quadruple<String, Double, String, Integer>("49er ORC", 4.995, "resources/PolarDiagram49ORC.csv", 2));
        this._boatClassesInfo.add(new Quadruple<String, Double, String, Integer>("49er STG", 4.876, "resources/PolarDiagram49STG.csv", 3));
        this._boatClassesInfo.add(new Quadruple<String, Double, String, Integer>("505 STG", 5.05, "resources/PolarDiagram505STG.csv", 4));

        this._racesInfo
                .add(new Quadruple<String, String, String, Integer>(
                        "Internationale Deutche Meisterschaft - 49er Race4",
                        "49er STG",
                        "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=d1f521fa-ec52-11e0-a523-406186cbf87c",
                        0));
        this._racesInfo
                .add(new Quadruple<String, String, String, Integer>(
                        "Internationale Deutche Meisterschaft - 49er Race5",
                        "49er STG",
                        "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=eb06795a-ec52-11e0-a523-406186cbf87c",
                        1));
        this._racesInfo
                .add(new Quadruple<String, String, String, Integer>(
                        "Internationale Deutche Meisterschaft - Star Race4",
                        "49er STG",
                        "http://germanmaster.traclive.dk/events/event_20110929_Internatio/clientparams.php?event=event_20110929_Internatio&race=6bb0829e-ec44-11e0-a523-406186cbf87c",
                        2));
        this._racesInfo
                .add(new Quadruple<String, String, String, Integer>(
                        "Kieler Woche 2012 - 49er Yellow - Race 1",
                        "49er STG",
                        "http://germanmaster.traclive.dk/events/event_20120615_KielerWoch/clientparams.php?event=event_20120615_KielerWoch&race=0b5969cc-b789-11e1-a845-406186cbf87c",
                        3));
    }

    @Test
    public void test_getPolarDiagramFileLocation() {
        Assert.assertEquals("resources/PolarDiagram49.csv", ConfigurationManager.INSTANCE
                .getPolarDiagramFileLocation(0));
        Assert.assertEquals("resources/PolarDiagram49Bethwaite.csv", ConfigurationManager.INSTANCE
                .getPolarDiagramFileLocation(1));
        Assert.assertEquals("resources/PolarDiagram49ORC.csv", ConfigurationManager.INSTANCE
                .getPolarDiagramFileLocation(2));
        Assert.assertEquals("resources/PolarDiagram49STG.csv", ConfigurationManager.INSTANCE
                .getPolarDiagramFileLocation(3));
        Assert.assertEquals("resources/PolarDiagram505STG.csv", ConfigurationManager.INSTANCE
                .getPolarDiagramFileLocation(4));
    }

    @Test
    public void test_getBoatClassesInfoCount() {
        Assert.assertEquals(5, ConfigurationManager.INSTANCE.getBoatClassesInfoCount());
    }

    @Test
    public void test_getBoatClassesInfo() {
        int index = 0;
        for (final Quadruple<String, Double, String, Integer> tuple : ConfigurationManager.INSTANCE.getBoatClassesInfo()) {
            Assert.assertEquals(this._boatClassesInfo.get(index).getA(), tuple.getA());
            Assert.assertEquals(this._boatClassesInfo.get(index).getB(), tuple.getB());
            Assert.assertEquals(this._boatClassesInfo.get(index).getC(), tuple.getC());
            index++;
        }
    }

    @Test
    public void test_getRacesInfo() {
        int index = 0;
        for (final Quadruple<String, String, String, Integer> tuple : ConfigurationManager.INSTANCE.getRacesInfo()) {
            Assert.assertEquals(this._racesInfo.get(index).getA(), tuple.getA());
            Assert.assertEquals(this._racesInfo.get(index).getB(), tuple.getB());
            Assert.assertEquals(this._racesInfo.get(index).getC(), tuple.getC());
            index++;
        }
    }
}
