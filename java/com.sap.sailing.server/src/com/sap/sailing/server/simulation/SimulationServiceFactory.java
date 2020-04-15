package com.sap.sailing.server.simulation;

import java.util.concurrent.ScheduledExecutorService;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.SimulationService;

public interface SimulationServiceFactory {

    static SimulationServiceFactory INSTANCE = new SimulationServiceFactoryImpl();

    SimulationService getService(ScheduledExecutorService executor, RacingEventService racingEventService);
    
}
