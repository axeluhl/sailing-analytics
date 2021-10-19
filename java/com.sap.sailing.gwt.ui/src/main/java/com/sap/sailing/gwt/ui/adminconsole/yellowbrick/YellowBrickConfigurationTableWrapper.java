package com.sap.sailing.gwt.ui.adminconsole.yellowbrick;

import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.YellowBrickConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.AbstractSortableTextColumn;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
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
 * A wrapper for a CellTable displaying an overview over the existing YellowBrick configurations. It shows the name, the
 * race URL, the user name, the presence of a password, the name of the creator, the group and user each user group is
 * owned by. There are options edit or to delete the configuration, change the ownership or edit the associated ACL.
 * Editing the connection will open a new instance of {@link YellowBrickConfigurationDialog}.<p>
 * 
 * TODO make into a TableWrapperWithSingleSelectionAndFilter
 */
public class YellowBrickConfigurationTableWrapper extends
        TableWrapper<YellowBrickConfigurationWithSecurityDTO, RefreshableMultiSelectionModel<YellowBrickConfigurationWithSecurityDTO>, StringMessages, CellTableWithCheckboxResources> {
    private final LabeledAbstractFilterablePanel<YellowBrickConfigurationWithSecurityDTO> filterField;
    private final SailingServiceAsync sailingServiceWriteAsync;
    private final com.sap.sailing.gwt.ui.client.StringMessages stringMessagesClient;

    public YellowBrickConfigurationTableWrapper(final UserService userService, final SailingServiceWriteAsync sailingServiceWriteAsync,
            final com.sap.sailing.gwt.ui.client.StringMessages stringMessages, final ErrorReporter errorReporter,
            final boolean enablePager, final CellTableWithCheckboxResources tableResources, final Runnable refresher) {
        super(stringMessages, errorReporter, true, enablePager,
                new EntityIdentityComparator<YellowBrickConfigurationWithSecurityDTO>() {
                    @Override
                    public boolean representSameEntity(YellowBrickConfigurationWithSecurityDTO dto1,
                            YellowBrickConfigurationWithSecurityDTO dto2) {
                        return dto1.getIdentifier().equals(dto2.getIdentifier());
                    }

                    @Override
                    public int hashCode(YellowBrickConfigurationWithSecurityDTO t) {
                        return t.getIdentifier().hashCode();
                    }
                }, tableResources);
        this.stringMessagesClient = stringMessages;
        this.sailingServiceWriteAsync = sailingServiceWriteAsync;
        final ListHandler<YellowBrickConfigurationWithSecurityDTO> yellowBrickAccountColumnListHandler = getColumnSortHandler();

        // table
        final TextColumn<YellowBrickConfigurationWithSecurityDTO> tracTracAccountNameColumn = new AbstractSortableTextColumn<>(
                dto -> dto.getName(), yellowBrickAccountColumnListHandler);
        final TextColumn<YellowBrickConfigurationWithSecurityDTO> yellowBrickAccountRaceUrlColumn = new AbstractSortableTextColumn<>(
                dto -> dto.getRaceUrl(), yellowBrickAccountColumnListHandler);
        final TextColumn<YellowBrickConfigurationWithSecurityDTO> tracTracAccountUsernameColumn = new AbstractSortableTextColumn<>(
                dto -> dto.getUsername(), yellowBrickAccountColumnListHandler);
        final TextColumn<YellowBrickConfigurationWithSecurityDTO> tracTracAccountCreatorNameColumn = new AbstractSortableTextColumn<>(
                dto -> dto.getCreatorName(), yellowBrickAccountColumnListHandler);
        final HasPermissions type = SecuredDomainType.YELLOWBRICK_ACCOUNT;
        final AccessControlledActionsColumn<YellowBrickConfigurationWithSecurityDTO, DefaultActionsImagesBarCell> actionColumn = create(
                new DefaultActionsImagesBarCell(stringMessages), userService);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_UPDATE, DefaultActions.UPDATE, dto -> {
            new YellowBrickConfigurationEditDialog(dto, new DialogCallback<YellowBrickConfigurationWithSecurityDTO>() {
                @Override
                public void ok(final YellowBrickConfigurationWithSecurityDTO editedObject) {
                    sailingServiceWriteAsync.updateYellowBrickConfiguration(editedObject,
                            new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError(
                                            "Exception trying to update configuration in DB: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Void voidResult) {
                                    refreshYellowBrickConfigurationList();
                                }
                            }));
                }

                @Override
                public void cancel() {
                }
            }, userService, errorReporter).show();
        });

        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_DELETE, DefaultActions.DELETE, dto -> {
            sailingServiceWriteAsync.deleteYellowBrickConfigurations(Collections.singletonList(dto), new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Exception trying to delete configuration in DB: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    refreshYellowBrickConfigurationList();
                }
            });
        });
        final EditOwnershipDialog.DialogConfig<YellowBrickConfigurationWithSecurityDTO> configOwnership = EditOwnershipDialog
                .create(userService.getUserManagementWriteService(), type, dto -> refreshYellowBrickConfigurationList(),
                        stringMessages);
        final EditACLDialog.DialogConfig<YellowBrickConfigurationWithSecurityDTO> configACL = EditACLDialog.create(
                userService.getUserManagementWriteService(), type, dto -> dto.getAccessControlList(), stringMessages);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_OWNERSHIP, DefaultActions.CHANGE_OWNERSHIP,
                configOwnership::openOwnershipDialog);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                u -> configACL.openDialog(u));
        filterField = new LabeledAbstractFilterablePanel<YellowBrickConfigurationWithSecurityDTO>(
                new Label(stringMessages.filterYellowBrickConfigurations()),
                new ArrayList<YellowBrickConfigurationWithSecurityDTO>(), dataProvider, stringMessages) {
            @Override
            public Iterable<String> getSearchableStrings(YellowBrickConfigurationWithSecurityDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.getName());
                strings.add(t.getCreatorName());
                strings.add(t.getRaceUrl());
                return strings;
            }

            @Override
            public AbstractCellTable<YellowBrickConfigurationWithSecurityDTO> getCellTable() {
                return table;
            }
        };
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());
        filterField
                .setUpdatePermissionFilterForCheckbox(connection -> userService.hasPermission(connection, DefaultActions.UPDATE));
        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(yellowBrickAccountColumnListHandler);
        table.addColumn(tracTracAccountNameColumn, getStringMessages().name());
        table.addColumn(yellowBrickAccountRaceUrlColumn, stringMessagesClient.jsonUrl());
        table.addColumn(tracTracAccountUsernameColumn, stringMessagesClient.tractracUsername());
        table.addColumn(tracTracAccountCreatorNameColumn, stringMessagesClient.creatorName());
        SecuredDTOOwnerColumn.configureOwnerColumns(table, yellowBrickAccountColumnListHandler, stringMessages);
        table.addColumn(actionColumn, stringMessages.actions());
        table.ensureDebugId("TracTracConfigurationWithSecurityDTOTable");
    }

    public LabeledAbstractFilterablePanel<YellowBrickConfigurationWithSecurityDTO> getFilterField() {
        return filterField;
    }

    public void refreshYellowBrickConfigurationList() {
        refreshYellowBrickConnectionList(/* selectWhenDone */ null);
    }
    
    /**
     * @param selectEntityWhenDone
     *            if not {@code null}, select an entity in the table that the {@link EntityIdentityComparator} considers
     *            the same entity if present
     */
    public void refreshYellowBrickConnectionList(final YellowBrickConfigurationWithSecurityDTO selectEntityWhenDone) {
        sailingServiceWriteAsync
                .getPreviousYellowBrickConfigurations(new AsyncCallback<List<YellowBrickConfigurationWithSecurityDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter
                                .reportError("Remote Procedure Call getPreviousTracTracConfigurations() - Failure: "
                                        + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(List<YellowBrickConfigurationWithSecurityDTO> result) {
                        filterField.updateAll(result);
                        if (selectEntityWhenDone != null) {
                            Scheduler.get().scheduleDeferred(()->{
                                for (final YellowBrickConfigurationWithSecurityDTO oneResult : result) {
                                    if (getSelectionModel().getEntityIdentityComparator().representSameEntity(oneResult, selectEntityWhenDone)) {
                                        getSelectionModel().setSelected(oneResult, true);
                                        filterField.search(oneResult.getRaceUrl());
                                        break;
                                    }
                                }
                            });
                        }
                    }
                });
    }
}
