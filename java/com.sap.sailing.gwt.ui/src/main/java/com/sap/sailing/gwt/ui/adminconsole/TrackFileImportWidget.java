package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class TrackFileImportWidget implements IsWidget {
    private final Panel mainPanel = new VerticalPanel();
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    private static Image loaderImage = new Image(resources.loaderGif());
    
    public TrackFileImportWidget(final TrackFileImportDeviceIdentifierTableWrapper table, final StringMessages stringMessages,
            final SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        this(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                table.getDataProvider().getList().clear();
                String[] uuids = event.getResults().split("\\n");
                if (uuids.length != 1 || (uuids.length > 0 && !uuids[0].isEmpty())) {
                    sailingService.getTrackFileImportDeviceIds(Arrays.asList(uuids),
                            new AsyncCallback<List<TrackFileImportDeviceIdentifierDTO>>() {
                        @Override
                        public void onSuccess(List<TrackFileImportDeviceIdentifierDTO> result) {
                            table.getDataProvider().getList().addAll(result);
                            loaderImage.setResource(resources.transparentGif());
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Could not load TrackFileImportDeviceIds: " + caught.getMessage());
                            loaderImage.setResource(resources.transparentGif());
                        }
                    });
                } else {
                    loaderImage.setResource(resources.transparentGif());
                    Window.alert(stringMessages.noTracksFound());
                }
            }
        }, stringMessages, sailingService, errorReporter);
    }
    
    public TrackFileImportWidget(final SubmitCompleteHandler submitCompleteHandler, StringMessages stringMessages,
            SailingServiceAsync sailingService, final ErrorReporter errorReporter) {        
        final FormPanel formPanel = new FormPanel();
        mainPanel.add(formPanel);
        
        HorizontalPanel inFormPanel = new HorizontalPanel();
        formPanel.add(inFormPanel);
        formPanel.setAction("/sailingserver/trackfiles/import");
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setMethod(FormPanel.METHOD_POST);
        formPanel.addSubmitCompleteHandler(submitCompleteHandler);
        FileUpload fileUpload = new FileUpload();
        fileUpload.setName("file");
        inFormPanel.add(fileUpload);
        loaderImage.setResource(resources.transparentGif());
        loaderImage.setPixelSize(16, 16);
        
        final ListBox preferredImporter = new ListBox();
        preferredImporter.setName("preferredImporter");
        inFormPanel.add(preferredImporter);
        preferredImporter.addItem("");
        sailingService.getGPSFixImporterTypes(new AsyncCallback<Collection<String>>() {
            @Override
            public void onSuccess(Collection<String> result) {
                for (String type : result) {
                    preferredImporter.addItem(type);
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load importer types: " + caught.getMessage());
            }
        });

        Button btnUpload = new Button(stringMessages.importFixes());
        inFormPanel.add(btnUpload);
        inFormPanel.add(loaderImage);
        btnUpload.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                formPanel.submit();
                loaderImage.setResource(resources.loaderGif());
            }
        });
    }
    @Override
    public Widget asWidget() {
        return mainPanel;
    }

}
