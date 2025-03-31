package com.sap.sailing.domain.persistence.media.impl;

import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sse.mongodb.MongoDBService;

public class MediaDBFactoryImpl implements MediaDBFactory {
    private final MediaDB defaultMediaDB;
    
    public MediaDBFactoryImpl() {
        super();
        this.defaultMediaDB = new MediaDBImpl(MongoDBService.INSTANCE.getDB());
    }

    @Override
    public MediaDB getDefaultMediaDB() {
        return defaultMediaDB;
    }

    @Override
    public MediaDB getMediaDB(MongoDBService mongoDBService) {
        return new MediaDBImpl(mongoDBService.getDB());
    }

}
