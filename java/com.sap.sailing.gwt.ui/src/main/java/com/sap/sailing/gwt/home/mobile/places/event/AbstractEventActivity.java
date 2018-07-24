package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.EventType;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.event.EventReferenceWithStateDTO;
import com.sap.sailing.gwt.home.communication.event.EventState;
import com.sap.sailing.gwt.home.communication.event.news.LeaderboardNewsEntryDTO;
import com.sap.sailing.gwt.home.communication.event.news.NewsEntryDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.communication.media.GetMediaForEventAction;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.mediatab.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.mediatab.RegattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab.RegattaRacesPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase.Presenter;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.event.overview.AbstractEventOverview;
import com.sap.sailing.gwt.home.mobile.places.series.minileaderboard.SeriesMiniOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.app.ActivityCallback;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.settings.client.EntryPointWithSettingsLinkFactory;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public abstract class AbstractEventActivity<PLACE extends AbstractEventPlace> extends AbstractActivity implements Presenter {
    private final MobileApplicationClientFactory clientFactory;
    protected final PLACE place;
    protected final EventViewDTO eventDTO;
    private AcceptsOneWidget panel;
    
    protected AbstractEventActivity(PLACE place, EventViewDTO eventDTO, MobileApplicationClientFactory clientFactory) {
        this.eventDTO = eventDTO;
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public final void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        this.panel = panel;
        final EventViewBase view = initView();
        panel.setWidget(view.asWidget());
    }
    
    protected abstract EventViewBase initView();
    
    protected final void initSeriesNavigation(EventViewBase view) {
        if (eventDTO.getType() == EventType.SERIES) {
            String seriesIdAsString = eventDTO.getSeriesIdAsString();
            PlaceNavigation<?> navigation = clientFactory.getNavigator().getEventSeriesNavigation(seriesIdAsString, null, false);
            view.setSeriesNavigation(eventDTO.getSeriesName(), navigation);
        }
    }
    
    protected final void initSailorInfo(EventViewBase view) {
        String sailorInfoUrl = eventDTO.getSailorsInfoWebsiteURL();
        if (sailorInfoUrl != null && !sailorInfoUrl.isEmpty()) {
            view.setSailorInfos(StringMessages.INSTANCE.sailorInfoLongText(), StringMessages.INSTANCE.sailorInfo(), sailorInfoUrl);
        }
    }
    
    protected final void initWindfinderNavigations(EventViewBase view) {
        final Iterable<SpotDTO> spots = eventDTO.getAllWindFinderSpotIdsUsedByEvent();
        if (spots != null && !Util.isEmpty(spots)) {
            view.setWindfinderNavigations(spots);
        }
    }

    protected final void initQuickfinder(EventViewBase view, boolean showQuickfinder) {
        EventViewDTO event = eventDTO;
        if(showQuickfinder && event.getType() == EventType.MULTI_REGATTA) {
            view.setQuickFinderValues(getRegattasByLeaderboardGroupName());
        } else if(showQuickfinder && event.getType() == EventType.SERIES) {
            List<EventReferenceWithStateDTO> eventsOfSeriesSorted = event.getEventsOfSeriesSorted();
            List<EventReferenceWithStateDTO> seriesEventToShow = new ArrayList<>(eventsOfSeriesSorted.size());
            for (EventReferenceWithStateDTO seriesEvent : eventsOfSeriesSorted) {
                if(seriesEvent.getState() != EventState.PLANNED) {
                    seriesEventToShow.add(seriesEvent);
                }
            }
            view.setQuickFinderValues(event.getSeriesName(), seriesEventToShow);
        } else {
            view.hideQuickfinder();
        }
    }
    
    private Map<String, Set<RegattaMetadataDTO>> getRegattasByLeaderboardGroupName() {
        Map<String, Set<RegattaMetadataDTO>> regattasByLeaderboardGroupName = new TreeMap<>(new NaturalComparator(false));
        for (RegattaMetadataDTO regatta : eventDTO.getRegattas()) {
            if (Util.isEmpty(regatta.getLeaderboardGroupNames())) {
                addRegattaToLeaderboardGroup(regattasByLeaderboardGroupName, null, regatta);
            }
            for (String leaderboardGroupName : regatta.getLeaderboardGroupNames()) {
                addRegattaToLeaderboardGroup(regattasByLeaderboardGroupName, leaderboardGroupName, regatta);
            }
        }
        return regattasByLeaderboardGroupName;
    }
    
    private void addRegattaToLeaderboardGroup(Map<String, Set<RegattaMetadataDTO>> regattasByLeaderboardGroupName, 
            String leaderboardGroupName, RegattaMetadataDTO regatta) {
        Set<RegattaMetadataDTO> regattasForLg = regattasByLeaderboardGroupName.get(leaderboardGroupName);
        if (regattasForLg == null) {
            regattasByLeaderboardGroupName.put(leaderboardGroupName, regattasForLg = new TreeSet<>());
        }
        regattasForLg.add(regatta);
    }
    
    protected final void initMedia(final AbstractEventOverview view) {
        this.initMedia(new MediaCallback() {
            @Override
            public void onSuccess(MediaDTO result) {
                view.setMediaForImpressions(result.getPhotos().size(), result.getVideos().size(), result.getPhotos());
            }
        });
    }
    
    protected final void initMedia(final MediaCallback callback) {
        if (eventDTO.isHasMedia()) {
            clientFactory.getDispatch().execute(new GetMediaForEventAction(eventDTO.getId()), 
                    new ActivityCallback<MediaDTO>(clientFactory, panel) {
                @Override
                public void onSuccess(MediaDTO result) {
                    callback.onSuccess(result);
                }
            });
        }
    }
    
    protected interface MediaCallback {
        void onSuccess(MediaDTO result);
    }
    
    protected final PLACE getPlace() {
        return place;
    }
    
    @Override
    public EventContext getCtx() {
        return getPlace().getCtx();
    }

    public SailingDispatchSystem getDispatch() {
        return clientFactory.getDispatch();
    }
    
    @Override
    public ErrorAndBusyClientFactory getErrorAndBusyClientFactory() {
        return clientFactory;
    }
    
    @Override
    public PlaceNavigation<?> getRegattaLeaderboardNavigation(String leaderboardName) {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(leaderboardName).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new RegattaLeaderboardPlace(ctx), null, false);
    }

    @Override
    public PlaceNavigation<?> getRegattaMiniLeaderboardNavigation(String leaderboardName) {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(leaderboardName).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new MiniLeaderboardPlace(ctx), null, false);
    }

    @Override
    public PlaceNavigation<?> getMiniOverallLeaderboardNavigation() {
        return clientFactory.getNavigator().getSeriesNavigation(new SeriesMiniOverallLeaderboardPlace(new SeriesContext(getCtx().getEventId())), null, false);
    }
    
    @Override
    public PlaceNavigation<?> getMiniLeaderboardNavigation(UUID eventId) {
        return clientFactory.getNavigator().getEventNavigation(new MiniLeaderboardPlace(eventId.toString(), null), null, false);
    }

    @Override
    public PlaceNavigation<?> getEventNavigation() {
        return clientFactory.getNavigator().getEventNavigation(new EventDefaultPlace(getCtx()), null, false);
    }
    
    public PlaceNavigation<?> getMediaPageNavigation() {
        if (eventDTO.getType() == EventType.MULTI_REGATTA) {
            return clientFactory.getNavigator().getEventNavigation(new MultiregattaMediaPlace(getCtx()), null, false);
        } else {
            return clientFactory.getNavigator().getEventNavigation(new RegattaMediaPlace(getCtx()), null, false);
        }
    }
    
    public PlaceNavigation<?> getNewsPlaceNavigation(List<NewsEntryDTO> values) {
        return clientFactory.getNavigator().getEventLastestNewsNavigation(getCtx(), values, null, false);
    }

    public PlaceNavigation<?> getNewsEntryPlaceNavigation(NewsEntryDTO entry) {
        if(entry instanceof LeaderboardNewsEntryDTO) {
            final LeaderboardNewsEntryDTO dto = (LeaderboardNewsEntryDTO) entry;
            return getRegattaMiniLeaderboardNavigation(dto.getLeaderboardName());
        } 
        return null;
    }
    
    public PlaceNavigation<?> getRegattaOverviewNavigation(String regattaId) {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(regattaId).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new RegattaOverviewPlace(ctx), null, false);
    }
    
    @Override
    public PlaceNavigation<?> getSeriesEventOverviewNavigation(UUID eventId) {
        return clientFactory.getNavigator().getEventNavigation(new EventDefaultPlace(eventId.toString()), null, false);
    }
    
    public PlaceNavigation<?> getSeriesNavigationForCurrentEvent() {
        return clientFactory.getNavigator().getSeriesNavigation(new SeriesDefaultPlace(place.getEventUuidAsString()), null, false);
    }
    
    public PlaceNavigation<?> getRegattaRacesNavigation(String regattaId) {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(regattaId).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new RegattaRacesPlace(ctx), null, false);
    }

    @Override
    public PlaceNavigation<?> getRegattaRacesNavigation(String regattaId, String prefSeriesName) {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(regattaId).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new RegattaRacesPlace(ctx, prefSeriesName), null, false);
    }
    
    public PlaceNavigation<?> getLatesNewsNavigation() {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(null).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new LatestNewsPlace(ctx), null, false);
    }
    
    @Override
    public PlaceNavigation<?> getSeriesEventRacesNavigation(UUID eventId) {
        return clientFactory.getNavigator().getEventNavigation(new RegattaRacesPlace(eventId.toString(), null), null, false);
    }
    
    @Override
    public String getRaceViewerURL(SimpleRaceMetadataDTO raceMetadata, String mode) {
        RegattaAndRaceIdentifier raceid = raceMetadata.getRegattaAndRaceIdentifier();
        return getRaceViewerURL(raceMetadata.getLeaderboardName(), raceMetadata.getLeaderboardGroupName(), raceid,
                mode);
    }
    
    private String getRaceViewerURL(String leaderboardName, String leaderboardGroupName,
            RegattaAndRaceIdentifier raceIdentifier, String mode) {
        RaceboardContextDefinition raceboardContext = new RaceboardContextDefinition(raceIdentifier.getRegattaName(),
                raceIdentifier.getRaceName(), leaderboardName, leaderboardGroupName,
                UUID.fromString(getCtx().getEventId()), mode);
        RaceBoardPerspectiveOwnSettings perspectiveOwnSettings = new RaceBoardPerspectiveOwnSettings();
        
        HashMap<String, Settings> innerSettings = new HashMap<>();
        innerSettings.put(RaceMapLifecycle.ID, RaceMapSettings.getDefaultWithShowMapControls(true));
        PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings = new PerspectiveCompositeSettings<>(
                perspectiveOwnSettings, innerSettings);
        return EntryPointWithSettingsLinkFactory.createRaceBoardLink(raceboardContext, settings);
    }
    
    public String getRegattaId() {
        String regattaId = place.getRegattaId();
        if(regattaId  != null) {
            return regattaId;
        }
        if(!eventDTO.getRegattas().isEmpty() && (eventDTO.getType() == EventType.SINGLE_REGATTA || eventDTO.getType() == EventType.SERIES)) {
            return eventDTO.getRegattas().iterator().next().getId();
        }
        return null;
    }
    
    public RegattaMetadataDTO getRegatta() {
        String regattaId = getRegattaId();
        if(regattaId == null) {
            return null;
        }
        for (RegattaMetadataDTO regatta : eventDTO.getRegattas()) {
            if(regattaId.equals(regatta.getId())) {
                return regatta;
            }
        }
        return null;
    }
    
    @Override
    public EventViewDTO getEventDTO() {
        return eventDTO;
    }
    
    @Override
    public boolean isMultiRegattaEvent() {
        return getEventDTO().getType() == EventType.MULTI_REGATTA;
    }

    @Override
    public boolean isSingleRegattaEvent() {
        return getEventDTO().getType() == EventType.SINGLE_REGATTA;
    }

    protected List<NavigationItem> getNavigationPathToEventLevel() {
        List<NavigationItem> navigationItems = new ArrayList<>();
        if(getEventDTO().getType() == EventType.SERIES) {
            navigationItems.add(new NavigationItem(getEventDTO().getSeriesName(), getSeriesNavigationForCurrentEvent()));
        }
        navigationItems.add(new NavigationItem(getEventDTO().getLocationOrDisplayName(), getEventNavigation()));
        return navigationItems;
    }
    
    protected List<NavigationItem> getNavigationPathToRegattaLevel() {
        List<NavigationItem> navigationItems = getNavigationPathToEventLevel();
        if(getEventDTO().getType() == EventType.MULTI_REGATTA) {
            navigationItems.add(new NavigationItem(getRegatta().getDisplayName(), getRegattaOverviewNavigation(getRegattaId())));
        }
        return navigationItems;
    }
}
