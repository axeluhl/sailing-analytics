package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class CompetitorDeserializer implements JsonDeserializer<Competitor> {
	
	@SuppressWarnings("unused")
	private SharedDomainFactory factory;
	
	public CompetitorDeserializer(SharedDomainFactory factory) {
		this.factory = factory;
	}

	@Override
	public Competitor deserialize(JSONObject object)
			throws JsonDeserializationException {
		
		return null;
	}

}
