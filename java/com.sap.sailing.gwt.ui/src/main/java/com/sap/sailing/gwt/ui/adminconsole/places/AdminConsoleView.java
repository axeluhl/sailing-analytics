package com.sap.sailing.gwt.ui.adminconsole.places;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.Refresher;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.security.ui.client.UserService;

public interface AdminConsoleView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    HeaderPanel createUI(final ServerInfoDTO serverInfo);
    
    void selectTabByPlace(AbstractAdminConsolePlace place);
    
    void setRedirectToPlace(AbstractAdminConsolePlace redirectoPlace);
    
    public interface Presenter {

        ErrorReporter getErrorReporter();

        SailingServiceWriteAsync getSailingService();

        UserService getUserService();

        MediaServiceWriteAsync getMediaServiceWrite();

        PlaceController getPlaceController();

        // Refresher
        Refresher<StrippedLeaderboardDTOWithSecurity> getLeaderboardsRefresher();
        Refresher<LeaderboardGroupDTO> getLeaderboardGroupsRefresher();
        Refresher<RegattaDTO> getRegattasRefresher();
        Refresher<EventDTO> getEventsRefresher();
        Refresher<MediaTrackWithSecurityDTO> getMediaTracksRefresher();
    }

}
