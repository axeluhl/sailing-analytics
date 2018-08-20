package com.sap.sailing.mongodb.test;

import java.net.UnknownHostException;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.mongodb.MongoDBService;

/**
 * Reverses the wind directions of <em>all</em> wind records stored in the MongoDB that is obtained using
 * {@link Activator#getDB()}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ConvertWindData extends AbstractMongoDBTest {
    public ConvertWindData() throws UnknownHostException, MongoException {
        super();
    }

    public static void main(String[] args) {
        MongoObjectFactoryImpl mof = new MongoObjectFactoryImpl(MongoDBService.INSTANCE.getDB());
        DBCollection windTracksCollection = mof.getWindTrackCollection();
        for (DBObject dbo : windTracksCollection.find()) {
             windTracksCollection.remove(dbo);
            DBObject wind = (DBObject) dbo.get(FieldNames.WIND.name());
            wind.put(FieldNames.DEGREE_BEARING.name(),
                    new DegreeBearingImpl((Double) wind.get(FieldNames.DEGREE_BEARING.name())).reverse().getDegrees());
            windTracksCollection.insert(dbo);
        }
    }
}
