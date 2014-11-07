package com.sap.sailing.domain.racelogtracking.servlet;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.gateway.AbstractJsonPostServlet;
import com.sap.sailing.server.gateway.HttpExceptionWithMessage;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceAndSessionIdentifierWithGPSFixesDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.GPSFixJsonHandler;
import com.sap.sse.common.Util;

public class RecordFixesPostServlet extends
        AbstractJsonPostServlet<Util.Triple<DeviceIdentifier, Serializable, List<GPSFix>>, Void> {
    private static final long serialVersionUID = 2778739335260621119L;
    private DeviceAndSessionIdentifierWithGPSFixesDeserializer deserializer;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        TypeBasedServiceFinder<GPSFixJsonHandler> fixServiceFinder = getServiceFinderFactory().createServiceFinder(
                GPSFixJsonHandler.class);
        TypeBasedServiceFinder<DeviceIdentifierJsonHandler> deviceServiceFinder = getServiceFinderFactory()
                .createServiceFinder(DeviceIdentifierJsonHandler.class);
        deserializer = new DeviceAndSessionIdentifierWithGPSFixesDeserializer(fixServiceFinder,
                new DeviceIdentifierJsonDeserializer(deviceServiceFinder));
    }

    @Override
    public JsonDeserializer<Util.Triple<DeviceIdentifier, Serializable, List<GPSFix>>> getRequestDeserializer() {
        return deserializer;
    }

    @Override
    public JsonSerializer<Void> getResponseSerializer() {
        return null;
    }

    @Override
    public Void process(Map<String, String> parameterValues,
            Util.Triple<DeviceIdentifier, Serializable, List<GPSFix>> domainObject) throws HttpExceptionWithMessage {
        DeviceIdentifier device = domainObject.getA();
        // might use the session id in the future
        // Serializable sessionId = domainObject.getB();
        List<GPSFix> fixes = domainObject.getC();

        for (GPSFix fix : fixes) {
            try {
                getService().getGPSFixStore().storeFix(device, fix);
            } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                logger.log(Level.WARNING, "Could not load store fix from device " + device);
            }
        }

        return null;
    }
}
