package com.sap.sailing.simulator.impl;

import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathGenerator;
import com.sap.sailing.simulator.SimulationParameters;

public class PathGeneratorBase implements PathGenerator {

    protected SimulationParameters parameters;

    public PathGeneratorBase() {
        this.parameters = null;
    }

    public PathGeneratorBase(SimulationParameters params) {
        this.parameters = params;
    }

    @Override
    public void setSimulationParameters(SimulationParameters params) {
        this.parameters = params;
    }

    @Override
    public SimulationParameters getSimulationParameters() {
        return this.parameters;
    }

    @Override
    public Path getPath(int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {
        return null;
    }

    @Override
    public Path getPathEvenTimed(long stepMilliseconds, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex) {

        Path path = this.getPath(selectedRaceIndex, selectedCompetitorIndex, selectedLegIndex);

        return path == null ? null : path.getEvenTimedPath(stepMilliseconds);
    }
}
