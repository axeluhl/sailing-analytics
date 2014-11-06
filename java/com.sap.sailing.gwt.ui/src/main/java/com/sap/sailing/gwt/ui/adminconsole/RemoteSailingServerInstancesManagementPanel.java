package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
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
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sailing.gwt.ui.shared.RemoteSailingServerReferenceDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public class RemoteSailingServerInstancesManagementPanel extends FlowPanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    private final MultiSelectionModel<RemoteSailingServerReferenceDTO> serverSelectionModel;
    private LabeledAbstractFilterablePanel<RemoteSailingServerReferenceDTO> filteredServerTable;

    public RemoteSailingServerInstancesManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        TextColumn<RemoteSailingServerReferenceDTO> serverNameColumn = new TextColumn<RemoteSailingServerReferenceDTO>() {
            @Override
            public String getValue(RemoteSailingServerReferenceDTO server) {
                return server.getName() != null ? server.getName() : "";
            }
        };
        TextColumn<RemoteSailingServerReferenceDTO> serverUrlColumn = new TextColumn<RemoteSailingServerReferenceDTO>() {
            @Override
            public String getValue(RemoteSailingServerReferenceDTO server) {
                return server.getUrl() != null ? server.getUrl() : "";
            }
        };
        final SafeHtmlCell eventsCell = new SafeHtmlCell();
        Column<RemoteSailingServerReferenceDTO, SafeHtml> eventsOrErrorColumn = new Column<RemoteSailingServerReferenceDTO, SafeHtml>(eventsCell) {
            @Override
            public SafeHtml getValue(RemoteSailingServerReferenceDTO server) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                final Iterable<EventBaseDTO> events = server.getEvents();
                if (events != null) {
                    for (EventBaseDTO event : events) {
                        builder.appendEscaped(event.getName());
                        builder.appendHtmlConstant("<br>");
                    }
                } else {
                    builder.appendEscaped(RemoteSailingServerInstancesManagementPanel.this.stringMessages
                            .errorAddingSailingServer(server.getLastErrorMessage()));
                }
                return builder.toSafeHtml();
            }
        };
        CellTable<RemoteSailingServerReferenceDTO> serverTable = new CellTable<RemoteSailingServerReferenceDTO>(10000, tableRes);
        serverTable.addColumn(serverNameColumn, stringMessages.name());
        serverTable.addColumn(serverUrlColumn, stringMessages.url());
        serverTable.addColumn(eventsOrErrorColumn, stringMessages.events());

        serverTable.setEmptyTableWidget(new Label(stringMessages.noSailingServerInstancesYet()));
        
        serverSelectionModel = new MultiSelectionModel<RemoteSailingServerReferenceDTO>();
        serverTable.setSelectionModel(serverSelectionModel);

        ListDataProvider<RemoteSailingServerReferenceDTO> serverDataProvider = new ListDataProvider<RemoteSailingServerReferenceDTO>();
        serverDataProvider.addDataDisplay(serverTable);

        Button addButton = new Button(stringMessages.add());
        Button removeButton = new Button(stringMessages.remove());
        Button refreshButton = new Button(stringMessages.refresh());
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addRemoteSailingServerReference();
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
        
        filteredServerTable = new LabeledAbstractFilterablePanel<RemoteSailingServerReferenceDTO>(
                new Label(stringMessages.registeredSailingServerInstances()), Collections.<RemoteSailingServerReferenceDTO>emptyList(), serverTable, serverDataProvider) {
            @Override
            public List<String> getSearchableStrings(RemoteSailingServerReferenceDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.getName());
                strings.add(t.getUrl());
                if (t.getEvents() != null) {
                    for (EventBaseDTO e : t.getEvents()) {
                        strings.add(e.getName());
                    }
                }
                return strings;
            }
        };
        providerSelectionPanel.add(filteredServerTable);
        vp.add(serverTable);
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(refreshButton);
        vp.add(buttonPanel);

        add(vp);
        refreshSailingServerList();
    }
    
    private void refreshSailingServerList() {
        sailingService.getRemoteSailingServerReferences(new AsyncCallback<List<RemoteSailingServerReferenceDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorRefreshingSailingServers(caught.getMessage()));
            }

            @Override
            public void onSuccess(List<RemoteSailingServerReferenceDTO> result) {
                filteredServerTable.updateAll(result);
            }
        });
    }

    private void removeSelectedSailingServers() {
        Set<String> toRemove = new HashSet<String>();
        for (RemoteSailingServerReferenceDTO selectedServer: serverSelectionModel.getSelectedSet()) {
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

    private void addRemoteSailingServerReference() {
        SailingServerCreateOrEditDialog dialog = new SailingServerCreateOrEditDialog(filteredServerTable.getAll(), stringMessages, new DialogCallback<RemoteSailingServerReferenceDTO>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(final RemoteSailingServerReferenceDTO server) {
                sailingService.addRemoteSailingServerReference(server, new AsyncCallback<RemoteSailingServerReferenceDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorAddingSailingServer(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(RemoteSailingServerReferenceDTO result) {
                    	filteredServerTable.add(result);
                        Window.setStatus(stringMessages.successfullyUpdatedSailingServers());
                    }
                });
            }
        });
        dialog.show();	
    }
}
