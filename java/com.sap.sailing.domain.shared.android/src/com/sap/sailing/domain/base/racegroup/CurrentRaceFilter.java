package com.sap.sailing.domain.base.racegroup;

import java.util.Collection;
import java.util.List;

public interface CurrentRaceFilter <ITEM extends RaceGroupFragment, SERIES extends IsFleetFragment, RACE extends IsRaceFragment> {


    List<ITEM> filterCurrentRaces(Collection<ITEM> allItems);
}
