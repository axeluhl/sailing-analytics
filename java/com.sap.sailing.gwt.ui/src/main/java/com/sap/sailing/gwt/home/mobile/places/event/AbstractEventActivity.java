package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.mediatab.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.mediatab.RegattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab.RegattaRacesPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase.Presenter;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.series.minileaderboard.SeriesMiniOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.news.LeaderboardNewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sailing.gwt.ui.shared.util.NullSafeComparableComparator;

public abstract class AbstractEventActivity<PLACE extends AbstractEventPlace> extends AbstractActivity implements Presenter {
    private final MobileApplicationClientFactory clientFactory;
    private final PLACE place;
    protected final EventViewDTO eventDTO;
    
    protected AbstractEventActivity(PLACE place, EventViewDTO eventDTO, MobileApplicationClientFactory clientFactory) {
        this.eventDTO = eventDTO;
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public final void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        final EventViewBase view = initView();
        panel.setWidget(view.asWidget());
    }
    
    protected abstract EventViewBase initView();
    
    protected final void initSailorInfoOrSeriesNavigation(EventViewBase view) {
        EventViewDTO event = eventDTO;
        String sailorInfoUrl = event.getSailorsInfoWebsiteURL();
        if (sailorInfoUrl != null && !sailorInfoUrl.isEmpty()) {
            view.setSailorInfos(StringMessages.INSTANCE.sailorInfoLongText(), StringMessages.INSTANCE.sailorInfo(), sailorInfoUrl);
        } else if (event.getType() == EventType.SERIES_EVENT) {
            String seriesIdAsString = event.getSeriesIdAsString();
            PlaceNavigation<?> navigation = clientFactory.getNavigator().getEventSeriesNavigation(seriesIdAsString, null, false);
            view.setSeriesNavigation(event.getSeriesName(), navigation);
        }
    }
    
    protected final void initQuickfinder(EventViewBase view, boolean showQuickfinder) {
        EventViewDTO event = eventDTO;
        if(showQuickfinder && event.getType() == EventType.MULTI_REGATTA) {
            view.setQuickFinderValues(getSortedQuickFinderValues());
        } else if(showQuickfinder && event.getType() == EventType.SERIES_EVENT) {
            view.setQuickFinderValues(event.getSeriesName(), event.getEventsOfSeries());
        } else {
            view.hideQuickfinder();
        }
    }
    
    private Collection<RegattaMetadataDTO> getSortedQuickFinderValues() {
        Collection<RegattaMetadataDTO> regattas = eventDTO.getRegattas();
        List<RegattaMetadataDTO> sortedRegattas = new ArrayList<RegattaMetadataDTO>(regattas);
        Collections.sort(sortedRegattas, new Comparator<RegattaMetadataDTO>() {
            private Comparator<String> bootCatComparator = new NullSafeComparableComparator<String>();
            @Override
            public int compare(RegattaMetadataDTO o1, RegattaMetadataDTO o2) {
                int bootCategoryComparison = bootCatComparator.compare(o1.getBoatCategory(), o2.getBoatCategory());
                return bootCategoryComparison == 0 ? o1.compareTo(o2) : bootCategoryComparison;
            }
        });
        return sortedRegattas;
    }
    
    protected final void initMedia(AsyncMediaCallback callback) {
        if (eventDTO.isHasMedia()) {
            clientFactory.getHomeService().getMediaForEvent(eventDTO.getId(), callback);
        }
    }
    
    protected abstract class AsyncMediaCallback implements AsyncCallback<MediaDTO> {
        @Override
        public void onFailure(Throwable caught) {
            GWT.log("Failed to load media");
        }
    }
    
    protected final PLACE getPlace() {
        return place;
    }
    
    @Override
    public EventContext getCtx() {
        return getPlace().getCtx();
    }

    public DispatchSystem getDispatch() {
        return clientFactory.getDispatch();
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
        return clientFactory.getNavigator().getSeriesNavigation(new SeriesMiniOverallLeaderboardPlace(new SeriesContext().withId(getCtx().getEventId())), null, false);
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
    
    public PlaceNavigation<?> getRegattaRacesNavigation(String regattaId) {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(regattaId).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new RegattaRacesPlace(ctx), null, false);
    }
    
    @Override
    public PlaceNavigation<?> getSeriesEventRacesNavigation(UUID eventId) {
        return clientFactory.getNavigator().getEventNavigation(new RegattaRacesPlace(eventId.toString(), null), null, false);
    }

    public String getRaceViewerURL(String regattaName, String trackedRaceName) {
        return null; // TODO No mobile "RaceViewer implemented yet
    }
    
    public String getRegattaId() {
        String regattaId = place.getRegattaId();
        if(regattaId  != null) {
            return regattaId;
        }
        if(!eventDTO.getRegattas().isEmpty() && (eventDTO.getType() == EventType.SINGLE_REGATTA || eventDTO.getType() == EventType.SERIES_EVENT)) {
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
}
