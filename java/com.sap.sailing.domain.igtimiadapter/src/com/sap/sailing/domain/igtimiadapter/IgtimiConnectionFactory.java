package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.impl.IgtimiConnectionFactoryImpl;

public interface IgtimiConnectionFactory {
    static IgtimiConnectionFactory INSTANCE = new IgtimiConnectionFactoryImpl();

    /**
     * Using the "account" service, retrieves the {@link Account} information for this access token and stores the token
     * that authorizes this factory's client to access the account together with the {@link Account} data. This information is
     * used when a caller wants to {@link #connect(Account)} to a specific account on behalf of the {@link Client} to
     * which this factory belongs.
     */
    void registerAccountForWhichClientIsAuthorized(String accessToken) throws ClientProtocolException, IllegalStateException, IOException, ParseException;
    
    /**
     * Matches <code>eMail</code> with the e-mail information retrieved from the "account" service earlier when an access
     * token was registered.
     */
    Account getAccountByEmail(String eMail);
    
    IgtimiConnection connect(Account account);
}
