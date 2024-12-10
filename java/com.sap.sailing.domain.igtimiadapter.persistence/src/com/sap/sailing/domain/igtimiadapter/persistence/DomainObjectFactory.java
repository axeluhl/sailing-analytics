package com.sap.sailing.domain.igtimiadapter.persistence;

import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Resource;

public interface DomainObjectFactory {

    Iterable<TokenAndCreator> getAccessTokens();

    Iterable<Resource> getResources();

    Iterable<DataAccessWindow> getDataAccessWindows();

}
