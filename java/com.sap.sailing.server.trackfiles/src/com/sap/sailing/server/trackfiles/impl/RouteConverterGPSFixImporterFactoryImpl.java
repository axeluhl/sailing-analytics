package com.sap.sailing.server.trackfiles.impl;

import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.server.trackfiles.RouteConverterGPSFixImporterFactory;

public class RouteConverterGPSFixImporterFactoryImpl implements RouteConverterGPSFixImporterFactory {

    @Override
    public GPSFixImporter createRouteConverterGPSFixImporter() {
        return new RouteConverterGPSFixImporterImpl();
    }

}
