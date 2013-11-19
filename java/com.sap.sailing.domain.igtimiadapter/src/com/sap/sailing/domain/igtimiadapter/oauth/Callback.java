package com.sap.sailing.domain.igtimiadapter.oauth;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
    static final String V1_AUTHORIZATIONCALLBACK = "/v1/authorizationcallback";
    private static final String OAUTH_TOKEN_URL = "https://www.igtimi.com/oauth/token";
    private static final String USER_EMAIL = "axel.uhl@gmx.de";
    private static final String USER_PASSWORD = "123456";

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/")
    public Response getEvent(@QueryParam("code") String code) throws ClientProtocolException, IOException, IllegalStateException, ParseException, URISyntaxException {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(OAUTH_TOKEN_URL);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        urlParameters.add(new BasicNameValuePair("client_id", "d29eae61621af3057db0e638232a027e96b1d2291b1b89a1481dfcac075b0bf4"));
        urlParameters.add(new BasicNameValuePair("client_secret", "537dbd14a84fcb470c91d85e8c4f8f7a356ac5ffc8727594d1bfe900ee5942ef"));
        urlParameters.add(new BasicNameValuePair("code", code));
        urlParameters.add(new BasicNameValuePair("redirect_uri", "http://sapsailing.com"));
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
    
    public boolean signIn() throws ClientProtocolException, IOException, ParserConfigurationException, IllegalStateException, SAXException {
        HttpClient client = new DefaultHttpClient();
        String url = "https://www.igtimi.com/oauth/authorize?response_type=code&client_id=d29eae61621af3057db0e638232a027e96b1d2291b1b89a1481dfcac075b0bf4&redirect_uri=http://sapsailing.com/";
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
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
                    if (attributes.getValue("value") != null) {
                        inputFieldsToSubmit.put(attributes.getValue("name"), attributes.getValue("value"));
                    } else if (attributes.getValue("id") != null) {
                        if (attributes.getValue("id").contains("email")) {
                            inputFieldsToSubmit.put(attributes.getValue("name"), USER_EMAIL);
                        } else if (attributes.getValue("id").contains("pass")) {
                            inputFieldsToSubmit.put(attributes.getValue("name"), USER_PASSWORD);
                        }
                    }
                } else {
                    super.startElement(uri, localName, qName, attributes);
                }
            }
        });
        } catch (SAXParseException e) {
            // swallow; we try to grab what we can; let's hope it was enough...
        }
        return true;
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("uri")
    public Response getWithUri(@Context UriInfo info) {
        return Response.ok().build();
    }
}
