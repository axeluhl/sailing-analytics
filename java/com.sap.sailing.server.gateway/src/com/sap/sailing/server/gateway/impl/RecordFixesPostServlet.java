package com.sap.sailing.server.gateway.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.devices.TypeBasedServiceFinderFactory;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.CachedOsgiTypeBasedServiceFinderFactory;
import com.sap.sailing.server.gateway.AbstractJsonPostServlet;
import com.sap.sailing.server.gateway.HttpExceptionWithMessage;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.devices.DeviceIdentifierJsonSerializationHandler;
import com.sap.sailing.server.gateway.serialization.devices.GPSFixJsonSerializationHandler;

public class RecordFixesPostServlet extends AbstractJsonPostServlet<Triple<DeviceIdentifier, Serializable, List<GPSFix>>, Void> {    
    private static final long serialVersionUID = 2778739335260621119L;
    private final DeviceAndSessionIdentifierWithGPSFixesDeserializer deserializer;
    
    public RecordFixesPostServlet() {
    	TypeBasedServiceFinderFactory serviceFinderFactory = new CachedOsgiTypeBasedServiceFinderFactory(getContext());
        TypeBasedServiceFinder<GPSFixJsonSerializationHandler> fixServiceFinder =
                serviceFinderFactory.createServiceFinder(GPSFixJsonSerializationHandler.class);
        TypeBasedServiceFinder<DeviceIdentifierJsonSerializationHandler> deviceServiceFinder =
        		serviceFinderFactory.createServiceFinder(DeviceIdentifierJsonSerializationHandler.class);
        deserializer = new DeviceAndSessionIdentifierWithGPSFixesDeserializer(fixServiceFinder, deviceServiceFinder);
        
    }

    @Override
    protected JsonDeserializer<Triple<DeviceIdentifier, Serializable, List<GPSFix>>> getRequestDeserializer() {
        return deserializer;
    }

    @Override
    protected JsonSerializer<Void> getResponseSerializer() {
        return null;
    }

    @Override
    protected Void process(Map<String, String> parameterValues,
            Triple<DeviceIdentifier, Serializable, List<GPSFix>> domainObject) throws HttpExceptionWithMessage {
        DeviceIdentifier device = domainObject.getA();
        //might use the session id in the future
//        Serializable sessionId = domainObject.getB();
        List<GPSFix> fixes = domainObject.getC();
        
        for (GPSFix fix : fixes) {
        	getService().storeGPSFix(device, fix);
        }
               
        return null;        
    }
}
