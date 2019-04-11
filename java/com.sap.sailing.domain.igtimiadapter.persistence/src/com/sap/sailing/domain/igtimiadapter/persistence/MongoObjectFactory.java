package com.sap.sailing.domain.igtimiadapter.persistence;

public interface MongoObjectFactory {

    void storeAccessToken(String creatorName, String accessToken);

    void removeAccessToken(String creatorName, String accessToken);

}
