package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.Client;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;

public class IgtimiConnectionFactoryImpl implements IgtimiConnectionFactory {
    private static final Logger logger = Logger.getLogger(IgtimiConnectionFactoryImpl.class.getName());
    
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
        Account account = new AccountImpl(new UserDeserializer().createUserFromJson(userJson));
        return account;
    }

    /**
     * @return trailing slash
     */
    private String getApiV1BaseUrl() {
        return getBaseUrl()+"/api/v1/";
    }

    private String getSignInUrl() {
        return "https://www.igtimi.com/users/sign_in";
    }

    private String getOauthTokenUrl() {
        return getBaseUrl()+"/oauth/token";
    }
    
    private String getOauthAuthorizeUrl() throws UnsupportedEncodingException {
        return getBaseUrl()+"/oauth/authorize?response_type=code&client_id="+getClient().getId()+"&redirect_uri="+URLEncoder.encode(getClient().getRedirectUri(), "UTF-8");
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
        return new IgtimiConnectionImpl(this, account);
    }

    private String getAccessTokenForAccount(Account account) {
        return accessTokensByAccount.get(account);
    }
    
    @Override
    public String getAccountUrl(Account account) {
        return getApiV1BaseUrl()+"account?"+getAccessTokenUrlParameter(account);
    }
    
    @Override
    public String getUsersUrl(Account account) {
        return getApiV1BaseUrl()+"users?"+getAccessTokenUrlParameter(account);
    }

    private String getAccessTokenUrlParameter(Account account) {
        return "access_token="+getAccessTokenForAccount(account);
    }

    @Override
    public String getAuthorizationUrl() {
        return getBaseUrl()+"/oauth";
    }

    private Client getClient() {
        return client;
    }

    @Override
    public String obtainAccessTokenFromAuthorizationCode(String code) throws ClientProtocolException, IOException, IllegalStateException, ParseException {
        HttpClient client = new SystemDefaultHttpClient();
        HttpPost post = new HttpPost(getOauthTokenUrl());
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        urlParameters.add(new BasicNameValuePair("client_id", getClient().getId()));
        urlParameters.add(new BasicNameValuePair("client_secret", getClient().getSecret()));
        urlParameters.add(new BasicNameValuePair("code", code));
        urlParameters.add(new BasicNameValuePair("redirect_uri", getClient().getRedirectUri()));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(post);
        JSONObject accessTokenJson = ConnectivityUtils.getJsonFromResponse(response);
        final String result;
        if (accessTokenJson.get("error") != null) {
            result = accessTokenJson.toString();
        } else {
            result = null;
            String accessToken = (String) accessTokenJson.get("access_token");
            registerAccountForWhichClientIsAuthorized(accessToken);
        }
        return result;
    }
    
    private HttpResponse signInAndReturnAuthorizationForm(DefaultHttpClient client, HttpResponse response,
            final String userEmail, final String userPassword) throws ParserConfigurationException, SAXException, IOException,
            UnsupportedEncodingException, ClientProtocolException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        final String[] action = new String[1];
        final Map<String, String> inputFieldsToSubmit = new HashMap<>();
        try {
            parser.parse(response.getEntity().getContent(), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {
                    if (qName.equals("form")) {
                        action[0] = attributes.getValue("action");
                    } else if (qName.equals("input")) {
                        if (attributes.getValue("value") != null && !attributes.getValue("value").isEmpty()) {
                            inputFieldsToSubmit.put(attributes.getValue("name"), attributes.getValue("value"));
                        } else if (attributes.getValue("id") != null) {
                            if (attributes.getValue("id").contains("email")) {
                                inputFieldsToSubmit.put(attributes.getValue("name"), userEmail);
                            } else if (attributes.getValue("id").contains("pass")) {
                                inputFieldsToSubmit.put(attributes.getValue("name"), userPassword);
                            }
                        }
                    }
                }
            });
        } catch (SAXParseException e) {
            // swallow; we try to grab what we can; let's hope it was enough...
        }
        response.getEntity().getContent().close();
        HttpResponse authorizationForm = ConnectivityUtils.postForm(getBaseUrl(), action[0], inputFieldsToSubmit, client, getSignInUrl());
        return authorizationForm;
    }
    
    /**
     * Tries to authorize our client on behalf of a user identified by e-mail and password.
     * 
     * @return the authorization code which can then be used to obtain a permanent access token to be used by our client
     *         to access data owned by the user identified by e-mail and password.
     */
    public String authorizeAndReturnAuthorizedCode(String userEmail, String userPassword) throws ClientProtocolException, IOException,
            IllegalStateException, ParserConfigurationException, SAXException {
        DefaultHttpClient client = new SystemDefaultHttpClient();
        CookieStore cookieStore = new BasicCookieStore();
        client.setCookieStore(cookieStore);
        HttpGet get = new HttpGet(getOauthAuthorizeUrl());
        HttpResponse responseForAuthorize = client.execute(get);
        HttpResponse authorizationForm = signInAndReturnAuthorizationForm(client, responseForAuthorize, userEmail, userPassword);
        return authorizeAndGetCode(authorizationForm, client);
    }

    /**
     * Parses the form in the <code>autorizationForm</code> response and posts it by submitting the form that contains
     * the commit button with the value "Authorize".
     * 
     * @return the code intercepted from the redirect location
     */
    private String authorizeAndGetCode(HttpResponse authorizationForm, DefaultHttpClient client) throws IllegalStateException, SAXException, IOException, ParserConfigurationException {
        final String[] code = new String[1];
        client.setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
                    throws ProtocolException {
                code[0] = response.getHeaders("Location")[0].getValue().substring(getClient().getRedirectUri().length() + "?".length());
                return response.getStatusLine().getStatusCode() >= 300
                        && response.getStatusLine().getStatusCode() < 400;
            }
        });
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        final String[] action = new String[1];
        final Map<String, String> inputFieldsToSubmit = new HashMap<>();
        try {
            parser.parse(authorizationForm.getEntity().getContent(), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {
                    // TODO stop after the Authorization form
                    if (qName.equals("form")) {
                        action[0] = attributes.getValue("action");
                    } else if (qName.equals("input")) {
                        if (attributes.getValue("value") != null && !attributes.getValue("value").isEmpty()) {
                            inputFieldsToSubmit.put(attributes.getValue("name"), attributes.getValue("value"));
                        }
                    }
                }
            });
        } catch (SAXParseException e) {
            // swallow; we try to grab what we can; let's hope it was enough...
        }
        authorizationForm.getEntity().getContent().close();
        HttpResponse authorizationResponse = ConnectivityUtils.postForm(getBaseUrl(), action[0], inputFieldsToSubmit, client, /* referer */ getSignInUrl());
        // TODO remove debug output:
        String content = ConnectivityUtils.getContent(authorizationResponse);
        logger.info("Authorization response: "+content);
        return code[0];
    }

}
