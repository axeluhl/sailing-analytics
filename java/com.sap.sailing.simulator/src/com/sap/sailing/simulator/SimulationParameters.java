package com.sap.sailing.simulator;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

public interface SimulationParameters {

    char getMode();

    void setCourse(List<Position> course);

    List<Position> getCourse();

    PolarDiagram getBoatPolarDiagram();

    WindFieldGenerator getWindField();

    Grid getGrid();

    Map<String,Double> getSettings();

    void setProperty(String name, Double value);

    Double getProperty(String name);
    
    boolean showOmniscient();
    boolean showOpportunist();
}
