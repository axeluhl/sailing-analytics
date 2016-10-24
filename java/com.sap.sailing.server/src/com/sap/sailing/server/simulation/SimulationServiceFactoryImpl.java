package com.sap.sailing.server.simulation;

import java.util.concurrent.ScheduledExecutorService;

import com.sap.sailing.server.RacingEventService;

public class SimulationServiceFactoryImpl implements SimulationServiceFactory {
    
    public SimulationService getService(ScheduledExecutorService executor, RacingEventService racingEventService) {
        return new SimulationServiceImpl(executor, racingEventService);
    }

}
