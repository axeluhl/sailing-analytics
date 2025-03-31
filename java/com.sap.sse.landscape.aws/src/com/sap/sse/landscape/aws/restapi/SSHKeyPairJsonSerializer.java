package com.sap.sse.landscape.aws.restapi;

import org.json.simple.JSONObject;

import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.shared.json.JsonSerializer;

public class SSHKeyPairJsonSerializer implements JsonSerializer<SSHKeyPair> {
    public static final String NAME_FIELD = "name";
    public static final String REGION_ID_FIELD = "regionId";
    public static final String CREATOR_NAME_FIELD = "creatorName";
    public static final String CREATION_TIME_MILLIS_FIELD = "creationTimeMillis";
    public static final String PUBLIC_KEY_FIELD = "publicKey";
    
    @Override
    public JSONObject serialize(SSHKeyPair object) {
        final JSONObject result = new JSONObject();
        result.put(NAME_FIELD, object.getName());
        result.put(REGION_ID_FIELD, object.getRegionId());
        result.put(CREATOR_NAME_FIELD, object.getCreatorName());
        result.put(CREATION_TIME_MILLIS_FIELD, object.getCreationTime().asMillis());
        result.put(PUBLIC_KEY_FIELD, object.getPublicKey() == null ? null : new String(object.getPublicKey())
                .trim().replaceFirst("\n$", ""));
        return result;
    }
}
