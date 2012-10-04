package com.sap.sailing.simulator.test;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.simulator.impl.ConfigurationManager;
import com.sap.sailing.simulator.impl.Tuple;

public class ConfigurationManagerTest {
    private final ArrayList<Tuple<String, Double, String>> _boatClassesInfo = new ArrayList<Tuple<String, Double, String>>();

    @Before
    public void initialize() {
        this._boatClassesInfo.add(new Tuple<String, Double, String>("49er", 4.995, "resources/PolarDiagram49.csv"));
        this._boatClassesInfo.add(new Tuple<String, Double, String>("49er Bethwaite", 4.876,
                "resources/PolarDiagram49Bethwaite.csv"));
        this._boatClassesInfo.add(new Tuple<String, Double, String>("49er ORC", 4.995,
                "resources/PolarDiagram49ORC.csv"));
        this._boatClassesInfo.add(new Tuple<String, Double, String>("49er STG", 4.876,
                "resources/PolarDiagram49STG.csv"));
        this._boatClassesInfo
                .add(new Tuple<String, Double, String>("505 STG", 5.05, "resources/PolarDiagram505STG.csv"));
    }

    @Test
    public void test_getPolarDiagramFileLocation() {
        Assert.assertEquals("resources/PolarDiagram49.csv", ConfigurationManager.getDefault()
                .getPolarDiagramFileLocation(0));
        Assert.assertEquals("resources/PolarDiagram49Bethwaite.csv", ConfigurationManager.getDefault()
                .getPolarDiagramFileLocation(1));
        Assert.assertEquals("resources/PolarDiagram49ORC.csv", ConfigurationManager.getDefault()
                .getPolarDiagramFileLocation(2));
        Assert.assertEquals("resources/PolarDiagram49STG.csv", ConfigurationManager.getDefault()
                .getPolarDiagramFileLocation(3));
        Assert.assertEquals("resources/PolarDiagram505STG.csv", ConfigurationManager.getDefault()
                .getPolarDiagramFileLocation(4));
    }

    @Test
    public void test_getBoatClassesInfoCount() {
        Assert.assertEquals(5, ConfigurationManager.getDefault().getBoatClassesInfoCount());
    }

    @Test
    public void test_getBoatClassesInfo() {
        int index = 0;
        for (Tuple<String, Double, String> tuple : ConfigurationManager.getDefault().getBoatClassesInfo()) {
            Assert.assertEquals(this._boatClassesInfo.get(index).first, tuple.first);
            Assert.assertEquals(this._boatClassesInfo.get(index).second, tuple.second);
            Assert.assertEquals(this._boatClassesInfo.get(index).third, tuple.third);
            index++;
        }
    }
}
