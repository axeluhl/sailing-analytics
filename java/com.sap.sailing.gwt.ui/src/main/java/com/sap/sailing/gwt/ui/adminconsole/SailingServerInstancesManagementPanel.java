package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.SailingServerDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class SailingServerInstancesManagementPanel extends FlowPanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final Button addButton;
    private final Button removeButton;
    private final Button refreshButton;

    private CellTable<SailingServerDTO> serverTable;
    private MultiSelectionModel<SailingServerDTO> serverSelectionModel;
    private ListDataProvider<SailingServerDTO> serverDataProvider;

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public SailingServerInstancesManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        TextColumn<SailingServerDTO> serverNameColumn = new TextColumn<SailingServerDTO>() {
            @Override
            public String getValue(SailingServerDTO server) {
                return server.getName() != null ? server.getName() : "";
            }
        };

        TextColumn<SailingServerDTO> serverUrlColumn = new TextColumn<SailingServerDTO>() {
            @Override
            public String getValue(SailingServerDTO server) {
                return server.getUrl() != null ? server.getUrl() : "";
            }
        };

        serverTable = new CellTable<SailingServerDTO>(10000, tableRes);
        serverTable.addColumn(serverNameColumn, stringMessages.name());
        serverTable.addColumn(serverUrlColumn, stringMessages.url());

        serverTable.setEmptyTableWidget(new Label("No sailing server instances yet."));
        
        serverSelectionModel = new MultiSelectionModel<SailingServerDTO>();
        serverTable.setSelectionModel(serverSelectionModel);

        serverDataProvider = new ListDataProvider<SailingServerDTO>();
        serverDataProvider.addDataDisplay(serverTable);

        addButton = new Button(stringMessages.add());
        removeButton = new Button(stringMessages.remove());
        refreshButton = new Button(stringMessages.refresh());
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addSailingServer();
            }
        });
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeSelectedSailingServers();
            }
        });
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshSailingServerList();
            }
        });
        VerticalPanel vp = new VerticalPanel();
        HorizontalPanel providerSelectionPanel = new HorizontalPanel();
        providerSelectionPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        vp.add(providerSelectionPanel);
        
        providerSelectionPanel.add(new Label("Registered SailingServer Instances"));
                
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(refreshButton);

        vp.add(serverTable);
        vp.add(buttonPanel);

        add(vp);
        refreshSailingServerList();
    }
    
    private void refreshSailingServerList() {
        sailingService.getSailingServers(new AsyncCallback<List<SailingServerDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorRefreshingSailingServers(caught.getMessage()));
            }

            @Override
            public void onSuccess(List<SailingServerDTO> result) {
            	serverDataProvider.getList().clear();
            	serverDataProvider.getList().addAll(result);
            }
        });
    }

    private void removeSelectedSailingServers() {
        Set<String> toRemove = new HashSet<String>();
        for (SailingServerDTO selectedServer: serverSelectionModel.getSelectedSet()) {
        	toRemove.add(selectedServer.getName());
        }
        
        sailingService.removeSailingServers(toRemove, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorRemovingSailingServers(caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
                refreshSailingServerList();
                Window.setStatus(stringMessages.successfullyUpdatedSailingServers());
            }
        });
    }

    private void addSailingServer() {
    	ArrayList<SailingServerDTO> existingServers = new ArrayList<SailingServerDTO>(serverDataProvider.getList());
        SailingServerCreateOrEditDialog dialog = new SailingServerCreateOrEditDialog(existingServers, stringMessages, new DialogCallback<SailingServerDTO>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(final SailingServerDTO server) {
                sailingService.addSailingServer(server, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorAddingSailingServer(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Void result) {
                    	serverDataProvider.getList().add(server);
                        Window.setStatus(stringMessages.successfullyUpdatedSailingServers());
                    }
                });
            }
        });
        dialog.show();	
    }
}
