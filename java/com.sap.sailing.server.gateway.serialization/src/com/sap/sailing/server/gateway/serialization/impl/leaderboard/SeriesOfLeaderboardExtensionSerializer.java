package com.sap.sailing.server.gateway.serialization.impl.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SeriesOfLeaderboardExtensionSerializer extends ExtensionJsonSerializer<Leaderboard, Series> {
	public static final String FIELD_SERIES = "series";
	
	
	public SeriesOfLeaderboardExtensionSerializer(JsonSerializer<Series> extensionSerializer) {
		super(extensionSerializer);
	}

	@Override
	public String getExtensionFieldName() {
		return FIELD_SERIES;
	}

	@Override
	public Object serializeExtension(Leaderboard parent) {
		Map<Series, List<RaceColumn>> seriesToRaceColumns = new HashMap<Series, List<RaceColumn>>();
		Series defaultSeries = new SeriesImpl(
				"Default Series", 
				false, 
				Collections.<Fleet>singleton(new FleetImpl("Default Fleet")), 
				Collections.<String>emptyList(), 
				null);
		seriesToRaceColumns.put(defaultSeries, Collections.<RaceColumn>emptyList());
		for (RaceColumn raceColumn : parent.getRaceColumns()) {
			
			if (raceColumn instanceof RaceColumnInSeries) {
				RaceColumnInSeries raceColumnInSeries = (RaceColumnInSeries) raceColumn;
				if (!seriesToRaceColumns.containsKey(raceColumnInSeries.getSeries())) {
					seriesToRaceColumns.put(raceColumnInSeries.getSeries(), new ArrayList<RaceColumn>());
				}
				seriesToRaceColumns.get(raceColumnInSeries.getSeries()).add(raceColumnInSeries);
			} else {
				seriesToRaceColumns.get(defaultSeries).add(raceColumn);
			}
		}
		
		JSONArray result = new JSONArray();
		for (Series series : seriesToRaceColumns.keySet()) {
			result.add(serialize(series));
		}
		
		return result;
	}
}
