package com.sap.sailing.domain.base.racegroup;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.state.RaceState;

/**
 * Given a set of races organized in fleets, series and regattas, this filter determines those races
 * relevant for a race officer based on the races' {@link RaceState state} and their place in the
 * regatta structure.
 * 
 * @author Charlotte Proeller (D062347)
 * @author Axel Uhl (d043530)
 *
 * @param <ITEM>
 * @param <SERIES>
 * @param <RACE>
 */
public interface CurrentRaceFilter <ITEM extends RaceGroupFragment, SERIES extends IsFleetFragment, RACE extends IsRaceFragment> {
    List<ITEM> filterCurrentRaces(Collection<ITEM> allItems);
}
