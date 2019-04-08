package com.sap.sse.security.ui.client.component.editacl;

import static com.sap.sse.gwt.client.Notification.NotificationType.ERROR;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Named;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog.AclDialogResult;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class EditACLDialog extends DataEntryDialog<AclDialogResult> {

    private final AclEditPanel aclEditPanel;

    static class AclDialogResult {
        private final AccessControlListDTO acl;

        private AclDialogResult(final AccessControlListDTO acl) {
            this.acl = acl;
        }

        public AccessControlListDTO getAcl() {
            return acl;
        }
    }

    private static class Validator implements DataEntryDialog.Validator<AclDialogResult> {
        @Override
        public String getErrorMessage(AclDialogResult valueToValidate) {
            // nothing to validate since user input is already validated in aclPanel
            return null;
        }
    }

    private EditACLDialog(UserManagementServiceAsync userManagementService,
            QualifiedObjectIdentifier qualifiedObjectIdentifier, Action[] availableActions,
            StringMessages stringMessages, DialogCallback<AclDialogResult> callback) {
        super(stringMessages.acl(), stringMessages.editACL(),
                stringMessages.ok(), stringMessages.cancel(), new Validator(), callback);
        final String typeString = qualifiedObjectIdentifier.getTypeIdentifier();
        final String id = qualifiedObjectIdentifier.getTypeRelativeObjectIdentifier().toString();
        aclEditPanel = new AclEditPanel(userManagementService, availableActions, stringMessages, typeString, id);
        userManagementService.getAccessControlListWithoutPruning(qualifiedObjectIdentifier,
                new AsyncCallback<AccessControlListDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Notification.notify(caught.getMessage(), NotificationType.ERROR);
                    }

                    @Override
                    public void onSuccess(AccessControlListDTO result) {
                        aclEditPanel.updateAcl(result);
                    }
                });
    }

    @Override
    protected Widget getAdditionalWidget() {
        return aclEditPanel;
    }

    @Override
    protected AclDialogResult getResult() {
        return new AclDialogResult(new AccessControlListDTO(aclEditPanel.getUserGroupsWithCombinedActions()));
    }

    /**
     * Creates a new {@link DialogConfig dialog configuration} instance which can be (re-)used to
     * {@link DialogConfig#openDialog(Named) open} a {@link EditACLDialog dialog}.
     * 
     * @param userManagementService
     *            {@link UserManagementServiceAsync} to use to set the secured object's ownership
     * @param type
     *            {@link SecuredDomainType} specifying the type of required permissions to modify the secured object
     * @param typeRelativeIdFactory
     *            {@link Function factory} to get a {@link String type relative identifier} for the secured object
     * @param updateCallback
     *            {@link Consumer callback} to execute when the dialog is confirmed and ownership update succeeded
     * @param errorCallback
     *            {@link Consumer callback} to execute when the dialog is confirmed and ownership update fails
     */
    public static <T extends Named & SecuredDTO> DialogConfig<T> create(
            final UserManagementServiceAsync userManagementService, final HasPermissions type,
            final Consumer<T> updateCallback,
            final StringMessages stringMessages) {
        return new DialogConfig<>(userManagementService, type, updateCallback, stringMessages);
    }

    public static class DialogConfig<T extends Named & SecuredDTO> {
        private final UserManagementServiceAsync userManagementService;
        private final Consumer<T> updateCallback;
        private final Function<T, QualifiedObjectIdentifier> identifierFactory;
        private final Supplier<Action[]> availableActionsFactory;
        private final StringMessages stringMessages;

        private DialogConfig(final UserManagementServiceAsync userManagementService, final HasPermissions type,
                final Consumer<T> updateCallback,
                final StringMessages stringMessages) {
            this.userManagementService = userManagementService;
            this.identifierFactory = SecuredDTO::getIdentifier;
            this.availableActionsFactory = type::getAvailableActions;
            this.updateCallback = updateCallback;
            this.stringMessages = stringMessages;
        }

        /**
         * Opens a {@link EditACLDialog dialog} to edit ownerships for the provided secured object instance.
         * 
         * @param securedObject
         *            {@link Named} {@link SecuredObject} instance to edit ownerships for
         */
        public void openDialog(final T securedObject) {
            new EditACLDialog(userManagementService, identifierFactory.apply(securedObject),
                    availableActionsFactory.get(), stringMessages, new EditAclDialogCallback(securedObject)).show();
        }

        private class EditAclDialogCallback implements DialogCallback<AclDialogResult> {

            private final T securedObject;

            private EditAclDialogCallback(T securedObject) {
                this.securedObject = securedObject;
            }

            @Override
            public void ok(AclDialogResult aclResult) {
                final QualifiedObjectIdentifier objectIdentifier = identifierFactory.apply(securedObject);
                userManagementService.overrideAccessControlList(objectIdentifier, aclResult.getAcl(),
                        new AsyncCallback<AccessControlListDTO>() {
                            @Override
                            public void onSuccess(AccessControlListDTO result) {
                                securedObject.setAccessControlList(result);
                                updateCallback.accept(securedObject);
                            }

                            @Override
                            public final void onFailure(Throwable caught) {
                                Notification.notify(stringMessages.errorUpdatingAcl(securedObject.getName()), ERROR);
                            }
                        });
            }

            @Override
            public final void cancel() {
            }
        }
    }

}
