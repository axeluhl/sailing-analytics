package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;

public class GetRegattasAndLiveRacesForEventAction implements Action<ResultWithTTL<RegattasAndLiveRacesDTO>> {
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
        Event event = context.getRacingEventService().getEvent(eventId);
        final Map<String, RegattaMetadataDTO> regattas = mapRegattas(event);
        final TreeMap<RegattaMetadataDTO, TreeSet<LiveRaceDTO>> regattasWithRaces = new TreeMap<>();
        EventState eventState = HomeServiceUtil.calculateEventState(event);
        
        final long ttl;
        if(eventState == EventState.RUNNING) {
            EventActionUtil.forRacesOfEvent(context, eventId, new RaceCallback() {
                @Override
                public void doForRace(RaceContext context) {
                    LiveRaceDTO liveRace = context.getLiveRaceOrNull();
                    if(liveRace != null) {
                        ensureRegatta(context, regattas, regattasWithRaces).add(liveRace);
                    }
                }
            });
            ttl = 1000 * 60 * 2;
        } else {
            ttl = EventActionUtil.calculateTtlForNonLiveEvent(event, eventState);
        }
        
        final TreeSet<RegattaMetadataDTO> regattasWithoutRaces = new TreeSet<>();
        for(RegattaMetadataDTO regatta : regattas.values()) {
            if(!regattasWithRaces.containsKey(regatta)) {
                regattasWithoutRaces.add(regatta);
            }
        }
        
        return new ResultWithTTL<RegattasAndLiveRacesDTO>(ttl, new RegattasAndLiveRacesDTO(regattasWithRaces, regattasWithoutRaces));
    }

    @GwtIncompatible
    private Map<String, RegattaMetadataDTO> mapRegattas(Event event) {
        Map<String, RegattaMetadataDTO> result = new HashMap<>();
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for (Leaderboard lb : lg.getLeaderboards()) {
                result.put(lb.getName(), HomeServiceUtil.toRegattaMetadataDTO(event, lg, lb));
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
