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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.AmazonMachineImageDTO;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.MongoScalingInstructionsDTO;
import com.sap.sailing.landscape.ui.shared.ProcessDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sailing.landscape.ui.shared.SailingAnalyticsProcessDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
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
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
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
public class LandscapeManagementPanel extends VerticalPanel {
    private final LandscapeManagementWriteServiceAsync landscapeManagementService;
    private final TableWrapperWithSingleSelectionAndFilter<String, StringMessages, AdminConsoleTableResources> regionsTable;
    private final TableWrapperWithSingleSelectionAndFilter<MongoEndpointDTO, StringMessages, AdminConsoleTableResources> mongoEndpointsTable;
    private final BusyIndicator mongoEndpointsBusy;
    private final TableWrapperWithSingleSelectionAndFilter<AmazonMachineImageDTO, StringMessages, AdminConsoleTableResources> machineImagesTable;
    private final BusyIndicator machineImagesBusy;
    private final SshKeyManagementPanel sshKeyManagementPanel;
    private final TableWrapperWithSingleSelectionAndFilter<SailingApplicationReplicaSetDTO<String>, StringMessages, AdminConsoleTableResources> applicationReplicaSetsTable;
    private final SimpleBusyIndicator applicationReplicaSetsBusy;
    private final ErrorReporter errorReporter;
    private final AwsMfaLoginWidget mfaLoginWidget;
    private final static String AWS_DEFAULT_REGION_USER_PREFERENCE = "aws.region.default";

    public LandscapeManagementPanel(StringMessages stringMessages, UserService userService,
            AdminConsoleTableResources tableResources, ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        landscapeManagementService = initAndRegisterLandscapeManagementService();
        final HorizontalPanel awsCredentialsAndSshKeys = new HorizontalPanel();
        add(awsCredentialsAndSshKeys);
        final CaptionPanel awsCredentialsPanel = new CaptionPanel(stringMessages.awsCredentials());
        awsCredentialsAndSshKeys.add(awsCredentialsPanel);
        mfaLoginWidget = new AwsMfaLoginWidget(landscapeManagementService, errorReporter, userService, stringMessages);
        mfaLoginWidget.addListener(validSession->refreshAllThatNeedsAwsCredentials());
        awsCredentialsPanel.add(mfaLoginWidget);
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
        sshKeyManagementPanel = new SshKeyManagementPanel(stringMessages, userService,
                landscapeManagementService, tableResources, errorReporter, /* access key provider */ mfaLoginWidget, regionsTable.getSelectionModel());
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
        refreshRegionsTable(userService);
        // MongoDB endpoints:
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
                for (final ProcessDTO hostnameAndPort : mongoEndpointDTO.getHostnamesAndPorts()) {
                    result.add(hostnameAndPort.getHost().getInstanceId());
                    result.add(hostnameAndPort.getHostname());
                    result.add(Integer.toString(hostnameAndPort.getPort()));
                }
                return result;
            }
        };
        mongoEndpointsTable.addColumn(mongoEndpointDTO->mongoEndpointDTO.getReplicaSetName(), stringMessages.replicaSet());
        mongoEndpointsTable.addColumn(new TextColumn<MongoEndpointDTO>() {
            @Override
            public String getValue(MongoEndpointDTO mongoEndpointDTO) {
                return Util.joinStrings(",",
                        Util.map(mongoEndpointDTO.getHostnamesAndPorts(),
                                hostnameAndPort -> hostnameAndPort.getHostname() + ":" + hostnameAndPort.getPort()
                                        + " (" + hostnameAndPort.getHost().getInstanceId() + ")"));
            }
        }, stringMessages.hostname());
        final ActionsColumn<MongoEndpointDTO, MongoEndpointsImagesBarCell> mongoEndpointsActionColumn = new ActionsColumn<MongoEndpointDTO, MongoEndpointsImagesBarCell>(
                new MongoEndpointsImagesBarCell(stringMessages), /* permission checker */ (mongoEndpoint, action)->true);
        mongoEndpointsActionColumn.addAction(MongoEndpointsImagesBarCell.ACTION_SCALE,
                mongoEndpointToScale -> scaleMongoEndpoint(stringMessages,
                        regionsTable.getSelectionModel().getSelectedObject(), mongoEndpointToScale));
        mongoEndpointsTable.addColumn(mongoEndpointsActionColumn);
        final CaptionPanel mongoEndpointsCaptionPanel = new CaptionPanel(stringMessages.mongoEndpoints());
        final VerticalPanel mongoEndpointsVerticalPanel = new VerticalPanel();
        mongoEndpointsCaptionPanel.add(mongoEndpointsVerticalPanel);
        mongoEndpointsVerticalPanel.add(mongoEndpointsTable);
        mongoEndpointsBusy = new SimpleBusyIndicator();
        mongoEndpointsVerticalPanel.add(mongoEndpointsBusy);
        add(mongoEndpointsCaptionPanel);
        // application replica sets:
        applicationReplicaSetsTable = new TableWrapperWithSingleSelectionAndFilter<SailingApplicationReplicaSetDTO<String>, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, /* enablePager */ false,
                /* entity identity comparator */ Optional.empty(), GWT.create(AdminConsoleTableResources.class),
                /* checkbox filter function */ Optional.empty(), /* filter label */ Optional.empty(),
                /* filter checkbox label */ null) {
            @Override
            protected Iterable<String> getSearchableStrings(SailingApplicationReplicaSetDTO<String> t) {
                final Set<String> result = new HashSet<>();
                result.add(t.getReplicaSetName());
                result.add(t.getMaster().getHostname());
                result.add(Integer.toString(t.getMaster().getPort()));
                result.add(t.getMaster().getServerName());
                result.add(t.getMaster().getHost().getInstanceId());
                for (final SailingAnalyticsProcessDTO replica : t.getReplicas()) {
                    result.add(replica.getHostname());
                    result.add(replica.getServerName());
                    result.add(replica.getHost().getInstanceId());
                }
                return result;
            }
        };
        applicationReplicaSetsTable.addColumn(rs->rs.getReplicaSetName(), stringMessages.name());
        applicationReplicaSetsTable.addColumn(rs->rs.getMaster().getHostname(), stringMessages.masterHostName());
        applicationReplicaSetsTable.addColumn(rs->Integer.toString(rs.getMaster().getPort()), stringMessages.masterPort());
        applicationReplicaSetsTable.addColumn(rs->rs.getMaster().getServerName(), stringMessages.masterServerName());
        applicationReplicaSetsTable.addColumn(rs->rs.getMaster().getHost().getInstanceId(), stringMessages.masterInstanceId());
        applicationReplicaSetsTable.addColumn(rs->Util.joinStrings(", ", Util.map(rs.getReplicas(), r->r.getHostname()+":"+r.getPort()+" ("+r.getServerName()+", "+r.getHost().getInstanceId()+")")),
                stringMessages.replicas());
        final CaptionPanel applicationReplicaSetsCaptionPanel = new CaptionPanel(stringMessages.applicationReplicaSets());
        final VerticalPanel applicationReplicaSetsVerticalPanel = new VerticalPanel();
        final Button applicationReplicaSetsRefreshButton = new Button(stringMessages.refresh());
        applicationReplicaSetsVerticalPanel.add(applicationReplicaSetsRefreshButton);
        applicationReplicaSetsRefreshButton.addClickHandler(e->refreshApplicationReplicaSetsTable());
        applicationReplicaSetsCaptionPanel.add(applicationReplicaSetsVerticalPanel);
        applicationReplicaSetsVerticalPanel.add(applicationReplicaSetsTable);
        applicationReplicaSetsBusy = new SimpleBusyIndicator();
        applicationReplicaSetsVerticalPanel.add(applicationReplicaSetsBusy);
        add(applicationReplicaSetsCaptionPanel);
        // machine images:
        machineImagesTable = new TableWrapperWithSingleSelectionAndFilter<AmazonMachineImageDTO, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, /* enablePager */ false,
                /* entity identity comparator */ Optional.empty(), GWT.create(AdminConsoleTableResources.class),
                /* checkbox filter function */ Optional.of(this::isNewest), /* filter label */ Optional.empty(),
                /* filter checkbox label */ stringMessages.showNewestOnlyPerType()) {
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
            final String selectedRegion = regionsTable.getSelectionModel().getSelectedObject();
            refreshAllThatNeedsAwsCredentials();
            storeRegionSelection(userService, selectedRegion);
        });
        // TODO upon region selection show AppServer clusters in region
        // TODO try to identify archive servers
        // TODO support creating a new app server cluster
        // TODO support archiving and dismantling of an application server cluster
        // TODO support deploying a new app server process instance onto an existing app server host (multi-instance)
        // TODO support archive server upgrade
        // TODO support upgrading all app server instances in a region
        // TODO region table should remember its last selection in user preferences
        // TODO upon region selection show RabbitMQ, and Central Reverse Proxy clusters in region
    }

    private void refreshRegionsTable(UserService userService) {
        landscapeManagementService.getRegions(new AsyncCallback<ArrayList<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<String> regions) {
                regionsTable.refresh(regions);
                userService.getPreference(AWS_DEFAULT_REGION_USER_PREFERENCE, new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Couldn't obtain "+AWS_DEFAULT_REGION_USER_PREFERENCE+" preference for user "+userService.getCurrentUser().getName());
                    }

                    @Override
                    public void onSuccess(String defaultRegion) {
                        regionsTable.getFilterPanel().search(defaultRegion);
                        regionsTable.getSelectionModel().setSelected(defaultRegion, /* selected */ true);
                    }
                });
            }
        });
    }
    
    private void refreshAllThatNeedsAwsCredentials() {
        refreshMongoEndpointsTable();
        refreshApplicationReplicaSetsTable();
        refreshMachineImagesTable();
        sshKeyManagementPanel.showKeysInRegion(mfaLoginWidget.hasValidSessionCredentials() ?
                regionsTable.getSelectionModel().getSelectedObject() : null);
    }

    private void storeRegionSelection(UserService userService, String selectedRegion) {
        if (selectedRegion != null) {
            userService.setPreference(AWS_DEFAULT_REGION_USER_PREFERENCE, selectedRegion, new AsyncCallback<Void>() {
                @Override public void onFailure(Throwable caught) {} @Override public void onSuccess(Void result) {}
            });
        }
    }

    private void scaleMongoEndpoint(StringMessages stringMessages, String selectedRegion, MongoEndpointDTO mongoEndpointToScale) {
        new MongoScalingDialog(mongoEndpointToScale, stringMessages, errorReporter, landscapeManagementService, new DialogCallback<MongoScalingInstructionsDTO>() {
            @Override
            public void ok(MongoScalingInstructionsDTO mongoScalingInstructions) {
                mongoEndpointsBusy.setBusy(true);
                landscapeManagementService.scaleMongo(selectedRegion, mongoScalingInstructions,
                    new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            mongoEndpointsBusy.setBusy(false);
                            errorReporter.reportError(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Void result) {
                            mongoEndpointsBusy.setBusy(false);
                            Notification.notify(stringMessages.successfullyScaledMongoDB(),
                                    NotificationType.SUCCESS);
                            refreshMongoEndpointsTable();
                        }
                    });
            }

            @Override
            public void cancel() {
            }
        }).show();
    }

    private boolean isNewest(AmazonMachineImageDTO ami) {
        final Comparator<TimePoint> timePointComparator = Comparator.nullsLast(Comparator.reverseOrder());
        final Comparator<AmazonMachineImageDTO> imageByTimePointComparator = (AmazonMachineImageDTO i1, AmazonMachineImageDTO i2)->
                timePointComparator.compare(i1.getCreationTimePoint(), i2.getCreationTimePoint());
        return ami.getCreationTimePoint().equals(
                machineImagesTable.getDataProvider().getList().stream().filter(imageFromTable->imageFromTable.getType().equals(ami.getType()))
                .sorted(imageByTimePointComparator)
                .findFirst().get().getCreationTimePoint());
    }

    private void refreshMongoEndpointsTable() {
        mongoEndpointsTable.getFilterPanel().removeAll();
        if (mfaLoginWidget.hasValidSessionCredentials() && regionsTable.getSelectionModel().getSelectedObject() != null) {
            mongoEndpointsBusy.setBusy(true);
            landscapeManagementService.getMongoEndpoints(regionsTable.getSelectionModel().getSelectedObject(), new AsyncCallback<ArrayList<MongoEndpointDTO>>() {
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
        }
    }
    
    private void refreshApplicationReplicaSetsTable() {
        applicationReplicaSetsTable.getFilterPanel().removeAll();
        if (mfaLoginWidget.hasValidSessionCredentials() && regionsTable.getSelectionModel().getSelectedObject() != null) {
            applicationReplicaSetsBusy.setBusy(true);
            final SSHKeyPairDTO selectedSshKeyPair = sshKeyManagementPanel.getSelectedKeyPair();
            landscapeManagementService.getApplicationReplicaSets(regionsTable.getSelectionModel().getSelectedObject(),
                    selectedSshKeyPair==null?null:selectedSshKeyPair.getName(),
                    sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption().getBytes(),
                    new AsyncCallback<ArrayList<SailingApplicationReplicaSetDTO<String>>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(caught.getMessage());
                            applicationReplicaSetsBusy.setBusy(false);
                        }

                        @Override
                        public void onSuccess(
                                ArrayList<SailingApplicationReplicaSetDTO<String>> applicationReplicaSetDTOs) {
                            applicationReplicaSetsTable.refresh(applicationReplicaSetDTOs);
                            applicationReplicaSetsBusy.setBusy(false);
                        }
                    });
        }
    }
    
    private void refreshMachineImagesTable() {
        machineImagesTable.getFilterPanel().removeAll();
        if (mfaLoginWidget.hasValidSessionCredentials() && regionsTable.getSelectionModel().getSelectedObject() != null) {
            machineImagesBusy.setBusy(true);
            landscapeManagementService.getAmazonMachineImages(regionsTable.getSelectionModel().getSelectedObject(), new AsyncCallback<ArrayList<AmazonMachineImageDTO>>() {
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
        }
    }
    
    private void upgradeMachineImage(final StringMessages stringMessages, final AmazonMachineImageDTO machineImageToUpgrade) {
        Notification.notify(stringMessages.startedImageUpgrade(machineImageToUpgrade.getName(), machineImageToUpgrade.getId(), machineImageToUpgrade.getRegionId()), NotificationType.INFO);
        machineImagesBusy.setBusy(true);
        landscapeManagementService.upgradeAmazonMachineImage(machineImageToUpgrade.getRegionId(), machineImageToUpgrade.getId(),
                new AsyncCallback<AmazonMachineImageDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
                machineImagesBusy.setBusy(false);
            }

            @Override
            public void onSuccess(AmazonMachineImageDTO result) {
                machineImagesBusy.setBusy(false);
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
            landscapeManagementService.removeAmazonMachineImage(machineImageToRemove.getRegionId(), machineImageToRemove.getId(),
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
}
