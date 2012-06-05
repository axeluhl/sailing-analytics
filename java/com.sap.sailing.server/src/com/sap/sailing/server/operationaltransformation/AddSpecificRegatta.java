package com.sap.sailing.server.operationaltransformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class AddSpecificRegatta extends AbstractAddRegattaOperation {
    private static final long serialVersionUID = -8018855620167669352L;
    private final Map<String, Pair<List<Pair<String, Integer>>, Boolean>> seriesNamesWithFleetNamesAndFleetOrderingAndMedal;
    private final boolean persistent;
    
    public AddSpecificRegatta(String regattaName, String boatClassName, boolean boatClassTypicallyStartsUpwind,
            Map<String, Pair<List<Pair<String, Integer>>, Boolean>> seriesNamesWithFleetNamesAndFleetOrdering, boolean persistent) {
        super(regattaName, boatClassName, boatClassTypicallyStartsUpwind);
        this.seriesNamesWithFleetNamesAndFleetOrderingAndMedal = seriesNamesWithFleetNamesAndFleetOrdering;
        this.persistent = persistent;
    }

    @Override
    public Regatta internalApplyTo(RacingEventService toState) throws Exception {
        return toState.createRegatta(getBaseEventName(), getBoatClassName(), isBoatClassTypicallyStartsUpwind(), createSeries(toState), persistent);
    }

    private Iterable<? extends Series> createSeries(TrackedRegattaRegistry trackedRegattaRegistry) {
        List<Series> result = new ArrayList<Series>();
        for (Map.Entry<String, Pair<List<Pair<String, Integer>>, Boolean>> e : seriesNamesWithFleetNamesAndFleetOrderingAndMedal.entrySet()) {
            final List<String> emptyRaceColumnNamesList = Collections.emptyList();
            Series s = new SeriesImpl(e.getKey(), /* isMedal */ e.getValue().getB(), createFleets(e.getValue().getA()), emptyRaceColumnNamesList, trackedRegattaRegistry);
            result.add(s);
        }
        return result;
    }

    private Iterable<? extends Fleet> createFleets(List<Pair<String, Integer>> fleetNamesAndOrdering) {
        List<Fleet> result = new ArrayList<Fleet>();
        for (Pair<String, Integer> fleetNameAndOrdering : fleetNamesAndOrdering) {
            Fleet fleet = new FleetImpl(fleetNameAndOrdering.getA(), fleetNameAndOrdering.getB());
            result.add(fleet);
        }
        return result;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}
