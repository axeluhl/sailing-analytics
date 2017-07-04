package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public interface IsRaceFragment extends RaceGroupFragment {
    RaceLogRaceStatus getCurrentStatus();
}
