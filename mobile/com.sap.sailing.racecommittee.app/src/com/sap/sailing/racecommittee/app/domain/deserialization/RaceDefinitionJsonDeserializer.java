package com.sap.sailing.racecommittee.app.domain.deserialization;

import java.util.Collections;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.RaceDefinitionJsonSerializer;

public class RaceDefinitionJsonDeserializer implements JsonDeserializer<RaceDefinition> {

	private JsonDeserializer<BoatClass> boatClassDeserializer;
	
	public RaceDefinitionJsonDeserializer(JsonDeserializer<BoatClass> boatClassDeserializer) {
		this.boatClassDeserializer = boatClassDeserializer;
	}
	
	public RaceDefinition deserialize(JSONObject object) throws JsonDeserializationException {
		String id = object.get(RaceDefinitionJsonSerializer.FIELD_ID).toString();
		String name = object.get(RaceDefinitionJsonSerializer.FIELD_NAME).toString();
		
		BoatClass boatClass = boatClassDeserializer.deserialize(
				Helpers.getNestedObjectSafe(object, RaceDefinitionJsonSerializer.FIELD_BOAT_CLASS));
		
		return new RaceDefinitionImpl(
				name, 
				null, 
				boatClass, 
				Collections.<Competitor>emptyList(), 
				Helpers.tryUuidConversion(id));
	}

}
