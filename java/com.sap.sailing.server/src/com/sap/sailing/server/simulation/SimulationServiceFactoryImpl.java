package com.sap.sailing.server.simulation;

import java.util.concurrent.Executor;

import com.sap.sailing.server.RacingEventService;

public class SimulationServiceFactoryImpl implements SimulationServiceFactory {
    
    public SimulationService getService(Executor executor, RacingEventService racingEventService) {
        return new SimulationServiceImpl(executor, racingEventService);
    }

}
