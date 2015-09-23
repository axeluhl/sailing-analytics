package com.sap.sailing.gwt.home.desktop.places.event;

import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.ui.client.HomeServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.eventview.HasRegattaMetadata;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sse.gwt.client.player.Timer;

public interface EventView<PLACE extends AbstractEventPlace, PRES extends EventView.Presenter> extends IsWidget {

    public interface Presenter {
        EventContext getCtx();
        
        void handleTabPlaceSelection(TabView<?, ? extends Presenter> selectedActivity);

        SafeUri getUrl(AbstractEventPlace place);
        void navigateTo(Place place);
        boolean needsSelectionInHeader();
        void forPlaceSelection(PlaceCallback callback);
        Timer getTimerForClientServerOffset();
        SailingServiceAsync getSailingService();
        HomeServiceAsync getHomeService();
        
        AbstractEventRegattaPlace getPlaceForRegatta(String regattaId);
        AbstractEventRegattaPlace getPlaceForRegattaRaces(String regattaId);
        
        PlaceNavigation<StartPlace> getHomeNavigation();
        PlaceNavigation<EventsPlace> getEventsNavigation();
        PlaceNavigation<EventDefaultPlace> getCurrentEventNavigation();
        PlaceNavigation<SeriesDefaultPlace> getCurrentEventSeriesNavigation();

        boolean showRegattaMetadata();
        boolean isEventOrRegattaLive();
        HasRegattaMetadata getRegattaMetadata();
        
        String getRegattaOverviewLink();

        String getRaceViewerURL(StrippedLeaderboardDTO leaderboard, RaceDTO race);
        
        String getRaceViewerURL(String leaderboardName, RegattaAndRaceIdentifier raceIdentifier);

        PlaceNavigation<RegattaRacesPlace> getRegattaRacesNavigation(String regattaName);
        PlaceNavigation<AbstractEventRegattaPlace> getRegattaNavigation(String regattaName);
        PlaceNavigation<RegattaLeaderboardPlace> getRegattaLeaderboardNavigation(String regattaName);
        
        void ensureMedia(AsyncCallback<MediaDTO> asyncCallback);

        boolean hasMedia();
        
        DispatchSystem getDispatch();
    }
    
    public interface PlaceCallback {
        void forPlace(AbstractEventPlace place, String title, boolean active);
    }

    /**
     * This is the presenter the view can talk to.
     * 
     * @param currentPresenter
     */
    void registerPresenter(PRES currentPresenter);

    /**
     * Tell the view to process tabbar place navigation
     * 
     * @param place
     */
    void navigateTabsTo(PLACE place);
    
    void showErrorInCurrentTab(IsWidget errorView);

}
