package com.sap.sailing.domain.racelogtracking.servlet;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.server.gateway.AbstractJsonPostServlet;
import com.sap.sailing.server.gateway.HttpExceptionWithMessage;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util.Pair;

public class RecordFixesFlatJsonPostServlet extends AbstractJsonPostServlet<Pair<UUID, GPSFixMoving>, Void> {
    private static final long serialVersionUID = -4618399862031700634L;
    
    private final JsonDeserializer<Pair<UUID, GPSFixMoving>> deserializer =
            new FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer();

    @Override
    public JsonDeserializer<Pair<UUID, GPSFixMoving>> getRequestDeserializer() {
        return deserializer;
    }

    @Override
    public JsonSerializer<Void> getResponseSerializer() {
        return null;
    }

    @Override
    public Void process(Map<String, String> parameterValues, Pair<UUID, GPSFixMoving> domainObject)
            throws HttpExceptionWithMessage {
        DeviceIdentifier device = new SmartphoneUUIDIdentifierImpl(domainObject.getA());
        GPSFixMoving fix = domainObject.getB();

        try {
            getService().getGPSFixStore().storeFix(device, fix);
            logger.log(Level.INFO, "Added fix for device " + device.toString()  + " to store");
        } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
            logger.log(Level.WARNING, "Could not load store fix from device " + device);
        }

        return null;
    }

}
