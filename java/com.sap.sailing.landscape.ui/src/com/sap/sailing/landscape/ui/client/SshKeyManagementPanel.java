package com.sap.sailing.landscape.ui.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.TableWrapperWithSingleSelectionAndFilter;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.landscape.common.SecuredLandscapeTypes;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;

public class SshKeyManagementPanel extends VerticalPanel {
    private final LandscapeManagementWriteServiceAsync landscapeManagementService;
    private final TableWrapperWithSingleSelectionAndFilter<SSHKeyPairDTO, StringMessages, AdminConsoleTableResources> sshKeyTable;
    private final BusyIndicator sshKeyLoadingBusy;
    private final ErrorReporter errorReporter;
    
    public SshKeyManagementPanel(StringMessages stringMessages, UserService userService,
            LandscapeManagementWriteServiceAsync landscapeManagementService, AdminConsoleTableResources tableResources,
            ErrorReporter errorReporter, AwsAccessKeyProvider awsAccessKeyProvider) {
        this.landscapeManagementService = landscapeManagementService;
        this.errorReporter = errorReporter;
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, SecuredLandscapeTypes.SSH_KEY);
        add(buttonPanel);
        buttonPanel.addCreateAction(stringMessages.add(), ()->{
            openAddSshKeyDialog(stringMessages);
        });
        sshKeyTable =
                new TableWrapperWithSingleSelectionAndFilter<SSHKeyPairDTO, StringMessages, AdminConsoleTableResources>(stringMessages, errorReporter, /* enablePager */ true,
                Optional.of(new EntityIdentityComparator<SSHKeyPairDTO>() {
                    @Override
                    public boolean representSameEntity(SSHKeyPairDTO dto1, SSHKeyPairDTO dto2) {
                        return Util.equalsWithNull(dto1.getRegionId(), dto2.getRegionId()) &&
                               Util.equalsWithNull(dto1.getName(), dto2.getName());
                    }

                    @Override
                    public int hashCode(SSHKeyPairDTO t) {
                        return (t.getRegionId() == null ? 0 : t.getRegionId().hashCode()) ^
                               (t.getName() == null ? 0 : t.getName().hashCode());
                    }
                }), tableResources, Optional.empty(), /* filter label */ Optional.empty(), /* filter checkbox label */ null) {
                    @Override
                    protected Iterable<String> getSearchableStrings(SSHKeyPairDTO t) {
                        return Arrays.asList(t.getName(), t.getRegionId(), t.getCreatorName());
                    }
        };
        sshKeyTable.addColumn(object->object.getRegionId(), stringMessages.region());
        sshKeyTable.addColumn(object->object.getName(), stringMessages.name());
        sshKeyTable.addColumn(object->object.getCreatorName(), stringMessages.creator());
        sshKeyTable.addColumn(object->object.getCreationTime().toString(), stringMessages.creationTime());
        add(sshKeyTable);
        sshKeyLoadingBusy = new SimpleBusyIndicator();
        add(sshKeyLoadingBusy);
        buttonPanel.addRemoveAction(stringMessages.remove(), sshKeyTable.getSelectionModel(), /* withConfirmation */ true, ()->{
            landscapeManagementService.removeSshKey(awsAccessKeyProvider.getAwsAccessKeyId(), awsAccessKeyProvider.getAwsSecret(),
                    sshKeyTable.getSelectionModel().getSelectedObject(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {}
            });
        });
    }
    
    private void openAddSshKeyDialog(StringMessages stringMessages) {
        new AddSshKeyDialog(stringMessages.sshKeys(), stringMessages.sshKeys(), stringMessages.ok(), stringMessages.cancel(), /* validator */ null,
                new DialogCallback<SSHKeyPairDTO>() {
                    @Override
                    public void ok(SSHKeyPairDTO editedObject) {
                    }
                    
                    @Override
                    public void cancel() {
                        // TODO Implement Type1610991600832.cancel(...)
                    }
                }).show();
    }

    public void showKeysInRegion(String awsAccessKey, String awsSecret, String regionId) {
        sshKeyTable.getDataProvider().getList().clear();
        if (regionId != null) {
            sshKeyLoadingBusy.setBusy(true);
            landscapeManagementService.getSshKeys(awsAccessKey, awsSecret, regionId, new AsyncCallback<ArrayList<SSHKeyPairDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.getMessage());
                    sshKeyLoadingBusy.setBusy(false);
                }
    
                @Override
                public void onSuccess(ArrayList<SSHKeyPairDTO> sshKeyPairDTOs) {
                    sshKeyTable.refresh(sshKeyPairDTOs);
                    sshKeyLoadingBusy.setBusy(false);
                }
            });
        } else {
            sshKeyTable.getDataProvider().getList().clear();
        }
    }
}
