package com.sap.sailing.gwt.home.desktop.places.event;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.event.EventState;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.home.communication.eventview.HasRegattaMetadata;
import com.sap.sailing.gwt.home.communication.media.GetMediaForEventAction;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab.RegattaRacesPlace;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public abstract class AbstractEventActivity<PLACE extends AbstractEventPlace> extends AbstractActivity implements
        EventView.Presenter {

    protected final PLACE currentPlace;

    protected final EventContext ctx;

    protected final EventClientFactory clientFactory;

    private final Timer timerForClientServerOffset;

    protected final DesktopPlacesNavigator homePlacesNavigator;

    private static final ApplicationHistoryMapper historyMapper = GWT.create(ApplicationHistoryMapper.class);

    protected final EventViewDTO eventDTO;

    public AbstractEventActivity(PLACE place, EventViewDTO eventDTO, EventClientFactory clientFactory, DesktopPlacesNavigator homePlacesNavigator) {
        this.currentPlace = place;
        this.eventDTO = eventDTO;
        this.homePlacesNavigator = homePlacesNavigator;
        this.ctx = new EventContext(place.getCtx());
        this.timerForClientServerOffset = new Timer(PlayModes.Replay);
        this.clientFactory = clientFactory;
    }

    public SailingServiceAsync getSailingService() {
        return clientFactory.getSailingService();
    }
    
    public SailingDispatchSystem getDispatch() {
        return clientFactory.getDispatch();
    }
    
    @Override
    public ErrorAndBusyClientFactory getErrorAndBusyClientFactory() {
        return clientFactory;
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
        return null;
    }

    @Override
    public Timer getTimerForClientServerOffset() {
        return timerForClientServerOffset;
    }

    @Override
    public AbstractEventRegattaPlace getPlaceForRegatta(String regattaId) {
        return new RegattaOverviewPlace(contextForRegatta(regattaId));
    }

    @Override
    public RegattaRacesPlace getPlaceForRegattaRaces(String regattaId) {
        return new RegattaRacesPlace(contextForRegatta(regattaId));
    }

    public RegattaLeaderboardPlace getPlaceForRegattaLeaderboard(String regattaId) {
        return new RegattaLeaderboardPlace(contextForRegatta(regattaId));
    }

    protected EventContext contextForRegatta(String regattaId) {
        return new EventContext(ctx).withRegattaId(regattaId).withRegattaAnalyticsManager(null);
    }
    
    @Override
    public String getRaceViewerURL(SimpleRaceMetadataDTO raceMetadata) {
        return getRaceViewerURL(raceMetadata.getLeaderboardName(), raceMetadata.getLeaderboardGroupName(),
                    raceMetadata.getRegattaAndRaceIdentifier());
    }
    
    @Override
    public String getRaceViewerURL(String leaderboardName, String leaderboardGroupName, RegattaAndRaceIdentifier raceIdentifier) {
        return EntryPointLinkFactory.createRaceBoardLink(createRaceBoardLinkParameters(leaderboardName, 
                leaderboardGroupName, raceIdentifier.getRegattaName(), raceIdentifier.getRaceName()));
    }

    private Map<String, String> createRaceBoardLinkParameters(String leaderboardName, String leaderboardGroupName,
            String regattaName, String trackedRaceName) {
        Map<String, String> linkParams = new HashMap<String, String>();
        linkParams.put("eventId", ctx.getEventId());
        linkParams.put("leaderboardName", leaderboardName);
        if (leaderboardGroupName != null) {
            linkParams.put("leaderboardGroupName", leaderboardGroupName);
        }
        linkParams.put("raceName", trackedRaceName);
        // TODO this must only be forwarded if there is a logged-on user
        // linkParams.put(RaceBoardViewConfiguration.PARAM_CAN_REPLAY_DURING_LIVE_RACES, "true");
        linkParams.put(RaceBoardPerspectiveSettings.PARAM_VIEW_SHOW_MAPCONTROLS, "true");
        linkParams.put(RaceBoardPerspectiveSettings.PARAM_VIEW_SHOW_NAVIGATION_PANEL, "true");
        linkParams.put("regattaName", regattaName);
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
        return homePlacesNavigator.getEventNavigation(ctx.getEventId(), null, false);
    }

    @Override
    public PlaceNavigation<SeriesDefaultPlace> getCurrentEventSeriesNavigation() {
        return homePlacesNavigator.getEventSeriesNavigation(ctx.getEventId(), null, false);
    }

    @Override
    public PlaceNavigation<RegattaRacesPlace> getRegattaRacesNavigation(String regattaId) {
        return homePlacesNavigator.getEventNavigation(getPlaceForRegattaRaces(regattaId), null, false);
    }

    @Override
    public PlaceNavigation<AbstractEventRegattaPlace> getRegattaNavigation(String regattaId) {
        return homePlacesNavigator.getEventNavigation(getPlaceForRegatta(regattaId), null, false);
    }

    @Override
    public PlaceNavigation<RegattaLeaderboardPlace> getRegattaLeaderboardNavigation(String regattaId) {
        return homePlacesNavigator.getEventNavigation(getPlaceForRegattaLeaderboard(regattaId), null, false);
    }

    @Override
    public void ensureMedia(final AsyncCallback<MediaDTO> callback) {
        getDispatch().execute(new GetMediaForEventAction(eventDTO.getId()), callback);
    }

    @Override
    public boolean hasMedia() {
        if (showRegattaMetadata()) {
            return false;
        }
        return eventDTO.isHasMedia();
    }
    
    @Override
    public String getRegattaOverviewLink() {
        String url = "RegattaOverview.html?ignoreLocalSettings=true&onlyrunningraces=false&event=" + getCtx().getEventId();
        url += "&onlyracesofsameday=" + eventDTO.isRunning();
        if(showRegattaMetadata()) {
            if(getRegattaMetadata().isFlexibleLeaderboard()) {
                String defaultCourseAreaId = getRegattaMetadata().getDefaultCourseAreaId();
                if(defaultCourseAreaId != null && !defaultCourseAreaId.isEmpty()) {
                    url += "&coursearea=" + defaultCourseAreaId;
                }
            } else {
                url += "&regatta=" + getRegattaId();
            }
        }
        return url;
    }
    
    @Override
    public boolean isEventOrRegattaLive() {
        return eventDTO.getState() == EventState.RUNNING;
    }
    
    public String getRegattaId() {
        String regattaId = currentPlace.getRegattaId();
        if(regattaId  != null) {
            return regattaId;
        }
        if(!eventDTO.getRegattas().isEmpty() && (eventDTO.getType() == EventType.SINGLE_REGATTA || eventDTO.getType() == EventType.SERIES_EVENT)) {
            return eventDTO.getRegattas().iterator().next().getId();
        }
        return null;
    }
    
    @Override
    public EventViewDTO getEventDTO() {
        return eventDTO;
    }

    protected abstract EventView<PLACE, ?> getView();

}
