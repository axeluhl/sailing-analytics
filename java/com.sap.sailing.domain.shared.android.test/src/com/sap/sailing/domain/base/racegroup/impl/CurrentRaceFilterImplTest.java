package com.sap.sailing.domain.base.racegroup.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.racegroup.CurrentRaceFilter;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.RaceGroupSeriesFleetRaceColumn;
import com.sap.sailing.domain.base.racegroup.RaceRow;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public class CurrentRaceFilterImplTest {

    private CurrentRaceFilter<SimpleFilterableRace> fixture;
    private RaceGroup _505;
    private RaceGroup yes;
    private RaceGroup isaf;
    private RaceGroup league;
    private Map<RaceGroupSeriesFleetRaceColumn, SimpleFilterableRace> races;

    @Before
    public void setUp() {
        races = new HashMap<>();
        // five regattas ("RaceGroup"s):
        // - simple single-series, single fleet set-up (505 style)
        // - single-series, two fleets (Yellow / Blue, as used by YES)
        // - qualification with Yellow / Blue; final with Gold / Silver; Medal
        // - league set-up with single series with three fleets Red/Green/Blue
        _505 = create505Regatta();
        races.putAll(getAllRaces(_505));
        yes = createYesRegatta();
        races.putAll(getAllRaces(yes));
        isaf = createIsafRegatta();
        races.putAll(getAllRaces(isaf));
        league = createLeagueRegatta();
        races.putAll(getAllRaces(league));
        fixture = new CurrentRaceFilterImpl<>(races.values());
    }

    private RaceGroup create505Regatta() {
        final List<SeriesWithRows> _505Series = new ArrayList<>();
        final List<RaceRow> _505RaceRows = new ArrayList<>();
        final RaceRow _505RaceRow = createRaceRow(/* firstRaceColumnNumber */ 1, /* numberOfRaces */ 9, /* raceColumnNamePrefix */ "R", /* fleetName */ "Default");
        _505RaceRows.add(_505RaceRow);
        _505Series.add(new SeriesWithRowsImpl("Default", /* isMedal */ false, _505RaceRows));
        final RaceGroup _505 = new RaceGroupImpl("505", /* displayName */ null,
                new BoatClassImpl(BoatClassMasterdata._5O5.getDisplayName(), BoatClassMasterdata._5O5),
                /* courseArea */ null, _505Series, /* regattaConfiguration */ null);
        return _505;
    }

    private RaceGroup createYesRegatta() {
        final List<SeriesWithRows> yesSeries = new ArrayList<>();
        final List<RaceRow> yesRaceRows = Arrays.asList(createRaceRow(1, 9, "R", "Yellow"), createRaceRow(1, 9, "R", "Blue"));
        yesSeries.add(new SeriesWithRowsImpl("Default", /* isMedal */ false, yesRaceRows));
        final RaceGroup yes = new RaceGroupImpl("YES", /* displayName */ null,
                new BoatClassImpl(BoatClassMasterdata._470.getDisplayName(), BoatClassMasterdata._470),
                /* courseArea */ null, yesSeries, /* regattaConfiguration */ null);
        return yes;
    }

    private RaceGroup createIsafRegatta() {
        final List<SeriesWithRows> isafSeries = new ArrayList<>();
        final List<RaceRow> isafQualificationRaceRows = Arrays.asList(createRaceRow(1, 5, "Q", "Yellow"), createRaceRow(1, 5, "Q", "Blue"));
        isafSeries.add(new SeriesWithRowsImpl("Qualification", /* isMedal */ false, isafQualificationRaceRows));
        final List<RaceRow> isafFinalRaceRows = Arrays.asList(createRaceRow(6, 5, "F", "Gold"), createRaceRow(6, 5, "F", "Silver"));
        isafSeries.add(new SeriesWithRowsImpl("Final", /* isMedal */ false, isafFinalRaceRows));
        final List<RaceRow> isafMedalRaceRows = Arrays.asList(createRaceRow(1, 1, "M", "Medal"));
        isafSeries.add(new SeriesWithRowsImpl("Medal", /* isMedal */ true, isafMedalRaceRows));
        final RaceGroup isaf = new RaceGroupImpl("ISAF", /* displayName */ null,
                new BoatClassImpl(BoatClassMasterdata._470.getDisplayName(), BoatClassMasterdata._470),
                /* courseArea */ null, isafSeries, /* regattaConfiguration */ null);
        return isaf;
    }

    private RaceGroup createLeagueRegatta() {
        final List<SeriesWithRows> leagueSeries = new ArrayList<>();
        final List<RaceRow> leagueRaceRows = Arrays.asList(createRaceRow(1, 15, "F", "Red"), createRaceRow(1, 15, "F", "Green"), createRaceRow(1, 15, "F", "Blue"));
        leagueSeries.add(new SeriesWithRowsImpl("Default", /* isMedal */ false, leagueRaceRows));
        final RaceGroup league = new RaceGroupImpl("League", /* displayName */ null,
                new BoatClassImpl(BoatClassMasterdata.J70.getDisplayName(), BoatClassMasterdata.J70),
                /* courseArea */ null, leagueSeries, /* regattaConfiguration */ null);
        return league;
    }

    private Map<RaceGroupSeriesFleetRaceColumn, SimpleFilterableRace> getAllRaces(RaceGroup raceGroup) {
        final Map<RaceGroupSeriesFleetRaceColumn, SimpleFilterableRace> result = new HashMap<>();
        int zeroBasedSeriesIndex = 0;
        for (SeriesWithRows series : raceGroup.getSeries()) {
            for (final RaceRow raceRow : series.getRaceRows()) {
                int zeroBasedIndexInFleet = 0;
                for (final RaceCell raceCell : raceRow.getCells()) {
                    final SimpleFilterableRace race = new SimpleFilterableRace(raceGroup, series, raceRow.getFleet(), zeroBasedIndexInFleet, zeroBasedSeriesIndex, raceCell.getName());
                    result.put(new RaceGroupSeriesFleetRaceColumn(race), race);
                    zeroBasedIndexInFleet++;
                }
            }
            zeroBasedSeriesIndex++;
        }
        return result;
    }

    @Test
    public void testRaceAccessAndStatusUpdate() {
        SimpleFilterableRace isafQ4Yellow = get(isaf, "F7", "Gold");
        assertNotNull(isafQ4Yellow);
        assertEquals("F7", isafQ4Yellow.getRaceColumnName());
        assertEquals("Gold", isafQ4Yellow.getFleet().getName());
        assertEquals(1, isafQ4Yellow.getZeroBasedIndexInFleet());
        assertEquals(1, isafQ4Yellow.getZeroBasedSeriesIndex());
        assertEquals(RaceLogRaceStatus.UNSCHEDULED, isafQ4Yellow.getStatus());
        isafQ4Yellow.setStatus(RaceLogRaceStatus.SCHEDULED);
        assertEquals(RaceLogRaceStatus.SCHEDULED, isafQ4Yellow.getStatus());
    }
    
    @Test
    public void testBasicFiltering() {
        // with all races unscheduled we can expect the first race of each regatta's first series to show
    }
    
    private SimpleFilterableRace get(RaceGroup raceGroup, String raceColumnName, String fleetName) {
        for (final SeriesWithRows series : raceGroup.getSeries()) {
            final Fleet fleet = getFleet(series, fleetName);
            if (fleet != null) {
                final RaceRow raceRow = series.getRaceRow(fleet);
                if (raceRow != null) {
                    for (final RaceCell raceCell : raceRow.getCells()) {
                        if (raceCell.getName().equals(raceColumnName)) {
                            return races.get(new RaceGroupSeriesFleetRaceColumn(raceGroup, series, fleet, raceColumnName));
                        }
                    }
                }
            }
        }
        return null;
    }

    private Fleet getFleet(SeriesWithRows series, String fleetName) {
        for (final Fleet fleet : series.getFleets()) {
            if (fleet.getName().equals(fleetName)) {
                return fleet;
            }
        }
        return null;
    }

    private RaceRow createRaceRow(final int firstRaceColumnNumber, final int numberOfRaces,
            final String raceColumnNamePrefix, final String fleetName) {
        final List<RaceCell> _505RaceCells = new ArrayList<>(); 
        for (int i=0; i < numberOfRaces; i++) {
            _505RaceCells.add(new RaceCellImpl(raceColumnNamePrefix+(i+firstRaceColumnNumber),
                    /* race log */ null, /* factor */ firstRaceColumnNumber, /* explicitFactor */ null,
                    /* zeroBasedIndexInFleet */ i));
        }
        final RaceRowImpl _505RaceRow = new RaceRowImpl(new FleetImpl(fleetName), _505RaceCells);
        return _505RaceRow;
    }
}
