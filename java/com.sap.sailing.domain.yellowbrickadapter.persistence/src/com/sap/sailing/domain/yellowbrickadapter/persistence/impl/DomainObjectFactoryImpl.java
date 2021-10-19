package com.sap.sailing.domain.yellowbrickadapter.persistence.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickConfiguration;
import com.sap.sailing.domain.yellowbrickadapter.impl.YellowBrickConfigurationImpl;
import com.sap.sailing.domain.yellowbrickadapter.persistence.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final MongoDatabase database;

    public DomainObjectFactoryImpl(MongoDatabase db) {
        super();
        this.database = db;
    }

    @Override
    public Iterable<YellowBrickConfiguration> getYellowBrickConfigurations() {
        final List<YellowBrickConfiguration> result = new ArrayList<YellowBrickConfiguration>();
        try {
            MongoCollection<org.bson.Document> ybConfigs = database.getCollection(CollectionNames.YELLOWBRICK_CONFIGURATIONS.name());
            for (Document o : ybConfigs.find()) {
                YellowBrickConfiguration ybConfig = loadYellowBrickConfiguration(o);
                result.add(ybConfig);
            }
        } catch (Exception e) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded YellowBrick configurations. Check MongoDB settings.", e);
        }
        return result;
    }
    
    private YellowBrickConfiguration loadYellowBrickConfiguration(Document object) {
        String name = (String) object.get(FieldNames.YB_CONFIG_NAME.name());
        Object usernameObject = object.get(FieldNames.YB_CONFIG_USERNAME.name());
        Object passwordObject = object.get(FieldNames.YB_CONFIG_PASSWORD.name());
        String username = usernameObject == null ? "" : (String) usernameObject;
        String password = passwordObject == null ? "" : new String(Base64.getDecoder().decode((String) passwordObject));
        String creatorName = (String) object.get(FieldNames.YB_CONFIG_CREATOR_NAME.name());
        String raceURL = (String) object.get(FieldNames.YB_CONFIG_RACE_URL.name());
        final boolean needsUpdate = (creatorName == null);
        if (needsUpdate) {
            // No creator is set yet -> existing configurations are assumed to belong to the admin
            creatorName = "admin";
        }
        final YellowBrickConfiguration loadedYellowBrickConfiguration = new YellowBrickConfigurationImpl(name, raceURL, username, password, creatorName);
        if (needsUpdate) {
            // recreating the config on the DB because the composite key changed
            new MongoObjectFactoryImpl(database).deleteYellowBrickConfiguration(null, raceURL);
            new MongoObjectFactoryImpl(database).createYellowBrickConfiguration(loadedYellowBrickConfiguration);
        }
        return loadedYellowBrickConfiguration;
    }

}
