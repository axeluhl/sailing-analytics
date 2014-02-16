package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;

public class RaceLogDeviceCompetitorMappingEventSerializer extends RaceLogDeviceMappingEventSerializer<Competitor> {
	public static final String VALUE_CLASS = DeviceCompetitorMappingEvent.class.getSimpleName();

	public RaceLogDeviceCompetitorMappingEventSerializer(
			JsonSerializer<Competitor> competitorSerializer,
			TypeBasedServiceFinder<DeviceIdentifierJsonHandler> deviceServiceFinder) {
		super(competitorSerializer, deviceServiceFinder);
	}
	
    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

	@Override
	protected JSONObject serializeItem(Competitor item) {
		return competitorSerializer.serialize(item);
	}
}
