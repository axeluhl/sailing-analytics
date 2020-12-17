package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;

public class LeaderboardGroupConfigPanelSupplier extends AdminConsolePanelSupplier<LeaderboardGroupConfigPanel> {

    private final StringMessages stringMessages;
    private final Presenter presenter;

    public LeaderboardGroupConfigPanelSupplier(final StringMessages stringMessages, final Presenter presenter) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
    }

    @Override
    public LeaderboardGroupConfigPanel init() {
        logger.info("Create LeaderboardGroupConfigPanel");
        final LeaderboardGroupConfigPanel leaderboardGroupConfigPanel = new LeaderboardGroupConfigPanel(presenter,
                stringMessages);
        leaderboardGroupConfigPanel.ensureDebugId("LeaderboardGroupConfiguration");
        presenter.addRegattasDisplayer(leaderboardGroupConfigPanel);
        presenter.addLeaderboardGroupsDisplayer(leaderboardGroupConfigPanel);
        presenter.addLeaderboardsDisplayer(leaderboardGroupConfigPanel);
        presenter.loadRegattas();
        presenter.loadLeaderboards();
        presenter.loadLeaderboardGroups();
        return leaderboardGroupConfigPanel;
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
