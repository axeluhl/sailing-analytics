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
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.security.ui.client.UserService;

public interface AdminConsoleView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    HeaderPanel createUI(final ServerInfoDTO serverInfo);
    
    void selectTabByPlace(AbstractAdminConsolePlace place);
    
    void setRedirectToPlace(AbstractAdminConsolePlace redirectoPlace);
    
    public interface Presenter extends LeaderboardGroupsRefresher, RegattaRefresher,
            LeaderboardsRefresher<StrippedLeaderboardDTOWithSecurity>, EventRefresher, MediaTracksRefresher {

        ErrorReporter getErrorReporter();

        Iterable<RegattasDisplayer> getRegattasDisplayers();

        void addRegattasDisplayer(RegattasDisplayer regattasDisplayer);

        Iterable<LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity>> getLeaderboardsDisplayers();

        void addLeaderboardsDisplayer(LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity> leaderboardsDisplayer);
        
        Iterable<LeaderboardGroupsDisplayer> getLeaderboardGroupsDisplayers();
        
        void addLeaderboardGroupsDisplayer(LeaderboardGroupsDisplayer leaderboardGroupsDisplayer);

        SailingServiceWriteAsync getSailingService();

        UserService getUserService();

        MediaServiceWriteAsync getMediaServiceWrite();

        PlaceController getPlaceController();
        
        Iterable<EventsDisplayer> getEventsDisplayers();
        
        void addEventsDisplayer(EventsDisplayer eventsDisplayer);
        
        void loadMediaTracks();
        
        void setMediaTracksRefresher(MediaTracksRefresher mediaTracksRefresher);
    }

}
