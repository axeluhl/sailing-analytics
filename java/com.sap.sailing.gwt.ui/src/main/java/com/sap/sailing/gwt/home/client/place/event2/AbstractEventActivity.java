package com.sap.sailing.gwt.home.client.place.event2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardViewConfiguration;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.HasRegattaMetadata;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public abstract class AbstractEventActivity<PLACE extends AbstractEventPlace> extends AbstractActivity implements EventView.Presenter {

    protected final PLACE currentPlace;

    protected final EventContext ctx;

    protected final EventClientFactory clientFactory;

    private final Timer timerForClientServerOffset;

    protected final HomePlacesNavigator homePlacesNavigator;
    
    private static final ApplicationHistoryMapper historyMapper = GWT.create(ApplicationHistoryMapper.class);

    public AbstractEventActivity(PLACE place, EventClientFactory clientFactory, HomePlacesNavigator homePlacesNavigator) {
        this.currentPlace = place;
        this.homePlacesNavigator = homePlacesNavigator;
        this.ctx = new EventContext(place.getCtx());
        this.timerForClientServerOffset = new Timer(PlayModes.Replay);

        this.clientFactory = clientFactory;
    }

    public SailingServiceAsync getSailingService() {
        return clientFactory.getSailingService();
    }

    @Override
    public EventContext getCtx() {
        return ctx;
    }

    @Override
    public void handleTabPlaceSelection(TabView<?, ? extends EventView.Presenter> selectedActivity) {
        Place tabPlaceToGo = selectedActivity.placeToFire();
        clientFactory.getPlaceController().goTo(tabPlaceToGo);
    }
    
    public void navigateTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }
    
    @Override
    public SafeUri getUrl(AbstractEventPlace place) {
        String token = historyMapper.getToken(place);
        return UriUtils.fromString("#" + token);
    }

    @Override
    public HasRegattaMetadata getRegattaMetadata() {
        return ctx.getRegatta();
    }

    @Override
    public Timer getTimerForClientServerOffset() {
        return timerForClientServerOffset;
    }
    
    @Override
    public AbstractEventRegattaPlace getPlaceForRegatta(String regattaId) {
        // TODO Overview isn't implemented yet
//        return new RegattaOverviewPlace(contextForRegatta(regattaId));
        return new RegattaRacesPlace(contextForRegatta(regattaId));
    }
    
    @Override
    public RegattaRacesPlace getPlaceForRegattaRaces(String regattaId) {
        return new RegattaRacesPlace(contextForRegatta(regattaId));
    }

    protected EventContext contextForRegatta(String regattaId) {
        return new EventContext(ctx).withRegattaId(regattaId);
    }
    
    public String getRaceViewerURL(StrippedLeaderboardDTO leaderboard, RaceDTO race) {
        RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier();
        return EntryPointLinkFactory.createRaceBoardLink(createRaceBoardLinkParameters(leaderboard.name, raceIdentifier));
    }
    
    private Map<String, String> createRaceBoardLinkParameters(String leaderboardName, RegattaAndRaceIdentifier raceIdentifier) {
        Map<String, String> linkParams = new HashMap<String, String>();
        linkParams.put("eventId", ctx.getEventId());
        linkParams.put("leaderboardName", leaderboardName);
        linkParams.put("raceName", raceIdentifier.getRaceName());
        // TODO this must only be forwarded if there is a logged-on user
//        linkParams.put(RaceBoardViewConfiguration.PARAM_CAN_REPLAY_DURING_LIVE_RACES, "true");
        linkParams.put(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_MAPCONTROLS, "true");
        linkParams.put(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_NAVIGATION_PANEL, "true");
        linkParams.put("regattaName", raceIdentifier.getRegattaName());
        return linkParams;
    }
    
    @Override
    public PlaceNavigation<StartPlace> getHomeNavigation() {
        return homePlacesNavigator.getHomeNavigation();
    }

    @Override
    public PlaceNavigation<EventsPlace> getEventsNavigation() {
        return homePlacesNavigator.getEventsNavigation();
    }
    
    @Override
    public PlaceNavigation<EventDefaultPlace> getCurrentEventNavigation() {
        return homePlacesNavigator.getEventNavigation(ctx.getEventId(), ctx.getEventDTO().getBaseURL(), ctx.getEventDTO().isOnRemoteServer());
    }
    
    @Override
    public PlaceNavigation<SeriesDefaultPlace> getCurrentEventSeriesNavigation() {
        return homePlacesNavigator.getEventSeriesNavigation(ctx.getEventId(), ctx.getEventDTO().getBaseURL(), ctx.getEventDTO().isOnRemoteServer());
    }
    
    @Override
    public PlaceNavigation<RegattaRacesPlace> getRegattaRacesNavigation(String regattaId) {
        return homePlacesNavigator.getEventNavigation(getPlaceForRegattaRaces(regattaId), new RegattaRacesPlace.Tokenizer(), ctx.getEventDTO().getBaseURL(), ctx.getEventDTO().isOnRemoteServer());
    }
    
    @Override
    public PlaceNavigation<RegattaRacesPlace> getRegattaNavigation(String regattaId) {
        return homePlacesNavigator.getEventNavigation(getPlaceForRegattaRaces(regattaId), new RegattaRacesPlace.Tokenizer(), ctx.getEventDTO().getBaseURL(), ctx.getEventDTO().isOnRemoteServer());
    }
    
    @Override
    public void ensureRegattaStructure(final AsyncCallback<List<RaceGroupDTO>> callback) {
        if(ctx.getRaceGroups() != null) {
            callback.onSuccess(ctx.getRaceGroups());
        }
        
        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();

        final EventViewDTO eventDTO = ctx.getEventDTO();
        
        getSailingService().getRegattaStructureOfEvent(eventDTO.id,
                new AsyncCallback<List<RaceGroupDTO>>() {
                    @Override
                    public void onSuccess(List<RaceGroupDTO> raceGroups) {
                        if (raceGroups.size() > 0) {
                            for (LeaderboardGroupDTO leaderboardGroupDTO : eventDTO.getLeaderboardGroups()) {
                                final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                                if (leaderboardGroupDTO.getAverageDelayToLiveInMillis() != null) {
                                    timerForClientServerOffset.setLivePlayDelayInMillis(
                                            leaderboardGroupDTO
                                            .getAverageDelayToLiveInMillis());
                                }
                                timerForClientServerOffset.adjustClientServerOffset(
                                        clientTimeWhenRequestWasSent,
                                        leaderboardGroupDTO.getCurrentServerTime(), clientTimeWhenResponseWasReceived);
                            }
                            ctx.withRaceGroups(raceGroups);
                            callback.onSuccess(raceGroups);
                        } else {
                            // TODO
                            // createEventWithoutRegattasView(event, panel);
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        // createErrorView(
                        // "Error while loading the regatta structure with service getRegattaStructureOfEvent()",
                        // caught, panel);
                        callback.onFailure(caught);
                    }
                });
    }
    
    @Override
    public void ensureMedia(AsyncCallback<MediaDTO> callback) {
        getSailingService().getMediaForEvent(ctx.getEventDTO().id, callback);
    }
    
    @Override
    public boolean hasMedia() {
        if(showRegattaMetadata()) {
            return false;
        }
        return ctx.getEventDTO().isHasMedia();
    }
}
