package com.sap.sailing.server.gateway.impl.rc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceCell;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceGroup;
import com.sap.sailing.domain.base.RaceRow;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.SeriesWithRows;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RaceCellImpl;
import com.sap.sailing.domain.base.impl.RaceGroupImpl;
import com.sap.sailing.domain.base.impl.RaceRowImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.SeriesWithRowsImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.impl.RaceLogIdentifierImpl;
import com.sap.sailing.server.RacingEventService;

public class RaceGroupFactory {
	
	private RacingEventService service;

	public RaceGroupFactory(RacingEventService service) {
		this.service = service;
	}

	public RaceGroup convert(Leaderboard leaderboard) {
		String name = leaderboard.getName();
		
		CourseArea courseArea = leaderboard.getDefaultCourseArea();
		
		BoatClass boatClass = null;
		if (leaderboard instanceof RegattaLeaderboard) {
			boatClass = ((RegattaLeaderboard) leaderboard).getRegatta().getBoatClass();
		}
		
		Iterable<SeriesWithRows> series = getSeries(leaderboard);
		
		return new RaceGroupImpl(name, boatClass, courseArea, series);
	}
	
	public Iterable<SeriesWithRows> getSeries(Leaderboard leaderboard) {
		
		Map<Series, List<RaceColumn>> seriesToRaceColumns = getSeriesToRaceColumns(leaderboard);
		
		Collection<SeriesWithRows> seriesWithRows = new ArrayList<>();
		for (Series series : seriesToRaceColumns.keySet()) {
			Collection<RaceRow> rows = new ArrayList<>();
			for (Fleet fleet : series.getFleets()) {
				Collection<RaceCell> cells = new ArrayList<>();
				for (RaceColumn raceColumn : seriesToRaceColumns.get(series)) {
					RaceLogIdentifier identifier = new RaceLogIdentifierImpl(leaderboard, raceColumn, fleet);
					cells.add(new RaceCellImpl(raceColumn.getName(), service.getRaceLog(identifier)));
				}
				rows.add(new RaceRowImpl(fleet, cells));
			}
			seriesWithRows.add(new SeriesWithRowsImpl(series.getName(), rows, series.isMedal()));
		}
		return seriesWithRows;
	}

	private Map<Series, List<RaceColumn>> getSeriesToRaceColumns(
			Leaderboard leaderboard) {
		Map<Series, List<RaceColumn>> seriesToRaceColumns = new HashMap<>();
		
		Series defaultSeries = null;
		for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
			if (raceColumn instanceof RaceColumnInSeries) {
				Series raceColumnSeries = ((RaceColumnInSeries) raceColumn).getSeries();
				insertSeriesIfNew(seriesToRaceColumns, raceColumnSeries).add(raceColumn);
			} else {
				if (defaultSeries == null) {
					defaultSeries = createDefaultSeries();
					insertSeriesIfNew(seriesToRaceColumns, defaultSeries);
				}
				seriesToRaceColumns.get(defaultSeries).add(raceColumn);
			}
		}
		return seriesToRaceColumns;
	}

	private Series createDefaultSeries() {
		Series defaultSeries;
		defaultSeries = new SeriesImpl(
				"Default Series", 
				false, 
				Collections.<Fleet>singleton(new FleetImpl("Default Fleet")), 
				Collections.<String>emptyList(), 
				null);
		return defaultSeries;
	}

	private List<RaceColumn> insertSeriesIfNew(Map<Series, List<RaceColumn>> target, Series newSeries) {
		if (!target.containsKey(newSeries)) {
			target.put(newSeries, new ArrayList<RaceColumn>());
		}
		return target.get(newSeries);
	}

}
