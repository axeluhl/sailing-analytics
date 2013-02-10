package com.sap.sailing.server.gateway.serialization.impl.leaderboard;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceCell;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceRow;
import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.domain.base.impl.RaceCellImpl;
import com.sap.sailing.domain.base.impl.RaceRowImpl;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceRowsOfSeriesDataExtensionSerializer extends ExtensionJsonSerializer<SeriesData, RaceRow> {
	public static final String FIELD_FLEETS = "fleets";
	
	public RaceRowsOfSeriesDataExtensionSerializer(JsonSerializer<RaceRow> extensionSerializer) {
		super(extensionSerializer);
	}

	@Override
	public String getExtensionFieldName() {
		return FIELD_FLEETS;
	}

	@Override
	public Object serializeExtension(SeriesData parent) {
		JSONArray result = new JSONArray();
		
		for (Fleet fleet : parent.getFleets()) {
			Collection<RaceCell> raceCells = new ArrayList<RaceCell>();
			for (RaceColumn column : parent.getRaceColumns()) {
				raceCells.add(new RaceCellImpl(column.getName(), null)); /// TODO: get racelog from column
				//column.getRaceLog(leaderboardName, fleet)
			}
			result.add(serialize(new RaceRowImpl(fleet, raceCells)));
		}
		
		return result;
	}

}
