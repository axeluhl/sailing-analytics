package com.sap.sailing.polars.factory.impl;

import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.factory.PolarDataServiceFactory;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;

public class PolarDataServiceImplFactory extends PolarDataServiceFactory {

    @Override
    public PolarDataService createPolarDataService() {
        return new PolarDataServiceImpl();
    }

}
