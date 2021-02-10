package com.sap.sailing.landscape.ui.client;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.AmazonMachineImageDTO;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.ActionsColumn;
import com.sap.sse.gwt.client.celltable.TableWrapperWithSingleSelectionAndFilter;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;

/**
 * A panel for managing an SAP Sailing Analytics landscape in the AWS cloud. The main widgets offered will address the
 * following areas:
 * <ul>
 * <li>AWS Credentials: manage an AWS key (with persistence) and the corresponding secret (without persistence, maybe
 * provided by the browser's password manager)</li>
 * <li>SSH Key Pairs: generate, import, export, and deploy SSH key pairs used for spinning up and connect to compute
 * instances</li>
 * <li>Application server replica sets: single process per instance, or multiple processes per instance; with or without
 * auto scaling groups and launch configurations for auto-scaling the number of replicas; change the software version running on an application server
 * replica set while maintaining availability as good as possible by de-registering the master instance from the master target group, then
 * spinning up a new master, then any desired number of replicas, then swap the old replicas for the new replicas in the public target group
 * and register the master instance again.</li>
 * <li>MongoDB replica sets: single node or true replica set; scale out / in by adding / removing instances</li>
 * <li>RabbitMQ infrastructure: usually a single node per region</li>
 * <li>Central Reverse Proxies: currently a single node per region, but ideally this could potentially be a
 * multi-instance scenario for high availability with those instances sharing a common configuration; eventually, the
 * functionality of these reverse proxies may be taken over by the AWS Application Load Balancer component, such as
 * central logging as well as specific re-write rules, SSL offloading, certificate management, and http-to-https
 * forwarding.</li>
 * <li>Amazon Machine Images (AMIs) of different types: the {@code image-type} tag tells the type; images and their
 * snapshots can exist in one or more versions, and updates can be triggered explicitly. Maybe at some point we would
 * have automated tests asserting that an update went well.</li>
 * </ul>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LandscapeManagementPanel extends VerticalPanel implements AwsAccessKeyProvider {
    private final LandscapeManagementWriteServiceAsync landscapeManagementService;
    private final TableWrapperWithSingleSelectionAndFilter<String, StringMessages, AdminConsoleTableResources> regionsTable;
    private final TableWrapperWithSingleSelectionAndFilter<MongoEndpointDTO, StringMessages, AdminConsoleTableResources> mongoEndpointsTable;
    private final BusyIndicator mongoEndpointsBusy;
    private final TableWrapperWithSingleSelectionAndFilter<AmazonMachineImageDTO, StringMessages, AdminConsoleTableResources> machineImagesTable;
    private final BusyIndicator machineImagesBusy;
    private final TextBox awsAccessKeyTextBox;
    private final PasswordTextBox awsSecretPasswordTextBox;
    private final ErrorReporter errorReporter;
    private final static String AWS_ACCESS_KEY_USER_PREFERENCE = "aws.access.key";

    public LandscapeManagementPanel(StringMessages stringMessages, UserService userService,
            AdminConsoleTableResources tableResources, ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        landscapeManagementService = initAndRegisterLandscapeManagementService();
        final HorizontalPanel awsCredentialsAndSshKeys = new HorizontalPanel();
        add(awsCredentialsAndSshKeys);
        final CaptionPanel awsCredentialsPanel = new CaptionPanel(stringMessages.awsCredentials());
        awsCredentialsAndSshKeys.add(awsCredentialsPanel);
        final Grid awsCredentialsGrid = new Grid(2, 2);
        awsCredentialsPanel.add(awsCredentialsGrid);
        awsCredentialsGrid.setWidget(0, 0, new Label(stringMessages.awsAccessKey()));
        awsAccessKeyTextBox = new TextBox();
        userService.getPreference(AWS_ACCESS_KEY_USER_PREFERENCE, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(String result) {
                awsAccessKeyTextBox.setValue(result);
            }
        });
        awsAccessKeyTextBox.addValueChangeHandler(e->userService.setPreference(AWS_ACCESS_KEY_USER_PREFERENCE, awsAccessKeyTextBox.getValue(),
                new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
            @Override
            public void onSuccess(Void result) {
            }
        }));
        awsCredentialsGrid.setWidget(0, 1, awsAccessKeyTextBox);
        awsCredentialsGrid.setWidget(1, 0, new Label(stringMessages.awsSecret()));
        awsSecretPasswordTextBox = new PasswordTextBox();
        awsCredentialsGrid.setWidget(1, 1, awsSecretPasswordTextBox);
        regionsTable = new TableWrapperWithSingleSelectionAndFilter<String, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, /* enablePager */ false,
                /* entity identity comparator */ Optional.empty(), GWT.create(AdminConsoleTableResources.class),
                /* checkbox filter function */ Optional.empty(), /* filter label */ Optional.empty(),
                /* filter checkbox label */ null) {
            @Override
            protected Iterable<String> getSearchableStrings(String t) {
                return Collections.singleton(t);
            }
        };
        final SshKeyManagementPanel sshKeyManagementPanel = new SshKeyManagementPanel(stringMessages, userService,
                landscapeManagementService, tableResources, errorReporter, /* access key provider */ this, regionsTable.getSelectionModel());
        final CaptionPanel sshKeysCaptionPanel = new CaptionPanel(stringMessages.sshKeys());
        awsCredentialsAndSshKeys.add(sshKeysCaptionPanel);
        sshKeysCaptionPanel.add(sshKeyManagementPanel);
        regionsTable.addColumn(new TextColumn<String>() {
            @Override
            public String getValue(String s) {
                return s;
            }
        }, stringMessages.region(), new NaturalComparator());
        final CaptionPanel regionsCaptionPanel = new CaptionPanel(stringMessages.region());
        regionsCaptionPanel.add(regionsTable);
        add(regionsCaptionPanel);
        landscapeManagementService.getRegions(new AsyncCallback<ArrayList<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<String> regions) {
                regionsTable.refresh(regions);
            }
        });
        mongoEndpointsTable = new TableWrapperWithSingleSelectionAndFilter<MongoEndpointDTO, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, /* enablePager */ false,
                /* entity identity comparator */ Optional.empty(), GWT.create(AdminConsoleTableResources.class),
                /* checkbox filter function */ Optional.empty(), /* filter label */ Optional.empty(),
                /* filter checkbox label */ null) {
            @Override
            protected Iterable<String> getSearchableStrings(MongoEndpointDTO mongoEndpointDTO) {
                final Set<String> result = new HashSet<>();
                if (mongoEndpointDTO.getReplicaSetName() != null) {
                    result.add(mongoEndpointDTO.getReplicaSetName());
                }
                for (final Pair<String, Integer> hostnameAndPort : mongoEndpointDTO.getHostnamesAndPorts()) {
                    result.add(hostnameAndPort.getA());
                    result.add(hostnameAndPort.getB().toString());
                }
                return result;
            }
        };
        mongoEndpointsTable.addColumn(mongoEndpointDTO->mongoEndpointDTO.getReplicaSetName(), stringMessages.replicaSet());
        mongoEndpointsTable.addColumn(new TextColumn<MongoEndpointDTO>() {
            @Override
            public String getValue(MongoEndpointDTO mongoEndpointDTO) {
                return Util.joinStrings(",", Util.map(mongoEndpointDTO.getHostnamesAndPorts(), hostnameAndPort->hostnameAndPort.getA()+":"+hostnameAndPort.getB()));
            }
        }, stringMessages.hostname());
        final CaptionPanel mongoEndpointsCaptionPanel = new CaptionPanel(stringMessages.mongoEndpoints());
        final VerticalPanel mongoEndpointsVerticalPanel = new VerticalPanel();
        mongoEndpointsCaptionPanel.add(mongoEndpointsVerticalPanel);
        mongoEndpointsVerticalPanel.add(mongoEndpointsTable);
        mongoEndpointsBusy = new SimpleBusyIndicator();
        mongoEndpointsVerticalPanel.add(mongoEndpointsBusy);
        add(mongoEndpointsCaptionPanel);
        machineImagesTable = new TableWrapperWithSingleSelectionAndFilter<AmazonMachineImageDTO, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, /* enablePager */ false,
                /* entity identity comparator */ Optional.empty(), GWT.create(AdminConsoleTableResources.class),
                /* checkbox filter function */ Optional.empty(), /* filter label */ Optional.empty(),
                /* filter checkbox label */ null) {
            @Override
            protected Iterable<String> getSearchableStrings(AmazonMachineImageDTO t) {
                return Arrays.asList(t.getRegionId(), t.getId(), t.getName(), t.getState() );
            }
        };
        machineImagesTable.addColumn(object->object.getId(), stringMessages.id());
        machineImagesTable.addColumn(object->object.getRegionId(), stringMessages.region());
        machineImagesTable.addColumn(object->object.getName(), stringMessages.name());
        machineImagesTable.addColumn(object->object.getType(), stringMessages.imageType());
        machineImagesTable.addColumn(object->object.getState(), stringMessages.state());
        machineImagesTable.addColumn(new TextColumn<AmazonMachineImageDTO>() {
            @Override
            public String getValue(AmazonMachineImageDTO object) {
                return object.getCreationTimePoint().toString();
            }
        }, stringMessages.createdAt(), Comparator.nullsLast((t1, t2)->t1.getCreationTimePoint().compareTo(t2.getCreationTimePoint())));
        final ActionsColumn<AmazonMachineImageDTO, AmazonMachineImagesImagesBarCell> machineImagesActionColumn = new ActionsColumn<AmazonMachineImageDTO, AmazonMachineImagesImagesBarCell>(
                new AmazonMachineImagesImagesBarCell(stringMessages), /* permission checker */ (machineImage, action)->true);
        machineImagesActionColumn.addAction(AmazonMachineImagesImagesBarCell.ACTION_REMOVE, DefaultActions.DELETE, machineImageToRemove->removeMachineImage(stringMessages, machineImageToRemove));
        machineImagesActionColumn.addAction(AmazonMachineImagesImagesBarCell.ACTION_UPGRADE, machineImageToUpgrade->upgradeMachineImage(stringMessages, machineImageToUpgrade));
        machineImagesTable.addColumn(machineImagesActionColumn);
        final CaptionPanel machineImagesCaptionPanel = new CaptionPanel(stringMessages.machineImages());
        final VerticalPanel machineImagesVerticalPanel = new VerticalPanel();
        machineImagesCaptionPanel.add(machineImagesVerticalPanel);
        machineImagesVerticalPanel.add(machineImagesTable);
        machineImagesBusy = new SimpleBusyIndicator();
        machineImagesVerticalPanel.add(machineImagesBusy);
        add(machineImagesCaptionPanel);
        regionsTable.getSelectionModel().addSelectionChangeHandler(e->
        {
            sshKeyManagementPanel.showKeysInRegion(awsAccessKeyTextBox.getValue(), awsSecretPasswordTextBox.getValue(),
                        regionsTable.getSelectionModel().getSelectedObject());
            refreshMongoEndpointsTable();
            refreshMachineImagesTable();
        });
        // TODO SSH keys: allow user to provide pass phrase for the decryption of a private key only in the browser
        // TODO upon region selection show AppServer clusters, and upgradable AMIs in region
        // TODO try to identify archive servers
        // TODO support creating a new app server cluster
        // TODO support AMI upgrade
        // TODO support archive server upgrade
        // TODO support upgrading all app server instances in a region
        // TODO region could be a drop-down maybe, and it could remember its last selection
        // TODO upon region selection show RabbitMQ, and Central Reverse Proxy clusters in region
        // TODO support archiving and dismantling of an application server cluster
        // TODO support deploying a new app server process instance onto an existing app server host (multi-instance)
    }

    private void refreshMongoEndpointsTable() {
        mongoEndpointsTable.getDataProvider().getList().clear();
        if (regionsTable.getSelectionModel().getSelectedObject() != null) {
            mongoEndpointsBusy.setBusy(true);
            landscapeManagementService.getMongoEndpoints(awsAccessKeyTextBox.getValue(), awsSecretPasswordTextBox.getValue(),
                    regionsTable.getSelectionModel().getSelectedObject(), new AsyncCallback<ArrayList<MongoEndpointDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.getMessage());
                    mongoEndpointsBusy.setBusy(false);
                }
    
                @Override
                public void onSuccess(ArrayList<MongoEndpointDTO> mongoEndpointDTOs) {
                    mongoEndpointsTable.refresh(mongoEndpointDTOs);
                    mongoEndpointsBusy.setBusy(false);
                }
            });
        } else {
            mongoEndpointsTable.getDataProvider().getList().clear();
        }

    }
    
    private void refreshMachineImagesTable() {
        machineImagesTable.getDataProvider().getList().clear();
        if (regionsTable.getSelectionModel().getSelectedObject() != null) {
            machineImagesBusy.setBusy(true);
            landscapeManagementService.getAmazonMachineImages(awsAccessKeyTextBox.getValue(), awsSecretPasswordTextBox.getValue(),
                    regionsTable.getSelectionModel().getSelectedObject(), new AsyncCallback<ArrayList<AmazonMachineImageDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.getMessage());
                    machineImagesBusy.setBusy(false);
                }
    
                @Override
                public void onSuccess(ArrayList<AmazonMachineImageDTO> machineImagesDTOs) {
                    machineImagesTable.refresh(machineImagesDTOs);
                    machineImagesBusy.setBusy(false);
                }
            });
        } else {
            machineImagesTable.getDataProvider().getList().clear();
        }

    }
    
    private void upgradeMachineImage(final StringMessages stringMessages, final AmazonMachineImageDTO machineImageToUpgrade) {
        landscapeManagementService.upgradeAmazonMachineImage(awsAccessKeyTextBox.getValue(), awsSecretPasswordTextBox.getValue(),
                machineImageToUpgrade.getRegionId(), machineImageToUpgrade.getId(),
                new AsyncCallback<AmazonMachineImageDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(AmazonMachineImageDTO result) {
                refreshMachineImagesTable();
                Notification.notify(
                        stringMessages.successfullyUpgradedMachineImage(machineImageToUpgrade.getName(),
                                machineImageToUpgrade.getId(), machineImageToUpgrade.getRegionId(), result.getName()),
                        NotificationType.SUCCESS);
            }
        });
    }

    private void removeMachineImage(final StringMessages stringMessages, final AmazonMachineImageDTO machineImageToRemove) {
        if (Window.confirm(stringMessages.doYouReallyWantToRemoveMachineImage(machineImageToRemove.getName(), machineImageToRemove.getId(), machineImageToRemove.getRegionId()))) {
            landscapeManagementService.removeAmazonMachineImage(awsAccessKeyTextBox.getValue(), awsSecretPasswordTextBox.getValue(),
                    machineImageToRemove.getRegionId(), machineImageToRemove.getId(),
                    new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.getMessage());
                }
    
                @Override
                public void onSuccess(Void result) {
                    machineImagesTable.remove(machineImageToRemove);
                            Notification.notify(
                                    stringMessages.successfullyRemovedMachineImage(machineImageToRemove.getName(),
                                            machineImageToRemove.getId(), machineImageToRemove.getRegionId()),
                                    NotificationType.SUCCESS);
                        }
            });
        }
    }

    private LandscapeManagementWriteServiceAsync initAndRegisterLandscapeManagementService() {
        final LandscapeManagementWriteServiceAsync result = GWT.create(LandscapeManagementWriteService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) result,
                RemoteServiceMappingConstants.landscapeManagementServiceRemotePath, HEADER_FORWARD_TO_MASTER);
        return result;
    }

    @Override
    public String getAwsAccessKeyId() {
        return awsAccessKeyTextBox.getValue();
    }

    @Override
    public String getAwsSecret() {
        return awsSecretPasswordTextBox.getValue();
    }
}
