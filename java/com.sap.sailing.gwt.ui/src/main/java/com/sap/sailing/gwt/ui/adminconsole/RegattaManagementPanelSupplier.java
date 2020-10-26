package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;

public class RegattaManagementPanelSupplier extends AdminConsolePanelSupplier<RegattaManagementPanel> {
    
    private Logger logger = Logger.getLogger(AdminConsolePanelSupplier.class.toString());

    private final StringMessages stringMessages;
    private final Presenter presenter;
    private final Set<RegattasDisplayer> regattasDisplayers;

    public RegattaManagementPanelSupplier(StringMessages stringMessages, Presenter presenter, Set<RegattasDisplayer> regattasDisplayers) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
        this.regattasDisplayers = regattasDisplayers;
    }

    public RegattaManagementPanel init() {
        logger.info("Create RegattaManagementPanel");
        RegattaManagementPanel regattaManagementPanel = new RegattaManagementPanel(stringMessages, presenter);
        regattaManagementPanel.ensureDebugId("RegattaStructureManagement");
        regattasDisplayers.add(regattaManagementPanel);
        presenter.fillRegattas();
        return regattaManagementPanel;
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
