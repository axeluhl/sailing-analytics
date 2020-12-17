package com.sap.sailing.gwt.ui.adminconsole.places;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.EventsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardsRefresher;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.MediaTracksRefresher;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.gwt.adminconsole.AbstractAdminConsolePlace;
import com.sap.sse.gwt.adminconsole.AdminConsolePresenter;
import com.sap.sse.gwt.client.ServerInfoDTO;

public interface AdminConsoleView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    HeaderPanel createUI(final ServerInfoDTO serverInfo);
    
    void selectTabByPlace(AbstractAdminConsolePlace place);
    
    void setRedirectToPlace(AbstractAdminConsolePlace redirectoPlace);
    
    public interface Presenter extends AdminConsolePresenter, LeaderboardGroupsRefresher, RegattaRefresher,
            LeaderboardsRefresher<StrippedLeaderboardDTOWithSecurity>, EventRefresher, MediaTracksRefresher {
        Iterable<RegattasDisplayer> getRegattasDisplayers();

        void addRegattasDisplayer(RegattasDisplayer regattasDisplayer);

        Iterable<LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity>> getLeaderboardsDisplayers();

        void addLeaderboardsDisplayer(LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity> leaderboardsDisplayer);
        
        Iterable<LeaderboardGroupsDisplayer> getLeaderboardGroupsDisplayers();
        
        void addLeaderboardGroupsDisplayer(LeaderboardGroupsDisplayer leaderboardGroupsDisplayer);

        SailingServiceWriteAsync getSailingService();

        MediaServiceWriteAsync getMediaServiceWrite();

        PlaceController getPlaceController();
        
        Iterable<EventsDisplayer> getEventsDisplayers();
        
        void addEventsDisplayer(EventsDisplayer eventsDisplayer);
        
        void setMediaTracksRefresher(MediaTracksRefresher mediaTracksRefresher);
    }
}
