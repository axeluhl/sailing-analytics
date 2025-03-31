package com.sap.sailing.domain.yellowbrickadapter.persistence;


import com.mongodb.DBObject;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickConfiguration;
import com.sap.sailing.domain.yellowbrickadapter.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sse.mongodb.MongoDBService;

/**
 * Offers methods to construct {@link DBObject MongoDB objects} from domain objects.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface MongoObjectFactory {
    MongoObjectFactory INSTANCE = new MongoObjectFactoryImpl(MongoDBService.INSTANCE.getDB());

    /**
     * Inserts/updates based on the {@link YellowBrickConfiguration#getCreatorName() creator name} and the
     * {@link YellowBrickConfiguration#getRaceUrl() race URL}. Any config previously contained in the DB with equal
     * creator name and race URL will be replaced / updated. Afterwards,
     * {@link DomainObjectFactory#getYellowBrickConfigurations()} called for the same <code>database</code> will return
     * an equal <code>tracTracConfiguration</code> in its results.
     */
    void createYellowBrickConfiguration(YellowBrickConfiguration yellowBrickConfiguration);

    /**
     * When the configuration has a {@code null} value for the {@link YellowBrickConfiguration#getPassword() password},
     * the password in the DB will not be changed.
     */
    void updateYellowBrickConfiguration(YellowBrickConfiguration yellowBrickConfiguration);

    void deleteYellowBrickConfiguration(String creatorName, String raceUrl);

    void clear();
}
