package com.sap.sailing.server.gateway.serialization.impl.regatta;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.server.gateway.serialization.ExtendableJsonSerializer;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RegattaJsonSerializer extends ExtendableJsonSerializer<Regatta> {
	public static final String FIELD_ID = "id";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_BASE_NAME = "baseName";
	public static final String FIELD_BOAT_CLASS = "boatClass";
	
	private final JsonSerializer<BoatClass> boatClassSerializer;
	
	public RegattaJsonSerializer(JsonSerializer<BoatClass> boatClassSerializer) {
		this.boatClassSerializer = boatClassSerializer;
	}

	public RegattaJsonSerializer(
			JsonSerializer<BoatClass> boatClassSerializer,
			ExtensionJsonSerializer<Regatta, ?> extensionSerializer) {
		super(extensionSerializer);
		this.boatClassSerializer = boatClassSerializer;
	}

	@Override
	protected JSONObject serializeFields(Regatta object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_ID, object.getId().toString());
		result.put(FIELD_NAME, object.getName());
		result.put(FIELD_BASE_NAME, object.getBaseName());
		if (object.getBoatClass() != null) {
			result.put(FIELD_BOAT_CLASS, boatClassSerializer.serialize(object.getBoatClass()));
		}
		return result;
	}

}
