package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.DELETE;
import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_DELETE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
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
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sailing.gwt.ui.shared.RemoteSailingServerReferenceDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.FlushableCellTable;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledActionsColumn;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;

public class RemoteServerInstancesManagementPanel extends SimplePanel {
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private FlushableCellTable<RemoteSailingServerReferenceDTO> remoteServersTable;

    private final ListDataProvider<RemoteSailingServerReferenceDTO> serverDataProvider;
    private MultiSelectionModel<RemoteSailingServerReferenceDTO> refreshableServerSelectionModel;
    private LabeledAbstractFilterablePanel<RemoteSailingServerReferenceDTO> filteredServerTablePanel;

    private final CaptionPanel remoteServersPanel;

    public RemoteServerInstancesManagementPanel(SailingServiceAsync sailingService, final UserService userService,
            ErrorReporter errorReporter, StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.userService = userService;
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
        filteredServerTablePanel = createFilteredServerTablePanel();
        remoteServersTable = createRemoteServersTable();
        serverDataProvider.addDataDisplay(remoteServersTable);
        remoteServersContentPanel.add(filteredServerTablePanel);
        remoteServersContentPanel.add(remoteServersTable);
        remoteServersContentPanel.add(createButtonToolbar());

        refreshSailingServerList();
    }

    private LabeledAbstractFilterablePanel<RemoteSailingServerReferenceDTO> createFilteredServerTablePanel() {
        return new LabeledAbstractFilterablePanel<RemoteSailingServerReferenceDTO>(
                new Label(stringMessages.filterBy() + ":"), Collections.<RemoteSailingServerReferenceDTO> emptyList(),
                serverDataProvider, stringMessages) {
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

            @Override
            public AbstractCellTable<RemoteSailingServerReferenceDTO> getCellTable() {
                return remoteServersTable;
            }
        };
    }

    private Panel createButtonToolbar() {
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        buttonPanel.add(new Button(stringMessages.add(), (ClickHandler) event -> addRemoteSailingServerReference()));
        buttonPanel.add(createRemoveButton(buttonPanel, event -> removeSelectedSailingServers()));
        buttonPanel.add(new Button(stringMessages.refresh(), (ClickHandler) event -> refreshSailingServerList()));
        return buttonPanel;
    }

    private Button createRemoveButton(HorizontalPanel buttonPanel, ClickHandler handler) {
        Button button = new Button(stringMessages.remove(), (ClickHandler) event -> removeSelectedSailingServers());
        refreshableServerSelectionModel.addSelectionChangeHandler(event -> {
            Set<RemoteSailingServerReferenceDTO> set = refreshableServerSelectionModel.getSelectedSet();
            button.setText(set.size() <= 1 ? stringMessages.remove() : stringMessages.removeNumber(set.size()));
        });
        return button;
    }

    private FlushableCellTable<RemoteSailingServerReferenceDTO> createRemoteServersTable() {
        RemoteServerInstancesManagementTableWrapper wrapper = new RemoteServerInstancesManagementTableWrapper(
                stringMessages, errorReporter, serverDataProvider);

        wrapper.addColumn(createTextColumn(RemoteSailingServerReferenceDTO::getName), stringMessages.name());
        wrapper.addColumn(createTextColumn(RemoteSailingServerReferenceDTO::getUrl), stringMessages.url());
        wrapper.addColumn(createEventsColumn(), stringMessages.events());
        wrapper.addColumn(createActionsColumn(), stringMessages.actions());

        wrapper.setEmptyTableWidget(new Label(stringMessages.noSailingServerInstancesYet()));

        refreshableServerSelectionModel = wrapper.getSelectionModel();
        return wrapper.getTable();
    }

    private TextColumn<RemoteSailingServerReferenceDTO> createTextColumn(
            Function<RemoteSailingServerReferenceDTO, String> getter) {
        return new TextColumn<RemoteSailingServerReferenceDTO>() {
            @Override
            public String getValue(RemoteSailingServerReferenceDTO server) {
                return getter.apply(server) != null ? getter.apply(server) : "";
            }
        };
    }

    private Column<RemoteSailingServerReferenceDTO, SafeHtml> createEventsColumn() {
        return new Column<RemoteSailingServerReferenceDTO, SafeHtml>(new SafeHtmlCell()) {
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
    }

    private AccessControlledActionsColumn<RemoteSailingServerReferenceDTO, DefaultActionsImagesBarCell> createActionsColumn() {
        final AccessControlledActionsColumn<RemoteSailingServerReferenceDTO, DefaultActionsImagesBarCell> actionsColumn = create(
                new DefaultActionsImagesBarCell(stringMessages), userService);

        actionsColumn.addAction(ACTION_DELETE, DELETE, e -> {
            Set<String> toDelete = new HashSet<>();
            toDelete.add(e.getName());
            removeSailingServers(toDelete);
        });

        final EditACLDialog.DialogConfig<RemoteSailingServerReferenceDTO> configACL = EditACLDialog.create(
                userService.getUserManagementService(), SecuredDomainType.REMOTE_SAILING_SERVER_REFERENCE_DTO,
                RemoteSailingServerReferenceDTO::getAccessControlList, stringMessages);

        actionsColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                configACL::openDialog);

        return actionsColumn;
    }

    private void refreshSailingServerList() {
        sailingService.getRemoteSailingServerReferences(createCallback(stringMessages::errorRefreshingSailingServers,
                filteredServerTablePanel::updateAll, false));
    }

    private void removeSelectedSailingServers() {
        Set<String> toRemove = new HashSet<String>();
        for (RemoteSailingServerReferenceDTO selectedServer : refreshableServerSelectionModel.getSelectedSet()) {
            toRemove.add(selectedServer.getName());
        }
        removeSailingServers(toRemove);
    }

    private void removeSailingServers(Set<String> toRemove) {
        sailingService.removeSailingServers(toRemove, createCallback(stringMessages::errorRemovingSailingServers,
                (result) -> refreshSailingServerList(), true));
    }

    private void addRemoteSailingServerReference() {
        new SailingServerCreateOrEditDialog(filteredServerTablePanel.getAll(), stringMessages,
                new DialogCallback<RemoteSailingServerReferenceDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final RemoteSailingServerReferenceDTO server) {
                        sailingService.addRemoteSailingServerReference(server, createCallback(
                                stringMessages::errorAddingSailingServer, filteredServerTablePanel::add, true));
                    }
                }).show();
    }

    private <T> AsyncCallback<T> createCallback(Function<String, String> errorMapper, Consumer<T> resultConsumer,
            boolean notify) {
        return new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(errorMapper.apply(caught.getMessage()));
            }

            @Override
            public void onSuccess(T result) {
                resultConsumer.accept(result);
                if (notify) {
                    Notification.notify(stringMessages.successfullyUpdatedSailingServers(), NotificationType.INFO);
                }
            }
        };
    }
}
