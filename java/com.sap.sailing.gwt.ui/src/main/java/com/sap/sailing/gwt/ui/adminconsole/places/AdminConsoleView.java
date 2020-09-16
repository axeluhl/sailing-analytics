package com.sap.sailing.gwt.ui.adminconsole.places;

import java.util.HashSet;

import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardsRefresher;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.security.ui.client.UserService;

public interface AdminConsoleView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    HeaderPanel createUI(final ServerInfoDTO serverInfo);
    
    void selectTabByNames(String verticalTabName, String horizontalTabName);
    
    public interface Presenter extends LeaderboardGroupsRefresher, RegattaRefresher, LeaderboardsRefresher<StrippedLeaderboardDTOWithSecurity> {
        
        public ErrorReporter getErrorReporter();
        
        public HashSet<RegattasDisplayer> getRegattasDisplayers();
        
        public HashSet<LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity>> getLeaderboardsDisplayer();
        
        public HashSet<LeaderboardGroupsDisplayer> getLeaderboardGroupsDisplayer();
        
        SailingServiceWriteAsync getSailingService();
        
        UserService getUserService();
        
    }

}
