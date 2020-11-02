package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;
import com.sap.sse.gwt.adminconsole.HandleTabSelectable;

public class EventManagementPanelSupplier extends AdminConsolePanelSupplier<EventManagementPanel> {

    private final StringMessages stringMessages;
    private final Presenter presenter;
    private final HandleTabSelectable handleTabSelectable;

    public EventManagementPanelSupplier(final StringMessages stringMessages, final Presenter presenter,
            final HandleTabSelectable handleTabSelectable) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
        this.handleTabSelectable = handleTabSelectable;
    }

    @Override
    public EventManagementPanel init() {
        logger.info("Create EventManagementPanel");
        final EventManagementPanel eventManagementPanel = new EventManagementPanel(presenter, stringMessages,
                handleTabSelectable);
        eventManagementPanel.ensureDebugId("EventManagement");
        presenter.getLeaderboardGroupsDisplayer().add(eventManagementPanel);
        presenter.setEventRefresher(eventManagementPanel);
        eventManagementPanel.fillEvents();
        presenter.fillLeaderboardGroups();
        return eventManagementPanel;
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