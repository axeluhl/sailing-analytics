package com.sap.sailing.landscape.ui.client;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.ui.client.CreateApplicationReplicaSetDialog.CreateApplicationReplicaSetInstructions;
import com.sap.sailing.landscape.ui.client.UpgradeApplicationReplicaSetDialog.UpgradeApplicationReplicaSetInstructions;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.AmazonMachineImageDTO;
import com.sap.sailing.landscape.ui.shared.MongoEndpointDTO;
import com.sap.sailing.landscape.ui.shared.MongoScalingInstructionsDTO;
import com.sap.sailing.landscape.ui.shared.ProcessDTO;
import com.sap.sailing.landscape.ui.shared.RedirectDTO;
import com.sap.sailing.landscape.ui.shared.ReleaseDTO;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sailing.landscape.ui.shared.SailingAnalyticsProcessDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sailing.landscape.ui.shared.SharedLandscapeConstants;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.ActionsColumn;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.TableWrapperWithMultiSelectionAndFilter;
import com.sap.sse.gwt.client.celltable.TableWrapperWithSingleSelectionAndFilter;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.SelectedElementsCountingButton;

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
public class LandscapeManagementPanel extends SimplePanel {
    private final LandscapeManagementWriteServiceAsync landscapeManagementService;
    private final TableWrapperWithSingleSelectionAndFilter<String, StringMessages, AdminConsoleTableResources> regionsTable;
    private final TableWrapperWithSingleSelectionAndFilter<MongoEndpointDTO, StringMessages, AdminConsoleTableResources> mongoEndpointsTable;
    private final BusyIndicator mongoEndpointsBusy;
    private final TableWrapperWithSingleSelectionAndFilter<AmazonMachineImageDTO, StringMessages, AdminConsoleTableResources> machineImagesTable;
    private final BusyIndicator machineImagesBusy;
    private final SshKeyManagementPanel sshKeyManagementPanel;
    private final TableWrapperWithMultiSelectionAndFilter<SailingApplicationReplicaSetDTO<String>, StringMessages, AdminConsoleTableResources> applicationReplicaSetsTable;
    private final SimpleBusyIndicator applicationReplicaSetsBusy;
    private final ErrorReporter errorReporter;
    private final AwsMfaLoginWidget mfaLoginWidget;
    private final static String AWS_DEFAULT_REGION_USER_PREFERENCE = "aws.region.default";
    private final static Duration DURATION_TO_WAIT_BETWEEN_REPLICA_SET_UPGRADE_REQUESTS = Duration.ONE_MINUTE;
    
    /**
     * The time to wait after archiving a replica set and before starting a "compare servers" run for the content
     * archived. This waiting period is owed to the process of loading the race content which is an asynchronous
     * process running mainly in the "background" and after the master data import has reported "completion."
     */
    private static final Duration DEFAULT_DURATION_TO_WAIT_BEFORE_COMPARE_SERVERS = Duration.ONE_MINUTE.times(5);
    private static final int DEFAULT_NUMBER_OF_COMPARE_SERVERS_ATTEMPTS = 3;

    public LandscapeManagementPanel(StringMessages stringMessages, UserService userService,
            AdminConsoleTableResources tableResources, ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        landscapeManagementService = initAndRegisterLandscapeManagementService();
        final VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        this.add(mainPanel);
        final HorizontalPanel awsCredentialsAndSshKeys = new HorizontalPanel();
        mainPanel.add(awsCredentialsAndSshKeys);
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
        mainPanel.add(regionsCaptionPanel);
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
        mongoEndpointsTable.addColumn(mongoEndpointsActionColumn, stringMessages.actions());
        final CaptionPanel mongoEndpointsCaptionPanel = new CaptionPanel(stringMessages.mongoEndpoints());
        final VerticalPanel mongoEndpointsVerticalPanel = new VerticalPanel();
        mongoEndpointsCaptionPanel.add(mongoEndpointsVerticalPanel);
        mongoEndpointsVerticalPanel.add(mongoEndpointsTable);
        mongoEndpointsBusy = new SimpleBusyIndicator();
        mongoEndpointsVerticalPanel.add(mongoEndpointsBusy);
        mainPanel.add(mongoEndpointsCaptionPanel);
        // application replica sets:
        applicationReplicaSetsTable = new TableWrapperWithMultiSelectionAndFilter<SailingApplicationReplicaSetDTO<String>, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, /* enablePager */ false,
                /* entity identity comparator */ Optional.of(new EntityIdentityComparator<SailingApplicationReplicaSetDTO<String>>() {
                    @Override
                    public boolean representSameEntity(SailingApplicationReplicaSetDTO<String> dto1,
                            SailingApplicationReplicaSetDTO<String> dto2) {
                        return dto1.getName().equals(dto2.getName());
                    }

                    @Override
                    public int hashCode(SailingApplicationReplicaSetDTO<String> t) {
                        return t.getName().hashCode();
                    }
                }), GWT.create(AdminConsoleTableResources.class),
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
        applicationReplicaSetsTable.addColumn(rs->rs.getVersion(), stringMessages.versionHeader());
        applicationReplicaSetsTable.addColumn(rs->rs.getMaster().getHost().getPublicIpAddress(), stringMessages.masterHostName());
        applicationReplicaSetsTable.addColumn(rs->Integer.toString(rs.getMaster().getPort()), stringMessages.masterPort());
        applicationReplicaSetsTable.addColumn(rs->rs.getHostname(), stringMessages.hostname());
        applicationReplicaSetsTable.addColumn(rs->rs.getMaster().getHost().getInstanceId(), stringMessages.masterInstanceId());
        applicationReplicaSetsTable.addColumn(rs->""+rs.getMaster().getStartTimePoint(), stringMessages.startTimePoint(),
                (rs1, rs2)->rs1.getMaster().getStartTimePoint().compareTo(rs2.getMaster().getStartTimePoint()));
        applicationReplicaSetsTable.addColumn(rs->Util.joinStrings(", ", Util.map(rs.getReplicas(),
                r->r.getHost().getPublicIpAddress()+":"+r.getPort()+" ("+r.getServerName()+", "+r.getHost().getInstanceId()+")")),
                stringMessages.replicas());
        applicationReplicaSetsTable.addColumn(rs->rs.getDefaultRedirectPath(), stringMessages.defaultRedirectPath());
        final ActionsColumn<SailingApplicationReplicaSetDTO<String>, ApplicationReplicaSetsImagesBarCell> applicationReplicaSetsActionColumn = new ActionsColumn<SailingApplicationReplicaSetDTO<String>, ApplicationReplicaSetsImagesBarCell>(
                new ApplicationReplicaSetsImagesBarCell(stringMessages), /* permission checker */ (applicationReplicaSet, action)->true);
        applicationReplicaSetsActionColumn.addAction(ApplicationReplicaSetsImagesBarCell.ACTION_ARCHIVE,
                applicationReplicaSetToArchive -> archiveApplicationReplicaSet(stringMessages,
                        regionsTable.getSelectionModel().getSelectedObject(), applicationReplicaSetToArchive));
        applicationReplicaSetsActionColumn.addAction(ApplicationReplicaSetsImagesBarCell.ACTION_UPGRADE,
                applicationReplicaSetToUpgrade -> upgradeApplicationReplicaSet(stringMessages,
                        regionsTable.getSelectionModel().getSelectedObject(), Collections.singleton(applicationReplicaSetToUpgrade)));
        applicationReplicaSetsActionColumn.addAction(ApplicationReplicaSetsImagesBarCell.ACTION_DEFINE_LANDING_PAGE,
                applicationReplicaSetForWhichToDefineLandingPage -> defineLandingPage(stringMessages,
                        regionsTable.getSelectionModel().getSelectedObject(), applicationReplicaSetForWhichToDefineLandingPage));
        applicationReplicaSetsActionColumn.addAction(ApplicationReplicaSetsImagesBarCell.ACTION_CREATE_LOAD_BALANCER_MAPPING,
                applicationReplicaSetForWhichToDefineLoadBalancerMapping -> createDefaultLoadBalancerMappings(stringMessages,
                        regionsTable.getSelectionModel().getSelectedObject(), applicationReplicaSetForWhichToDefineLoadBalancerMapping));
        applicationReplicaSetsActionColumn.addAction(ApplicationReplicaSetsImagesBarCell.ACTION_REMOVE,
                applicationReplicaSetToRemove -> removeApplicationReplicaSet(stringMessages,
                        regionsTable.getSelectionModel().getSelectedObject(), applicationReplicaSetToRemove));
        applicationReplicaSetsTable.addColumn(applicationReplicaSetsActionColumn, stringMessages.actions());
        final CaptionPanel applicationReplicaSetsCaptionPanel = new CaptionPanel(stringMessages.applicationReplicaSets());
        final VerticalPanel applicationReplicaSetsVerticalPanel = new VerticalPanel();
        final HorizontalPanel applicationReplicaSetsButtonPanel = new HorizontalPanel();
        applicationReplicaSetsVerticalPanel.add(applicationReplicaSetsButtonPanel);
        final Button applicationReplicaSetsRefreshButton = new Button(stringMessages.refresh());
        applicationReplicaSetsButtonPanel.add(applicationReplicaSetsRefreshButton);
        applicationReplicaSetsRefreshButton.addClickHandler(e->refreshApplicationReplicaSetsTable());
        final Button addApplicationReplicaSetButton = new Button(stringMessages.add());
        applicationReplicaSetsButtonPanel.add(addApplicationReplicaSetButton);
        addApplicationReplicaSetButton.addClickHandler(e->createApplicationReplicaSet(stringMessages, regionsTable.getSelectionModel().getSelectedObject()));
        final SelectedElementsCountingButton<SailingApplicationReplicaSetDTO<String>> upgradeApplicationReplicaSetButton = new SelectedElementsCountingButton<>(
                stringMessages.upgrade(), applicationReplicaSetsTable.getSelectionModel(),
                e->upgradeApplicationReplicaSet(stringMessages, regionsTable.getSelectionModel().getSelectedObject(),
                        applicationReplicaSetsTable.getSelectionModel().getSelectedSet()));
        applicationReplicaSetsButtonPanel.add(upgradeApplicationReplicaSetButton);
        applicationReplicaSetsCaptionPanel.add(applicationReplicaSetsVerticalPanel);
        applicationReplicaSetsVerticalPanel.add(applicationReplicaSetsTable);
        applicationReplicaSetsBusy = new SimpleBusyIndicator();
        applicationReplicaSetsVerticalPanel.add(applicationReplicaSetsBusy);
        mainPanel.add(applicationReplicaSetsCaptionPanel);
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
        machineImagesTable.addColumn(machineImagesActionColumn, stringMessages.actions());
        final CaptionPanel machineImagesCaptionPanel = new CaptionPanel(stringMessages.machineImages());
        final VerticalPanel machineImagesVerticalPanel = new VerticalPanel();
        machineImagesCaptionPanel.add(machineImagesVerticalPanel);
        machineImagesVerticalPanel.add(machineImagesTable);
        machineImagesBusy = new SimpleBusyIndicator();
        machineImagesVerticalPanel.add(machineImagesBusy);
        mainPanel.add(machineImagesCaptionPanel);
        regionsTable.getSelectionModel().addSelectionChangeHandler(e->
        {
            final String selectedRegion = regionsTable.getSelectionModel().getSelectedObject();
            refreshAllThatNeedsAwsCredentials();
            storeRegionSelection(userService, selectedRegion);
        });
        // TODO try to identify archive servers
        // TODO support archiving of an application server cluster
        // TODO support deploying a new app server process instance onto an existing app server host (multi-instance)
        // TODO support archive server upgrade
        // TODO support upgrading all app server instances in a region
        // TODO upon region selection show RabbitMQ, and Central Reverse Proxy clusters in region
    }

    private void defineLandingPage(StringMessages stringMessages, String selectedRegion,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToDefineLandingPageFor) {
        if (sshKeyManagementPanel.getSelectedKeyPair() == null) {
            Notification.notify(stringMessages.pleaseSelectSshKeyPair(), NotificationType.INFO);
        } else {
            new DefineRedirectDialog(applicationReplicaSetToDefineLandingPageFor, stringMessages, errorReporter, landscapeManagementService, new DialogCallback<RedirectDTO>() {
                @Override
                public void ok(final RedirectDTO redirect) {
                    applicationReplicaSetsBusy.setBusy(true);
                    landscapeManagementService.defineDefaultRedirect(selectedRegion, applicationReplicaSetToDefineLandingPageFor.getHostname(),
                            redirect, sshKeyManagementPanel.getSelectedKeyPair().getName(),
                            sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption(),
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                applicationReplicaSetsBusy.setBusy(false);
                                errorReporter.reportError(caught.getMessage());
                            }
    
                            @Override
                            public void onSuccess(Void result) {
                                applicationReplicaSetsBusy.setBusy(false);
                                final String newDefaultRedirect = RedirectDTO.toString(redirect.getPath(), redirect.getQuery());
                                applicationReplicaSetsTable.getFilterPanel().remove(applicationReplicaSetToDefineLandingPageFor);
                                applicationReplicaSetsTable.getFilterPanel().add(new SailingApplicationReplicaSetDTO<String>(
                                        applicationReplicaSetToDefineLandingPageFor.getReplicaSetName(),
                                        applicationReplicaSetToDefineLandingPageFor.getMaster(),
                                        applicationReplicaSetToDefineLandingPageFor.getReplicas(),
                                        applicationReplicaSetToDefineLandingPageFor.getVersion(),
                                        applicationReplicaSetToDefineLandingPageFor.getHostname(),
                                        newDefaultRedirect));
                                Notification.notify(stringMessages.successfullyUpdatedLandingPage(), NotificationType.SUCCESS);
                            }
                        });
                }
    
                @Override
                public void cancel() {
                }
            }).show();
        }
    }

    private void createApplicationReplicaSet(StringMessages stringMessages, String regionId) {
        landscapeManagementService.getReleases(new AsyncCallback<ArrayList<ReleaseDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<ReleaseDTO> result) {
                new CreateApplicationReplicaSetDialog(landscapeManagementService, result.stream().map(r->r.getName())::iterator,
                        stringMessages, errorReporter, new DialogCallback<CreateApplicationReplicaSetDialog.CreateApplicationReplicaSetInstructions>() {
                    @Override
                    public void ok(CreateApplicationReplicaSetInstructions instructions) {
                        applicationReplicaSetsBusy.setBusy(true);
                        landscapeManagementService.createApplicationReplicaSet(regionId, instructions.getName(), instructions.getInstanceType(),
                                instructions.isDynamicLoadBalancerMapping(), instructions.getReleaseNameOrNullForLatestMaster(),
                                        sshKeyManagementPanel.getSelectedKeyPair()==null?null:sshKeyManagementPanel.getSelectedKeyPair().getName(),
                                                sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption() != null
                                                ? sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption().getBytes() : null,
                                                instructions.getMasterReplicationBearerToken(), instructions.getReplicaReplicationBearerToken(),
                                                instructions.getOptionalDomainName(),
                                                new AsyncCallback<SailingApplicationReplicaSetDTO<String>>() {
                                 @Override
                                 public void onFailure(Throwable caught) {
                                    applicationReplicaSetsBusy.setBusy(false);
                                    errorReporter.reportError(caught.getMessage());
                                 }
                                 
                                 @Override
                                 public void onSuccess(SailingApplicationReplicaSetDTO<String> result) {
                                    applicationReplicaSetsBusy.setBusy(false);
                                    Notification.notify(stringMessages.successfullyCreatedReplicaSet(instructions.getName()), NotificationType.SUCCESS);
                                    if (result != null) {
                                        applicationReplicaSetsTable.getFilterPanel().add(result);
                                    }
                                 }
                              });
                    }
                    
                    @Override
                    public void cancel() {
                    }
                }).show();
            }
        });
    }

    private void removeApplicationReplicaSet(StringMessages stringMessages, String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToRemove) {
        if (Window.confirm(stringMessages.reallyRemoveApplicationReplicaSet(applicationReplicaSetToRemove.getName()))) {
            applicationReplicaSetsBusy.setBusy(true);
            landscapeManagementService.removeApplicationReplicaSet(regionId, applicationReplicaSetToRemove,
                    sshKeyManagementPanel.getSelectedKeyPair()==null?null:sshKeyManagementPanel.getSelectedKeyPair().getName(),
                            sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption() != null
                            ? sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption().getBytes() : null, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    applicationReplicaSetsBusy.setBusy(false);
                    errorReporter.reportError(caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    applicationReplicaSetsBusy.setBusy(false);
                    applicationReplicaSetsTable.getFilterPanel().remove(applicationReplicaSetToRemove);
                }
            });
        }
    }
    
    private static class ReplicaSetArchivingParameters {
        private final String bearerTokenOrNullForApplicationReplicaSetToArchive;
        private final String bearerTokenOrNullForArchive;
        private final boolean removeApplicationReplicaSet;
        private final Duration durationToWaitBeforeAndBetweenCompareServerAttempts;
        private final int numberOfTimesToTryCompareServers;
        public ReplicaSetArchivingParameters(String bearerTokenOrNullForApplicationReplicaSetToArchive,
                String bearerTokenOrNullForArchive, boolean removeApplicationReplicaSet,
                Duration durationToWaitBeforeAndBetweenCompareServerAttempts,
                int numberOfTimesToTryCompareServers) {
            super();
            this.bearerTokenOrNullForApplicationReplicaSetToArchive = bearerTokenOrNullForApplicationReplicaSetToArchive;
            this.bearerTokenOrNullForArchive = bearerTokenOrNullForArchive;
            this.removeApplicationReplicaSet = removeApplicationReplicaSet;
            this.durationToWaitBeforeAndBetweenCompareServerAttempts = durationToWaitBeforeAndBetweenCompareServerAttempts;
            this.numberOfTimesToTryCompareServers = numberOfTimesToTryCompareServers;
        }
        public String getBearerTokenOrNullForApplicationReplicaSetToArchive() {
            return bearerTokenOrNullForApplicationReplicaSetToArchive;
        }
        public String getBearerTokenOrNullForArchive() {
            return bearerTokenOrNullForArchive;
        }
        public boolean isRemoveApplicationReplicaSet() {
            return removeApplicationReplicaSet;
        }
        public Duration getDurationToWaitBeforeAndBetweenCompareServerAttempts() {
            return durationToWaitBeforeAndBetweenCompareServerAttempts;
        }
        public int getNumberOfTimesToTryCompareServers() {
            return numberOfTimesToTryCompareServers;
        }
    }

    private void archiveApplicationReplicaSet(StringMessages stringMessages, String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToArchive) {
        final MongoEndpointDTO selectedMongoEndpointForDBArchiving = mongoEndpointsTable.getSelectionModel().getSelectedObject();
        new DataEntryDialog<ReplicaSetArchivingParameters>(stringMessages.createLoadBalancerMapping(), stringMessages.createLoadBalancerMapping(),
                stringMessages.ok(), stringMessages.cancel(), /* validator */ null, new DialogCallback<ReplicaSetArchivingParameters>() {
                    @Override
                    public void ok(ReplicaSetArchivingParameters bearerTokensAndWhetherToRemoveReplicaSet) {
                        applicationReplicaSetsBusy.setBusy(true);
                        landscapeManagementService.archiveReplicaSet(regionId, applicationReplicaSetToArchive,
                                bearerTokensAndWhetherToRemoveReplicaSet.getBearerTokenOrNullForApplicationReplicaSetToArchive(),
                                bearerTokensAndWhetherToRemoveReplicaSet.getBearerTokenOrNullForArchive(),
                                bearerTokensAndWhetherToRemoveReplicaSet.getDurationToWaitBeforeAndBetweenCompareServerAttempts(),
                                bearerTokensAndWhetherToRemoveReplicaSet.getNumberOfTimesToTryCompareServers(),
                                bearerTokensAndWhetherToRemoveReplicaSet.isRemoveApplicationReplicaSet(),
                                selectedMongoEndpointForDBArchiving, sshKeyManagementPanel.getSelectedKeyPair().getName(),
                                sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption() != null
                                    ? sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption().getBytes() : null,
                                new AsyncCallback<UUID>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                applicationReplicaSetsBusy.setBusy(false);
                                errorReporter.reportError(caught.getMessage());
                            }

                            @Override
                            public void onSuccess(UUID progressId) {
                                // TODO try to follow the progress and trigger removing the replica set from the table when complete
                                applicationReplicaSetsBusy.setBusy(false);
                                if (bearerTokensAndWhetherToRemoveReplicaSet.isRemoveApplicationReplicaSet()) {
                                    applicationReplicaSetsTable.getFilterPanel().remove(applicationReplicaSetToArchive);
                                }
                                Notification.notify(stringMessages.successfullyArchivedReplicaSet(
                                        applicationReplicaSetToArchive.getName()), NotificationType.SUCCESS);
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                    }
            
        }) {
            private final TextBox bearerTokenOrNullForApplicationReplicaSetToArchiveBox = createTextBox("");
            private final TextBox bearerTokenOrNullForArchiveBox = createTextBox("");
            private final IntegerBox numberOfMinutesBeforeAndBetweenCompareServersBox = createIntegerBox((int) DEFAULT_DURATION_TO_WAIT_BEFORE_COMPARE_SERVERS.asMinutes(), /* visible length */ 3);
            private final IntegerBox numberOfCompareServersAttemptsBox = createIntegerBox(DEFAULT_NUMBER_OF_COMPARE_SERVERS_ATTEMPTS, /* visible length */ 3);
            private final CheckBox removeReplicaSetBox = createCheckbox(stringMessages.removeArchivedReplicaSet());
            
            @Override
            protected Widget getAdditionalWidget() {
                final Grid result = new Grid(5, 2);
                int row=0;
                result.setWidget(row, 0, new Label(stringMessages.bearerTokenOrNullForApplicationReplicaSetToArchive(applicationReplicaSetToArchive.getName())));
                result.setWidget(row++, 1, bearerTokenOrNullForApplicationReplicaSetToArchiveBox);
                result.setWidget(row, 0, new Label(stringMessages.bearerTokenOrNullForArchive()));
                result.setWidget(row++, 1, bearerTokenOrNullForArchiveBox);
                result.setWidget(row, 0, new Label(stringMessages.numberOfMinutesBeforeAndBetweenCompareServers()));
                result.setWidget(row++, 1, numberOfMinutesBeforeAndBetweenCompareServersBox);
                result.setWidget(row, 0, new Label(stringMessages.numberOfCompareServersAttempts()));
                result.setWidget(row++, 1, numberOfCompareServersAttemptsBox);
                result.setWidget(row++, 0, removeReplicaSetBox);
                return result;
            }

            @Override
            protected FocusWidget getInitialFocusWidget() {
                return bearerTokenOrNullForApplicationReplicaSetToArchiveBox;
            }

            @Override
            protected ReplicaSetArchivingParameters getResult() {
                return new ReplicaSetArchivingParameters(
                        Util.hasLength(bearerTokenOrNullForApplicationReplicaSetToArchiveBox.getValue())
                                ? bearerTokenOrNullForApplicationReplicaSetToArchiveBox.getValue()
                                : null,
                        Util.hasLength(bearerTokenOrNullForArchiveBox.getValue())
                                ? bearerTokenOrNullForArchiveBox.getValue()
                                : null,
                        removeReplicaSetBox.getValue(),
                        numberOfMinutesBeforeAndBetweenCompareServersBox.getValue() == null
                                ? DEFAULT_DURATION_TO_WAIT_BEFORE_COMPARE_SERVERS
                                : Duration.ONE_MINUTE
                                        .times(numberOfMinutesBeforeAndBetweenCompareServersBox.getValue()),
                        numberOfCompareServersAttemptsBox.getValue() == null
                                ? DEFAULT_NUMBER_OF_COMPARE_SERVERS_ATTEMPTS
                                : numberOfCompareServersAttemptsBox.getValue());
            }
        }.show();
    }
    
    private void createDefaultLoadBalancerMappings(final StringMessages stringMessages, String regionId,
            SailingApplicationReplicaSetDTO<String> applicationReplicaSetToCreateLoadBalancerMappingFor) {
        new DataEntryDialog<Triple<Boolean, String, Boolean>>(stringMessages.createLoadBalancerMapping(), stringMessages.createLoadBalancerMapping(),
                stringMessages.ok(), stringMessages.cancel(), /* validator */ null, new DialogCallback<Triple<Boolean, String, Boolean>>() {
                    @Override
                    public void ok(Triple<Boolean, String, Boolean> useDynamicLoadBalancerAndOptionalDomainNameAndForceDNSUpdate) {
                        applicationReplicaSetsBusy.setBusy(true);
                        landscapeManagementService.createDefaultLoadBalancerMappings(regionId,
                                applicationReplicaSetToCreateLoadBalancerMappingFor,
                                useDynamicLoadBalancerAndOptionalDomainNameAndForceDNSUpdate.getA(),
                                useDynamicLoadBalancerAndOptionalDomainNameAndForceDNSUpdate.getB(),
                                useDynamicLoadBalancerAndOptionalDomainNameAndForceDNSUpdate.getC(),
                                new AsyncCallback<SailingApplicationReplicaSetDTO<String>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                applicationReplicaSetsBusy.setBusy(false);
                                errorReporter.reportError(caught.getMessage());
                            }

                            @Override
                            public void onSuccess(SailingApplicationReplicaSetDTO<String> result) {
                                applicationReplicaSetsBusy.setBusy(false);
                                applicationReplicaSetsTable.getFilterPanel().remove(applicationReplicaSetToCreateLoadBalancerMappingFor);
                                applicationReplicaSetsTable.getFilterPanel().add(result);
                                Notification.notify(stringMessages.successfullyCreatedLoadBalancerMappingFor(
                                        applicationReplicaSetToCreateLoadBalancerMappingFor.getName()), NotificationType.SUCCESS);
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                    }
            
        }) {
            private final CheckBox useDynamicLoadBalancerCheckbox = createCheckbox(stringMessages.useDynamicLoadBalancer());
            private final TextBox optionalDomainNameBox = createTextBox(SharedLandscapeConstants.DEFAULT_DOMAIN_NAME);
            private final CheckBox forceDNSUpdateCheckbox = createCheckbox(stringMessages.forceDNSUpdate());
            
            @Override
            protected Widget getAdditionalWidget() {
                final VerticalPanel result = new VerticalPanel();
                result.add(useDynamicLoadBalancerCheckbox);
                final HorizontalPanel domainNamePanel = new HorizontalPanel();
                result.add(domainNamePanel);
                domainNamePanel.add(new Label(stringMessages.domainName()));
                domainNamePanel.add(optionalDomainNameBox);
                result.add(forceDNSUpdateCheckbox);
                return result;
            }

            @Override
            protected FocusWidget getInitialFocusWidget() {
                return useDynamicLoadBalancerCheckbox;
            }

            @Override
            protected Triple<Boolean, String, Boolean> getResult() {
                return new Triple<>(useDynamicLoadBalancerCheckbox.getValue(),
                        Util.hasLength(optionalDomainNameBox.getValue())?optionalDomainNameBox.getValue():null,
                        forceDNSUpdateCheckbox.getValue());
            }
        }.show();
    }

    /**
     * Performs an in-place upgrade for the master service if the replica set has distinct public and master
     * target groups. If no replica exists, one is launched with the master's release, and the method waits
     * until the replica has reached its healthy state. The replica is then registered in the public target group.<p>
     * 
     * Then, the {@code ./refreshInstance.sh install-release <release>} command is sent to the master which will
     * download and unpack the new release but will not yet stop the master process. In parallel, an existing
     * launch configuration will be copied and updated with user data reflecting the new release to be used.
     * An existing auto-scaling group will then be updated to use the new launch configuration. The old launch
     * configuration will then be removed.<p>
     * 
     * Replication is then stopped for all existing replicas, then the master is de-registered from the master
     * target group and the public target group, effectively making the replica set "read-only." Then, the {@code ./stop}
     * command is issued which is expected to wait until all process resources have been released so that it's
     * appropriate to call {@code ./start} just after the {@code ./stop} call has returned, thus spinning up the
     * master process with the new release configuration.<p>
     * 
     * When the master process has reached its healthy state, it is registered with both target groups while all other
     * replicas are de-registered and then stopped. For replica processes being the last on their host, the host will
     * be terminated. It is up to an auto-scaling group or to the user to decide whether to launch new replicas again.
     * This won't happen automatically by this procedure.
     */
    private void upgradeApplicationReplicaSet(StringMessages stringMessages, String regionId,
            Iterable<SailingApplicationReplicaSetDTO<String>> replicaSets) {
        landscapeManagementService.getReleases(new AsyncCallback<ArrayList<ReleaseDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<ReleaseDTO> result) {
                new UpgradeApplicationReplicaSetDialog(landscapeManagementService, result.stream().map(r->r.getName())::iterator,
                        stringMessages, errorReporter, new DialogCallback<UpgradeApplicationReplicaSetDialog.UpgradeApplicationReplicaSetInstructions>() {
                            @Override
                            public void ok(UpgradeApplicationReplicaSetInstructions upgradeInstructions) {
                                applicationReplicaSetsBusy.setBusy(true);
                                final int[] howManyMoreToGo = new int[] { Util.size(replicaSets) };
                                Duration timeToWaitUntilUpgradingNextReplicaSet = Duration.NULL;
                                for (final SailingApplicationReplicaSetDTO<String> replicaSet : replicaSets) {
                                    new Timer() {
                                        @Override
                                        public void run() {
                                            landscapeManagementService.upgradeApplicationReplicaSet(regionId, replicaSet,
                                                    upgradeInstructions.getReleaseNameOrNullForLatestMaster(),
                                                    sshKeyManagementPanel.getSelectedKeyPair()==null?null:sshKeyManagementPanel.getSelectedKeyPair().getName(),
                                                            sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption() != null
                                                            ? sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption().getBytes() : null,
                                                    upgradeInstructions.getReplicaReplicationBearerToken(),
                                                    new AsyncCallback<SailingApplicationReplicaSetDTO<String>>() {
                                                        @Override
                                                        public void onFailure(Throwable caught) {
                                                            if (--howManyMoreToGo[0] == 0) {
                                                                applicationReplicaSetsBusy.setBusy(false);
                                                            }
                                                            errorReporter.reportError(caught.getMessage());
                                                        }
            
                                                        @Override
                                                        public void onSuccess(SailingApplicationReplicaSetDTO<String> result) {
                                                            if (--howManyMoreToGo[0] == 0) {
                                                                applicationReplicaSetsBusy.setBusy(false);
                                                            }
                                                            if (result != null) {
                                                                Notification.notify(stringMessages.successfullyUpgradedApplicationReplicaSet(
                                                                                result.getName(), result.getVersion()), NotificationType.SUCCESS);
                                                                applicationReplicaSetsTable.getFilterPanel().remove(replicaSet);
                                                                applicationReplicaSetsTable.getFilterPanel().add(result);
                                                            } else {
                                                                Notification.notify(stringMessages.upgradingApplicationReplicaSetFailed(replicaSet.getName()),
                                                                        NotificationType.ERROR);
                                                            }
                                                        }
                                                    });
                                        }
                                    }.schedule((int) timeToWaitUntilUpgradingNextReplicaSet.asMillis());
                                    timeToWaitUntilUpgradingNextReplicaSet = timeToWaitUntilUpgradingNextReplicaSet.plus(
                                            DURATION_TO_WAIT_BETWEEN_REPLICA_SET_UPGRADE_REQUESTS);
                                }
                            }

                            @Override
                            public void cancel() {
                            }
                }).show();
            }
        });
        /*
         * Distinguish the following cases:
         *  1) with auto-scaling group: we assume two target groups exist; ensure a replica is running, launch one
         *    if none is running currently by increasing the minimum number of replicas in the auto-scaling group
         *    to "1" and wait for it to become healthy in the public target group. When at least one replica is
         *    healthy, stop replication on all of them using /replication/replication?action=STOP_REPLICATING.
         *    Then update master in place, issuing a "./refreshInstance.sh install-release {release}; ./stop; sleep 5; ./start"
         *    sequence. Wait for the master to become healthy, then add to the master and public target group,
         *    remove replicas from public target group, create a new launch configuration for the new release
         *    and update the auto-scaling group with it; re-adjust the minimum number of instances to its old value,
         *    then terminate all replicas running the old release.
         *  2) without auto-scaling group and without replication, but distinct master/public target groups: ensure
         *    that at least one healthy replica is registered in public target group; if not, spin one up and wait until
         *    it's healthy. Then stop replication using the new replication API.
         *    Then remove the master process from the master target group (making the replica set "read-only").
         *    Upgrade master in place using refreshInstance.sh install-release {release}. After the new master has become
         *    healthy, register it again with the
         *    master and public target groups while de-registering all replicas from the public target group, then
         *    stopping the old process and subsequently terminating its instance in case it was the last application
         *    process on that host.
         *  3) without auto-scaling group, without replication, and with only a single target group: the master couldn't
         *    be left reachable as we would not be able to distinguish between "read" and "write" calls without
         *    separate target groups and their routing rules. Therefore, this scenario should be reduced to scenario 2)
         *    by first establishing two separate target groups (if naming conventions match, use the existing target
         *    group as the public target group and only create the separate master target group) and making sure the
         *    old master is registered with the (potentially new) public target group. Then proceed as in 2).
         * 
         * Both, for 2) and 3), we'll need a way to identify a host/instance that qualifies for hosting the new master.
         * This brings us to the topic of allowing the user / landscape manager to provide data allowing us to choose
         * an appropriate scaling for the new master (mostly CPU, RAM). This could similarly apply to 1) where we could
         * also allow the user to select CPU/RAM for a new master and where we could make adjustments to the launch configuration
         * that we're touching anyhow for the release update.
         * 
         * For implementation order, let's consider "bang for the buck": finally getting the "club server" upgrades done
         * in a way that leaves them available at least read-only would help our general availability, especially where
         * "club servers" provide content featured through the archive server(s), such as tractrac.sapsailing.com,
         * lyc.sapsailing.com, and vsaw.sapsailing.com. Upgrading auto-scaling groups manually is tedious and would also
         * benefit a lot from more automation.
         */
        
        // TODO Implement LandscapeManagementPanel.upgradeApplicationReplicaSet(...)
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
        if (sshKeyManagementPanel.getSelectedKeyPair() == null) {
            Notification.notify(stringMessages.pleaseSelectSshKeyPair(), NotificationType.INFO);
        } else {
            new MongoScalingDialog(mongoEndpointToScale, stringMessages, errorReporter, landscapeManagementService, new DialogCallback<MongoScalingInstructionsDTO>() {
                @Override
                public void ok(MongoScalingInstructionsDTO mongoScalingInstructions) {
                    mongoEndpointsBusy.setBusy(true);
                    landscapeManagementService.scaleMongo(selectedRegion, mongoScalingInstructions, sshKeyManagementPanel.getSelectedKeyPair().getName(),
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
        if (mfaLoginWidget.hasValidSessionCredentials() && regionsTable.getSelectionModel().getSelectedObject() != null
                && Util.hasLength(sshKeyManagementPanel.getPassphraseForPrivateKeyDecryption())) {
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
