package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import org.bson.Document;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.racelog.tracking.FixMongoHandler;
import com.sap.sailing.shared.persistence.device.DeviceIdentifierMongoHandler;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.Timed;
import com.sap.sse.common.TypeBasedServiceFinder;

abstract class MongoFixHandler {
    protected final TypeBasedServiceFinder<FixMongoHandler<?>> fixServiceFinder;
    protected final TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceServiceFinder;

    public MongoFixHandler(TypeBasedServiceFinder<FixMongoHandler<?>> fixServiceFinder,
            TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceServiceFinder) {
        super();
        this.fixServiceFinder = fixServiceFinder;
        this.deviceServiceFinder = deviceServiceFinder;
    }

    /**
     * Try to find a service implementing specified type of objects interface using the {@link #fixServiceFinder}.
     * <p>
     * To support an additional fix type, an implementation of {@link FixMongoHandler} is required, which specifies how
     * to transform a fix forth to and back from database. This implementation needs to be registered during OSGi bundle
     * activator startup, providing respective type mapping properties.
     * </p>
     * 
     * @param type
     *            type object to find service for
     * @return the registered {@link FixMongoHandler} implementation
     * 
     * @see TypeBasedServiceFinder#findService(String)
     */
    @SuppressWarnings("unchecked")
    protected <FixT extends Timed> FixMongoHandler<FixT> findService(String type) {
        return (FixMongoHandler<FixT>) fixServiceFinder.findService(type);
    }

    protected <T extends Timed> T loadFix(Document object)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        String type = (String) object.get(FieldNames.GPSFIX_TYPE.name());
        Document fixObject = (Document) object.get(FieldNames.GPSFIX.name());
        return this.<T> findService(type).transformBack(fixObject);
    }
    
    /**
     * Writes the {@code fix} to the {@code entry} document such that {@link #loadFix(Document)} can re-establish
     * the fix from that document.
     * 
     * @return for convenience and call chaining, the updated {@code entry} is returned
     */
    protected <FixT extends Timed> Document storeFixToDocument(Document entry, FixT fix) throws TransformationException {
        String type = fix.getClass().getName();
        FixMongoHandler<FixT> mongoHandler = findService(type);
        Object fixObject = mongoHandler.transformForth(fix);
        entry.append(FieldNames.GPSFIX_TYPE.name(), type).append(FieldNames.GPSFIX.name(), fixObject);
        return entry;
    }
}
