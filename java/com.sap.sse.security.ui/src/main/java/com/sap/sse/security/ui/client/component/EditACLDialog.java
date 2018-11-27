package com.sap.sse.security.ui.client.component;

import static com.sap.sse.gwt.client.Notification.NotificationType.ERROR;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Named;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.SecuredObject;
import com.sap.sse.security.shared.impl.AccessControlListImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.EditACLDialog.AclDialogResult;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class EditACLDialog extends DataEntryDialog<AclDialogResult> {

    private final AclEditPanel aclEditPanel;

    static class AclDialogResult {
        private final AccessControlList acl;

        private AclDialogResult(final AccessControlList acl) {
            this.acl = acl;
        }

        public AccessControlList getAcl() {
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
            QualifiedObjectIdentifier qualifiedObjectIdentifier,
            StringMessages stringMessages, DialogCallback<AclDialogResult> callback) {
        super("ACL", stringMessages.editACL(), stringMessages.ok(), stringMessages.cancel(), new Validator(), callback);
        // TODO: ^ i18n ^
        aclEditPanel = new AclEditPanel(userManagementService, stringMessages);
        userManagementService.getAccessControlListWithoutPruning(qualifiedObjectIdentifier,
                new AsyncCallback<AccessControlList>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onSuccess(AccessControlList result) {
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
        return new AclDialogResult(new AccessControlListImpl(aclEditPanel.getUserGroupsWithPermissions()));
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
    public static <T extends Named & SecuredObject> DialogConfig<T> create(
            final UserManagementServiceAsync userManagementService, final HasPermissions type,
            final Function<T, String> typeRelativeIdFactory, final Consumer<T> updateCallback,
            final StringMessages stringMessages) {
        return new DialogConfig<>(userManagementService, type, typeRelativeIdFactory, updateCallback, stringMessages);
    }

    public static class DialogConfig<T extends Named & SecuredObject> {
        private final UserManagementServiceAsync userManagementService;
        private final Consumer<T> updateCallback;
        private final Function<T, QualifiedObjectIdentifier> identifierFactory;
        private final StringMessages stringMessages;

        private DialogConfig(final UserManagementServiceAsync userManagementService, final HasPermissions type,
                final Function<T, String> idFactory, final Consumer<T> updateCallback,
                final StringMessages stringMessages) {
            this.userManagementService = userManagementService;
            this.identifierFactory = idFactory.andThen(type::getQualifiedObjectIdentifier);
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
            new EditACLDialog(userManagementService, identifierFactory.apply(securedObject), StringMessages.INSTANCE,
                    new EditAclDialogCallback(securedObject)).show();
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
                        new AsyncCallback<AccessControlList>() {
                            @Override
                            public void onSuccess(AccessControlList result) {
                                securedObject.setAccessControlList(result);
                                updateCallback.accept(securedObject);
                            }

                            @Override
                            public final void onFailure(Throwable caught) {
                                // TODO: v i18n v
                                Notification.notify(stringMessages.errorUpdatingOwnership(securedObject.getName()),
                                        ERROR);
                            }
                        });
            }

            @Override
            public final void cancel() {
            }
        }
    }

}
