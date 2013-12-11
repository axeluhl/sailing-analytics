package com.sap.sailing.polars.factory;

import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.factory.impl.PolarDataServiceImplFactory;

public abstract class PolarDataServiceFactory {

    public static PolarDataService createStandardPolarDataService() {
        PolarDataServiceFactory factory = new PolarDataServiceImplFactory();
        return factory.createPolarDataService();
    }

    protected abstract PolarDataService createPolarDataService();


}
