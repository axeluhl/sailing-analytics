package com.sap.sailing.gwt.home.communication.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.EventActionUtil.LeaderboardCallback;
import com.sap.sailing.gwt.home.server.LeaderboardContext;
import com.sap.sailing.gwt.home.server.RaceContext;
import com.sap.sailing.gwt.home.server.RaceRefreshCalculator;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown on the mobile event overview page for the
 * {@link #GetRegattasAndLiveRacesForEventAction(UUID) given event-id}, preparing the appropriate data structure.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live is <i>2 minutes</i> for upcoming and currently running events,
 * otherwise it {@link EventActionUtil#calculateTtlForNonLiveEvent(Event, EventState) depends on the event's state}.
 * </p>
 */
public class GetRegattasAndLiveRacesForEventAction implements SailingAction<ResultWithTTL<RegattasAndLiveRacesDTO>>,
        IsClientCacheable {
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetRegattasAndLiveRacesForEventAction() {
    }

    /**
     * Creates a {@link GetRegattasAndLiveRacesForEventAction} instance for the given event-id.
     * 
     * @param eventId
     *            {@link UUID} of the event to load regattas and live races for
     */
    public GetRegattasAndLiveRacesForEventAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<RegattasAndLiveRacesDTO> execute(SailingDispatchContext context) {
        final Map<String, RegattaMetadataDTO> regattas = mapRegattas(context);
        final TreeMap<RegattaMetadataDTO, TreeSet<LiveRaceDTO>> regattasWithRaces = new TreeMap<>();
        final TreeSet<RegattaMetadataDTO> regattasWithoutRaces = new TreeSet<>();
        final RaceRefreshCalculator refreshCalculator = new RaceRefreshCalculator();

        EventActionUtil.forRacesOfEvent(context, eventId, raceContent -> {
            Optional<LiveRaceDTO> liveRace = Optional.ofNullable(raceContent.getLiveRaceOrNull());
            liveRace.ifPresent(r -> ensureRegatta(raceContent, regattas, regattasWithRaces).add(r));
            refreshCalculator.doForRace(raceContent);
        });
        regattas.values().stream().filter(regatta -> !regattasWithRaces.containsKey(regatta))
                .forEach(regattasWithoutRaces::add);
        RegattasAndLiveRacesDTO dto = new RegattasAndLiveRacesDTO(regattasWithRaces, regattasWithoutRaces);
        ResultWithTTL<RegattasAndLiveRacesDTO> result = new ResultWithTTL<>(refreshCalculator.getTTL(), dto);
        return EventActionUtil.withLiveRaceOrDefaultSchedule(context, eventId, event -> result, dto);
    }

    @GwtIncompatible
    private Map<String, RegattaMetadataDTO> mapRegattas(SailingDispatchContext context) {
        final Map<String, RegattaMetadataDTO> result = new HashMap<>();
        EventActionUtil.forLeaderboardsOfEvent(context, eventId, new LeaderboardCallback() {
            @Override
            public void doForLeaderboard(LeaderboardContext context) {
                result.put(context.getLeaderboardName(), context.asRegattaMetadataDTO());
            }
        });
        return result;
    }

    @GwtIncompatible
    protected Set<LiveRaceDTO> ensureRegatta(RaceContext context, Map<String, RegattaMetadataDTO> regattas,
            Map<RegattaMetadataDTO, TreeSet<LiveRaceDTO>> regattasWithRaces) {
        String regattaName = context.getRegattaName();
        RegattaMetadataDTO regatta = regattas.get(regattaName);
        TreeSet<LiveRaceDTO> races = regattasWithRaces.get(regatta);
        if (races == null) {
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
