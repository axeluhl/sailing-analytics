package com.sap.sse.security.ui.client.component;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.DELETE;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_DELETE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.AbstractSortableTextColumn;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapper;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.dto.UserGroupWithSecurityDTO;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

/**
 * @param <S>
 */
public class UserGroupTableWrapper extends
        TableWrapper<UserGroupWithSecurityDTO, RefreshableSingleSelectionModel<UserGroupWithSecurityDTO>, StringMessages, CellTableWithCheckboxResources> {
    private final LabeledAbstractFilterablePanel<UserGroupWithSecurityDTO> filterField;
    private final UserService userService;

    public UserGroupTableWrapper(UserService userService, Iterable<HasPermissions> additionalPermissions,
            StringMessages stringMessages, ErrorReporter errorReporter, boolean enablePager,
            CellTableWithCheckboxResources tableResources) {
        super(stringMessages, errorReporter, false, enablePager,
                new EntityIdentityComparator<UserGroupWithSecurityDTO>() {
                    @Override
                    public boolean representSameEntity(UserGroupWithSecurityDTO dto1, UserGroupWithSecurityDTO dto2) {
                        return dto1.getId().toString().equals(dto2.getId().toString());
                    }

                    @Override
                    public int hashCode(UserGroupWithSecurityDTO t) {
                        return t.getId().hashCode();
                    }
                }, tableResources);
        this.userService = userService;
        ListHandler<UserGroupWithSecurityDTO> userColumnListHandler = getColumnSortHandler();

        // users table
        TextColumn<UserGroupWithSecurityDTO> UserGroupWithSecurityDTONameColumn = new AbstractSortableTextColumn<UserGroupWithSecurityDTO>(
                UserGroupWithSecurityDTO -> UserGroupWithSecurityDTO.getName(), userColumnListHandler);

        final HasPermissions type = SecuredSecurityTypes.USER_GROUP;
        final Function<UserGroupWithSecurityDTO, String> idFactory = UserGroupWithSecurityDTO::getName;
        final AccessControlledActionsColumn<UserGroupWithSecurityDTO, DefaultActionsImagesBarCell> actionColumn = new AccessControlledActionsColumn<>(
                new DefaultActionsImagesBarCell(stringMessages), userService, type, idFactory);
        actionColumn.addAction(ACTION_DELETE, DELETE, UserGroupWithSecurityDTO -> {
            if (Window.confirm(stringMessages.doYouReallyWantToRemoveUserGroup(UserGroupWithSecurityDTO.getName()))) {
                getUserManagementService().deleteUserGroup(UserGroupWithSecurityDTO.getId().toString(),
                        new AsyncCallback<SuccessInfo>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                deletingUserGroupWithSecurityDTOFailed(UserGroupWithSecurityDTO, caught.getMessage());
                            }

                            @Override
                            public void onSuccess(SuccessInfo result) {
                                if (result.isSuccessful()) {
                                    filterField.remove(UserGroupWithSecurityDTO);
                                } else {
                                    deletingUserGroupWithSecurityDTOFailed(UserGroupWithSecurityDTO,
                                            result.getMessage());
                                }
                            }

                            private void deletingUserGroupWithSecurityDTOFailed(UserGroupWithSecurityDTO user,
                                    String message) {
                                // TODO: v i18n v
                                errorReporter.reportError(stringMessages.errorDeletingUser(user.getName(), message));
                            }
                        });
            }
        });

        final EditOwnershipDialog.DialogConfig<UserGroupWithSecurityDTO> configOwnership = EditOwnershipDialog.create(
                userService.getUserManagementService(), type, idFactory,
                user -> refreshUserList((Callback<Iterable<UserGroupWithSecurityDTO>, Throwable>) null),
                stringMessages);

        final EditACLDialog.DialogConfig<UserGroupWithSecurityDTO> configACL = EditACLDialog.create(
                userService.getUserManagementService(), type, idFactory, user -> user.getAccessControlList(),
                stringMessages);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_OWNERSHIP, DefaultActions.CHANGE_OWNERSHIP,
                configOwnership::openDialog);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                u -> configACL.openDialog(u));

        filterField = new LabeledAbstractFilterablePanel<UserGroupWithSecurityDTO>(
                new Label(stringMessages.filterUserGroups()), new ArrayList<UserGroupWithSecurityDTO>(), dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(UserGroupWithSecurityDTO t) {
                List<String> string = new ArrayList<String>();
                string.add(t.getName());
                return string;
            }

            @Override
            public AbstractCellTable<UserGroupWithSecurityDTO> getCellTable() {
                return table;
            }
        };
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());

        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(userColumnListHandler);
        table.addColumn(UserGroupWithSecurityDTONameColumn, getStringMessages().groupName());
        table.addColumn(actionColumn, stringMessages.actions());
        table.ensureDebugId("UserGroupWithSecurityDTOTable");
    }

    public Iterable<UserGroupWithSecurityDTO> getAllUsers() {
        return filterField.getAll();
    }

    public LabeledAbstractFilterablePanel<UserGroupWithSecurityDTO> getFilterField() {
        return filterField;
    }

    public void refreshUserGroups(Iterable<UserGroupWithSecurityDTO> UserGroupWithSecurityDTOs) {
        getFilteredUserGroups(UserGroupWithSecurityDTOs);
    }

    /**
     * @param callback
     *            optional; may be {@code null}
     */
    public void refreshUserList(final Callback<Iterable<UserGroupWithSecurityDTO>, Throwable> callback) {
        final AsyncCallback<Collection<UserGroupWithSecurityDTO>> myCallback = new AsyncCallback<Collection<UserGroupWithSecurityDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(
                        "Remote Procedure Call getUserGroupWithSecurityDTOs() - Failure: " + caught.getMessage());
                if (callback != null) {
                    callback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(Collection<UserGroupWithSecurityDTO> result) {
                getFilteredUserGroups(result);
                refreshUserGroups(result);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        };
        getUserManagementService().getSecuredUserGroups(myCallback);
    }

    private void getFilteredUserGroups(Iterable<UserGroupWithSecurityDTO> result) {
        filterField.updateAll(result);
    }

    private UserManagementServiceAsync getUserManagementService() {
        return userService.getUserManagementService();
    }
}
