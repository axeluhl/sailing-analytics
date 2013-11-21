package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

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
     * @return <code>null</code> if everything went OK; some indication as to the error state otherwise
     */
    String obtainAccessTokenFromAuthorizationCode(String code) throws UnsupportedEncodingException,
            ClientProtocolException, IOException, IllegalStateException, ParseException;

    String getAccountUrl(Account account);

    String getUsersUrl(Account account);
}
