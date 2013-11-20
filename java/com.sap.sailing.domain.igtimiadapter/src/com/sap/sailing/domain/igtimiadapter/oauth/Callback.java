package com.sap.sailing.domain.igtimiadapter.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnectorFactory;

@Path(Callback.V1_AUTHORIZATIONCALLBACK)
public class Callback {
    private static final String CLIENT_SECRET = "537dbd14a84fcb470c91d85e8c4f8f7a356ac5ffc8727594d1bfe900ee5942ef";
    private static final String CLIENT_ID = "d29eae61621af3057db0e638232a027e96b1d2291b1b89a1481dfcac075b0bf4";
    static final String V1_AUTHORIZATIONCALLBACK = "/v1/authorizationcallback";
    private static final String OAUTH_TOKEN_URL = "https://www.igtimi.com/oauth/token";
    private static final String USER_EMAIL = "axel.uhl@gmx.de";
    private static final String USER_PASSWORD = "123456";
    private static final String baseUrl = "https://www.igtimi.com";
    private static final String REDIRECT_URL = "http://sapsailing.com";

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/")
    public Response obtainAccessToken(@QueryParam("code") String code) throws ClientProtocolException, IOException, IllegalStateException, ParseException, URISyntaxException {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(OAUTH_TOKEN_URL);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        urlParameters.add(new BasicNameValuePair("client_id", CLIENT_ID));
        urlParameters.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
        urlParameters.add(new BasicNameValuePair("code", code));
        urlParameters.add(new BasicNameValuePair("redirect_uri", REDIRECT_URL));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(post);
        JSONParser jsonParser = new JSONParser();
        final Header contentEncoding = response.getEntity().getContentEncoding();
        final Reader reader;
        if (contentEncoding == null) {
            reader = new InputStreamReader(response.getEntity().getContent());
        } else {
            reader = new InputStreamReader(response.getEntity().getContent(), contentEncoding.getValue());
        }
        JSONObject accessTokenJson = (JSONObject) jsonParser.parse(reader);
        String accessToken = (String) accessTokenJson.get("access_token");
        IgtimiConnectorFactory.INSTANCE.storeCodeAndAccessToken(code, accessToken);
        return Response.seeOther(new URI("http://www.sap.com")).build();
    }
    
    @SuppressWarnings("unused") // may be needed for further HTTP debugging
    private String getContent(HttpResponse response) throws IOException {
        StringBuilder result = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while ((line=reader.readLine()) != null) {
            result.append(line);
            result.append('\n');
        }
        return result.toString();
    }

    private HttpResponse signInAndReturnAuthorizationForm(DefaultHttpClient client, HttpResponse response) throws ParserConfigurationException,
            SAXException, IOException, UnsupportedEncodingException, ClientProtocolException {
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
                                inputFieldsToSubmit.put(attributes.getValue("name"), USER_EMAIL);
                            } else if (attributes.getValue("id").contains("pass")) {
                                inputFieldsToSubmit.put(attributes.getValue("name"), USER_PASSWORD);
                            }
                        }
                    }
                }
            });
        } catch (SAXParseException e) {
            // swallow; we try to grab what we can; let's hope it was enough...
        }
        response.getEntity().getContent().close();
        HttpResponse authorizationForm = postForm(action[0], inputFieldsToSubmit, client, /* referer */ "https://www.igtimi.com/users/sign_in");
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
        HttpGet get = new HttpGet(baseUrl+"/oauth/authorize?response_type=code&client_id="+CLIENT_ID+"&redirect_uri="+REDIRECT_URL);
        HttpResponse responseForAuthorize = client.execute(get);
        HttpResponse authorizationForm = signInAndReturnAuthorizationForm(client, responseForAuthorize);
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
                code[0] = response.getHeaders("Location")[0].getValue().substring(REDIRECT_URL.length() + "?".length());
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
        HttpResponse authorizationResponse = postForm(action[0], inputFieldsToSubmit, client, /* referer */ "https://www.igtimi.com/users/sign_in");
        // TODO remove debug output:
        String content = getContent(authorizationResponse);
        return code[0];
    }

    private HttpResponse postForm(final String action, final Map<String, String> inputFieldsToSubmit, DefaultHttpClient client, String referer)
            throws UnsupportedEncodingException, IOException, ClientProtocolException {
        HttpPost post = new HttpPost(baseUrl+action);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        for (Entry<String, String> nameValue : inputFieldsToSubmit.entrySet()) {
            urlParameters.add(new BasicNameValuePair(nameValue.getKey(), nameValue.getValue()));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        // TODO check if this is necessary at all
        post.setHeader("Origin", "https://www.igtimi.com");
        post.setHeader("Referer", referer);
        post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36");
        HttpResponse responseForSignIn = client.execute(post);
        return responseForSignIn;
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("uri")
    public Response getWithUri(@Context UriInfo info) {
        return Response.ok().build();
    }
}
