package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;
import com.sap.sse.gwt.adminconsole.ReplicationPanel;

public class ReplicationPanelSupplier extends AdminConsolePanelSupplier<ReplicationPanel> {

    private final StringMessages stringMessages;
    private final Presenter presenter;

    public ReplicationPanelSupplier(final StringMessages stringMessages, final Presenter presenter) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
    }

    public ReplicationPanel init() {
        final ReplicationPanel replicationPanel = new ReplicationPanel(presenter.getSailingService(),
                presenter.getUserService(), presenter.getErrorReporter(), stringMessages);
        replicationPanel.ensureDebugId("replicationPanel");
        return replicationPanel;
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