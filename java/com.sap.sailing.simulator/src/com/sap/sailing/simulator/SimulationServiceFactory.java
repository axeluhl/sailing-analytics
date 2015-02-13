package com.sap.sailing.simulator;

import java.util.concurrent.Executor;

import com.sap.sailing.simulator.impl.SimulationServiceFactoryImpl;

public interface SimulationServiceFactory {

    static SimulationServiceFactory INSTANCE = new SimulationServiceFactoryImpl();

    SimulationService getService(Executor executor);
    
}
