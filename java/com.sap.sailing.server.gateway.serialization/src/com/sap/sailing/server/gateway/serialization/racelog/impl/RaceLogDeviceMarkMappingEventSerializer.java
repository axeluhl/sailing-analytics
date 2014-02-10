package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.racelog.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;

public class RaceLogDeviceMarkMappingEventSerializer extends RaceLogDeviceMappingEventSerializer<Competitor> {
	public static final String VALUE_CLASS = DeviceMarkMappingEvent.class.getSimpleName();
	
	public RaceLogDeviceMarkMappingEventSerializer(
			JsonSerializer<Competitor> competitorSerializer,
			TypeBasedServiceFinder<DeviceIdentifierJsonHandler> deviceServiceFinder) {
		super(competitorSerializer, deviceServiceFinder);
	}
	
    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
