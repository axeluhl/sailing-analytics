package com.sap.sailing.gwt.ui.adminconsole.swisstiming;

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
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationWithSecurityDTO;
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
 * A wrapper for a CellTable displaying an overview over the existing Swiss Timing Archived Connections. It shows the
 * User-Group name, the group and user each user group is owned by and options to delete the group, change the ownership
 * or edit the associated ACL.
 */
public class SwissTimingArchivedConnectionTableWrapper extends
        TableWrapper<SwissTimingArchiveConfigurationWithSecurityDTO, RefreshableSingleSelectionModel<SwissTimingArchiveConfigurationWithSecurityDTO>, StringMessages, CellTableWithCheckboxResources> {
    private final LabeledAbstractFilterablePanel<SwissTimingArchiveConfigurationWithSecurityDTO> filterField;
    private final SailingServiceAsync sailingServiceAsync;
    private final com.sap.sailing.gwt.ui.client.StringMessages stringMessagesClient;

    public SwissTimingArchivedConnectionTableWrapper(final UserService userService, final SailingServiceAsync sailingServiceAsync,
            final com.sap.sailing.gwt.ui.client.StringMessages stringMessages, final ErrorReporter errorReporter,
            final boolean enablePager, final CellTableWithCheckboxResources tableResources, final Runnable refresher) {
        super(stringMessages, errorReporter, false, enablePager,
                new EntityIdentityComparator<SwissTimingArchiveConfigurationWithSecurityDTO>() {
                    @Override
                    public boolean representSameEntity(SwissTimingArchiveConfigurationWithSecurityDTO dto1,
                            SwissTimingArchiveConfigurationWithSecurityDTO dto2) {
                        return dto1.getIdentifier().equals(dto2.getIdentifier());
                    }

                    @Override
                    public int hashCode(SwissTimingArchiveConfigurationWithSecurityDTO t) {
                        return t.getIdentifier().hashCode();
                    }
                }, tableResources);
        this.stringMessagesClient = stringMessages;
        this.sailingServiceAsync = sailingServiceAsync;
        final ListHandler<SwissTimingArchiveConfigurationWithSecurityDTO> swissTimingAccountColumnListHandler = getColumnSortHandler();

        // table
        final TextColumn<SwissTimingArchiveConfigurationWithSecurityDTO> swissTimingAccountJsonUrlColumn = new AbstractSortableTextColumn<SwissTimingArchiveConfigurationWithSecurityDTO>(
                dto -> dto.getJsonUrl(), swissTimingAccountColumnListHandler);
        final TextColumn<SwissTimingArchiveConfigurationWithSecurityDTO> swissTimingAccountCreatorNameColumn = new AbstractSortableTextColumn<SwissTimingArchiveConfigurationWithSecurityDTO>(
                dto -> dto.getCreatorName(), swissTimingAccountColumnListHandler);

        final HasPermissions type = SecuredDomainType.SWISS_TIMING_ARCHIVE_ACCOUNT;
        final AccessControlledActionsColumn<SwissTimingArchiveConfigurationWithSecurityDTO, DefaultActionsImagesBarCell> actionColumn = create(
                new DefaultActionsImagesBarCell(stringMessages), userService);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_UPDATE, DefaultActions.UPDATE, dto -> {
            new EditSwissTimingArchivedConnectionDialog(dto,
                    new DialogCallback<SwissTimingArchiveConfigurationWithSecurityDTO>() {
                @Override
                        public void ok(final SwissTimingArchiveConfigurationWithSecurityDTO editedObject) {
                            sailingServiceAsync.updateSwissTimingArchiveConfiguration(editedObject,
                            new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError(
                                            "Exception trying to update configuration in DB: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Void voidResult) {
                                    refreshConnectionList();
                                }
                            }));
                }

                @Override
                public void cancel() {
                }
            }, userService, errorReporter).show();
        });
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_DELETE, DefaultActions.DELETE, dto -> {
            sailingServiceAsync.deleteSwissTimingArchiveConfiguration(dto, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Exception trying to delete configuration in DB: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    refreshConnectionList();
                }
            });
        });
        final EditOwnershipDialog.DialogConfig<SwissTimingArchiveConfigurationWithSecurityDTO> configOwnership = EditOwnershipDialog
                .create(userService.getUserManagementService(), type, dto -> refreshConnectionList(),
                        stringMessages);
        final EditACLDialog.DialogConfig<SwissTimingArchiveConfigurationWithSecurityDTO> configACL = EditACLDialog
                .create(
                userService.getUserManagementService(), type, dto -> dto.getAccessControlList(), stringMessages);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_OWNERSHIP, DefaultActions.CHANGE_OWNERSHIP,
                configOwnership::openDialog);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                u -> configACL.openDialog(u));
        filterField = new LabeledAbstractFilterablePanel<SwissTimingArchiveConfigurationWithSecurityDTO>(
                new Label(stringMessages.filterSwissTimingAchivedConnections()),
                new ArrayList<SwissTimingArchiveConfigurationWithSecurityDTO>(), dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(SwissTimingArchiveConfigurationWithSecurityDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.getName());
                strings.add(t.getCreatorName());
                return strings;
            }

            @Override
            public AbstractCellTable<SwissTimingArchiveConfigurationWithSecurityDTO> getCellTable() {
                return table;
            }
        };
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());

        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(swissTimingAccountColumnListHandler);
        table.addColumn(swissTimingAccountJsonUrlColumn, stringMessagesClient.jsonUrl());
        table.addColumn(swissTimingAccountCreatorNameColumn, stringMessagesClient.creatorName());
        SecuredDTOOwnerColumn.configureOwnerColumns(table, swissTimingAccountColumnListHandler, stringMessages);
        table.addColumn(actionColumn, stringMessages.actions());
        table.ensureDebugId("SwissTimingConfigurationWithSecurityDTOTable");
    }

    public LabeledAbstractFilterablePanel<SwissTimingArchiveConfigurationWithSecurityDTO> getFilterField() {
        return filterField;
    }

    public void refreshConnectionList() {
        sailingServiceAsync.getPreviousSwissTimingArchiveConfigurations(
                new AsyncCallback<List<SwissTimingArchiveConfigurationWithSecurityDTO>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(
                                "Remote Procedure Call getPreviousSwissTimingArchiveConfigurations() - Failure: "
                                        + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(List<SwissTimingArchiveConfigurationWithSecurityDTO> result) {
                        filterField.updateAll(result);

                    }
                });
    }
}
