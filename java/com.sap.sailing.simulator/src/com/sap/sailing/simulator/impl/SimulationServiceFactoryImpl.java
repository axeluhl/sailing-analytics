package com.sap.sailing.simulator.impl;

import java.util.concurrent.Executor;

import com.sap.sailing.simulator.SimulationService;
import com.sap.sailing.simulator.SimulationServiceFactory;

public class SimulationServiceFactoryImpl implements SimulationServiceFactory {
    
    public SimulationService getService(Executor executor) {
        return new SimulationServiceImpl(executor);
    }

}
