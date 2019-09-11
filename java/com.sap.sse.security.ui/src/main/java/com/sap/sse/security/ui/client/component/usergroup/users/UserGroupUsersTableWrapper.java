package com.sap.sse.security.ui.client.component.usergroup.users;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.UPDATE;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.AbstractSortableTextColumn;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapper;
import com.sap.sse.security.shared.dto.StrippedUserDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledActionsColumn;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class UserGroupUsersTableWrapper extends
        TableWrapper<StrippedUserDTO, RefreshableMultiSelectionModel<StrippedUserDTO>, StringMessages, CellTableWithCheckboxResources> {

    public UserGroupUsersTableWrapper(StringMessages stringMessages, ErrorReporter errorReporter,
            CellTableWithCheckboxResources tableResources, UserService userService,
            SingleSelectionModel<UserGroupDTO> userGroupSelectionModel, Runnable refresher) {
        super(stringMessages, errorReporter, true, true, new EntityIdentityComparator<StrippedUserDTO>() {
            @Override
            public boolean representSameEntity(StrippedUserDTO user1, StrippedUserDTO user2) {
                return user1.getId().equals(user2.getId());
            }

            @Override
            public int hashCode(StrippedUserDTO user) {
                return user.getId().hashCode();
            }
        }, tableResources);

        final TextColumn<StrippedUserDTO> usernameColumn = new AbstractSortableTextColumn<StrippedUserDTO>(
                StrippedUserDTO::getName, getColumnSortHandler());
        final AccessControlledActionsColumn<StrippedUserDTO, UserGroupUsersImagesBarCell> actionColumns = AccessControlledActionsColumn
                .create(new UserGroupUsersImagesBarCell(stringMessages), userService,
                        user -> userGroupSelectionModel.getSelectedObject());
        actionColumns.addAction(UserGroupUsersImagesBarCell.ACTION_DELETE, UPDATE, user -> {
            final UserGroupDTO tenant = userGroupSelectionModel.getSelectedObject();
            final String username = user.getName();
            userService.getUserManagementService().removeUserFromUserGroup(tenant.getId().toString(), username,
                    new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(stringMessages.couldNotRemoveUserFromUserGroup(username, tenant.getName(),
                                    caught.getMessage()));
                        }

                        @Override
                        public void onSuccess(Void result) {
                            StrippedUserDTO userToRemoveFromTenant = null;
                            for (final StrippedUserDTO userInTenant : tenant.getUsers()) {
                                if (Util.equalsWithNull(userInTenant.getName(), username)) {
                                    userToRemoveFromTenant = userInTenant;
                                    break;
                                }
                            }
                            if (userToRemoveFromTenant != null) {
                                tenant.remove(userToRemoveFromTenant);
                            }
                            refresher.run();
                        }
                    });
        });
        
        table.addColumn(usernameColumn, stringMessages.username());
        table.addColumn(actionColumns);
    }


}
