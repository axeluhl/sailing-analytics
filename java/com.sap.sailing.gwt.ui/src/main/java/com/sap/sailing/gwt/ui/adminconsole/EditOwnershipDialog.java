package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.impl.OwnershipImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

public class EditOwnershipDialog extends DataEntryDialog<Ownership> {
    private final StringMessages stringMessages;
    private final UserManagementServiceAsync userManagementService;
    private final TextBox usernameBox;
    private final TextBox groupnameBox;
    private boolean resolvingUsername;
    private boolean resolvingUserGroupName;
    private SecurityUser resolvedUser;
    private UserGroup resolvedUserGroup;
    
    public EditOwnershipDialog(UserManagementServiceAsync userManagementService, Ownership ownership,
            StringMessages stringMessages, DialogCallback<Ownership> callback) {
        super(stringMessages.ownership(), stringMessages.editObjectOwnership(), stringMessages.ok(),
                stringMessages.cancel(), /* validator */ null, callback);
        this.userManagementService = userManagementService;
        this.usernameBox = createTextBox(ownership.getUserOwner()==null?"":ownership.getUserOwner().getName(), /* visibleLength */ 20);
        this.groupnameBox = createTextBox(ownership.getTenantOwner()==null?"":ownership.getTenantOwner().getName(), /* visibileLength */ 20);
        this.usernameBox.addChangeHandler(e->resolveUser());
        this.groupnameBox.addChangeHandler(e->resolveUserGroup());
        this.stringMessages = stringMessages;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(2, 2);
        result.setWidget(0, 0, new Label(stringMessages.user()));
        result.setWidget(0, 1, usernameBox);
        result.setWidget(1, 0, new Label(stringMessages.group()));
        result.setWidget(1, 1, groupnameBox);
        return result;
    }



    private void resolveUserGroup() {
        resolvedUserGroup = null;
        resolvingUserGroupName = true;
        getUserManagementService().getUserGroupByName(usernameBox.getText(), new AsyncCallback<UserGroup>() {
            @Override
            public void onSuccess(UserGroup result) {
                resolvedUserGroup = result;
                resolvingUserGroupName = false;
            }
            
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.errorObtainingUserGroup(caught.getMessage()), NotificationType.ERROR);
            }
        });
    }

    private void resolveUser() {
        resolvedUser = null;
        resolvingUsername = true;
        getUserManagementService().getUserByName(usernameBox.getText(), new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                resolvedUser = result;
                resolvingUsername = false;
            }
            
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.errorObtainingUser(caught.getMessage()), NotificationType.ERROR);
            }
        });
    }

    public UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }

    @Override
    protected Ownership getResult() {
        final Ownership result;
        if (resolvingUsername || resolvingUserGroupName) {
            result = null;
        } else {
            result = new OwnershipImpl(resolvedUser, resolvedUserGroup);
        }
        return result;
    }

}
