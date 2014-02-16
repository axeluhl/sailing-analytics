package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMappingEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;

public abstract class RaceLogDeviceMappingEventSerializer<ItemT extends WithID> extends BaseRaceLogEventSerializer {
	private final TypeBasedServiceFinder<DeviceIdentifierJsonHandler> deviceServiceFinder;
    public static final String FIELD_ITEM = "item";
    public static final String FIELD_DEVICE = "device";
    public static final String FIELD_DEVICE_TYPE = "deviceType";
    public static final String FIELD_FROM_MILLIS = "fromMillis";
    public static final String FIELD_TO_MILLIS = "toMillis";

    public RaceLogDeviceMappingEventSerializer(JsonSerializer<Competitor> competitorSerializer,
    		TypeBasedServiceFinder<DeviceIdentifierJsonHandler> deviceServiceFinder) {
        super(competitorSerializer);
        this.deviceServiceFinder = deviceServiceFinder;
    }
    
    protected abstract JSONObject serializeItem(ItemT item);

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        @SuppressWarnings("unchecked")
		DeviceMappingEvent<ItemT> event = (DeviceMappingEvent<ItemT>) object;
        JSONObject result = super.serialize(event);
        String deviceType = event.getDevice().getIdentifierType();
        result.put(FIELD_FROM_MILLIS, event.getFrom().asMillis());
        result.put(FIELD_TO_MILLIS, event.getTo().asMillis());
        result.put(FIELD_ITEM, serializeItem(event.getMappedTo()));
        result.put(FIELD_DEVICE_TYPE, deviceType);        
        try {
			result.put(FIELD_DEVICE, deviceServiceFinder.findService(deviceType).transformForth(event.getDevice()));
		} catch (Exception e) {
			e.printStackTrace();
		}
        return result;
    }

}
