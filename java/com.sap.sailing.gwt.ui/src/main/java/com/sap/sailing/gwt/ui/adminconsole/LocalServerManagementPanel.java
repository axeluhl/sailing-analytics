package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.CHANGE_ACL;
import static com.sap.sse.security.shared.HasPermissions.DefaultActions.CHANGE_OWNERSHIP;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ServerConfigurationDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;

public class LocalServerManagementPanel extends SimplePanel {

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final AccessControlledButtonPanel buttonPanel;
    private Label serverNameInfo, buildVersionInfo, groupOwnerInfo, userOwnerInfo;
    private CheckBox isStandaloneServerCheckbox, isPublicServerCheckbox, isSelfServiceServerCheckbox;

    private ServerInfoDTO currentServerInfo;
    private final UserService userService;
    
    private final UserStatusEventHandler userStatusEventHandler = new UserStatusEventHandler() {
        @Override
        public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
            updateServerInfo(userService.getServerInfo());
        }
    };

    public LocalServerManagementPanel(final SailingServiceAsync sailingService, final UserService userService,
            final ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.userService = userService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        final Panel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");

        mainPanel.add(this.buttonPanel = createServerActionsUi(userService));
        mainPanel.add(createServerInfoUI());
        mainPanel.add(createServerConfigurationUI());

        refreshServerConfiguration();
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        userService.addUserStatusEventHandler(userStatusEventHandler, true);
    }
    
    @Override
    protected void onUnload() {
        super.onUnload();
        userService.removeUserStatusEventHandler(userStatusEventHandler);
    }
    
    private AccessControlledButtonPanel createServerActionsUi(final UserService userService) {
        final HasPermissions type = SecuredSecurityTypes.SERVER;
        final Consumer<ServerInfoDTO> updateCallback = event -> userService.updateUser(false);
        final EditOwnershipDialog.DialogConfig<ServerInfoDTO> configOwner = EditOwnershipDialog
                .create(userService.getUserManagementService(), type, updateCallback, stringMessages);
        final EditACLDialog.DialogConfig<ServerInfoDTO> configACL = EditACLDialog
                .create(userService.getUserManagementService(), type, updateCallback, stringMessages);

        final Predicate<DefaultActions> permissionCheck = action -> currentServerInfo != null
                && userService.hasPermission(type.getPermission(action), currentServerInfo.getOwnership());

        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, type);
        buttonPanel.addAction(stringMessages.actionChangeOwnership(), () -> permissionCheck.test(CHANGE_OWNERSHIP),
                () -> configOwner.openDialog(currentServerInfo));
        buttonPanel.addAction(stringMessages.actionChangeACL(), () -> permissionCheck.test(CHANGE_ACL),
                () -> configACL.openDialog(currentServerInfo));
        return buttonPanel;
    }

    private Widget createServerInfoUI() {
        final ServerDataCaptionPanel captionPanel = new ServerDataCaptionPanel(stringMessages.serverInformation(), 4);
        serverNameInfo = captionPanel.addInformation(stringMessages.name() + ":");
        buildVersionInfo = captionPanel.addInformation(stringMessages.buildVersion() + ":");
        groupOwnerInfo = captionPanel.addInformation(stringMessages.ownership() + " - " + stringMessages.group() + ":");
        userOwnerInfo = captionPanel.addInformation(stringMessages.ownership() + " - " + stringMessages.user() + ":");
        return captionPanel;
    }
    
    private Widget createServerConfigurationUI() {
        final ServerDataCaptionPanel captionPanel = new ServerDataCaptionPanel(stringMessages.serverConfiguration(), 3);
        final Command callback = this::serverConfigurationChanged;
        isStandaloneServerCheckbox = captionPanel.addChekBox(stringMessages.standaloneServer() + ":", callback);
        isPublicServerCheckbox = captionPanel.addChekBox(stringMessages.publicServer() + ":", callback);
        isSelfServiceServerCheckbox = captionPanel.addChekBox(stringMessages.selfServiceServer() + ":", callback);
        return captionPanel;
    }    

    private void serverConfigurationChanged() {
        final Boolean publicServer = isPublicServerCheckbox.isEnabled() ? isPublicServerCheckbox.getValue() : null;
        // FIXME self service not yet supported
        final Boolean selfServiceServer = isSelfServiceServerCheckbox.isEnabled()
                ? isSelfServiceServerCheckbox.getValue()
                : null;
        final ServerConfigurationDTO serverConfig = new ServerConfigurationDTO(isStandaloneServerCheckbox.getValue(),
                publicServer, selfServiceServer, null);

        sailingService.updateServerConfiguration(serverConfig, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.updatedServerSetupError(), NotificationType.ERROR);
                errorReporter.reportError(caught.getMessage());
                refreshServerConfiguration();
            }

            @Override
            public void onSuccess(Void result) {
                Notification.notify(stringMessages.updatedServerSetup(), NotificationType.SUCCESS);
                refreshServerConfiguration();
            }
        });
    }

    private void refreshServerConfiguration() {
        sailingService.getServerConfiguration(new RefreshAsyncCallback<>(this::updateServerConfiguration));
    }
    
    private void updateServerInfo(ServerInfoDTO serverInfo) {
        LocalServerManagementPanel.this.currentServerInfo = serverInfo;
        LocalServerManagementPanel.this.buttonPanel.updateVisibility();
        serverNameInfo.setText(serverInfo.getName());
        buildVersionInfo.setText(serverInfo.getBuildVersion() != null ? serverInfo.getBuildVersion() : "Unknown");
        final OwnershipDTO ownership = serverInfo.getOwnership();
        final boolean hasGroupOwner = ownership != null && ownership.getTenantOwner() != null;
        final boolean hasUserOwner = ownership != null && ownership.getUserOwner() != null;
        groupOwnerInfo.setText(hasGroupOwner ? ownership.getTenantOwner().getName() : "---");
        userOwnerInfo.setText(hasUserOwner ? ownership.getUserOwner().getName() : "---");
        
        // Update changeability
        isSelfServiceServerCheckbox.setEnabled(userService.hasCurrentUserMetaPermission(serverInfo.getIdentifier().getPermission(ServerActions.CREATE_OBJECT), serverInfo.getOwnership()));
        // TODO update isPublicServerCheckbox -> default server tenant is currently not available in the UI
    }
    
    private void updateServerConfiguration(ServerConfigurationDTO result) {
        isStandaloneServerCheckbox.setValue(result.isStandaloneServer(), false);
        isStandaloneServerCheckbox.setEnabled(true);
        
        isPublicServerCheckbox.setValue(result.isPublic(), false);
        isSelfServiceServerCheckbox.setValue(result.isSelfService(), false);
    }

    private class RefreshAsyncCallback<T> implements AsyncCallback<T> {

        private final Consumer<T> successCallback;

        private RefreshAsyncCallback(Consumer<T> successCallback) {
            this.successCallback = successCallback;
        }

        @Override
        public final void onFailure(Throwable caught) {
            errorReporter.reportError(caught.getMessage());
        }

        @Override
        public final void onSuccess(T result) {
            this.successCallback.accept(result);
        }

    }

    private class ServerDataCaptionPanel extends CaptionPanel {
        
        private final Grid grid;
        private int actualRows = 0;
        
        private ServerDataCaptionPanel(final String caption, final int rowCount) {
            super(caption);
            this.grid = new Grid(rowCount, 2);
            setContentWidget(grid);
        }

        private Label addInformation(final String labelText) {
            return addWidget(labelText, new Label());
        }

        private CheckBox addChekBox(final String labelText, final Command callback) {
            final CheckBox checkBox = new CheckBox();
            checkBox.addValueChangeHandler(event -> callback.execute());
            checkBox.setEnabled(false);
            return addWidget(labelText, checkBox);
        }

        private <T extends Widget> T addWidget(final String labelText, final T widget) {
            final int nextRowIndex = actualRows++;
            this.grid.setText(nextRowIndex, 0, labelText);
            this.grid.setWidget(nextRowIndex, 1, widget);
            return widget;
        }
    }
}
