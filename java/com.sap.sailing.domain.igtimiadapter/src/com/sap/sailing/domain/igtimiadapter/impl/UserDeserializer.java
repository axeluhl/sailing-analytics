package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.User;

public class UserDeserializer {
    public User createUserFromJson(JSONObject userJson) {
        Boolean blob = (Boolean) userJson.get("blob");
        final Object idObject = userJson.get("id");
        final Long id;
        if (idObject instanceof String) { // this is a bug in a version of the Igtimi API of 2013-12-03
            id = Long.valueOf((String) idObject);
        } else {
            id = (Long) idObject;
        }
        return new UserImpl(id,
                (String) userJson.get("first_name"),
                (String) userJson.get("surname"),
                (String) userJson.get("email"),
                blob == null ? false : blob);
    }
}
