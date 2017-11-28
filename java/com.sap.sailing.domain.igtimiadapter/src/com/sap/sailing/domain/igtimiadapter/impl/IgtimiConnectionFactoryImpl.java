package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
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
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.igtimiadapter.persistence.MongoObjectFactory;
import com.sap.sse.common.TimePoint;
import com.sap.sse.util.LaxRedirectStrategyForAllRedirectResponseCodes;

public class IgtimiConnectionFactoryImpl implements IgtimiConnectionFactory {
    private static final Logger logger = Logger.getLogger(IgtimiConnectionFactoryImpl.class.getName());
    
    private final Map<Account, String> accessTokensByAccount;
    private final Map<String, Account> accountsByEmail;
    private final Map<Account, IgtimiConnection> connectionsByAccount;
    private final Client client;
    private final MongoObjectFactory mongoObjectFactory;
    
    public IgtimiConnectionFactoryImpl(Client client, DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory) {
        this.accessTokensByAccount = new HashMap<>();
        this.accountsByEmail = new HashMap<>();
        connectionsByAccount = new HashMap<>();
        this.client = client;
        this.mongoObjectFactory = mongoObjectFactory;
        for (String accessToken : domainObjectFactory.getAccessTokens()) {
            try {
                registerAccountForWhichClientIsAuthorized(accessToken);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error registering Igtimi access token "+accessToken+"; probably the access token was revoked or expired.", e);
                mongoObjectFactory.removeAccessToken(accessToken);
            }
        }
    }
    
    @Override
    public Account registerAccountForWhichClientIsAuthorized(String accessToken) throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        Account account = getAccount(accessToken);
        accountsByEmail.put(account.getUser().getEmail(), account);
        accessTokensByAccount.put(account, accessToken);
        mongoObjectFactory.storeAccessToken(accessToken);
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
        return getBaseUrl()+"/users/sign_in";
    }

    private String getOauthTokenUrl() {
        return getBaseUrl()+"/oauth/token";
    }
    
    private String getOauthAuthorizeUrl() throws UnsupportedEncodingException {
        return getBaseUrl()+"/oauth/authorize?response_type=code&client_id="+getClient().getId()+"&redirect_uri="+URLEncoder.encode(getClient().getDefaultRedirectUri(), "UTF-8");
    }
    
    /**
     * @return no trailing slash
     */
    private String getBaseUrl() {
//        return "http://staging.igtimi.com"; // Use this for testing a staged Igtimi server version
        return "https://www.igtimi.com";
    }

    @Override
    public Account getExistingAccountByEmail(String eMail) {
        return accountsByEmail.get(eMail);
    }
    
    @Override
    public Iterable<Account> getAllAccounts() {
        return new ArrayList<Account>(accountsByEmail.values());
    }

    public IgtimiConnection getConnectionOfAccount(Account account) {
        return connectionsByAccount.get(account);
    }
    
    @Override
    public IgtimiConnection connect(Account account) {
        IgtimiConnection connection;
        synchronized (connectionsByAccount) {
            connection = connectionsByAccount.get(account);
            if (connection == null) {
                connection = new IgtimiConnectionImpl(this, account);
                connectionsByAccount.put(account, connection);
            }
        }
        return connection;
    }

    private String getAccessTokenForAccount(Account account) {
        return accessTokensByAccount.get(account);
    }
    
    public String getAccountUrl(Account account) {
        return getApiV1BaseUrl()+"account?"+getAccessTokenUrlParameter(account);
    }
    
    /**
     * Retrieves the JSON object to send in its string-serialized form to a web socket connection in order to receive
     * live data from the units whose IDs are specified by <code>unitIds</code>. The sending units are expected to
     * belong to the user account to which this factory's application client has been granted permission.
     * 
     * @param account
     *            represents this factory's client's permissions to access a user account's data
     * @param deviceIds
     *            IDs of the transmitting units expected to be visible to the account's {@link Account#getUser() user's}
     */
    public JSONObject getWebSocketConfigurationMessage(Account account, Iterable<String> deviceIds) {
        JSONObject result = new JSONObject();
        result.put("access_token", getAccessTokenForAccount(account));
        JSONArray deviceIdsJson = new JSONArray();
        result.put("devices", deviceIdsJson);
        for (String deviceId : deviceIds) {
            deviceIdsJson.add(deviceId);
        }
        return result;
    }
    
    public String getUsersUrl(Account account) {
        return getApiV1BaseUrl()+"users?"+getAccessTokenUrlParameter(account);
    }
    
    public String getUserUrl(long id, Account account) {
        return getApiV1BaseUrl()+"users/"+id+"?"+getAccessTokenUrlParameter(account);
    }

    public String getGroupsUrl(Account account) {
        return getApiV1BaseUrl()+"groups?"+getAccessTokenUrlParameter(account);
    }
    
    public String getGroupUrl(long id, Account account) {
        return getApiV1BaseUrl()+"groups/"+id+"?"+getAccessTokenUrlParameter(account);
    }

    public String getSessionUrl(long id, Account account) {
        return getApiV1BaseUrl()+"sessions/"+id+"?"+getAccessTokenUrlParameter(account);
    }

    public String getOwnedDevicesUrl(Account account) {
        return getApiV1BaseUrl()+"devices/?"+getAccessTokenUrlParameter(account);
    }

    public String getDataAccessWindowsUrl(Permission permission, TimePoint startTime, TimePoint endTime,
            Iterable<String> deviceSerialNumbers, Account account) {
        StringBuilder url = new StringBuilder(getApiV1BaseUrl());
        url.append("devices/data_access_windows?type=");
        url.append(permission.name());
        if (startTime != null) {
            url.append("&start_time=");
            url.append(startTime.asMillis());
        }
        if (endTime != null) {
            url.append("&end_time=");
            url.append(endTime.asMillis());
        }
        if (deviceSerialNumbers != null) {
            for (String serialNumber : deviceSerialNumbers) {
                url.append("&serial_numbers[]=");
                url.append(serialNumber);
            }
        }
        url.append("&");
        url.append(getAccessTokenUrlParameter(account));
        return url.toString();
    }

    public String getLatestDatumUrl(Iterable<String> deviceSerialNumbers, Type type, Account account) {
        StringBuilder url = new StringBuilder(getApiV1BaseUrl());
        url.append("resources/data/latest?type=");
        url.append(type.getCode());
        for (String deviceSerialNumber : deviceSerialNumbers) {
            url.append("&serial_numbers[]=");
            url.append(deviceSerialNumber);
        }
        url.append("&");
        url.append(getAccessTokenUrlParameter(account));
        return url.toString();
    }

    public String getResourceDataUrl(TimePoint startTime, TimePoint endTime, Iterable<String> serialNumbers,
            Map<Type, Double> typeAndCompression, Account account) {
        StringBuilder url = new StringBuilder(getApiV1BaseUrl());
        url.append("resources/data?start_time=");
        url.append(startTime.asMillis());
        url.append("&end_time=");
        url.append(endTime.asMillis());
        for (String serialNumber : serialNumbers) {
            url.append("&serial_numbers[]=");
            url.append(serialNumber);
        }
        for (Entry<Type, Double> e : typeAndCompression.entrySet()) {
            url.append("&types["+e.getKey().getCode()+"]="+e.getValue());
        }
        url.append("&");
        url.append(getAccessTokenUrlParameter(account));
        return url.toString();
    }

    public String getResourcesUrl(Permission permission, TimePoint startTime, TimePoint endTime,
            Iterable<String> serialNumbers, Iterable<String> streamIds, Account account) {
        StringBuilder url = new StringBuilder(getApiV1BaseUrl());
        url.append("resources?");
        url.append("permission=");
        url.append(permission.name());
        if (startTime != null) {
            url.append("&start_time=");
            url.append(startTime.asMillis());
        }
        if (endTime != null) {
            url.append("&end_time=");
            url.append(endTime.asMillis());
        }
        if (serialNumbers != null) {
            for (String serialNumber : serialNumbers) {
                url.append("&serial_numbers[]=");
                url.append(serialNumber);
            }
        }
        if (streamIds != null) {
            for (String streamId : streamIds) {
                url.append("&stream_ids[]=");
                url.append(streamId);
            }
        }
        url.append("&");
        url.append(getAccessTokenUrlParameter(account));
        return url.toString();
    }

    
    public String getSessionsUrl(Iterable<Long> sessionIds, Boolean isPublic, Integer limit, Boolean includeIncomplete, Account account) {
        StringBuilder url = new StringBuilder(getApiV1BaseUrl());
        url.append("sessions?");
        url.append(getAccessTokenUrlParameter(account));
        if (sessionIds != null) {
            for (Long sessionId : sessionIds) {
                url.append("&ids[]=");
                url.append(sessionId);
            }
        }
        if (isPublic != null) {
            url.append("&public=");
            url.append(isPublic.toString().toLowerCase());
        }
        if (limit != null) {
            url.append("&limit=");
            url.append(limit);
        }
        if (includeIncomplete != null) {
            url.append("&include_incomplete");
            url.append(includeIncomplete.toString().toLowerCase());
        }
        return url.toString();
    }

    private String getAccessTokenUrlParameter(Account account) {
        return "access_token="+getAccessTokenForAccount(account);
    }

    @Override
    public String getAuthorizationUrl(String redirectProtocol, String redirectHost, String redirectPortAsString) throws MalformedURLException, UnsupportedEncodingException {
        final URL redirectTarget;
        int redirectPort;
        if (redirectPortAsString == null || redirectPortAsString.trim().isEmpty() || (redirectPort=Integer.valueOf(redirectPortAsString)) == 0) {
            redirectTarget = new URL(redirectProtocol, redirectHost, /* file */ "");
        } else {
            redirectTarget = new URL(redirectProtocol, redirectHost, redirectPort, /* file */ "");
        }
        return getBaseUrl()+"/oauth/authorize?response_type=code&client_id="+getClient().getId()+
                "&redirect_uri="+URLEncoder.encode(client.getDefaultRedirectUri(), "UTF-8")
                + "&state="+URLEncoder.encode(redirectTarget.toString(), "UTF-8");
    }

    private Client getClient() {
        return client;
    }

    /**
     * Uses the /oauth/token service to obtain and {@link #registerAccountForWhichClientIsAuthorized(String) register}
     * an access token for an authorization code which encodes the authorization given by a user to this factory's
     * client.
     * 
     * @return the account encoding the application that is authorized for a user's account
     * @throws RuntimeException in case there was an error while retrieving the token
     */
    public Account obtainAccessTokenFromAuthorizationCode(String code) throws ClientProtocolException, IOException, IllegalStateException, ParseException {
        HttpClient client = new SystemDefaultHttpClient();
        HttpPost post = new HttpPost(getOauthTokenUrl());
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        urlParameters.add(new BasicNameValuePair("client_id", getClient().getId()));
        urlParameters.add(new BasicNameValuePair("client_secret", getClient().getSecret()));
        urlParameters.add(new BasicNameValuePair("code", code));
        urlParameters.add(new BasicNameValuePair("redirect_uri", getClient().getDefaultRedirectUri()));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(post);
        JSONObject accessTokenJson = ConnectivityUtils.getJsonFromResponse(response);
        final Account result;
        if (accessTokenJson.get("error") != null) {
            throw new RuntimeException(accessTokenJson.toString());
        } else {
            String accessToken = (String) accessTokenJson.get("access_token");
            result = registerAccountForWhichClientIsAuthorized(accessToken);
        }
        return result;
    }

    /**
     * Tries to authorize our client on behalf of a user identified by e-mail and password.
     * 
     * @return the authorization code which can then be used to obtain a permanent access token to be used by our client
     *         to access data owned by the user identified by e-mail and password.
     */
    public String authorizeAndReturnAuthorizedCode(String userEmail, String userPassword)
            throws ClientProtocolException, IOException, IllegalStateException, ParserConfigurationException,
            SAXException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
        logger.info("Trying to authorize application client " + getClient().getId() + " for user " + userEmail);
        DefaultHttpClient client = new SystemDefaultHttpClient();
        CookieStore cookieStore = new BasicCookieStore();
        client.setCookieStore(cookieStore);
        client.setRedirectStrategy(new LaxRedirectStrategyForAllRedirectResponseCodes());
        HttpGet get = new HttpGet(getOauthAuthorizeUrl());
        HttpResponse responseForAuthorize = client.execute(get);
        return signInAndReturnAuthorizationForm(client, responseForAuthorize, userEmail, userPassword);
    }
    
    @Override
    public Account createAccountToAccessUserData(String userEmail, String userPassword) throws ClientProtocolException,
            IOException, IllegalStateException, ParserConfigurationException, SAXException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, ClassCastException, ParseException {
        final Account result;
        String code = authorizeAndReturnAuthorizedCode(userEmail, userPassword);
        if (code != null) {
            result = obtainAccessTokenFromAuthorizationCode(code);
        } else {
            result = null;
        }
        return result;
    }

    private String signInAndReturnAuthorizationForm(DefaultHttpClient client, HttpResponse response,
            final String userEmail, final String userPassword) throws ParserConfigurationException, SAXException,
            IOException, UnsupportedEncodingException, ClientProtocolException, IllegalStateException,
            ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        final String[] action = new String[1];
        final Map<String, String> inputFieldsToSubmit = new HashMap<>();
        try {
            String pageContent = ConnectivityUtils.getContent(response).replaceAll("<link (.*[^/])>", "<link $1 />");
            parser.parse(new ByteArrayInputStream(pageContent.getBytes("UTF-8")), new DefaultHandler() {
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
                    super.startElement(uri, localName, qName, attributes);
                }
            });
        } catch (SAXParseException e) {
            // swallow; we try to grab what we can; let's hope it was enough...
            logger.log(Level.FINE, "Exception trying to parse Igtimi authorization document", e);
        }
        response.getEntity().getContent().close();
        final RedirectStrategy oldRedirectStrategy = client.getRedirectStrategy();
        final RedirectStrategyExtractingAuthorizationCode codeExtractor = new RedirectStrategyExtractingAuthorizationCode(oldRedirectStrategy);
        client.setRedirectStrategy(codeExtractor);
        logger.info("Posting sign-in form for user "+userEmail);
        HttpResponse authorizationForm = ConnectivityUtils.postForm(getBaseUrl(), action[0], inputFieldsToSubmit, client, getSignInUrl());
        if (codeExtractor.getCode() == null) {
            logger.info("Client app "+getClient().getId()+" doesn't seem to be authorized yet.");
            authorizeAndGetCode(authorizationForm, client);
        } else {
            logger.info("Client app "+getClient().getId()+" seems to be authorized for user "+userEmail+" already.");
        }
        return codeExtractor.getCode();
    }
    
    /**
     * Parses the form in the <code>autorizationForm</code> response and posts it by submitting the form that contains
     * the commit button with the value "Authorize". If a redirect strategy is set on the <code>client</code> it will
     * see the redirect URL in the <code>Location</code> header. The redirection target is closed immediately.
     */
    private void authorizeAndGetCode(HttpResponse authorizationForm, DefaultHttpClient client)
            throws IllegalStateException, SAXException, IOException, ParserConfigurationException,
            ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
        // If the user already authorized the app, an empty document will be returned
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        final String action[] = new String[1];
        final Map<String, String> inputFieldsToSubmit = new HashMap<>();
        try {
            String pageContent = ConnectivityUtils.getContent(authorizationForm);
            String unescapedHtml = StringEscapeUtils.unescapeHtml(pageContent);
            final boolean[] completedAuthorizeForm = new boolean[1];
            parser.parse(new ByteArrayInputStream(unescapedHtml.getBytes("UTF-8")), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {
                    if (!completedAuthorizeForm[0]) {
                        if (qName.equals("form")) {
                            action[0] = attributes.getValue("action");
                        } else if (qName.equals("input")) {
                            if (attributes.getValue("value") != null && !attributes.getValue("value").isEmpty()) {
                                inputFieldsToSubmit.put(attributes.getValue("name"), attributes.getValue("value"));
                            }
                        }
                    }
                    super.startElement(uri, localName, qName, attributes);
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equals("form") && inputFieldsToSubmit.get("commit").equals("Authorize")) {
                        completedAuthorizeForm[0] = true;
                    }
                    super.endElement(uri, localName, qName);
                }
            });
        } catch (SAXParseException e) {
            // swallow; we try to grab what we can; let's hope it was enough...
            logger.warning("The authorization form was not well-formed. Extracted the following parameters so far: "+inputFieldsToSubmit+" ("+e.getMessage()+")");
        }
        authorizationForm.getEntity().getContent().close();
        logger.info("Posting authorization form to authorize client "+getClient().getId()+" for access to data of user");
        HttpResponse authorizationResponse = ConnectivityUtils.postForm(getBaseUrl(), action[0], inputFieldsToSubmit, client, /* referer */ getSignInUrl());
        authorizationResponse.getEntity().getContent().close();
    }

    public HttpClient getHttpClient() {
        HttpClient client = new SystemDefaultHttpClient();
        return client;
    }
    
    public Iterable<URI> getWebsocketServers() throws IllegalStateException, ClientProtocolException, IOException, ParseException, URISyntaxException {
        HttpClient client = getHttpClient();
        HttpGet getWebsocketServers = new HttpGet(getBaseUrl()+"/server_listers/web_sockets");
        JSONObject serversJson = ConnectivityUtils.getJsonFromResponse(client.execute(getWebsocketServers));
        final List<URI> result = new ArrayList<>();
        for (Object serverUrl : (JSONArray) serversJson.get("web_socket_servers")) {
            URI uri = new URI((String) serverUrl);
            result.add(uri);
        }
        // sort those to the front that don't do port 443 nor wss://
        Collections.shuffle(result); // shuffle as a failover strategy
        logger.info("Trying Igtimi WebSocket servers in the following order: "+result);
        return result;
    }

    @Override
    public void removeAccount(String eMail) {
        Account account = getExistingAccountByEmail(eMail);
        if (account != null) {
            String accessToken = accessTokensByAccount.remove(account);
            accountsByEmail.remove(eMail);
            mongoObjectFactory.removeAccessToken(accessToken);
        }
    }

}
