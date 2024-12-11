package com.sap.sailing.domain.igtimiadapter;

import java.net.URL;

public interface IgtimiConnectionFactory {
    IgtimiConnection createConnection(URL baseUrl, String bearerToken);
}
