package com.sap.sailing.gwt.home.desktop.places.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.event.EventSeriesReferenceDTO;
import com.sap.sailing.gwt.home.communication.event.EventState;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.HasRegattaMetadata;
import com.sap.sailing.gwt.home.communication.eventview.SeriesReferenceWithEventsDTO;
import com.sap.sailing.gwt.home.communication.media.GetMediaForEventAction;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab.RegattaRacesPlace;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.settings.client.EntryPointWithSettingsLinkFactory;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaOverviewContextDefinition;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

/**
 * Base Activity for all desktop event pages. This includes the event page itself for multi-regatta events as well as
 * regatta/single-regatta-event pages.
 *
 * @param <PLACE> The concrete {@link AbstractEventPlace} subclass, this instance is bound to.
 */
public abstract class AbstractEventActivity<PLACE extends AbstractEventPlace> extends AbstractActivity implements
        EventView.Presenter {

    protected final PLACE currentPlace;

    protected final EventContext ctx;

    protected final EventClientFactory clientFactory;

    private final Timer timerForClientServerOffset;

    protected final DesktopPlacesNavigator homePlacesNavigator;

    private static final ApplicationHistoryMapper historyMapper = GWT.create(ApplicationHistoryMapper.class);

    protected final EventViewDTO eventDTO;

    /**
     * @param place the place of the tab to be activated
     * @param eventDTO the basic event data
     * @param clientFactory the {@link EventClientFactory} to use
     * @param homePlacesNavigator the {@link DesktopPlacesNavigator} to use to navigate to other places in the application
     */
    public AbstractEventActivity(PLACE place, EventViewDTO eventDTO, EventClientFactory clientFactory, DesktopPlacesNavigator homePlacesNavigator) {
        this.currentPlace = place;
        this.eventDTO = eventDTO;
        this.homePlacesNavigator = homePlacesNavigator;
        this.ctx = new EventContext(place.getCtx());
        this.timerForClientServerOffset = new Timer(PlayModes.Replay);
        this.clientFactory = clientFactory;
    }
    
    /**
     * Provides the navigation path to the {@link #getEventDTO() event}.
     * 
     * @return {@link List} of {@link NavigationItem} to the event level
     */
    protected final List<NavigationItem> getNavigationPathToEventLevel() {
        final List<NavigationItem> navItems = new ArrayList<>();
        navItems.add(new NavigationItem(StringMessages.INSTANCE.home(), getHomeNavigation()));
        navItems.add(new NavigationItem(StringMessages.INSTANCE.events(), getEventsNavigation()));
        final SeriesReferenceWithEventsDTO seriesData = getEventDTO().getSeriesData();
        if (seriesData != null) {
            navItems.add(new NavigationItem(seriesData.getSeriesDisplayName(), getCurrentEventSeriesNavigation()));
        }
        navItems.add(new NavigationItem(getEventDTO().getLocationOrDisplayName(), getCurrentEventNavigation()));
        return navItems;
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
    public String getRaceViewerURL(SimpleRaceMetadataDTO raceMetadata, String mode) {
        return getRaceViewerURL(raceMetadata.getLeaderboardName(), raceMetadata.getLeaderboardGroupName(),
                raceMetadata.getRegattaAndRaceIdentifier(), mode);
    }
    
    @Override
    public String getRaceViewerURL(String leaderboardName, String leaderboardGroupName, RegattaAndRaceIdentifier raceIdentifier) {
        return getRaceViewerURL(leaderboardName, leaderboardGroupName, raceIdentifier, null);
    }
    
    private String getRaceViewerURL(String leaderboardName, String leaderboardGroupName, RegattaAndRaceIdentifier raceIdentifier, String mode) {
        RaceboardContextDefinition raceboardContext = new RaceboardContextDefinition(raceIdentifier.getRegattaName(),
                raceIdentifier.getRaceName(), leaderboardName, leaderboardGroupName, UUID.fromString(ctx.getEventId()), mode);
        RaceBoardPerspectiveOwnSettings perspectiveOwnSettings = new RaceBoardPerspectiveOwnSettings();
        
        HashMap<String, Settings> innerSettings = new HashMap<>();
        innerSettings.put(RaceMapLifecycle.ID, RaceMapSettings.getDefaultWithShowMapControls(true));
        PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings = new PerspectiveCompositeSettings<>(
                perspectiveOwnSettings, innerSettings);
        return EntryPointWithSettingsLinkFactory.createRaceBoardLink(raceboardContext, settings);
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
    
    protected PlaceNavigation<SeriesDefaultPlace> getEventSeriesNavigation(final EventSeriesReferenceDTO seriesReference) {
        if (seriesReference != null) {
            final UUID seriesLeaderboardGroupId = seriesReference.getSeriesLeaderboardGroupId();
            return homePlacesNavigator.getEventSeriesNavigation(SeriesContext.createWithLeaderboardGroupId(seriesLeaderboardGroupId), null, false);
        }
        return null;
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
        RegattaRaceStatesSettings regattaRaceStatesSettings = new RegattaRaceStatesSettings();
        regattaRaceStatesSettings.setShowOnlyCurrentlyRunningRaces(false);
        regattaRaceStatesSettings.setShowOnlyRaceOfSameDay(eventDTO.isRunning());
        if(showRegattaMetadata()) {
            if(getRegattaMetadata().isFlexibleLeaderboard()) {
                String defaultCourseAreaId = getRegattaMetadata().getDefaultCourseAreaId();
                if(defaultCourseAreaId != null && !defaultCourseAreaId.isEmpty()) {
                    regattaRaceStatesSettings.getVisibleCourseAreaSettings().addValue(UUID.fromString(defaultCourseAreaId));
                }
            } else {
                regattaRaceStatesSettings.getVisibleRegattaSettings().addValue(getRegattaId());
            }
        }
        return EntryPointWithSettingsLinkFactory.createRegattaOverviewLink(
                new RegattaOverviewContextDefinition(getCtx().getEventId()), regattaRaceStatesSettings, false);
    }
    
    @Override
    public boolean isEventOrRegattaLive() {
        return eventDTO.getState() == EventState.RUNNING;
    }
    
    public String getRegattaId() {
        String regattaId = currentPlace.getRegattaId();
        if (regattaId != null) {
            return regattaId;
        }
        if (!eventDTO.getRegattas().isEmpty() && !eventDTO.isMultiRegatta()) {
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
