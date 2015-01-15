package com.sap.sailing.simulator.impl;

import java.util.List;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.Grid;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sse.common.Duration;

public class SimulationParametersImpl implements SimulationParameters {

    private List<Position> course;
    private PolarDiagram polarDiagram;
    private WindFieldGenerator windField;
    private Duration simuStep;
    private char mode;
    private boolean showOmniscient;
    private boolean showOpportunist;

    public SimulationParametersImpl(List<Position> crs, PolarDiagram pd, WindFieldGenerator wf, Duration simuStep,
            char mode, boolean showOmniscient, boolean showOpportunist) {
        this.course = crs;
        this.polarDiagram = pd;
        this.windField = wf;
        this.simuStep = simuStep;
        this.mode = mode;
        this.showOmniscient = showOmniscient;
        this.showOpportunist = showOpportunist;
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
    public Duration getSimuStep() {
        return simuStep;
    }

    @Override
    public Grid getGrid() {
        return windField.getGrid();
    }

    @Override
    public boolean showOmniscient() {
        return showOmniscient;
    }

    @Override
    public boolean showOpportunist() {
        return showOpportunist;
    }

}
