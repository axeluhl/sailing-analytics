package com.sap.sailing.domain.persistence.media;

import com.sap.sailing.domain.persistence.media.impl.MediaDBFactoryImpl;
import com.sap.sailing.mongodb.MongoDBService;

public interface MediaDBFactory {
    MediaDBFactory INSTANCE = new MediaDBFactoryImpl();
    
    MediaObjectFactory getDefaultDomainObjectFactory();
    MediaObjectFactory getDomainObjectFactory(MongoDBService mongoDBService);
    MediaDB getDefaultMediaDB();
    MediaDB getMediaDB(MongoDBService mongoDBService);
}
