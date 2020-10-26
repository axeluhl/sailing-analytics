package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.LeaderboardsDisplayer;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;

public class LeaderboardConfigPanelSupplier extends AdminConsolePanelSupplier<LeaderboardConfigPanel> {

    private Logger logger = Logger.getLogger(this.getClass().toString());

    private final StringMessages stringMessages;
    private final Presenter presenter;
    private final Set<RegattasDisplayer> regattasDisplayers;
    private Set<LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity>> leaderboardsDisplayers;
    private final boolean showRaceDetails;

    public LeaderboardConfigPanelSupplier(StringMessages stringMessages, Presenter presenter,
            Set<RegattasDisplayer> regattasDisplayers,
            Set<LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity>> leaderboardsDisplayers,
            boolean showRaceDetails) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
        this.regattasDisplayers = regattasDisplayers;
        this.leaderboardsDisplayers = leaderboardsDisplayers;
        this.showRaceDetails = showRaceDetails;
    }

    /*
     * final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(sailingService, userService,
     * presenter, errorReporter, getStringMessages(), true, presenter);
     * leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
     */
    public LeaderboardConfigPanel init() {
        logger.info("Create RegattaManagementPanel");
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(presenter, stringMessages,
                showRaceDetails);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        regattasDisplayers.add(leaderboardConfigPanel);
        leaderboardsDisplayers.add(leaderboardConfigPanel);
        presenter.fillLeaderboards();
        return leaderboardConfigPanel;
    }

    @Override
    public void getAsync(RunAsyncCallback callback) {
        GWT.runAsync(new RunAsyncCallback() {
            
            @Override
            public void onSuccess() {
                widget = init();
                callback.onSuccess();
            }
            
            @Override
            public void onFailure(Throwable reason) {
                callback.onFailure(reason);
            }
        });
    }

}
