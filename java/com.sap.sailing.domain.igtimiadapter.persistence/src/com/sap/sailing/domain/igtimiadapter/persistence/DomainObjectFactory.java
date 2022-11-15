package com.sap.sailing.domain.igtimiadapter.persistence;

public interface DomainObjectFactory {

    Iterable<TokenAndCreator> getAccessTokens();

}
