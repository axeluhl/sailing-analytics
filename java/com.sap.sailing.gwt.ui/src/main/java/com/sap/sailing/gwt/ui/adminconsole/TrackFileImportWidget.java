package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.adminconsole.TrackFileImportDeviceIdentifierTableWrapper.TrackFileImportDeviceIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TrackFileImportWidget implements IsWidget {
    private final Panel mainPanel = new VerticalPanel();
    
    public TrackFileImportWidget(final TrackFileImportDeviceIdentifierTableWrapper table, StringMessages stringMessages,
            SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        this(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                JSONArray json = JSONParser.parseLenient(event.getResults()).isArray();
                table.getDataProvider().getList().clear();
                for (int i=0; i<json.size(); i++) {
                    JSONObject obj = json.get(i).isObject();
                    JSONObject deviceIdJson = obj.get("id").isObject();
                    String uuid = deviceIdJson.get("UUID").isString().stringValue();
                    String fileName = deviceIdJson.get("FILE_NAME").isString().stringValue();
                    String trackName = deviceIdJson.get("TRACK_NAME").isString().stringValue();
                    long fromMillis = (long) obj.get("FROM_MILLIS").isNumber().doubleValue();
                    long toMillis = (long) obj.get("TO_MILLIS").isNumber().doubleValue();
                    TrackFileImportDeviceIdentifier deviceId = new TrackFileImportDeviceIdentifier(uuid, fileName,
                            trackName, new Date(fromMillis), new Date(toMillis), /* number of fixes */ 0);
                    table.getDataProvider().getList().add(deviceId);
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
        btnUpload.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                formPanel.submit();
            }
        });
    }
    @Override
    public Widget asWidget() {
        return mainPanel;
    }

}
