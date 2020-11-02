package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;
import com.sap.sse.gwt.client.controls.filestorage.FileStoragePanel;

public class FileStoragePanelSupplier extends AdminConsolePanelSupplier<FileStoragePanel> {

    private final Presenter presenter;

    public FileStoragePanelSupplier(final Presenter presenter) {
        super();
        this.presenter = presenter;
    }

    @Override
    public FileStoragePanel init() {
        logger.info("Create FileStoragePanel");
        final FileStoragePanel fileStoragePanel = new FileStoragePanel(presenter.getSailingService(),
                presenter.getErrorReporter());
        fileStoragePanel.ensureDebugId("fileStoragePanel");
        return fileStoragePanel;
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