package com.sap.sailing.gwt.home.client.place.event;

import java.util.List;

import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
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
        
        AbstractEventRegattaPlace getPlaceForRegatta(String regattaId);
        AbstractEventRegattaPlace getPlaceForRegattaRaces(String regattaId);
        
        PlaceNavigation<StartPlace> getHomeNavigation();
        PlaceNavigation<EventsPlace> getEventsNavigation();
        PlaceNavigation<EventDefaultPlace> getCurrentEventNavigation();
        PlaceNavigation<SeriesDefaultPlace> getCurrentEventSeriesNavigation();

        boolean showRegattaMetadata();
        HasRegattaMetadata getRegattaMetadata();

        String getRaceViewerURL(StrippedLeaderboardDTO leaderboard, RaceDTO race);

        PlaceNavigation<RegattaRacesPlace> getRegattaRacesNavigation(String regattaName);
        PlaceNavigation<RegattaRacesPlace> getRegattaNavigation(String regattaName);
        
        void ensureRegattaStructure(AsyncCallback<List<RaceGroupDTO>> callback);
        void ensureLeaderboardGroups(AsyncCallback<List<LeaderboardGroupDTO>> callback);
        void ensureMedia(AsyncCallback<MediaDTO> asyncCallback);

        boolean hasMedia();
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
