package com.sap.sailing.domain.tractracadapter.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final MongoDatabase database;

    private final DomainFactory tracTracDomainFactory;
    
    public DomainObjectFactoryImpl(MongoDatabase db, DomainFactory tracTracDomainFactory) {
        super();
        this.database = db;
        this.tracTracDomainFactory = tracTracDomainFactory;
    }

    @Override
    public Iterable<TracTracConfiguration> getTracTracConfigurations() {
        List<TracTracConfiguration> result = new ArrayList<TracTracConfiguration>();
        try {
            MongoCollection<org.bson.Document> ttConfigs = database.getCollection(CollectionNames.TRACTRAC_CONFIGURATIONS.name());
            for (Document o : ttConfigs.find()) {
                TracTracConfiguration ttConfig = loadTracTracConfiguration(o);
                result.add(ttConfig);
            }
        } catch (Exception e) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded TracTrac configurations. Check MongoDB settings.");
            logger.log(Level.SEVERE, "getTracTracConfigurations", e);
        }
        return result;
    }
    
    private TracTracConfiguration loadTracTracConfiguration(Document object) {
        Object courseDesignUpdateUriObject = object.get(FieldNames.TT_CONFIG_COURSE_DESIGN_UPDATE_URI.name());
        Object tracTracUsernameObject = object.get(FieldNames.TT_CONFIG_TRACTRAC_USERNAME.name());
        Object tracTracPasswordObject = object.get(FieldNames.TT_CONFIG_TRACTRAC_PASSWORD.name());
        
        String courseDesignUpdateUri = courseDesignUpdateUriObject == null ? "" : (String) courseDesignUpdateUriObject;
        String tracTracUsername = tracTracUsernameObject == null ? "" : (String) tracTracUsernameObject;
        String tracTracPassword = tracTracPasswordObject == null ? "" : (String) tracTracPasswordObject;
        String creatorName = (String) object.get(FieldNames.TT_CONFIG_CREATOR_NAME.name());
        String jsonURL = (String) object.get(FieldNames.TT_CONFIG_JSON_URL.name());
        
        final boolean needsUpdate = (creatorName == null);
        if (needsUpdate) {
            // No creator is set yet -> existing configurations are assumed to belong to the admin
            creatorName = "admin";
        }
        
        final TracTracConfiguration loadedTracTracConfiguration = tracTracDomainFactory.createTracTracConfiguration(
                creatorName,
                (String) object.get(FieldNames.TT_CONFIG_NAME.name()),
                jsonURL,
                (String) object.get(FieldNames.TT_CONFIG_LIVE_DATA_URI.name()),
                (String) object.get(FieldNames.TT_CONFIG_STORED_DATA_URI.name()),
                courseDesignUpdateUri,
                tracTracUsername,
                tracTracPassword);
        
        if (needsUpdate) {
            // recreating the config on the DB because the composite key changed
            new MongoObjectFactoryImpl(database).deleteTracTracConfiguration(null, jsonURL);
            new MongoObjectFactoryImpl(database).createTracTracConfiguration(loadedTracTracConfiguration);
        }
        return loadedTracTracConfiguration;
    }

}
