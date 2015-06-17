package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RacesActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public class GetRegattasAndLiveRacesForEventAction implements Action<ResultWithTTL<RegattasAndLiveRacesDTO>> {
    private static final Logger logger = Logger.getLogger(GetRegattasAndLiveRacesForEventAction.class.getName());
    
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetRegattasAndLiveRacesForEventAction() {
    }

    public GetRegattasAndLiveRacesForEventAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<RegattasAndLiveRacesDTO> execute(DispatchContext context) {
        long start = System.currentTimeMillis();
        final Map<String, RegattaMetadataDTO> regattas = mapRegattas(context);
        final TreeMap<RegattaMetadataDTO, TreeSet<LiveRaceDTO>> regattasWithRaces = new TreeMap<>();
        
        RacesActionUtil.forRacesOfEvent(context, eventId, new RaceCallback() {
            @Override
            public void doForRace(RaceContext context) {
                LiveRaceDTO liveRace = context.getLiveRaceOrNull();
                if(liveRace != null) {
                    ensureRegatta(context, regattas, regattasWithRaces).add(liveRace);
                }
            }
        });
        
        final TreeSet<RegattaMetadataDTO> regattasWithoutRaces = new TreeSet<>();
        for(RegattaMetadataDTO regatta : regattas.values()) {
            if(!regattasWithRaces.containsKey(regatta)) {
                regattasWithoutRaces.add(regatta);
            }
        }
        
        RegattasAndLiveRacesDTO result = new RegattasAndLiveRacesDTO(regattasWithRaces, regattasWithoutRaces);
        long duration = System.currentTimeMillis() - start;
        logger.log(Level.INFO, "Calculating live races for event "+ eventId + " took: "+ duration + "ms");
        return new ResultWithTTL<RegattasAndLiveRacesDTO>(1000 * 60 * 2, result);
    }

    @GwtIncompatible
    private Map<String, RegattaMetadataDTO> mapRegattas(DispatchContext context) {
        Event event = context.getRacingEventService().getEvent(eventId);
        Map<String, RegattaMetadataDTO> result = new HashMap<>();
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for (Leaderboard lb : lg.getLeaderboards()) {
                result.put(lb.getName(), HomeServiceUtil.toRegattaMetadataDTO(lg, lb));
            }
        }
        return result;
    }

    @GwtIncompatible
    protected Set<LiveRaceDTO> ensureRegatta(RaceContext context, Map<String, RegattaMetadataDTO> regattas, Map<RegattaMetadataDTO, TreeSet<LiveRaceDTO>> regattasWithRaces) {
        String regattaName = context.getRegattaName();
        RegattaMetadataDTO regatta = regattas.get(regattaName);
        TreeSet<LiveRaceDTO> races = regattasWithRaces.get(regatta);
        if(races == null) {
            races = new TreeSet<>();
            regattasWithRaces.put(regatta, races);
        }
        return races;
    }
}
