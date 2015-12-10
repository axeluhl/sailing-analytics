package com.sap.sailing.gwt.home.communication.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.LeaderboardContext;
import com.sap.sailing.gwt.home.server.RaceContext;
import com.sap.sailing.gwt.home.server.EventActionUtil.LeaderboardCallback;
import com.sap.sailing.gwt.home.server.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sse.common.Duration;

public class GetRegattasAndLiveRacesForEventAction implements SailingAction<ResultWithTTL<RegattasAndLiveRacesDTO>>,
        IsClientCacheable {
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetRegattasAndLiveRacesForEventAction() {
    }

    public GetRegattasAndLiveRacesForEventAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<RegattasAndLiveRacesDTO> execute(SailingDispatchContext context) {
        Event event = context.getRacingEventService().getEvent(eventId);
        final Map<String, RegattaMetadataDTO> regattas = mapRegattas(context, event);
        final TreeMap<RegattaMetadataDTO, TreeSet<LiveRaceDTO>> regattasWithRaces = new TreeMap<>();
        EventState eventState = HomeServiceUtil.calculateEventState(event);
        
        final Duration ttl;
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
            ttl = Duration.ONE_MINUTE.times(2);
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
    private Map<String, RegattaMetadataDTO> mapRegattas(SailingDispatchContext context, Event event) {
        final Map<String, RegattaMetadataDTO> result = new HashMap<>();
        EventActionUtil.forLeaderboardsOfEvent(context, event, new LeaderboardCallback() {
            @Override
            public void doForLeaderboard(LeaderboardContext context) {
                result.put(context.getLeaderboardName(), context.asRegattaMetadataDTO());
            }
        });
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

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
    }
}
