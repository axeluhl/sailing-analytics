package com.sap.sailing.domain.igtimiadapter.persistence.impl;

import org.bson.Document;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.persistence.MongoObjectFactory;
import com.sap.sse.common.Util;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final MongoDatabase db;

    public MongoObjectFactoryImpl(MongoDatabase db) {
        this.db = db;
    }
    
    @Override
    public void clear() {
        db.getCollection(CollectionNames.IGTIMI_DEVICES.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).drop();
        db.getCollection(CollectionNames.IGTIMI_RESOURCES.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).drop();
        db.getCollection(CollectionNames.IGTIMI_DATA_ACCESS_WINDOWS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).drop();
    }

    
    @Override
    public void storeDevice(Device device) {
        final Document filter = new Document(FieldNames.IGTIMI_DEVICES_ID.name(), device.getId());
        final Document update = new Document();
        update.put(FieldNames.IGTIMI_DEVICES_ID.name(), device.getId());
        update.put(FieldNames.IGTIMI_DEVICES_NAME.name(), device.getName());
        update.put(FieldNames.IGTIMI_DEVICES_SERIAL_NUMBER.name(), device.getSerialNumber());
        update.put(FieldNames.IGTIMI_DEVICES_SERVICE_TAG.name(), device.getServiceTag());
        db.getCollection(CollectionNames.IGTIMI_DEVICES.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).replaceOne(filter, update, new ReplaceOptions().upsert(true));
    }

    @Override
    public void removeDevice(long deviceId) {
        final Document filter = new Document(FieldNames.IGTIMI_DEVICES_ID.name(), deviceId);
        db.getCollection(CollectionNames.IGTIMI_DEVICES.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(filter);
    }
    
    @Override
    public void storeResource(Resource resource) {
        final Document filter = new Document(FieldNames.IGTIMI_RESOURCES_ID.name(), resource.getId());
        final Document update = new Document();
        update.put(FieldNames.IGTIMI_RESOURCES_ID.name(), resource.getId());
        update.put(FieldNames.IGTIMI_RESOURCES_DEVICE_SERIAL_NUMBER.name(), resource.getDeviceSerialNumber());
        update.put(FieldNames.IGTIMI_RESOURCES_START_TIME_MILLIS.name(), resource.getStartTime() == null ? null : resource.getStartTime().asMillis());
        update.put(FieldNames.IGTIMI_RESOURCES_END_TIME_MILLIS.name(), resource.getEndTime() == null ? null : resource.getEndTime().asMillis());
        update.put(FieldNames.IGTIMI_RESOURCES_DATA_TYPES.name(), Util.asList(Util.map(resource.getDataTypes(), dataType->dataType.getCode())));
        db.getCollection(CollectionNames.IGTIMI_RESOURCES.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).replaceOne(filter, update, new ReplaceOptions().upsert(true));
    }

    @Override
    public void removeResource(long resourceId) {
        final Document filter = new Document(FieldNames.IGTIMI_DEVICES_ID.name(), resourceId);
        db.getCollection(CollectionNames.IGTIMI_RESOURCES.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(filter);
    }
    
    @Override
    public void storeDataAccessWindow(DataAccessWindow daw) {
        final Document filter = new Document(FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_ID.name(), daw.getId());
        final Document update = new Document();
        update.put(FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_ID.name(), daw.getId());
        update.put(FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_DEVICE_SERIAL_NUMBER.name(), daw.getDeviceSerialNumber());
        update.put(FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_START_TIME_MILLIS.name(), daw.getStartTime() == null ? null : daw.getStartTime().asMillis());
        update.put(FieldNames.IGTIMI_DATA_ACCESS_WINDOWS_END_TIME_MILLIS.name(), daw.getEndTime() == null ? null : daw.getEndTime().asMillis());
        db.getCollection(CollectionNames.IGTIMI_DATA_ACCESS_WINDOWS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).replaceOne(filter, update, new ReplaceOptions().upsert(true));
    }

    @Override
    public void removeDataAccessWindow(long dawId) {
        final Document filter = new Document(FieldNames.IGTIMI_DEVICES_ID.name(), dawId);
        db.getCollection(CollectionNames.IGTIMI_DATA_ACCESS_WINDOWS.name()).withWriteConcern(WriteConcern.ACKNOWLEDGED).deleteOne(filter);
    }
}
