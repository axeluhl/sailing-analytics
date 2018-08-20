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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sailing.gwt.ui.shared.RemoteSailingServerReferenceDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public class RemoteServerInstancesManagementPanel extends SimplePanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    private final ListDataProvider<RemoteSailingServerReferenceDTO> serverDataProvider;
    private RefreshableMultiSelectionModel<RemoteSailingServerReferenceDTO> refreshableServerSelectionModel;
    private LabeledAbstractFilterablePanel<RemoteSailingServerReferenceDTO> filteredServerTablePanel;

    private final CaptionPanel remoteServersPanel;

    public RemoteServerInstancesManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");

        remoteServersPanel = new CaptionPanel(stringMessages.registeredSailingServerInstances());
        mainPanel.add(remoteServersPanel);
        VerticalPanel remoteServersContentPanel = new VerticalPanel();
        remoteServersPanel.setContentWidget(remoteServersContentPanel);
        
        serverDataProvider = new ListDataProvider<RemoteSailingServerReferenceDTO>();
        filteredServerTablePanel = new LabeledAbstractFilterablePanel<RemoteSailingServerReferenceDTO>(
                new Label(stringMessages.filterBy() + ":"), Collections.<RemoteSailingServerReferenceDTO> emptyList(),
                new CellTable<RemoteSailingServerReferenceDTO>(), serverDataProvider) {
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
        CellTable<RemoteSailingServerReferenceDTO> remoteServersTable = createRemoteServersTable();
        filteredServerTablePanel.setTable(remoteServersTable);
        serverDataProvider.addDataDisplay(remoteServersTable);

        remoteServersContentPanel.add(filteredServerTablePanel);
        remoteServersContentPanel.add(remoteServersTable);
        remoteServersContentPanel.add(createButtonToolbar());

        refreshSailingServerList();
    }

    private Panel createButtonToolbar() {
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);

        Button addButton = new Button(stringMessages.add());
        buttonPanel.add(addButton);
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addRemoteSailingServerReference();
            }
        });

        Button removeButton = new Button(stringMessages.remove());
        buttonPanel.add(removeButton);
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeSelectedSailingServers();
            }
        });

        Button refreshButton = new Button(stringMessages.refresh());
        buttonPanel.add(refreshButton);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshSailingServerList();
            }
        });
        return buttonPanel;
    }
    
    private CellTable<RemoteSailingServerReferenceDTO> createRemoteServersTable() {
        CellTable<RemoteSailingServerReferenceDTO> serverTable = new BaseCelltable<RemoteSailingServerReferenceDTO>(
                10000, tableRes);
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
                    builder.appendEscaped(RemoteServerInstancesManagementPanel.this.stringMessages
                            .errorAddingSailingServer(server.getLastErrorMessage()));
                }
                return builder.toSafeHtml();
            }
        };
        serverTable.addColumn(serverNameColumn, stringMessages.name());
        serverTable.addColumn(serverUrlColumn, stringMessages.url());
        serverTable.addColumn(eventsOrErrorColumn, stringMessages.events());

        serverTable.setEmptyTableWidget(new Label(stringMessages.noSailingServerInstancesYet()));
        
        refreshableServerSelectionModel = new RefreshableMultiSelectionModel<RemoteSailingServerReferenceDTO>(
                new EntityIdentityComparator<RemoteSailingServerReferenceDTO>() {
                    @Override
                    public boolean representSameEntity(RemoteSailingServerReferenceDTO dto1,
                            RemoteSailingServerReferenceDTO dto2) {
                        return dto1.getUrl().equals(dto2.getUrl());
                    }
                    @Override
                    public int hashCode(RemoteSailingServerReferenceDTO t) {
                        return t.getUrl().hashCode();
                    }
                }, filteredServerTablePanel.getAllListDataProvider());
        serverTable.setSelectionModel(refreshableServerSelectionModel);

        return serverTable;
    }
    
    private void refreshSailingServerList() {
        sailingService.getRemoteSailingServerReferences(new AsyncCallback<List<RemoteSailingServerReferenceDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorRefreshingSailingServers(caught.getMessage()));
            }

            @Override
            public void onSuccess(List<RemoteSailingServerReferenceDTO> result) {
                filteredServerTablePanel.updateAll(result);
            }
        });
    }

    private void removeSelectedSailingServers() {
        Set<String> toRemove = new HashSet<String>();
        for (RemoteSailingServerReferenceDTO selectedServer: refreshableServerSelectionModel.getSelectedSet()) {
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
                Notification.notify(stringMessages.successfullyUpdatedSailingServers(), NotificationType.INFO);
            }
        });
    }

    private void addRemoteSailingServerReference() {
        SailingServerCreateOrEditDialog dialog = new SailingServerCreateOrEditDialog(filteredServerTablePanel.getAll(), stringMessages, new DialogCallback<RemoteSailingServerReferenceDTO>() {
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
                        filteredServerTablePanel.add(result);
                        Notification.notify(stringMessages.successfullyUpdatedSailingServers(), NotificationType.INFO);
                    }
                });
            }
        });
        dialog.show();	
    }
}
