package com.sap.sailing.mongodb;

import com.mongodb.DB;
import com.sap.sailing.mongodb.internal.MongoDBServiceImpl;

public interface MongoDBService {

    MongoDBService INSTANCE = new MongoDBServiceImpl();

    DB getDB();

    MongoDBConfiguration getConfiguration();

    void setConfiguration(MongoDBConfiguration configuration);
}
