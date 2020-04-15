package com.sap.sailing.mongodb.test;

import java.net.UnknownHostException;

import org.bson.Document;

import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
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
        MongoCollection<Document> windTracksCollection = mof.getWindTrackCollection();
        for (Document dbo : windTracksCollection.find()) {
             windTracksCollection.deleteOne(dbo);
            DBObject wind = (DBObject) dbo.get(FieldNames.WIND.name());
            wind.put(FieldNames.DEGREE_BEARING.name(),
                    new DegreeBearingImpl((Double) wind.get(FieldNames.DEGREE_BEARING.name())).reverse().getDegrees());
            windTracksCollection.insertOne(dbo);
        }
    }
}
