package com.sap.sailing.domain.persistence.media.impl;

import java.util.logging.Logger;

import com.mongodb.DB;
import com.sap.sailing.domain.persistence.media.MediaObjectFactory;

public class MediaObjectFactoryImpl implements MediaObjectFactory {

    private static final Logger logger = Logger.getLogger(MediaObjectFactoryImpl.class.getName());

    private final DB database;

    public MediaObjectFactoryImpl(DB db) {
        super();
        this.database = db;
    }

}
