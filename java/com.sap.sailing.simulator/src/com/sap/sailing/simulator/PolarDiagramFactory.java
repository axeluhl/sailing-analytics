package com.sap.sailing.simulator;

import com.sap.sailing.domain.base.BoatClass;


public interface PolarDiagramFactory {

    PolarDiagram getPolarDiagram(BoatClass boatClass);

}
