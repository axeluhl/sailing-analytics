package com.sap.sailing.gwt.ui.adminconsole;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;

public class IgtimiAccountsPanelSupplier extends AdminConsolePanelSupplier<IgtimiAccountsPanel> {

    private Logger logger = Logger.getLogger(this.getClass().toString());

    private final StringMessages stringMessages;
    private final Presenter presenter;

    public IgtimiAccountsPanelSupplier(StringMessages stringMessages, Presenter presenter) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
    }

    public IgtimiAccountsPanel init() {
        logger.info("Create IgtimiAccountsPanel");
        IgtimiAccountsPanel igtimiAccountsPanel = new IgtimiAccountsPanel(presenter, stringMessages);
        igtimiAccountsPanel.ensureDebugId("IgtimiAccounts");
        igtimiAccountsPanel.refresh();
        return igtimiAccountsPanel;
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