package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.User;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;

public class IgtimiConnectionImpl implements IgtimiConnection {
    private final Account account;
    private final IgtimiConnectionFactory connectionFactory;
    
    public IgtimiConnectionImpl(IgtimiConnectionFactory connectionFactory, Account account) {
        this.connectionFactory = connectionFactory;
        this.account = account;
    }
    
    @Override
    public Iterable<User> getUsers() throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getUsers = new HttpGet(connectionFactory.getUsersUrl(account));
        JSONObject usersJson = ConnectivityUtils.getJsonFromResponse(client.execute(getUsers));
        final List<User> result = new ArrayList<>();
        for (Object userJson : (JSONArray) usersJson.get("users")) {
            User user = new UserDeserializer().createUserFromJson((JSONObject) ((JSONObject) userJson).get("user"));
            result.add(user);
        }
        return result;
    }

    @Override
    public Iterable<Resource> getResources(Permission permission, TimePoint startTime, TimePoint endTime,
            Iterable<String> deviceIds, Iterable<String> streamIds) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getResources = new HttpGet(connectionFactory.getResourcesUrl(permission, startTime, endTime, deviceIds, streamIds, account));
        JSONObject resourcesJson = ConnectivityUtils.getJsonFromResponse(client.execute(getResources));
        final List<Resource> result = new ArrayList<>();
        for (Object userJson : (JSONArray) resourcesJson.get("resources")) {
            Resource user = new ResourceDeserializer().createResourceFromJson((JSONObject) ((JSONObject) userJson).get("resource"));
            result.add(user);
        }
        return result;
    }

    @Override
    public Iterable<Fix> getResourceData(TimePoint startTime, TimePoint endTime,
            Iterable<String> serialNumbers, Map<Type, Double> typeAndCompression) throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = connectionFactory.getHttpClient();
        HttpGet getResourceData = new HttpGet(connectionFactory.getResourceDataUrl(startTime, endTime, serialNumbers, typeAndCompression, account));
        JSONObject resourceDataJson = ConnectivityUtils.getJsonFromResponse(client.execute(getResourceData));
        return new FixFactory().createFixes(resourceDataJson);
    }
}
