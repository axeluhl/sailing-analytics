package com.sap.sailing.domain.racelog.tracking.impl;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.tracking.NotDenotableForTrackingException;
import com.sap.sailing.domain.racelog.tracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelog.tracking.RaceNotCreatedException;
import com.sap.sailing.domain.racelog.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class RaceLogTrackingAdapterImpl implements RaceLogTrackingAdapter {
	private static final Logger logger = Logger.getLogger(RaceLogTrackingAdapterImpl.class.getName());
	
	private final DomainFactory domainFactory;
	private final long delayToLiveInMillis;

	public RaceLogTrackingAdapterImpl(DomainFactory domainFactory) {
		this.domainFactory = domainFactory;
		this.delayToLiveInMillis = TrackedRace.DEFAULT_LIVE_DELAY_IN_MILLISECONDS;
	}

	@Override
	public RacesHandle addRace(RacingEventService service, RegattaIdentifier regattaToAddTo, Leaderboard leaderboard,
			RaceColumn raceColumn, Fleet fleet, long timeoutInMilliseconds)
			throws MalformedURLException, FileNotFoundException, URISyntaxException, RaceNotCreatedException, Exception {
		RaceLog raceLog = raceColumn.getRaceLog(fleet);
		Regatta regatta = regattaToAddTo == null ? null : service.getRegatta(regattaToAddTo);
		RaceLogConnectivityParams params = new RaceLogConnectivityParams(service, regatta,
				raceLog, raceColumn, fleet, leaderboard, delayToLiveInMillis);
		return service.addRace(regattaToAddTo, params, timeoutInMilliseconds);
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
    
    @Override
    public Map<RaceColumn, Collection<Fleet>> listRacesThatCanBeAdded(final RacingEventService service,
    		Leaderboard leaderboard) {
    	return findInLeaderboard(leaderboard, new Function<Util.Pair<RaceColumn,Fleet>, Boolean>() {
			@Override
			public Boolean perform(Pair<RaceColumn, Fleet> in) {
				return canRaceBeAdded(service, in.getA(), in.getB());
			}
		});
    }

	@Override
	public void denoteForRaceLogTracking(RacingEventService service, Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet,
			String raceName)
			throws NotDenotableForTrackingException {
		
		BoatClass boatClass = null;
		if (leaderboard instanceof RegattaLeaderboard) {
			RegattaLeaderboard rLeaderboard = (RegattaLeaderboard) leaderboard;
			boatClass = rLeaderboard.getRegatta().getBoatClass();
		} else if (leaderboard instanceof FlexibleLeaderboard) {
			logger.log(Level.INFO, "Choosing 49er as default boat class for racelog-tracked race in FlexibleLeaderboard");
			boatClass = domainFactory.getOrCreateBoatClass("49er");			
		}
		
		if (raceName == null) {
			raceName = leaderboard.getName() + " " + raceColumn.getName() + " " + fleet.getName();
		}
		
		RaceLog raceLog = raceColumn.getRaceLog(fleet);
		assert raceLog != null : new NotDenotableForTrackingException("No RaceLog found in place");
		assert raceLog.isEmpty() : new NotDenotableForTrackingException("RaceLog is not empty");
		
		RaceLogEvent event = RaceLogEventFactory.INSTANCE.createDenoteForTrackingEvent(
				MillisecondsTimePoint.now(), service.getServerAuthor(), raceLog.getCurrentPassId(), raceName, boatClass);
		raceLog.add(event);
	}

	@Override
	public boolean canRaceBeAdded(RacingEventService service, RaceColumn raceColumn, Fleet fleet) {
		if (raceColumn.getTrackedRace(fleet) == null) {
			RaceLog raceLog = raceColumn.getRaceLog(fleet);
			if (service.getRaceTrackerById(raceLog.getId()) == null) {
				RaceLogTrackingState state = new RaceLogTrackingStateAnalyzer(raceLog).analyze();
				return state.isForTracking();
			}
		}
		return false;
	}
}
