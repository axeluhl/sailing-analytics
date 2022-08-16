package com.sap.sailing.landscape.gateway.impl;

import org.json.simple.JSONObject;

import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.shared.json.JsonSerializer;

public class HostJsonSerializer<ShardingKey> implements JsonSerializer<AwsInstance<ShardingKey>> {
    private static final String AZ_ID = "availabilityZoneId";
    private static final String AZ_NAME = "availabilityZoneName";
    private static final String INSTANCE_ID = "instanceId";
    private static final String PRIVATE_ADDRESS = "privateAddress";
    private static final String PUBLIC_ADDRESS = "publicAddress";
    private static final String NAME = "name";

    @Override
    public JSONObject serialize(AwsInstance<ShardingKey> object) {
        final JSONObject result = new JSONObject();
        result.put(AZ_ID, object.getAvailabilityZone().getId());
        result.put(AZ_NAME, object.getAvailabilityZone().getName());
        result.put(INSTANCE_ID, object.getId());
        result.put(PRIVATE_ADDRESS, object.getPrivateAddress()==null?null:object.getPrivateAddress().getHostAddress());
        result.put(PUBLIC_ADDRESS, object.getPublicAddress()==null?null:object.getPublicAddress().getHostAddress());
        object.getInstance().tags().stream().filter(tag->tag.key().equals("Name")).findAny().ifPresent(tag->result.put(NAME, tag.value()));
        return result;
    }
}
