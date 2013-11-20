package com.sap.sailing.domain.igtimiadapter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.User;

public class IgtimiConnectionImpl implements IgtimiConnection {
    private final Account account;
    private final IgtimiConnectionFactory connectionFactory;
    
    public IgtimiConnectionImpl(IgtimiConnectionFactory connectionFactory, Account account) {
        this.connectionFactory = connectionFactory;
        this.account = account;
    }
    
    @Override
    public Iterable<User> getUsers() throws IllegalStateException, ClientProtocolException, IOException, ParseException {
        HttpClient client = new SystemDefaultHttpClient();
        HttpGet getUsers = new HttpGet(connectionFactory.getUsersUrl(account));
        JSONObject usersJson = ConnectivityUtils.getJsonFromResponse(client.execute(getUsers));
        final List<User> result = new ArrayList<>();
        for (Object userJson : (JSONArray) usersJson.get("users")) {
            User user = new UserDeserializer().createUserFromJson((JSONObject) ((JSONObject) userJson).get("user"));
            result.add(user);
        }
        return result;
    }
}
