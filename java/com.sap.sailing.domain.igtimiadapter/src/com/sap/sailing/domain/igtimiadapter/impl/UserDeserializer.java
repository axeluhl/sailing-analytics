package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.User;

public class UserDeserializer {
    public User createUserFromJson(JSONObject userJson) {
        Boolean blob = (Boolean) userJson.get("blob");
        return new UserImpl((Long) userJson.get("id"),
                (String) userJson.get("first_name"),
                (String) userJson.get("surname"),
                (String) userJson.get("email"),
                blob == null ? false : blob);
    }
}
