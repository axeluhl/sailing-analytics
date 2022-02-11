package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.base.impl.RemoteSailingServerReferenceImpl;
import com.sap.sailing.server.gateway.serialization.impl.RemoteSailingServerReferenceJsonSerializer;
import com.sap.sse.common.Util;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

public class RemoteSailingServerReferenceJsonDeserializer implements JsonDeserializer<RemoteSailingServerReference> {
    @Override
    public RemoteSailingServerReference deserialize(JSONObject object) throws JsonDeserializationException {
        final String name = (String) object.get(RemoteSailingServerReferenceJsonSerializer.NAME);
        URL url;
        try {
            url = new URL((String) object.get(RemoteSailingServerReferenceJsonSerializer.URL));
        } catch (MalformedURLException e) {
            throw new JsonDeserializationException(e);
        }
        final boolean include = (Boolean) object.get(RemoteSailingServerReferenceJsonSerializer.INCLUDE);
        final JSONArray eventIds = (JSONArray) object.get(RemoteSailingServerReferenceJsonSerializer.EVENT_IDS);
        return new RemoteSailingServerReferenceImpl(name, url, include, Util.map(eventIds, id->UUID.fromString(id.toString())));
    }
}
