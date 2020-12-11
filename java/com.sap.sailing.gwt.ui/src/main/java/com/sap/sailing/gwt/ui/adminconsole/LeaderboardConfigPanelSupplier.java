package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;

public class LeaderboardConfigPanelSupplier extends AdminConsolePanelSupplier<LeaderboardConfigPanel> {

    private final StringMessages stringMessages;
    private final Presenter presenter;
    private final boolean showRaceDetails;

    public LeaderboardConfigPanelSupplier(final StringMessages stringMessages, final Presenter presenter,
            final boolean showRaceDetails) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
        this.showRaceDetails = showRaceDetails;
    }

    @Override
    public LeaderboardConfigPanel init() {
        logger.info("Create LeaderboardConfigPanel");
        final LeaderboardConfigPanel leaderboardConfigPanel = new LeaderboardConfigPanel(presenter, stringMessages,
                showRaceDetails);
        leaderboardConfigPanel.ensureDebugId("LeaderboardConfiguration");
        presenter.getRegattasDisplayers().add(leaderboardConfigPanel);
        presenter.getLeaderboardsDisplayers().add(leaderboardConfigPanel);
        presenter.loadLeaderboards();
        presenter.loadRegattas();
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
