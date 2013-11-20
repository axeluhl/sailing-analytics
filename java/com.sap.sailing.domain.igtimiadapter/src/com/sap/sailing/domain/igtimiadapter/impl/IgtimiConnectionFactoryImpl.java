package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.Client;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;

public class IgtimiConnectionFactoryImpl implements IgtimiConnectionFactory {
    private final Map<Account, String> accessTokensByAccount;
    private Map<String, Account> accountsByEmail;
    private final Client client;
    
    public IgtimiConnectionFactoryImpl(Client client) {
        this.accessTokensByAccount = new HashMap<>();
        this.accountsByEmail = new HashMap<>();
        this.client = client;
    }
    
    @Override
    public Account registerAccountForWhichClientIsAuthorized(String accessToken) throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        Account account = getAccount(accessToken);
        accountsByEmail.put(account.getUser().getEmail(), account);
        accessTokensByAccount.put(account, accessToken);
        return account;
    }

    private Account getAccount(String accessToken) throws ClientProtocolException, IOException, IllegalStateException, ParseException {
        HttpClient client = new SystemDefaultHttpClient();
        HttpGet getAccount = new HttpGet(getApiV1BaseUrl()+"account?access_token="+accessToken);
        HttpResponse accountResponse = client.execute(getAccount);
        JSONObject accountJson = ConnectivityUtils.getJsonFromResponse(accountResponse);
        JSONObject userJson = (JSONObject) accountJson.get("user");
        Account account = new AccountImpl(new UserImpl((Long) userJson.get("id"),
                (String) userJson.get("first_name"),
                (String) userJson.get("surname"),
                (String) userJson.get("email")));
        return account;
    }

    /**
     * @return trailing slash
     */
    private String getApiV1BaseUrl() {
        return getBaseUrl()+"/api/v1/";
    }

    /**
     * @return no trailing slash
     */
    private String getBaseUrl() {
        return "https://www.igtimi.com";
    }

    @Override
    public Account getAccountByEmail(String eMail) {
        return accountsByEmail.get(eMail);
    }

    @Override
    public IgtimiConnection connect(Account account) {
        return new IgtimiConnectionImpl(client, account);
    }

    @Override
    public String getAuthorizationUrl() {
        return getBaseUrl()+"/oauth";
    }

}
