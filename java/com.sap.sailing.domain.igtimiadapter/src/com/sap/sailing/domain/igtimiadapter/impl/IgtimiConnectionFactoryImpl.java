package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.ByteArrayInputStream;
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
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.Client;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.Permission;

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
    
    @Override
    public String getResourcesUrl(Permission permission, TimePoint startTime, TimePoint endTime, Iterable<String> serialNumbers, Iterable<String> streamIds, Account account) {
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
    public Account obtainAccessTokenFromAuthorizationCode(String code) throws ClientProtocolException, IOException, IllegalStateException, ParseException {
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
        client.setRedirectStrategy(new LaxRedirectStrategy());
        HttpGet get = new HttpGet(getOauthAuthorizeUrl());
        HttpResponse responseForAuthorize = client.execute(get);
        return signInAndReturnAuthorizationForm(client, responseForAuthorize, userEmail, userPassword);
    }

    private String signInAndReturnAuthorizationForm(DefaultHttpClient client, HttpResponse response,
            final String userEmail, final String userPassword) throws ParserConfigurationException, SAXException,
            IOException, UnsupportedEncodingException, ClientProtocolException, IllegalStateException,
            ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        final String[] action = new String[1];
        final Map<String, String> inputFieldsToSubmit = new HashMap<>();
        try {
            String pageContent = ConnectivityUtils.getContent(response);
            String unescapedHtml = StringEscapeUtils.unescapeHtml(pageContent);
            parser.parse(new ByteArrayInputStream(unescapedHtml.getBytes("UTF-8")), new DefaultHandler() {
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
     * @throws ClassCastException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
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

}
