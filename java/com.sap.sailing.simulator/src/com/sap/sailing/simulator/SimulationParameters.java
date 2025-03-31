package com.sap.sailing.simulator;

import java.util.List;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sse.common.Duration;

public interface SimulationParameters {
    char getMode();

    void setCourse(List<Position> course);

    List<Position> getCourse();
    
    List<Position> getStartLine();

    List<Position> getEndLine();

    PolarDiagram getBoatPolarDiagram();

    WindFieldGenerator getWindField();

    Duration getSimuStep();

    Grid getGrid();

    boolean showOmniscient();

    boolean showOpportunist();
    
    LegType getLegType();
}
