package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.EventView.PlaceCallback;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventReferenceDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.HasRegattaMetadata;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaReferenceDTO;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public abstract class AbstractEventActivity<PLACE extends AbstractEventPlace> extends AbstractActivity implements EventView.Presenter {

    protected final PLACE currentPlace;

    protected final EventContext ctx;

    protected final EventClientFactory clientFactory;

    private final Timer timerForClientServerOffset;

    private final HomePlacesNavigator homePlacesNavigator;
    
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
    public void forPlaceSelection(PlaceCallback callback) {
        EventViewDTO event = ctx.getEventDTO();
        if (event.getType() == EventType.SERIES_EVENT) {
            for(EventReferenceDTO seriesEvent : event.getEventsOfSeries()) {
                RegattaOverviewPlace place = new RegattaOverviewPlace(new EventContext().withId(seriesEvent.getId().toString()));
                callback.forPlace(place, seriesEvent.getDisplayName());
            }
        } else {
            for(RegattaReferenceDTO regatta : event.getRegattas()) {
                RegattaOverviewPlace place = new RegattaOverviewPlace(
                        new EventContext(ctx.getEventDTO()).withRegattaId(regatta.getId()));
                callback.forPlace(place, regatta.getDisplayName());
            }
        }
    }
    
    @Override
    public SafeUri getUrl(Place place) {
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
    public RegattaOverviewPlace getPlaceForRegatta(String regattaId) {
        return new RegattaOverviewPlace(contextForRegatta(regattaId));
    }
    
    @Override
    public RegattaRacesPlace getPlaceForRegattaRaces(String regattaId) {
        return new RegattaRacesPlace(contextForRegatta(regattaId));
    }

    private EventContext contextForRegatta(String regattaId) {
        return new EventContext(ctx.getEventDTO()).withRegattaId(regattaId);
    }


    @Override
    public HomePlacesNavigator getHomePlaceNavigator() {
        return homePlacesNavigator;
    }

    @Override
    public void goToOverview() {
        clientFactory.getPlaceController().goTo(new RegattaOverviewPlace(this.ctx));

    }

    @Override
    public void goToRegattas() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void goToSchedule() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void goToMedia() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void goToRegattaRaces(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard,
            RaceGroupDTO raceGroup) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getRaceViewerURL(StrippedLeaderboardDTO leaderboard, RaceDTO race) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void openOverallLeaderboardViewer(LeaderboardGroupDTO leaderboardGroup) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void openLeaderboardViewer(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard) {
        // TODO Auto-generated method stub
        
    }
}
