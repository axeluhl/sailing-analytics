package com.sap.sailing.domain.igtimiadapter;

import javax.security.auth.callback.Callback;

/**
 * Represents what the Igtimi API or OAuth calls a "client." In particular, a client has an ID and a secret. The Igtimi
 * connector of which this interface is a part acts as a client. A client needs to be authorized by a user to be allowed
 * to access the user's data via the Igtimi web services API. Authorization works by directing the user to a URL using
 * his/her user agent (usually a browser) which is constructed by the {@link IgtimiConnectionFactory} and encodes a
 * callback URL that leads to the {@link Callback} servlet. The servlet records the access token that can be used for
 * Igtimi web service API calls that require access to the authenticated user's data.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Client {
    String getId();
    
    String getSecret();
    
    String getDefaultRedirectUri();

    String getRedirectUri(String redirectProtocol, String redirectHost, String redirectPort);
}
