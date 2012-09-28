package com.sap.sailing.domain.persistence.media.impl;

import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.persistence.media.MediaObjectFactory;
import com.sap.sailing.mongodb.MongoDBService;

public class MediaDBFactoryImpl implements MediaDBFactory {
    private final MediaObjectFactory defaultMediaObjectFactory;
    private final MediaDB defaultMediaDB;
    
    public MediaDBFactoryImpl() {
        super();
        this.defaultMediaObjectFactory = new MediaObjectFactoryImpl(MongoDBService.INSTANCE.getDB());
        this.defaultMediaDB = new MediaDBImpl(MongoDBService.INSTANCE.getDB());
    }

    @Override
    public MediaObjectFactory getDefaultDomainObjectFactory() {
        return defaultMediaObjectFactory;
    }

    @Override
    public MediaObjectFactory getDomainObjectFactory(MongoDBService mongoDBService) {
        return new MediaObjectFactoryImpl(mongoDBService.getDB());
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
