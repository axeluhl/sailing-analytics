package com.sap.sailing.domain.igtimiadapter.persistence;

public class TokenAndCreator {

    private final String creatorName;
    private final String accessToken;

    public TokenAndCreator(String creatorName, String accessToken) {
        this.creatorName = creatorName;
        this.accessToken = accessToken;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getAccessToken() {
        return accessToken;
    }

}
