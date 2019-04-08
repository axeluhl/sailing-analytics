package com.sap.sailing.domain.persistence.media;

import com.sap.sailing.domain.persistence.media.impl.MediaDBFactoryImpl;
import com.sap.sse.mongodb.MongoDBService;

public interface MediaDBFactory {
    MediaDBFactory INSTANCE = new MediaDBFactoryImpl();
    
    MediaDB getDefaultMediaDB();
    MediaDB getMediaDB(MongoDBService mongoDBService);
}
