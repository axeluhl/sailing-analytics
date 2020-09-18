package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import static com.sap.sailing.shared.persistence.impl.MongoObjectFactoryImpl.storeDeviceId;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.FixMongoHandler;
import com.sap.sailing.shared.persistence.device.DeviceIdentifierMongoHandler;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.TypeBasedServiceFinder;

public class MetadataCollection extends MongoFixHandler {
    private static final Logger logger = Logger.getLogger(MetadataCollection.class.getName());
    private final MongoCollection<Document> metadataCollection;
    private final ConcurrentHashMap<DeviceIdentifier, MetadataUpdater> metadataUpdaters;
    private final WriteConcern writeConcern;

    public MetadataCollection(MongoObjectFactoryImpl mongoOF,
            TypeBasedServiceFinder<FixMongoHandler<?>> fixServiceFinder,
            TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceServiceFinder, WriteConcern writeConcern) {
        super(fixServiceFinder, deviceServiceFinder);
        this.metadataCollection = mongoOF.getGPSFixMetadataCollection();
        this.metadataUpdaters = new ConcurrentHashMap<>();
        this.writeConcern = writeConcern;
    }

    /**
     * First {@link #waitForPendingMetadataUpdates(DeviceIdentifier) waits for any pending metadata updates},
     * then loads and returns them.
     */
    private Document findMetadataObject(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        waitForPendingMetadataUpdates(device);
        return findMetadataObjectInternal(device);
    }

    /**
     * Doesn't {@link #waitForPendingMetadataUpdates(DeviceIdentifier) wait for pending updates} but obtains the
     * metadata object directly from the DB
     */
    private Document findMetadataObjectInternal(DeviceIdentifier device) throws TransformationException {
        Bson query = getDeviceQuery(device);
        Document result = metadataCollection.find(query).first();
        return result;
    }

    private Bson getDeviceQuery(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        Document dbDeviceId = storeDeviceId(deviceServiceFinder, device);
        Bson query = Filters.eq(FieldNames.DEVICE_ID.name(), dbDeviceId);
        return query;
    }

    TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        final Document resultDocument = findMetadataObject(device);
        return getTimeRangeCoveredByFixesInternal(resultDocument);
    }

    private TimeRange getTimeRangeCoveredByFixesInternal(DeviceIdentifier device) throws TransformationException {
        return getTimeRangeCoveredByFixesInternal(findMetadataObjectInternal(device));
    }
    
    /**
     * Doesn't {@link #waitForPendingMetadataUpdates(DeviceIdentifier) wait for pending updates} but tells the current
     * status as loaded from the DB
     */
    private TimeRange getTimeRangeCoveredByFixesInternal(final Document resultDocument) {
        final TimeRange result;
        if (resultDocument == null) {
            result = null;
        } else {
            result = DomainObjectFactoryImpl.loadTimeRange(resultDocument, FieldNames.TIMERANGE);
        }
        return result;
    }
    
    long getNumberOfFixes(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        final Document resultDocument = findMetadataObject(device);
        final long result;
        if (resultDocument == null) {
            result = 0;
        } else {
            result = ((Number) resultDocument.get(FieldNames.NUM_FIXES.name())).longValue();
        }
        return result;
    }
    
    <FixT extends Timed> Map<DeviceIdentifier, FixT> getFixLastReceived(Iterable<DeviceIdentifier> forDevices)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        Map<DeviceIdentifier, FixT> result = new HashMap<>();
        for (final DeviceIdentifier deviceIdentifier : forDevices) {
            final Document metadataDoc = findMetadataObject(deviceIdentifier);
            if (metadataDoc != null) {
                final Document lastFixForDeviceDbObject = (Document) metadataDoc.get(FieldNames.LAST_FIX_RECEIVED.name());
                if (lastFixForDeviceDbObject != null) {
                    final Timed lastFixForDevice = loadFix(lastFixForDeviceDbObject);
                    @SuppressWarnings("unchecked")
                    final FixT lastFixForDeviceTyped = (FixT) lastFixForDevice;
                    result.put(deviceIdentifier, lastFixForDeviceTyped);
                }
            }
        }
        return result;
    }

    <FixT extends Timed> void enqueueMetadataUpdate(DeviceIdentifier device, final Object dbDeviceId,
            final int nrOfTotalFixes, TimeRange fixesTimeRange, FixT latestFix) throws TransformationException {
        final MetadataUpdater metadataUpdaterForDevice = metadataUpdaters.computeIfAbsent(device, d->new MetadataUpdater(this, device));
        metadataUpdaterForDevice.enqueueMetadataUpdate(device, dbDeviceId, nrOfTotalFixes, fixesTimeRange, latestFix);
    }
    
    private void waitForPendingMetadataUpdates(DeviceIdentifier device) {
        final MetadataUpdater metadataUpdaterForDevice = metadataUpdaters.get(device);
        if (metadataUpdaterForDevice != null) {
            metadataUpdaterForDevice.waitForPendingUpdates();
        }
    }

    <FixT extends Timed> void update(MetadataUpdate<FixT> update) throws TransformationException, NoCorrespondingServiceRegisteredException {
        logger.fine(()->"Updating sensor fix store metadata with "+update);
        final TimeRange oldTimeRange = getTimeRangeCoveredByFixesInternal(update.getDevice());
        final TimeRange newTimeRange = oldTimeRange == null ? update.getFixesTimeRange() : update.getFixesTimeRange() == null ? oldTimeRange : oldTimeRange.extend(update.getFixesTimeRange());
        final Document updateOperation = new Document();
        final Document newMetadata = new Document();
        newMetadata.put(FieldNames.DEVICE_ID.name(), update.getDbDeviceId());
        if (update.getLatestFix() != null) {
            newMetadata.put(FieldNames.LAST_FIX_RECEIVED.name(), storeFixToDocument(new Document(), update.getLatestFix()));
        }
        MongoObjectFactoryImpl.storeTimeRange(newTimeRange, newMetadata, FieldNames.TIMERANGE);
        updateOperation.append("$set", newMetadata);
        updateOperation.append("$inc", new Document(FieldNames.NUM_FIXES.name(), update.getNrOfTotalFixes()));
        metadataCollection.withWriteConcern(writeConcern).updateOne(getDeviceQuery(update.getDevice()), updateOperation, new UpdateOptions().upsert(true));
    }
}
