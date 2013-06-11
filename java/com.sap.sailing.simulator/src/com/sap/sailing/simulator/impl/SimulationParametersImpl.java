package com.sap.sailing.simulator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

public class SimulationParametersImpl implements SimulationParameters {

    private List<Position> course;
    private PolarDiagram polarDiagram;
    private WindFieldGenerator windField;
    private Map<String, Double> settings;
    private char mode;

    public SimulationParametersImpl(List<Position> crs, PolarDiagram pd, WindFieldGenerator wf, char mode) {
        this.course = crs;
        this.polarDiagram = pd;
        this.windField = wf;
        this.mode = mode;

        this.settings = new HashMap<String, Double>();

        this.settings.put("Heuristic.targetTolerance[double]", 0.05);
        this.settings.put("Heuristic.timeResolution[long]", 30000.0);
        this.settings.put("Djikstra.gridv[int]", 10.0);
        this.settings.put("Djikstra.gridh[int]", 100.0);
    }

    @Override
    public void setCourse(List<Position> course) {
        this.course = course;
    }

    @Override
    public char getMode() {
        return mode;
    }

    @Override
    public List<Position> getCourse() {
        return course;
    }

    @Override
    public PolarDiagram getBoatPolarDiagram() {
        return polarDiagram;
    }

    @Override
    public WindFieldGenerator getWindField() {
        return windField;
    }

    @Override
    public Boundary getBoundaries() {
        return windField.getBoundaries();
    }

    @Override
    public Map<String, Double> getSettings() {
        return settings;
    }

    @Override
    public void setProperty(String name, Double value) {
        settings.put(name, value);
    }

    @Override
    public Double getProperty(String name) {
        return settings.get(name);
    }
}
