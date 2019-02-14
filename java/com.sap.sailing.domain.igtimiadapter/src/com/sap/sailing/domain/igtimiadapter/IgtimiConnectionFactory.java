package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

/**
 * A connection factory is bound to a {@link Client} and allows that client to obtain a connection to the Igtimi
 * services on behalf of an {@link Account} for which an authorization token must have been
 * {@link #registerAccountForWhichClientIsAuthorized(String) registered} with this factory previously.
 * 
 * @author Axel Uhl (d043530)
 */
public interface IgtimiConnectionFactory {
    Iterable<Account> getAllAccounts();

    IgtimiConnection connect(Account account);
    
    /**
     * Removes the account and the credentials associated with it permanently. The account is identified by the e-mail
     * address of the owner of the data to which the account granted access. See also {@link #getExistingAccountByEmail(String)}.
     */
    void removeAccount(Account account);
    
    /**
     * Obtains a URL that a user agent (e.g., a web browser) can be sent to in order to allow that user to authenticate
     * and then authorize this factory's {@link Client} for accessing the user's Igtimi data. The URL is chosen such
     * that it redirects to the {@link com.sap.sailing.domain.igtimiadapter.oauth.AuthorizationCallback} with the
     * <code>code</code> response type.
     */
    String getAuthorizationUrl(String redirectProtocol, String redirectHost, String redirectPort) throws MalformedURLException, UnsupportedEncodingException;
    
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
    Account getExistingAccountByEmail(String eMail);
    
    /**
     * Tries to authorize our client on behalf of a user identified by e-mail and password. If successful, the
     * account data and the relevant OAuth access token will be stored persistently.
     * 
     * @return the account with which a caller can then {@link #connect obtain a connection} for the data that the user
     *         identified by <code>userEmail</code> and <code>userPassword</code> shares with our client.
     */
    Account createAccountToAccessUserData(String userEmail, String userPassword) throws ClientProtocolException,
            IOException, IllegalStateException, ParserConfigurationException, SAXException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, ClassCastException, ParseException;

}
