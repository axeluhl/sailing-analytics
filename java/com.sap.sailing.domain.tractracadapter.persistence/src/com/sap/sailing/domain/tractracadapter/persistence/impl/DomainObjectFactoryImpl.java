package com.sap.sailing.domain.tractracadapter.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final DB database;

    private final DomainFactory tracTracDomainFactory;
    
    public DomainObjectFactoryImpl(DB db, DomainFactory tracTracDomainFactory) {
        super();
        this.database = db;
        this.tracTracDomainFactory = tracTracDomainFactory;
    }

    @Override
    public Iterable<TracTracConfiguration> getTracTracConfigurations() {
        List<TracTracConfiguration> result = new ArrayList<TracTracConfiguration>();
        try {
            DBCollection ttConfigs = database.getCollection(CollectionNames.TRACTRAC_CONFIGURATIONS.name());
            for (DBObject o : ttConfigs.find().sort(new BasicDBObject(FieldNames.TT_CONFIG_NAME.name(), 1))) {
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
    
    private TracTracConfiguration loadTracTracConfiguration(DBObject object) {
        Object courseDesignUpdateUriObject = object.get(FieldNames.TT_CONFIG_COURSE_DESIGN_UPDATE_URI.name());
        Object tracTracUsernameObject = object.get(FieldNames.TT_CONFIG_TRACTRAC_USERNAME.name());
        Object tracTracPasswordObject = object.get(FieldNames.TT_CONFIG_TRACTRAC_PASSWORD.name());
        
        String courseDesignUpdateUri = courseDesignUpdateUriObject == null ? "" : (String) courseDesignUpdateUriObject;
        String tracTracUsername = tracTracUsernameObject == null ? "" : (String) tracTracUsernameObject;
        String tracTracPassword = tracTracPasswordObject == null ? "" : (String) tracTracPasswordObject;
        
        return tracTracDomainFactory.createTracTracConfiguration((String) object.get(FieldNames.TT_CONFIG_NAME.name()),
                (String) object.get(FieldNames.TT_CONFIG_JSON_URL.name()),
                (String) object.get(FieldNames.TT_CONFIG_LIVE_DATA_URI.name()),
                (String) object.get(FieldNames.TT_CONFIG_STORED_DATA_URI.name()),
                courseDesignUpdateUri,
                tracTracUsername,
                tracTracPassword);
    }

}
