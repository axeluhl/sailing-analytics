package com.sap.sailing.server.simulation;

import java.util.concurrent.Executor;

import com.sap.sailing.server.RacingEventService;

public interface SimulationServiceFactory {

    static SimulationServiceFactory INSTANCE = new SimulationServiceFactoryImpl();

    SimulationService getService(Executor executor, RacingEventService racingEventService);
    
}
