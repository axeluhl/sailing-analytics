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
import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SeriesDataOfLeaderboardExtensionSerializer extends ExtensionJsonSerializer<Leaderboard, SeriesData> {
	public static final String FIELD_SERIES = "series";
	
	
	public SeriesDataOfLeaderboardExtensionSerializer(JsonSerializer<SeriesData> extensionSerializer) {
		super(extensionSerializer);
	}

	@Override
	public String getExtensionFieldName() {
		return FIELD_SERIES;
	}

	@Override
	public Object serializeExtension(Leaderboard parent) {
		Map<Series, List<RaceColumn>> seriesToRaceColumns = new HashMap<Series, List<RaceColumn>>();
		
		Series defaultSeries = null;
		for (RaceColumn raceColumn : parent.getRaceColumns()) {
			if (raceColumn instanceof RaceColumnInSeries) {
				RaceColumnInSeries raceColumnInSeries = (RaceColumnInSeries) raceColumn;
				insertSeriesIfNew(seriesToRaceColumns, raceColumnInSeries.getSeries()).add(raceColumnInSeries);
			} else {
				if (defaultSeries == null) {
					defaultSeries = new SeriesImpl(
							"Default Series", 
							false, 
							Collections.<Fleet>singleton(new FleetImpl("Default Fleet")), 
							Collections.<String>emptyList(), 
							null, EmptyRaceLogStore.INSTANCE);
					insertSeriesIfNew(seriesToRaceColumns, defaultSeries);
				}
				seriesToRaceColumns.get(defaultSeries).add(raceColumn);
			}
		}
		
		JSONArray result = new JSONArray();
		for (Series series : seriesToRaceColumns.keySet()) {
			result.add(serialize(series));
		}
		
		return result;
	}

	private List<RaceColumn> insertSeriesIfNew(Map<Series, List<RaceColumn>> target, Series newSeries) {
		if (!target.containsKey(newSeries)) {
			target.put(newSeries, new ArrayList<RaceColumn>());
		}
		return target.get(newSeries);
	}
}
