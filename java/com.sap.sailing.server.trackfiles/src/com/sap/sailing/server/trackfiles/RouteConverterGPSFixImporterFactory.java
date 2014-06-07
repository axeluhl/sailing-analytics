package com.sap.sailing.server.trackfiles;

import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.server.trackfiles.impl.RouteConverterGPSFixImporterFactoryImpl;

public interface RouteConverterGPSFixImporterFactory {
    RouteConverterGPSFixImporterFactory INSTANCE = new RouteConverterGPSFixImporterFactoryImpl();
    
    GPSFixImporter createRouteConverterGPSFixImporter();
}
