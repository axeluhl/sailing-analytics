package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.types.Binary;

import com.google.protobuf.InvalidProtocolBufferException;
import com.igtimi.IgtimiStream.Msg;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final MongoDatabase db;
    private final MongoCollection<Document> messagesCollection;

    public DomainObjectFactoryImpl(MongoDatabase db) {
        this.db = db;
        messagesCollection = MongoObjectFactoryImpl.getOrCreateMessagesCollection(db);
    }
    
    @Override
    public Iterable<Resource> getResources() {
        final List<Resource> result = new ArrayList<>();
        final MongoCollection<org.bson.Document> resourcesCollection = db.getCollection(CollectionNames.IGTIMI_RESOURCES.name());
        for (Object o : resourcesCollection.find()) {
            final long id = ((Number) ((Document) o).get(FieldNames.IGTIMI_RESOURCES_ID.name())).longValue();
            final TimePoint startTime = getTimePoint((Document) o, FieldNames.IGTIMI_RESOURCES_START_TIME_MILLIS);
            final TimePoint endTime = getTimePoint((Document) o, FieldNames.IGTIMI_RESOURCES_END_TIME_MILLIS);
            final String deviceSerialNumber = (String) ((Document) o).get(FieldNames.IGTIMI_RESOURCES_DEVICE_SERIAL_NUMBER.name());
            final Integer[] dataTypesAsInteger = ((List<?>) ((Document) o).get(FieldNames.IGTIMI_RESOURCES_DATA_TYPES.name())).toArray(new Integer[0]);
            final int[] dataTypes = new int[dataTypesAsInteger.length];
            for (int i=0; i<dataTypesAsInteger.length; i++) {
                dataTypes[i] = dataTypesAsInteger[i];
            }
            result.add(Resource.create(id, startTime, endTime, deviceSerialNumber, dataTypes));
        }
        return result;
    }

    private TimePoint getTimePoint(Document o, final FieldNames timePointFieldName) {
        final Number startTimeMillisNumber = (Number) o.get(timePointFieldName.name());
        final TimePoint startTime = startTimeMillisNumber == null ? null : TimePoint.of(startTimeMillisNumber.longValue());
        return startTime;
    }

    @Override
    public Iterable<DataAccessWindow> getDataAccessWindows() {
        final List<DataAccessWindow> result = new ArrayList<>();
        final MongoCollection<org.bson.Document> devicesCollection = db.getCollection(CollectionNames.IGTIMI_DATA_ACCESS_WINDOWS.name());
        for (Object o : devicesCollection.find()) {
            final Number id = ((Number) ((Document) o).get(FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_ID.name()));
            final TimePoint startTime = getTimePoint((Document) o, FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_START_TIME_MILLIS);
            final TimePoint endTime = getTimePoint((Document) o, FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_END_TIME_MILLIS);
            final String deviceSerialNumber = (String) ((Document) o).get(FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_DEVICE_SERIAL_NUMBER.name());
            result.add(DataAccessWindow.create(id.longValue(), startTime, endTime, deviceSerialNumber));
        }
        return result;
    }

    @Override
    public Iterable<Device> getDevices() {
        final List<Device> result = new ArrayList<>();
        final MongoCollection<org.bson.Document> devicesCollection = db.getCollection(CollectionNames.IGTIMI_DEVICES.name());
        for (Object o : devicesCollection.find()) {
            final Number id = ((Number) ((Document) o).get(FieldNames.IGTIMI_DEVICES_ID.name()));
            final String serialNumber = (String) ((Document) o).get(FieldNames.IGTIMI_DEVICES_SERIAL_NUMBER.name());
            final String name = (String) ((Document) o).get(FieldNames.IGTIMI_DEVICES_NAME.name());
            final String serviceTag = (String) ((Document) o).get(FieldNames.IGTIMI_DEVICES_SERVICE_TAG.name());
            result.add(Device.create(id.longValue(), serialNumber, name, serviceTag));
        }
        return result;
    }

    @Override
    public Iterable<Msg> getMessages(String deviceSerialNumber, TimeRange timeRange) {
        final Document query = new Document();
        appendTimeRangeQuery(query, timeRange);
        query.append(FieldNames.IGTIMI_MESSAGES_DEVICE_SERIAL_NUMBER.name(), deviceSerialNumber);
        final Iterable<Document> queryResult = messagesCollection.find(query);
        return Util.map(queryResult,
                doc->{
                    try {
                        return Msg.parseFrom(doc.get(FieldNames.IGTIMI_MESSAGES_PROTOBUF_MESSAGE.name(), Binary.class).getData());
                    } catch (InvalidProtocolBufferException e) {
                        logger.log(Level.SEVERE, "Error trying to parse an Igtimi message for device "+deviceSerialNumber, e);
                        return null;
                    }
                });
    }
    
    private void appendTimeRangeQuery(Document query, TimeRange timeRange) {
        final Document timeRangeQuery = new Document();
        if (timeRange.from() != null) {
            timeRangeQuery.append("$gte", timeRange.from().asDate());
        }
        if (timeRange.to() != null) {
            timeRangeQuery.append("$lt", timeRange.to().asDate());
        }
        if (!timeRangeQuery.isEmpty()) {
            query.append(FieldNames.IGTIMI_MESSAGES_TIMESTAMP.name(), timeRangeQuery);
        }
    }
}
