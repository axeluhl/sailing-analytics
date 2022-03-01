package com.sap.sse.security.util.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.jaxrs.api.OwnershipResource;
import com.sap.sse.security.jaxrs.api.SecurityResource;
import com.sap.sse.security.jaxrs.api.UserGroupResource;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.util.SecuredServer;
import com.sap.sse.util.LaxRedirectStrategyForAllRedirectResponseCodes;

public class SecuredServerImpl implements SecuredServer {
    private static final Logger logger = Logger.getLogger(SecuredServerImpl.class.getName());

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
            jsonParseResult = new String(bos.toByteArray());
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
        final URL getUserGroupIdByNameUrl = new URL(getBaseUrl(), SECURITY_API_PREFIX + UserGroupResource.RESTSECURITY_USERGROUP
                + "?" + UserGroupResource.KEY_GROUP_NAME+"="+userGroupName);
        final HttpGet getRequest = new HttpGet(getUserGroupIdByNameUrl.toString());
        final Pair<Object, Integer> result = getJsonParsedResponse(getRequest);
        final UUID groupId;
        if (result.getB() >= 200 && result.getB() < 300) {
            final JSONObject groupJson = (JSONObject) result.getA();
            groupId = groupJson == null ? null : UUID.fromString(groupJson.get(UserGroupResource.KEY_GROUP_ID).toString());
        } else {
            groupId = null;
        }
        return groupId;
    }

    @Override
    public Pair<UUID, String> getGroupAndUserOwner(HasPermissions type, TypeRelativeObjectIdentifier typeRelativeObjectId) throws ClientProtocolException, IOException, ParseException {
        final URL getGroupAndUserOwnerUrl = new URL(getBaseUrl(), SECURITY_API_PREFIX + OwnershipResource.RESTSECURITY_OWNERSHIP
                + "/" + type.getName() + "/" + typeRelativeObjectId.toString());
        final HttpGet getRequest = new HttpGet(getGroupAndUserOwnerUrl.toString());
        final JSONObject ownershipJson = (JSONObject) getJsonParsedResponse(getRequest).getA();
        final Object groupIdValue = ownershipJson.get(OwnershipResource.KEY_GROUP_ID);
        final UUID groupId = groupIdValue == null ? null : UUID.fromString(groupIdValue.toString());
        final Object usernameValue = ownershipJson.get(OwnershipResource.KEY_USERNAME);
        final String username = usernameValue == null ? null : usernameValue.toString();
        return new Pair<>(groupId, username);
    }
    
    @Override
    public Iterable<Pair<WildcardPermission, Boolean>> hasPermissions(Iterable<WildcardPermission> permissions) throws ClientProtocolException, IOException, ParseException {
        final StringBuilder sb = new StringBuilder(SECURITY_API_PREFIX + SecurityResource.RESTSECURITY + SecurityResource.HAS_PERMISSION_METHOD + "?");
        for (final WildcardPermission permission : permissions) {
            sb.append(SecurityResource.PERMISSION);
            sb.append('=');
            sb.append(URLEncoder.encode(permission.toString(), "UTF-8"));
            sb.append('&');
        }
        sb.delete(sb.length()-1, sb.length());
        final URL getPermissionsUrl = new URL(getBaseUrl(), sb.toString());
        final HttpGet getRequest = new HttpGet(getPermissionsUrl.toString());
        final JSONArray permissionsJson = (JSONArray) getJsonParsedResponse(getRequest).getA();
        final List<Pair<WildcardPermission, Boolean>> result = new ArrayList<>();
        for (final Object o : permissionsJson) {
            final JSONObject permissionAndGranted = (JSONObject) o;
            final String permissionAsString = permissionAndGranted.get(SecurityResource.PERMISSION).toString();
            final Boolean permissionGranted = (Boolean) permissionAndGranted.get(SecurityResource.GRANTED);
            result.add(new Pair<>(new WildcardPermission(permissionAsString), permissionGranted));
        }
        return result;
    }

    @Override
    public String getUsername() throws ClientProtocolException, IOException, ParseException {
        final URL getUsernameUrl = new URL(getBaseUrl(), SECURITY_API_PREFIX + SecurityResource.RESTSECURITY + SecurityResource.ACCESS_TOKEN_METHOD);
        final HttpGet getRequest = new HttpGet(getUsernameUrl.toString());
        final JSONObject accessTokenJson = (JSONObject) getJsonParsedResponse(getRequest).getA();
        final Object usernameValue = accessTokenJson.get(SecurityResource.USERNAME);
        final String username = usernameValue == null ? null : usernameValue.toString();
        return username;
    }

    @Override
    public void addUserToGroup(UUID userGroupId) throws ClientProtocolException, IOException, ParseException {
        final String username = getUsername();
        final URL createUserGroupUrl = new URL(getBaseUrl(), SECURITY_API_PREFIX + UserGroupResource.RESTSECURITY_USERGROUP +
                "/"+userGroupId.toString()+UserGroupResource.USER+"/"+username);
        final HttpPut putRequest = new HttpPut(createUserGroupUrl.toString());
        final Pair<Object, Integer> result = getJsonParsedResponse(putRequest);
        final Integer status = result.getB();
        if (status < 200 || status >= 300) {
            throw new IllegalArgumentException("Couldn't add user "+username+" to user group with ID "+userGroupId+": "+result.getA());
        }
    }

    @Override
    public UUID createUserGroupAndAddCurrentUser(String userGroupName) throws ClientProtocolException, IOException, ParseException {
        final UUID result;
        if (getUserGroupIdByName(userGroupName) == null) {
            final JSONObject paramPayload = new JSONObject();
            paramPayload.put(UserGroupResource.KEY_GROUP_NAME, userGroupName);
            final URL createUserGroupUrl = new URL(getBaseUrl(), SECURITY_API_PREFIX + UserGroupResource.RESTSECURITY_USERGROUP);
            final HttpPut putRequest = new HttpPut(createUserGroupUrl.toString());
            putRequest.setEntity(new StringEntity(paramPayload.toJSONString()));
            putRequest.setHeader(HTTP.CONTENT_TYPE, "application/json");
            final JSONObject userGroupJson = (JSONObject) getJsonParsedResponse(putRequest).getA();
            final UUID newGroupId = UUID.fromString(userGroupJson.get(UserGroupResource.KEY_GROUP_ID).toString());
            result = newGroupId;
        } else {
            logger.warning("User group name "+userGroupName+" already exists on server "+getBaseUrl()+". Not creating again.");
            result = null;
        }
        return result;
    }

    @Override
    public String toString() {
        return getBaseUrl().toString();
    }
}
