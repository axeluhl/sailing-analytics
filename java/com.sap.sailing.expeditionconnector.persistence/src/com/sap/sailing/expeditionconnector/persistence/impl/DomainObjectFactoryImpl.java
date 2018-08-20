package com.sap.sailing.expeditionconnector.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;
import com.sap.sailing.expeditionconnector.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private final DB db;

    public DomainObjectFactoryImpl(DB db) {
        this.db = db;
    }
    
    @Override
    public Iterable<ExpeditionDeviceConfiguration> getExpeditionDeviceConfigurations() {
        final List<ExpeditionDeviceConfiguration> result = new ArrayList<>();
        final DBCollection expeditionDeviceConfigurationsCollection = db.getCollection(CollectionNames.EXPEDITION_DEVICE_CONFIGURATIONS.name());
        for (final Object o : expeditionDeviceConfigurationsCollection.find()) {
            final DBObject dbo = (DBObject) o;
            final UUID uuid = (UUID) dbo.get(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_UUID.name());
            final String name = (String) dbo.get(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_NAME.name());
            final Number boatIdAsNumber = (Number) dbo.get(FieldNames.EXPEDITION_DEVICE_CONFIGURATION_BOAT_ID.name());
            final Integer boatId = boatIdAsNumber == null ? null : boatIdAsNumber.intValue();
            result.add(new ExpeditionDeviceConfiguration(name, uuid, boatId));
        }
        return result;
    }
    
}
