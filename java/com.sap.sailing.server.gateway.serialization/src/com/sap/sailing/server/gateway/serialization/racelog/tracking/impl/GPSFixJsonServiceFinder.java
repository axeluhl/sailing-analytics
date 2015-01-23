package com.sap.sailing.server.gateway.serialization.racelog.tracking.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingNmeaDTOJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixNmeaDTOJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingNmeaDTOJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixNmeaDTOJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.GPSFixJsonHandler;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;

public class GPSFixJsonServiceFinder implements TypeBasedServiceFinder<GPSFixJsonHandler> {
    private final GPSFixJsonHandler gpsFixHandler = new GPSFixJsonHandlerImpl<GPSFix>(new GPSFixJsonDeserializer(),
            new GPSFixJsonSerializer());
    private final GPSFixJsonHandler gpsFixMovingHandler = new GPSFixJsonHandlerImpl<GPSFixMoving>(
            new GPSFixMovingJsonDeserializer(), new GPSFixMovingJsonSerializer());
    private final GPSFixJsonHandler gpsFixNmeaDTOHandler = new GPSFixJsonHandlerImpl<GPSFix>(
            new GPSFixNmeaDTOJsonDeserializer(), new GPSFixNmeaDTOJsonSerializer());
    private final GPSFixJsonHandler gpsFixMovingNmeaDTOHandler = new GPSFixJsonHandlerImpl<GPSFixMoving>(
            new GPSFixMovingNmeaDTOJsonDeserializer(), new GPSFixMovingNmeaDTOJsonSerializer());

    @Override
    public GPSFixJsonHandler findService(String type) throws NoCorrespondingServiceRegisteredException {
        if (type.equals(GPSFixJsonDeserializer.TYPE)) {
            return gpsFixHandler;
        } else if (type.equals(GPSFixMovingJsonDeserializer.TYPE)) {
            return gpsFixMovingHandler;
        } else if (type.equals(GPSFixNmeaDTOJsonDeserializer.TYPE)) {
            return gpsFixNmeaDTOHandler;
        } else if (type.equals(GPSFixMovingNmeaDTOJsonDeserializer.TYPE)) {
            return gpsFixMovingNmeaDTOHandler;
        }
        throw new NoCorrespondingServiceRegisteredException(
                "Only handlers for GPSFix, GPSFixMoving, GPSFixNmeaDTO, GPSFixMovingNmeaDTO are registered", type,
                GPSFixJsonHandler.class.getSimpleName());
    }

    @Override
    public void setFallbackService(GPSFixJsonHandler fallback) {

    }

    @Override
    public Set<GPSFixJsonHandler> findAllServices() {
        return new HashSet<GPSFixJsonHandler>(Arrays.asList(gpsFixHandler, gpsFixMovingHandler,
                gpsFixMovingNmeaDTOHandler, gpsFixNmeaDTOHandler));
    }

}
