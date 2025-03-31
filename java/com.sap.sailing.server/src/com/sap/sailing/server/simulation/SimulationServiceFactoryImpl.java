package com.sap.sailing.server.simulation;

import java.util.concurrent.ScheduledExecutorService;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.SimulationService;

public class SimulationServiceFactoryImpl implements SimulationServiceFactory {
    @Override
    public SimulationService getService(ScheduledExecutorService executor, RacingEventService racingEventService) {
        return new SimulationServiceImpl(executor, racingEventService);
    }
}
