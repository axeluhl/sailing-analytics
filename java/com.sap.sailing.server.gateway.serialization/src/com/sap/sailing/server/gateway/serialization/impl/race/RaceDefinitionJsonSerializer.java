package com.sap.sailing.server.gateway.serialization.impl.race;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.server.gateway.serialization.ExtendableJsonSerializer;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceDefinitionJsonSerializer extends ExtendableJsonSerializer<RaceDefinition> {
	public static final String FIELD_ID = "id";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_BOAT_CLASS = "boatClass";
	
	private final JsonSerializer<BoatClass> boatClassSerializer;
	
	public RaceDefinitionJsonSerializer(JsonSerializer<BoatClass> boatClassSerializer) {
		this.boatClassSerializer = boatClassSerializer;
	}
	
	public RaceDefinitionJsonSerializer(
			JsonSerializer<BoatClass> boatClassSerializer,
			ExtensionJsonSerializer<RaceDefinition, ?> extensionSerializer) {
		super(extensionSerializer);
		this.boatClassSerializer = boatClassSerializer;
	}



	@Override
	protected JSONObject serializeFields(RaceDefinition object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_ID, object.getId().toString());
		result.put(FIELD_NAME, object.getName());
		result.put(FIELD_BOAT_CLASS, boatClassSerializer.serialize(object.getBoatClass()));
		
		return result;
	}

}
