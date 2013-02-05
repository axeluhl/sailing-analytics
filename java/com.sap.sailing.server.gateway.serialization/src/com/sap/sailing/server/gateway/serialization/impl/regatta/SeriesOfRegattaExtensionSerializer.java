package com.sap.sailing.server.gateway.serialization.impl.regatta;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SeriesOfRegattaExtensionSerializer extends ExtensionJsonSerializer<Regatta, Series> {
	public static final String FIELD_NAME = "series";
	
	public SeriesOfRegattaExtensionSerializer(JsonSerializer<Series> extensionSerializer) {
		super(extensionSerializer);
	}
	
	@Override
	public String getExtensionFieldName() {
		return FIELD_NAME;
	}

	@Override
	public Object serializeExtension(Regatta object) {
		JSONArray result = new JSONArray();
		if (object.getSeries() != null) {
			for (Series series : object.getSeries()) {
				serialize(series);
			}
		}
		return result;
	}

}
