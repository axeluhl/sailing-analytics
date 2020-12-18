package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;

public class EventManagementPanelSupplier extends AdminConsolePanelSupplier<EventManagementPanel> {

    private final StringMessages stringMessages;
    private final Presenter presenter;
    private final PlaceController placeController;

    public EventManagementPanelSupplier(final StringMessages stringMessages, final Presenter presenter,
            final PlaceController placeController) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
        this.placeController = placeController;
    }

    @Override
    public EventManagementPanel init() {
        logger.info("Create EventManagementPanel");
        final EventManagementPanel eventManagementPanel = new EventManagementPanel(presenter, stringMessages,
                placeController);
        eventManagementPanel.ensureDebugId("EventManagement");
        presenter.getLeaderboardGroupsRefresher().addDisplayerAndCallFillOnInit(eventManagementPanel);
        presenter.getEventsRefresher().addDisplayerAndCallFillOnInit(eventManagementPanel);
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