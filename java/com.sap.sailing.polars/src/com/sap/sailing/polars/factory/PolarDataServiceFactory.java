package com.sap.sailing.polars.factory;

import java.util.concurrent.Executor;

import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.factory.impl.PolarDataServiceImplFactory;

public abstract class PolarDataServiceFactory {

    public static PolarDataService createStandardPolarDataService(Executor executor) {
        PolarDataServiceFactory factory = new PolarDataServiceImplFactory();
        return factory.createPolarDataService(executor);
    }

    protected abstract PolarDataService createPolarDataService(Executor executor);


}
