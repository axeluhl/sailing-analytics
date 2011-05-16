package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class DynamicTrackedRaceImpl extends TrackedRaceImpl implements
        DynamicTrackedRace {
    private final Map<Competitor, List<GPSFixMoving>> fixes;
    
    public DynamicTrackedRaceImpl(RaceDefinition race) {
        super(race);
        fixes = new HashMap<Competitor, List<GPSFixMoving>>();
    }

    @Override
    public void recordFix(Competitor competitor, GPSFixMoving fix) {
        List<GPSFixMoving> list = fixes.get(competitor);
        if (list == null) {
            list = new ArrayList<GPSFixMoving>();
            fixes.put(competitor, list);
        }
        list.add(fix);
    }

}
