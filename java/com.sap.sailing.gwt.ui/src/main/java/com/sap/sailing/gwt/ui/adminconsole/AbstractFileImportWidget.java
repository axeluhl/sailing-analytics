package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.adminconsole.resulthandling.SensorDataImportResponse;
import com.sap.sailing.gwt.ui.adminconsole.resulthandling.SensorDataImportResultsDialog;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public abstract class AbstractFileImportWidget extends Composite {

    private static AbstractFileImportWidgetUiBinder uiBinder = GWT.create(AbstractFileImportWidgetUiBinder.class);

    interface AbstractFileImportWidgetUiBinder extends UiBinder<Widget, AbstractFileImportWidget> {
    }

    @UiField
    AdminConsoleResources res;
    @UiField
    FileUpload fileUploadUi;
    @UiField
    ListBox preferredImporterUi;
    @UiField
    Button importButtonUi;
    @UiField
    Image loadingImageUi;
    @UiField
    CheckBox downsampleUi;
    @UiField
    FormPanel formPanelUi;

    private final TrackFileImportDeviceIdentifierTableWrapper table;
    protected final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    protected AbstractFileImportWidget(String formActionUrl, StringMessages stringMessages,
            TrackFileImportDeviceIdentifierTableWrapper table, SailingServiceAsync sailingService,
            final ErrorReporter errorReporter) {
        this.table = table;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        initWidget(uiBinder.createAndBindUi(this));
        importButtonUi.setText(stringMessages.importFixes());
        formPanelUi.setAction(formActionUrl);
        getImporterTypes(new AsyncCallback<Collection<String>>() {
            @Override
            public void onSuccess(Collection<String> result) {
                for (String importType : result)
                    preferredImporterUi.addItem(importType);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load importer types: " + caught.getMessage());
            }
        });
    }

    @UiHandler("importButtonUi")
    void onImportButtonClick(ClickEvent event) {
        showLoadingImage(true);
        formPanelUi.submit();
    }

    protected void setDownsampleOptionVisible(boolean visible) {
        downsampleUi.setVisible(visible);
    }
    
    @UiHandler("formPanelUi")
    void onFileImportComplete(SubmitCompleteEvent event) {
        SensorDataImportResponse importResponse = SensorDataImportResponse.parse(event.getResults());
        if (importResponse == null) {
            Notification.notify(StringMessages.INSTANCE.unexpectedErrorDuringFileImport(), NotificationType.ERROR);
        } else {
            SensorDataImportResultsDialog.showResults(importResponse);
            if (importResponse.didSucceedImportingAnyFile()) {
                sailingService.getTrackFileImportDeviceIds(importResponse.getUploads(),
                        new AsyncCallback<List<TrackFileImportDeviceIdentifierDTO>>() {
                            @Override
                            public void onSuccess(List<TrackFileImportDeviceIdentifierDTO> result) {
                                table.getDataProvider().getList().addAll(result);
                                showLoadingImage(false);
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter
                                        .reportError("Could not load TrackFileImportDeviceIds: " + caught.getMessage());
                                showLoadingImage(false);
                            }
                        });
            }
        }
        showLoadingImage(false);
    }

    protected void setMultipleFileUploadEnabled(boolean enableMultipleFileUpload) {
        fileUploadUi.getElement().setPropertyBoolean("multiple", enableMultipleFileUpload);
    }

    protected void showLoadingImage(boolean show) {
        loadingImageUi.setResource(show ? res.loaderGif() : res.transparentGif());
    }

    protected abstract void getImporterTypes(AsyncCallback<Collection<String>> callback);
}
