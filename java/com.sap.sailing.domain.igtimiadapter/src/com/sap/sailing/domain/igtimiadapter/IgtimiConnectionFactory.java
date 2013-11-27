package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.oauth.AuthorizationCallback;

/**
 * A connection factory is bound to a {@link Client} and allows that client to obtain a connection to the Igtimi
 * services on behalf of an {@link Account} for which an authorization token must have been
 * {@link #registerAccountForWhichClientIsAuthorized(String) registered} with this factory previously.
 * 
 * @author Axel Uhl (d043530)
 */
public interface IgtimiConnectionFactory {
    /**
     * Obtains a URL that a user agent (e.g., a web browser) can be sent to in order to allow that user to authenticate
     * and then authorize this factory's {@link Client} for accessing the user's Igtimi data. The URL is chosen such that
     * it redirects to the {@link AuthorizationCallback} with the <code>code</code> response type.
     */
    String getAuthorizationUrl();
    
    /**
     * Using the "account" service, retrieves the {@link Account} information for this access token and stores the token
     * that authorizes this factory's client to access the account together with the {@link Account} data. This information is
     * used when a caller wants to {@link #connect(Account)} to a specific account on behalf of the {@link Client} to
     * which this factory belongs.
     * @return 
     */
    Account registerAccountForWhichClientIsAuthorized(String accessToken) throws ClientProtocolException,
            IllegalStateException, IOException, ParseException;

    /**
     * Matches <code>eMail</code> with the e-mail information retrieved from the "account" service earlier when an access
     * token was registered.
     */
    Account getAccountByEmail(String eMail);
    
    IgtimiConnection connect(Account account);
    
    /**
     * Uses the /oauth/token service to obtain and {@link #registerAccountForWhichClientIsAuthorized(String) register}
     * an access token for an authorization code which encodes the authorization given by a user to this factory's
     * client.
     * 
     * @return the account encoding the application that is authorized for a user's account
     * @throws RuntimeException in case there was an error while retrieving the token
     */
    Account obtainAccessTokenFromAuthorizationCode(String code) throws UnsupportedEncodingException,
            ClientProtocolException, IOException, IllegalStateException, ParseException;

    String getAccountUrl(Account account);

    String getUsersUrl(Account account);

    String getResourcesUrl(Permission permission, TimePoint startTime, TimePoint endTime,
            Iterable<String> serialNumbers, Iterable<String> streamIds, Account account);

    String getResourceDataUrl(TimePoint startTime, TimePoint endTime, Iterable<String> serialNumbers,
            Map<Type, Double> typeAndCompression, Account account);

    Iterable<URI> getWebsocketServers() throws IllegalStateException, ClientProtocolException, IOException, ParseException, URISyntaxException;

    HttpClient getHttpClient();
}
