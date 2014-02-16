package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;

public class RaceLogDeviceMarkMappingEventDeserializer extends
		RaceLogDeviceMappingEventDeserializer<Mark> {
	private final JsonDeserializer<Mark> markDeserializer;
	
	public RaceLogDeviceMarkMappingEventDeserializer(
			JsonDeserializer<Competitor> competitorDeserializer, JsonDeserializer<Mark> markDeserializer,
			TypeBasedServiceFinder<DeviceIdentifierJsonHandler> deviceServiceFinder) {
		super(competitorDeserializer, deviceServiceFinder);
		this.markDeserializer = markDeserializer;
	}

	@Override
	protected RaceLogEvent furtherDeserialize(JSONObject itemObject, TimePoint from, TimePoint to,
			DeviceIdentifier device, Serializable id, TimePoint createdAt,
			RaceLogEventAuthor author, TimePoint timePoint, int passId) throws JsonDeserializationException {
		Mark mappedTo = (Mark) markDeserializer.deserialize(itemObject);
		return factory.createDeviceMarkMappingEvent(createdAt, author, timePoint, id, device,
				mappedTo, passId, from, to);
	}

}
