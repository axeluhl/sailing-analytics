package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;

public class StructureImportManagementPanelSupplier extends AdminConsolePanelSupplier<StructureImportManagementPanel> {

    private final StringMessages stringMessages;
    private final Presenter presenter;

    public StructureImportManagementPanelSupplier(final StringMessages stringMessages, final Presenter presenter) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
    }

    @Override
    public StructureImportManagementPanel init() {
        logger.info("Create StructureImportManagementPanel");
        StructureImportManagementPanel structureImportUrlsManagementPanel = new StructureImportManagementPanel(
                presenter, stringMessages);
        structureImportUrlsManagementPanel.ensureDebugId("structureImportUrlsManagementPanel");
        presenter.getEventsRefresher().addDisplayerAndCallFillOnInit(structureImportUrlsManagementPanel.getEventsDisplayer());
        return structureImportUrlsManagementPanel;
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