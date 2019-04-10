package com.sap.sailing.gwt.ui.adminconsole.tractrac;

import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.AbstractSortableTextColumn;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapper;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledActionsColumn;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog;
import com.sap.sse.security.ui.client.component.SecuredDTOOwnerColumn;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * A wrapper for a CellTable displaying an overview over the existing TracTracConnections. It shows the name, the Live
 * URI, the Stored Uri, the JSON URL, the TracTrac Server Update URI, the TracTrac user name, the TracTrac password, the
 * name of the creator, the group and user each user group is owned by. There are options edit or to delete the
 * connection, change the ownership or edit the associated ACL. Editing the connection will open a new instance of
 * {@link EditTracTracConnectionDialog}.
 */
public class TracTracConnectionTableWrapper extends
        TableWrapper<TracTracConfigurationWithSecurityDTO, RefreshableSingleSelectionModel<TracTracConfigurationWithSecurityDTO>, StringMessages, CellTableWithCheckboxResources> {
    private final LabeledAbstractFilterablePanel<TracTracConfigurationWithSecurityDTO> filterField;
    private final SailingServiceAsync sailingServiceAsync;
    private final com.sap.sailing.gwt.ui.client.StringMessages stringMessagesClient;

    public TracTracConnectionTableWrapper(final UserService userService, final SailingServiceAsync sailingServiceAsync,
            final com.sap.sailing.gwt.ui.client.StringMessages stringMessages, final ErrorReporter errorReporter,
            final boolean enablePager, final CellTableWithCheckboxResources tableResources, final Runnable refresher) {
        super(stringMessages, errorReporter, false, enablePager,
                new EntityIdentityComparator<TracTracConfigurationWithSecurityDTO>() {
                    @Override
                    public boolean representSameEntity(TracTracConfigurationWithSecurityDTO dto1,
                            TracTracConfigurationWithSecurityDTO dto2) {
                        return dto1.getIdentifier().equals(dto2.getIdentifier());
                    }

                    @Override
                    public int hashCode(TracTracConfigurationWithSecurityDTO t) {
                        return t.getIdentifier().hashCode();
                    }
                }, tableResources);
        this.stringMessagesClient = stringMessages;
        this.sailingServiceAsync = sailingServiceAsync;
        final ListHandler<TracTracConfigurationWithSecurityDTO> tracTracAccountColumnListHandler = getColumnSortHandler();

        // table
        final TextColumn<TracTracConfigurationWithSecurityDTO> tracTracAccountNameColumn = new AbstractSortableTextColumn<TracTracConfigurationWithSecurityDTO>(
                dto -> dto.getName(), tracTracAccountColumnListHandler);
        final TextColumn<TracTracConfigurationWithSecurityDTO> tracTracAccountLiveUriColumn = new AbstractSortableTextColumn<TracTracConfigurationWithSecurityDTO>(
                dto -> dto.getLiveDataURI(), tracTracAccountColumnListHandler);
        final TextColumn<TracTracConfigurationWithSecurityDTO> tracTracAccountStoredUriColumn = new AbstractSortableTextColumn<TracTracConfigurationWithSecurityDTO>(
                dto -> dto.getStoredDataURI(), tracTracAccountColumnListHandler);
        final TextColumn<TracTracConfigurationWithSecurityDTO> tracTracAccountJsonUrlColumn = new AbstractSortableTextColumn<TracTracConfigurationWithSecurityDTO>(
                dto -> dto.getJSONURL(), tracTracAccountColumnListHandler);
        final TextColumn<TracTracConfigurationWithSecurityDTO> tracTracAccountTracTracServerUpdateUriColumn = new AbstractSortableTextColumn<TracTracConfigurationWithSecurityDTO>(
                dto -> dto.getCourseDesignUpdateURI(), tracTracAccountColumnListHandler);
        final TextColumn<TracTracConfigurationWithSecurityDTO> tracTracAccountUsernameColumn = new AbstractSortableTextColumn<TracTracConfigurationWithSecurityDTO>(
                dto -> dto.getTracTracUsername(), tracTracAccountColumnListHandler);
        final TextColumn<TracTracConfigurationWithSecurityDTO> tracTracAccountPasswordColumn = new AbstractSortableTextColumn<TracTracConfigurationWithSecurityDTO>(
                dto -> dto.getTracTracPassword(), tracTracAccountColumnListHandler);
        final TextColumn<TracTracConfigurationWithSecurityDTO> tracTracAccountCreatorNameColumn = new AbstractSortableTextColumn<TracTracConfigurationWithSecurityDTO>(
                dto -> dto.getCreatorName(), tracTracAccountColumnListHandler);

        final HasPermissions type = SecuredDomainType.TRACTRAC_ACCOUNT;
        final AccessControlledActionsColumn<TracTracConfigurationWithSecurityDTO, DefaultActionsImagesBarCell> actionColumn = create(
                new DefaultActionsImagesBarCell(stringMessages), userService);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_UPDATE, DefaultActions.UPDATE, dto -> {
            new EditTracTracConnectionDialog(dto, new DialogCallback<TracTracConfigurationWithSecurityDTO>() {
                @Override
                public void ok(final TracTracConfigurationWithSecurityDTO editedObject) {
                    sailingServiceAsync.updateTracTracConfiguration(editedObject,
                            new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError(
                                            "Exception trying to update configuration in DB: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Void voidResult) {
                                    refreshTracTracConnectionList();
                                }
                            }));
                }

                @Override
                public void cancel() {
                }
            }, userService, errorReporter).show();
        });

        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_DELETE, DefaultActions.DELETE, dto -> {
            sailingServiceAsync.deleteTracTracConfiguration(dto, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Exception trying to delete configuration in DB: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    refreshTracTracConnectionList();
                }
            });
        });

        final EditOwnershipDialog.DialogConfig<TracTracConfigurationWithSecurityDTO> configOwnership = EditOwnershipDialog
                .create(userService.getUserManagementService(), type, dto -> refreshTracTracConnectionList(),
                        stringMessages);

        final EditACLDialog.DialogConfig<TracTracConfigurationWithSecurityDTO> configACL = EditACLDialog.create(
                userService.getUserManagementService(), type, dto -> dto.getAccessControlList(), stringMessages);

        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_OWNERSHIP, DefaultActions.CHANGE_OWNERSHIP,
                configOwnership::openDialog);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                u -> configACL.openDialog(u));

        filterField = new LabeledAbstractFilterablePanel<TracTracConfigurationWithSecurityDTO>(
                new Label(stringMessages.filterTracTracConnections()),
                new ArrayList<TracTracConfigurationWithSecurityDTO>(), dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(TracTracConfigurationWithSecurityDTO t) {
                List<String> string = new ArrayList<String>();
                string.add(t.getName());
                return string;
            }

            @Override
            public AbstractCellTable<TracTracConfigurationWithSecurityDTO> getCellTable() {
                return table;
            }
        };
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());

        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(tracTracAccountColumnListHandler);
        table.addColumn(tracTracAccountNameColumn, getStringMessages().name());
        table.addColumn(tracTracAccountLiveUriColumn, stringMessagesClient.liveUri());
        table.addColumn(tracTracAccountStoredUriColumn, stringMessagesClient.storedUri());
        table.addColumn(tracTracAccountJsonUrlColumn, stringMessagesClient.jsonUrl());
        table.addColumn(tracTracAccountTracTracServerUpdateUriColumn, stringMessagesClient.tracTracUpdateUrl());
        table.addColumn(tracTracAccountUsernameColumn, stringMessagesClient.tractracUsername());
        table.addColumn(tracTracAccountPasswordColumn, stringMessagesClient.tractracPassword());
        table.addColumn(tracTracAccountCreatorNameColumn, stringMessagesClient.creatorName());
        SecuredDTOOwnerColumn.configureOwnerColumns(table, tracTracAccountColumnListHandler, stringMessages);
        table.addColumn(actionColumn, stringMessages.actions());
        table.ensureDebugId("TracTracConfigurationWithSecurityDTOTable");
    }

    public LabeledAbstractFilterablePanel<TracTracConfigurationWithSecurityDTO> getFilterField() {
        return filterField;
    }

    public void refreshTracTracConnectionList() {
        sailingServiceAsync
                .getPreviousTracTracConfigurations(new AsyncCallback<List<TracTracConfigurationWithSecurityDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter
                                .reportError("Remote Procedure Call getPreviousTracTracConfigurations() - Failure: "
                                        + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(List<TracTracConfigurationWithSecurityDTO> result) {
                        filterField.updateAll(result);
                    }
                });
    }
}
