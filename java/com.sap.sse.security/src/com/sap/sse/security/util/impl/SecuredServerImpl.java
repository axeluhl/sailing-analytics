package com.sap.sse.security.util.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.jaxrs.api.OwnershipResource;
import com.sap.sse.security.jaxrs.api.SecurityResource;
import com.sap.sse.security.jaxrs.api.UserGroupResource;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.util.SecuredServer;
import com.sap.sse.util.LaxRedirectStrategyForAllRedirectResponseCodes;

public class SecuredServerImpl implements SecuredServer {
    private final String bearerToken;
    private final URL baseUrl;

    public SecuredServerImpl(URL baseUrl, String bearerToken) {
        super();
        this.baseUrl = baseUrl;
        this.bearerToken = bearerToken;
    }

    @Override
    public URL getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getBearerToken() {
        return bearerToken;
    }

    protected Pair<Object, Integer> getJsonParsedResponse(final HttpUriRequest request)
            throws IOException, ClientProtocolException, ParseException {
        authenticate(request);
        final HttpClient client = createHttpClient();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final HttpResponse response = client.execute(request);
        response.getEntity().writeTo(bos);
        Object jsonParseResult;
        try {
            jsonParseResult = new JSONParser()
                    .parse(new InputStreamReader(new ByteArrayInputStream(bos.toByteArray())));
        } catch (ParseException e) {
            jsonParseResult = null;
        }
        return new Pair<>(jsonParseResult, response.getStatusLine().getStatusCode());
    }

    private CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategyForAllRedirectResponseCodes())
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
    }

    private void authenticate(HttpRequest request) {
        if (bearerToken != null) {
            request.setHeader("Authorization", "Bearer " + bearerToken);
        }
    }
    
    @Override
    public UUID getUserGroupIdByName(String userGroupName) throws ClientProtocolException, IOException, ParseException {
        final URL getRemoteReferencesUrl = new URL(getBaseUrl(), SECURITY_API_PREFIX + UserGroupResource.RESTSECURITY_USERGROUP
                + "?" + UserGroupResource.KEY_GROUP_NAME+"="+userGroupName);
        final HttpGet getRequest = new HttpGet(getRemoteReferencesUrl.toString());
        final JSONObject groupJson = (JSONObject) getJsonParsedResponse(getRequest).getA();
        final UUID groupId = groupJson == null ? null : UUID.fromString(groupJson.get(UserGroupResource.KEY_GROUP_ID).toString());
        return groupId;
    }

    @Override
    public Pair<UUID, String> getGroupAndUserOwner(HasPermissions type, TypeRelativeObjectIdentifier typeRelativeObjectId) throws ClientProtocolException, IOException, ParseException {
        final URL getRemoteReferencesUrl = new URL(getBaseUrl(), SECURITY_API_PREFIX + OwnershipResource.RESTSECURITY_OWNERSHIP
                + "/" + type.getName() + "/" + typeRelativeObjectId.toString());
        final HttpGet getRequest = new HttpGet(getRemoteReferencesUrl.toString());
        final JSONObject ownershipJson = (JSONObject) getJsonParsedResponse(getRequest).getA();
        final Object groupIdValue = ownershipJson.get(OwnershipResource.KEY_GROUP_ID);
        final UUID groupId = groupIdValue == null ? null : UUID.fromString(groupIdValue.toString());
        final Object usernameValue = ownershipJson.get(OwnershipResource.KEY_USERNAME);
        final String username = usernameValue == null ? null : usernameValue.toString();
        return new Pair<>(groupId, username);
    }
    
    @Override
    public String getUsername() throws ClientProtocolException, IOException, ParseException {
        final URL getRemoteReferencesUrl = new URL(getBaseUrl(), SECURITY_API_PREFIX + SecurityResource.RESTSECURITY + SecurityResource.ACCESS_TOKEN_METHOD);
        final HttpGet getRequest = new HttpGet(getRemoteReferencesUrl.toString());
        final JSONObject accessTokenJson = (JSONObject) getJsonParsedResponse(getRequest).getA();
        final Object usernameValue = accessTokenJson.get(SecurityResource.USERNAME);
        final String username = usernameValue == null ? null : usernameValue.toString();
        return username;
        
    }

    @Override
    public String toString() {
        return getBaseUrl().toString();
    }
}
