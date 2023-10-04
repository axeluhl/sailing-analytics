package com.sap.sailing.landscape.ui.client;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.CHANGE_OWNERSHIP;
import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapperWithSingleSelectionAndFilter;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.landscape.common.shared.SecuredLandscapeTypes;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledActionsColumn;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog.DialogConfig;
import com.sap.sse.security.ui.client.component.SecuredDTOOwnerColumn;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;

/**
 * A panel with a table that allows the user to manage a set of SSH key pairs for the landscape.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SshKeyManagementPanel extends VerticalPanel {
    private final LandscapeManagementWriteServiceAsync landscapeManagementService;
    private final TableWrapperWithSingleSelectionAndFilter<SSHKeyPairDTO, StringMessages, AdminConsoleTableResources> sshKeyTable;
    private final PasswordTextBox sshPrivateKeyPassphrase;
    private final BusyIndicator sshKeyLoadingBusy;
    private final ErrorReporter errorReporter;
    private final RefreshableSingleSelectionModel<String> regionSelectionModel;
    private final Label passphraseStatus;
    private final Label passphraseText;
    private final Button addButton;
    private final Button generateButton;
    private final AwsAccessKeyProvider awsAccessKeyProvider;
    
    public SshKeyManagementPanel(StringMessages stringMessages, UserService userService,
            LandscapeManagementWriteServiceAsync landscapeManagementService, AdminConsoleTableResources tableResources,
            ErrorReporter errorReporter, AwsAccessKeyProvider awsAccessKeyProvider,
            RefreshableSingleSelectionModel<String> regionSelectionModel, int countKeysPerPage) {
        this.regionSelectionModel = regionSelectionModel;
        this.landscapeManagementService = landscapeManagementService;
        this.errorReporter = errorReporter;
        this.sshPrivateKeyPassphrase = new PasswordTextBox();
        this.passphraseStatus = new Label();
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, SecuredLandscapeTypes.SSH_KEY);
        add(buttonPanel);
        this.awsAccessKeyProvider = awsAccessKeyProvider;
        addButton = buttonPanel.addCreateAction(stringMessages.add(), ()->{
            openAddSshKeyDialog(stringMessages, awsAccessKeyProvider);
        });
        addButton.setEnabled(regionSelectionModel.getSelectedObject() != null);
        generateButton = buttonPanel.addCreateAction(stringMessages.generate(), ()->{
            openGenerateSshKeyDialog(stringMessages, awsAccessKeyProvider);
        });
        generateButton.setEnabled(regionSelectionModel.getSelectedObject() != null);
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
                }), tableResources, Optional.of(sshKey -> userService.hasPermission(sshKey, DefaultActions.UPDATE)),
                /* filter label */ Optional.empty(), /* filter checkbox label */ stringMessages.hideElementsWithoutUpdateRights()) {
                    @Override
                    protected Iterable<String> getSearchableStrings(SSHKeyPairDTO t) {
                        return Arrays.asList(t.getName(), t.getRegionId(), t.getCreatorName());
                    }
        };
        sshKeyTable.getTable().setPageSize(countKeysPerPage);
        sshKeyTable.addColumn(object->object.getRegionId(), stringMessages.region());
        sshKeyTable.addColumn(object->object.getName(), stringMessages.name());
        sshKeyTable.addColumn(object->object.getCreatorName(), stringMessages.creator());
        sshKeyTable.addColumn(object->object.getCreationTime().toString(), stringMessages.creationTime());
        SecuredDTOOwnerColumn.configureOwnerColumns(sshKeyTable.getTable(), sshKeyTable.getColumnSortHandler(), stringMessages);
        final AccessControlledActionsColumn<SSHKeyPairDTO, SshKeyPairImagesBarCell> sshKeyPairActionColumn = create(
                new SshKeyPairImagesBarCell(stringMessages), userService);
        final DialogConfig<SSHKeyPairDTO> editOwnerShipDialog = EditOwnershipDialog.create(userService.getUserManagementWriteService(), SecuredLandscapeTypes.SSH_KEY,
                competitorDTO -> sshKeyTable.refresh(), stringMessages);
        sshKeyPairActionColumn.addAction(SshKeyPairImagesBarCell.ACTION_CHANGE_OWNERSHIP, CHANGE_OWNERSHIP,
                editOwnerShipDialog::openOwnershipDialog);
        final EditACLDialog.DialogConfig<SSHKeyPairDTO> configACL = EditACLDialog
                .create(userService.getUserManagementWriteService(), SecuredLandscapeTypes.SSH_KEY, null, stringMessages);
        sshKeyPairActionColumn.addAction(SshKeyPairImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                configACL::openDialog);
        sshKeyPairActionColumn.addAction(SshKeyPairImagesBarCell.ACTION_REMOVE, DefaultActions.DELETE,
                sshKeyPairDTO->landscapeManagementService.removeSshKey(sshKeyPairDTO, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        sshKeyTable.remove(sshKeyPairDTO);
                    }
            }));
        sshKeyPairActionColumn.addAction(SshKeyPairImagesBarCell.ACTION_SHOW_KEYS, DefaultActions.READ,
                sshKeyPairDTO->landscapeManagementService.getSshPublicKey(sshKeyPairDTO.getRegionId(), sshKeyPairDTO.getName(), new AsyncCallback<byte[]>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(final byte[] publicKey) {
                        landscapeManagementService.getEncryptedSshPrivateKey(sshKeyPairDTO.getRegionId(), sshKeyPairDTO.getName(), new AsyncCallback<byte[]>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(caught.getMessage());
                            }

                            @Override
                            public void onSuccess(final byte[] encryptedPrivateKey) {
                                showKeys(sshKeyPairDTO.getName(), publicKey, encryptedPrivateKey, stringMessages);
                            }
                        });
                    }
                }));
        sshKeyTable.addColumn(sshKeyPairActionColumn);
        add(sshKeyTable);
        passphraseText = new Label(stringMessages.sshPrivateKeyPassphraseForSelectedKeyPair());
        add(passphraseText);
        add(sshPrivateKeyPassphrase);
        add(passphraseStatus);
        sshKeyLoadingBusy = new SimpleBusyIndicator();
        add(sshKeyLoadingBusy);
        buttonPanel.addRemoveAction(stringMessages.remove(), sshKeyTable.getSelectionModel(), /* withConfirmation */ true, ()->{
            landscapeManagementService.removeSshKey(sshKeyTable.getSelectionModel().getSelectedObject(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
            errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
            sshKeyTable.remove(sshKeyTable.getSelectionModel().getSelectedObject());
            }
         });
        });
        
        regionSelectionModel.addSelectionChangeHandler(e->{
            if (awsAccessKeyProvider.hasValidSessionCredentials()) {
                showKeysInRegion(regionSelectionModel.getSelectedObject());
                addButton.setEnabled(regionSelectionModel.getSelectedObject() != null);
                generateButton.setEnabled(regionSelectionModel.getSelectedObject() != null);
            }
        });
        addSshKeySelectionChangedHandler(event -> {
            boolean value = sshKeyTable.getSelectionModel().getSelectedObject() != null;
            sshPrivateKeyPassphrase.setVisible(value);
            passphraseText.setVisible(value);
            passphraseStatus.setVisible(value);
        } );
    }
    
    public void addSshKeySelectionChangedHandler(SelectionChangeEvent.Handler handler) {
        sshKeyTable.getSelectionModel().addSelectionChangeHandler(handler);
    }
    
    public String getPassphraseForPrivateKeyDecryption() {
        return sshPrivateKeyPassphrase.getValue();
    }
    
    private void showKeys(String keyName, byte[] publicKey, byte[] encryptedPrivateKey, StringMessages stringMessages) {
        new SshKeyDisplayAndDownloadDialog(keyName, publicKey, encryptedPrivateKey, stringMessages).show();
    }

    private void openGenerateSshKeyDialog(StringMessages stringMessages, AwsAccessKeyProvider awsAccessKeyProvider) {
        new GenerateSshKeyDialog(stringMessages,
                new DialogCallback<Triple<String, String, String>>() {
                    @Override
                    public void ok(Triple<String, String, String> keyPairNameAndPassphrases) {
                        landscapeManagementService.generateSshKeyPair(regionSelectionModel.getSelectedObject(),
                                keyPairNameAndPassphrases.getA(), keyPairNameAndPassphrases.getB(),
                                new AsyncCallback<SSHKeyPairDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(caught.getMessage());
                            }

                            @Override
                            public void onSuccess(SSHKeyPairDTO result) {
                                sshKeyTable.add(result);
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                    }
            }).show();
    }

    private void openAddSshKeyDialog(StringMessages stringMessages, AwsAccessKeyProvider awsAccessKeyProvider) {
        new AddSshKeyDialog(stringMessages,
                new DialogCallback<Triple<String, String, String>>() {
                    @Override
                    public void ok(Triple<String, String, String> keyPairNameAndPublicAndPrivateKey) {
                        landscapeManagementService.addSshKeyPair(regionSelectionModel.getSelectedObject(), keyPairNameAndPublicAndPrivateKey.getA(),
                                keyPairNameAndPublicAndPrivateKey.getB(), keyPairNameAndPublicAndPrivateKey.getC(),
                                new AsyncCallback<SSHKeyPairDTO>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError(caught.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(SSHKeyPairDTO result) {
                                        sshKeyTable.add(result);
                                    }
                        });
                    }
                    
                    @Override
                    public void cancel() {
                    }
                }).show();
    }

    public void showKeysInRegion(String regionId) {
        sshKeyTable.getFilterPanel().removeAll();
        if (regionId != null) {
            if (awsAccessKeyProvider.hasValidSessionCredentials()) {
                addButton.setEnabled(regionSelectionModel.getSelectedObject() != null);
                generateButton.setEnabled(regionSelectionModel.getSelectedObject() != null);
            }
            sshKeyLoadingBusy.setBusy(true);
            landscapeManagementService.getSshKeys(regionId, new AsyncCallback<ArrayList<SSHKeyPairDTO>>() {
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
            addButton.setEnabled(false);
            generateButton.setEnabled(false);
        }
    }

    public SSHKeyPairDTO getSelectedKeyPair() {
        return sshKeyTable.getSelectionModel().getSelectedObject();
    }
    
    public void addOnPassphraseChangedListener(ChangeHandler handler) {
        sshPrivateKeyPassphrase.addChangeHandler(handler);
    }
    
    public void setPassphraseValidation(boolean isValid, com.sap.sse.gwt.adminconsole.StringMessages stringMessages) {
        passphraseStatus.setText(isValid ?  stringMessages.validPassphrase(): stringMessages.invalidPassphrase());
        passphraseStatus.getElement().getStyle().setColor(isValid ?  "green": "red");
    }
    
}
