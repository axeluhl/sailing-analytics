package com.sap.sailing.simulator.impl;

import java.util.List;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.Grid;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sse.common.Duration;

public class SimulationParametersImpl implements SimulationParameters {

    private List<Position> course;
    private List<Position> startLine;
    private PolarDiagram polarDiagram;
    private WindFieldGenerator windField;
    private Duration simuStep;
    private char mode;
    private boolean showOmniscient;
    private boolean showOpportunist;
    private LegType legType;

    public SimulationParametersImpl(List<Position> course, PolarDiagram pd, WindFieldGenerator wf, Duration simuStep,
            char mode, boolean showOmniscient, boolean showOpportunist) {
        this.course = course;
        this.startLine = null;
        this.polarDiagram = pd;
        this.windField = wf;
        this.simuStep = simuStep;
        this.mode = mode;
        this.showOmniscient = showOmniscient;
        this.showOpportunist = showOpportunist;
        this.legType = null;
    }

    public SimulationParametersImpl(List<Position> course, PolarDiagram pd, WindFieldGenerator wf, Duration simuStep,
            char mode, boolean showOmniscient, boolean showOpportunist, LegType legType) {
        this.course = course;
        this.startLine = null;
        this.polarDiagram = pd;
        this.windField = wf;
        this.simuStep = simuStep;
        this.mode = mode;
        this.showOmniscient = showOmniscient;
        this.showOpportunist = showOpportunist;
        this.legType = legType;
    }

    public SimulationParametersImpl(List<Position> course, List<Position> startLine, PolarDiagram pd, WindFieldGenerator wf, Duration simuStep,
            char mode, boolean showOmniscient, boolean showOpportunist, LegType legType) {
        this.course = course;
        this.startLine = startLine;
        this.polarDiagram = pd;
        this.windField = wf;
        this.simuStep = simuStep;
        this.mode = mode;
        this.showOmniscient = showOmniscient;
        this.showOpportunist = showOpportunist;
        this.legType = legType;
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
    public List<Position> getStartLine() {
        return startLine;
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
    
    @Override
    public LegType getLegType() {
        return legType;
    }
}
