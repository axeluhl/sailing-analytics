package com.sap.sailing.domain.racelog.tracking.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.impl.Function;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.racelog.tracking.RaceNotCreatedException;
import com.sap.sailing.domain.racelog.tracking.analyzing.impl.RaceInformationFinder;
import com.sap.sailing.domain.racelog.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackerManager;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.server.impl.RaceLogConnectivityParams;
import com.sap.sailing.server.impl.RacingEventServiceImpl.RaceLogStatusFinder;

public class RaceLogTrackingAdapterImpl implements RaceLogTrackingAdapter {
	private final DomainFactory domainFactory;

	public RaceLogTrackingAdapterImpl(DomainFactory domainFactory) {
		this.domainFactory = domainFactory;
	}

	@Override
	public RacesHandle addRace(TrackerManager trackerManager, RegattaIdentifier regattaToAddTo,
			Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet, RaceLogStore raceLogStore,
			WindStore windStore, long timeoutInMilliseconds) throws RaceNotCreatedException {
		RaceLog raceLog = raceColumn.getRaceLog(fleet);
		RaceLogTrackingState state = new RaceLogTrackingStateAnalyzer(raceLog).analyze();
		RaceLogConnectivityParams params = new RaceLogConnectivityParams(serviceFinder, windStore, gpsFixStore, raceLog, raceColumn, fleet, leaderboard, service, boatClass, delayToLiveInMillis)
		trackerManager.addRace(regattaToAddTo, params, timeoutInMilliseconds)
		raceLog.add(RaceLogEventFactory.INSTANCE.createPreRacePhaseStartedEvent(MillisecondsTimePoint.now(),
				raceLogEventAuthorForServer, UUID.randomUUID(), raceLog.getCurrentPassId(), raceColumn.getName(), boatClass));
		return addRaceLogTrackedRaceTracker(regatta, windStore, gpsFixStore, raceLog, raceColumn, fleet, leaderboard, boatClass);
	}
    
    private Map<RaceColumn, Collection<Fleet>> findInLeaderboard(Leaderboard leaderboard,
    		Function<Pair<RaceColumn, Fleet>, Boolean> includeInResult) {
        Map<RaceColumn, Collection<Fleet>> result = new HashMap<>();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
            	if (includeInResult.perform(new Pair<>(raceColumn, fleet))) {
            		Collection<Fleet> coll = result.get(raceColumn);
                    if (coll == null) {
                        coll = new ArrayList<>();
                        result.put(raceColumn, coll);
                    }
                    coll.add(fleet);
            	}
            }
        }
        return result;
    }
    
    private static class RaceLogStatusFinder implements Function<Util.Pair<RaceColumn,Fleet>, Boolean> {
    	private final RaceLogTrackingState desiredState;    	
    	public RaceLogStatusFinder(RaceLogTrackingState desiredState) {
    		this.desiredState = desiredState;
    	}
    	
    	@Override
    	public Boolean perform(Pair<RaceColumn, Fleet> in) {
    		if (in.getA().getTrackedRace(in.getB()) == null) {
    			RaceLog raceLog = in.getA().getRaceLog(in.getB());
    			RaceLogTrackingState state = new RaceLogTrackingStateAnalyzer(raceLog).analyze();
    			return state == desiredState;
    		}
    		return false;
    	}
    }
    
    @Override
    public Map<RaceColumn, Collection<Fleet>> listLoadableStoredRaceLogTrackedRaces(Leaderboard leaderboard) {
    	return findInLeaderboard(leaderboard, new RaceLogStatusFinder(RaceLogTrackingState.TRACKING));
    }

	@Override
	public Map<RaceColumn, Collection<Fleet>> listRaceLogTrackedRacesAwaitingRaceDefinition(Leaderboard leaderboard) {
		return findInLeaderboard(leaderboard, new RaceLogStatusFinder(RaceLogTrackingState.AWAITING_RACE_DEFINITION));
	}

    private RacesHandle addRaceLogTrackedRaceTracker(RegattaIdentifier regatta, WindStore windStore,
                RaceLog raceLog, RaceColumn raceColumn, Fleet fleet, Leaderboard leaderboard,
                BoatClass boatClass) throws RaceNotCreatedException {
        /*
         * We do not want a timeout of the tracker, the end of the pre race phase may arrive considerably
         * later than for other providers, as the data is collected piecewise, e.g. as the the individual
         * competitors register.
         */
        long timeoutInMillis = -1;
        RaceLogConnectivityParams params = new RaceLogConnectivityParams(deviceTypeServiceFinder, windStore, gpsFixStore, 
                raceLog, raceColumn, fleet, leaderboard, this, boatClass, delayToLiveInMillis);
        return addRace(regatta, params, gpsFixStore, timeoutInMillis);
    }

    @Override
    public RacesHandle startTrackingRaceLogTrackedRace(Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet, 
            RegattaIdentifier regatta, BoatClass boatClass, WindStore windStore) throws RaceNotCreatedException {
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        raceLog.add(RaceLogEventFactory.INSTANCE.createPreRacePhaseStartedEvent(MillisecondsTimePoint.now(),
                raceLogEventAuthorForServer, UUID.randomUUID(), raceLog.getCurrentPassId(), raceColumn.getName(), boatClass));
        return addRaceLogTrackedRaceTracker(regatta, windStore, gpsFixStore, raceLog, raceColumn, fleet, leaderboard, boatClass);
    }
}
