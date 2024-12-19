package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sse.common.TimePoint;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private final MongoDatabase db;

    public DomainObjectFactoryImpl(MongoDatabase db) {
        this.db = db;
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
}
