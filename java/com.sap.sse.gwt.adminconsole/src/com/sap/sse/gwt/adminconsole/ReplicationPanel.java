package com.sap.sse.gwt.adminconsole;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.replication.RemoteReplicationServiceAsync;
import com.sap.sse.gwt.shared.replication.ReplicaDTO;
import com.sap.sse.gwt.shared.replication.ReplicationMasterDTO;
import com.sap.sse.gwt.shared.replication.ReplicationStateDTO;

/**
 * Allows administrators to manage all aspects of server instance replication such as showing whether the instance
 * is a master or a replica and for a master showing the replicas to which the master is currently replicating.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ReplicationPanel extends FlowPanel {
    private final Grid registeredReplicas;
    private final Grid registeredMasters;
    
    private final RemoteReplicationServiceAsync replicationServiceAsync;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    
    private final Button addButton;
    private final Button stopReplicationButton;
    private final Button removeAllReplicas;
    
    /**
     * For administrators it is important to understand whether an instance is a replica. A
     * {@link ErrorReporter#reportPersistentInformation(String) persistent error message} shall be shown
     * in this case; however, some replicables may not be considered part of the "domain-based" replication.
     * For example, if only a security service for user management is replicated in order to implement
     * central user and session management then this shall not lead to the warning being displayed.
     * This set contains the stringified IDs of those replicables for which being a replica shall
     * lead to a warnings.
     */
    private final Set<String> replicableIdsAsStringThatShallLeadToWarningAboutInstanceBeingReplica;
    
    public ReplicationPanel(RemoteReplicationServiceAsync replicationService, ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.replicationServiceAsync = replicationService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.replicableIdsAsStringThatShallLeadToWarningAboutInstanceBeingReplica = new HashSet<>();
        replicationService.getReplicableIdsAsStringThatShallLeadToWarningAboutInstanceBeingReplica(new MarkedAsyncCallback<>(new AsyncCallback<String[]>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log(stringMessages.errorFetchingReplicaData(caught.getMessage()));
            }

            @Override
            public void onSuccess(String[] result) {
                for (final String replicableIdAsStringThatShallLeadToWarningAboutInstanceBeingReplica : result) {
                    replicableIdsAsStringThatShallLeadToWarningAboutInstanceBeingReplica.add(replicableIdAsStringThatShallLeadToWarningAboutInstanceBeingReplica);
                }
            }
        }));
        
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateReplicaList();
            }
        });
        add(refreshButton);
        
        final CaptionPanel mastergroup = new CaptionPanel(stringMessages.explainReplicasRegistered());
        final VerticalPanel masterpanel = new VerticalPanel();
        
        registeredReplicas = new Grid();
        registeredReplicas.resizeColumns(3);
        masterpanel.add(registeredReplicas);
        
        removeAllReplicas = new Button(stringMessages.stopAllReplicas());
        removeAllReplicas.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                stopAllReplicas();
              };
        });
        removeAllReplicas.setEnabled(false);
        masterpanel.add(removeAllReplicas);
        
        mastergroup.add(masterpanel);
        add(mastergroup);
        
        final CaptionPanel replicagroup = new CaptionPanel(stringMessages.explainConnectionsToMaster());
        final VerticalPanel replicapanel = new VerticalPanel();
        final HorizontalPanel replicapanelbuttons = new HorizontalPanel();
        
        registeredMasters = new Grid();
        registeredMasters.resizeColumns(3);
        replicapanel.add(registeredMasters);
        
        addButton = new Button(stringMessages.connectToMaster());
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addReplication();
            }
        });
        replicapanelbuttons.add(addButton);
        stopReplicationButton = new Button(stringMessages.stopConnectionToMaster());
        stopReplicationButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                stopReplication();
              };
        });
        stopReplicationButton.setEnabled(false);
        replicapanelbuttons.add(stopReplicationButton);
        
        replicapanel.add(replicapanelbuttons);
        replicagroup.add(replicapanel);
        add(replicagroup);
        
        updateReplicaList();
    }

    protected void stopAllReplicas() {
        replicationServiceAsync.stopAllReplicas(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
                updateReplicaList();
            }
            @Override
            public void onSuccess(Void result) {
                removeAllReplicas.setEnabled(false);
                updateReplicaList();
            }
        });
    }

    private void stopReplication() {
        stopReplicationButton.setEnabled(false);
        replicationServiceAsync.stopReplicatingFromMaster(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
                stopReplicationButton.setEnabled(true);
            }
            @Override
            public void onSuccess(Void result) {
                addButton.setEnabled(true);
                stopReplicationButton.setEnabled(false);
                updateReplicaList();
            }
        });
    }
    
    private void addReplication() {
        AddReplicationDialog dialog = new AddReplicationDialog(null,
                new DialogCallback<Util.Triple<Util.Triple<String, String, String>, Integer, Integer>>() {
                    @Override
                    /**
                     * @param masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber a triple containing the RabbitMQ exchange hostname, the servlet hostname and the RabbitMQ exchange name,
                     * followed by the RabbitMQ messaging port and the servlet port
                     */
                    public void ok(final Util.Triple<Util.Triple<String, String, String>, Integer, Integer> masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber) {
                        registeredMasters.removeRow(0);
                        registeredMasters.insertRow(0);
                        registeredMasters.setWidget(0, 0, new Label(stringMessages.loading()));
                        addButton.setEnabled(false);
                        stopReplicationButton.setEnabled(false);
                        replicationServiceAsync.startReplicatingFromMaster(masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getA(),
                                masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getB(),
                                masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getC(),
                                masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getC(),
                                masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getB(),
                                /* TODO username */ null, /* TODO password */ null, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable e) {
                                addButton.setEnabled(true);
                                errorReporter.reportError(stringMessages.errorStartingReplication(
                                        masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getB(),
                                        masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getC(), e.getMessage()));
                                updateReplicaList();
                            }

                            @Override
                            public void onSuccess(Void arg0) {
                                addButton.setEnabled(false);
                                stopReplicationButton.setEnabled(true);
                                updateReplicaList();
                            }
                        });
                    }
                    
                    @Override
                    public void cancel() {
                        // simply don't add replication
                    }
                });
        dialog.show();
    }

    public void updateReplicaList() {
        replicationServiceAsync.getReplicaInfo(new AsyncCallback<ReplicationStateDTO>() {
            @Override
            public void onSuccess(ReplicationStateDTO replicas) {
                int i=0;
                int replicaCount = 0;
                while (registeredReplicas.getRowCount() > 0) {
                    registeredReplicas.removeRow(0);
                }
                boolean replicaRegistered = false;
                for (final ReplicaDTO replica : replicas.getReplicas()) {
                    replicaCount++;
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 0, new Label(replicaCount + ". " + replica.getHostname() + " (" + replica.getIdentifier() + ")"));
                    registeredReplicas.setWidget(i, 1, new Label(stringMessages.registeredAt(replica.getRegistrationTime().toString())));
                    final Button removeReplicaButton = new Button(stringMessages.dropReplicaConnection());
                    removeReplicaButton.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            replicationServiceAsync.stopSingleReplicaInstance(replica.getIdentifier(), new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError(caught.getMessage());
                                    updateReplicaList();
                                }
                                @Override
                                public void onSuccess(Void result) {
                                    updateReplicaList();
                                }
                            });
                        }
                    });
                    registeredReplicas.setWidget(i, 2, removeReplicaButton);
                    i++;
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 1, new Label(stringMessages.replicables()));
                    HTML replicablesLabel = new HTML(new SafeHtmlBuilder().appendEscapedLines(Arrays.toString(replica.getReplicableIdsAsStrings()).replaceAll(",", "\n")).toSafeHtml());
                    registeredReplicas.setWidget(i, 2, replicablesLabel);
                    i++;
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 1, new Label(stringMessages.averageNumberOfOperationsPerMessage()));
                    registeredReplicas.setWidget(i, 2, new Label(""+replica.getAverageNumberOfOperationsPerMessage()));
                    i++;
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 1, new Label(stringMessages.numberOfQueueMessagesSent()));
                    registeredReplicas.setWidget(i, 2, new Label(""+replica.getNumberOfMessagesSent()));
                    i++;
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 1, new Label(stringMessages.averageMessageSize()));
                    registeredReplicas.setWidget(i, 2, new Label(""+replica.getAverageMessageSizeInBytes()));
                    i++;
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 1, new Label(stringMessages.totalSize()));
                    registeredReplicas.setWidget(i, 2, new Label(""+replica.getNumberOfBytesSent()+"B ("+replica.getNumberOfBytesSent()/1024.0/1024.0+"MB)"));
                    i++;
                    long totalNumberOfOperations = 0;
                    for (Map.Entry<String, Integer> e : replica.getOperationCountByOperationClassName().entrySet()) {
                        registeredReplicas.insertRow(i);
                        registeredReplicas.setWidget(i, 1, new Label(e.getKey()));
                        registeredReplicas.setWidget(i, 2, new Label(e.getValue().toString()));
                        totalNumberOfOperations += e.getValue();
                        i++;
                    }
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 1, new Label(stringMessages.totalNumberOfOperations()));
                    registeredReplicas.setWidget(i, 2, new Label(""+totalNumberOfOperations));
                    i++;
                    replicaRegistered = true;
                }
                
                if (!replicaRegistered) {
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 0, new Label(stringMessages.explainNoConnectionsFromReplicas()));
                    removeAllReplicas.setEnabled(false);
                } else {
                    removeAllReplicas.setEnabled(true);
                }
                
                while (registeredMasters.getRowCount() > 0) {
                    registeredMasters.removeRow(0);
                }
                i = 0;
                
                registeredMasters.insertRow(i);
                registeredMasters.setWidget(i, 0, new Label("Client UUID: " + replicas.getServerIdentifier()));
                i++;
                final ReplicationMasterDTO replicatingFromMaster = replicas.getReplicatingFromMaster();
                if (replicatingFromMaster != null) { // TODO bug 2465: replicating only the user service from a "domain controller master" shouldn't lead to a warning here...
                    for (final String replicableIdAsStringOfReplicableThatWeAreReplicatingCurrently : replicatingFromMaster.getReplicableIdsAsString()) {
                        if (Util.contains(replicableIdsAsStringThatShallLeadToWarningAboutInstanceBeingReplica, replicableIdAsStringOfReplicableThatWeAreReplicatingCurrently)) {
                            errorReporter.reportPersistentInformation(stringMessages.warningServerIsReplica());
                            break;
                        }
                    }
                    registeredMasters.insertRow(i);
                    registeredMasters.setWidget(i, 0, new Label(stringMessages.replicatingFromMaster(replicatingFromMaster.getHostname(),
                            replicatingFromMaster.getMessagingPort(), replicatingFromMaster.getServletPort(),
                            replicatingFromMaster.getMessagingHostname(), replicatingFromMaster.getExchangeName(),
                            Arrays.toString(replicatingFromMaster.getReplicableIdsAsString()))));
                    i++;
                    addButton.setEnabled(false);
                    stopReplicationButton.setEnabled(true);
                } else {
                    errorReporter.reportPersistentInformation("");
                    registeredMasters.insertRow(i);
                    registeredMasters.setWidget(i, 0, new Label(stringMessages.explainNoConnectionsToMaster()));
                    addButton.setEnabled(true);
                    stopReplicationButton.setEnabled(false);
                }
                
            }
            
            @Override
            public void onFailure(Throwable e) {
                errorReporter.reportError(stringMessages.errorFetchingReplicaData(e.getMessage()));
            }
        });
    }
    
    /**
     * A text entry dialog with ok/cancel button and configurable validation rule. Subclasses may provide a redefinition for
     * {@link #getAdditionalWidget()} to add a widget below the text field, e.g., for capturing additional data. The result of
     * this dialog is a triple containing the RabbitMQ exchange hostname, the servlet hostname and the RabbitMQ exchange name,
     * followed by the RabbitMQ messaging port and the servlet port.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private class AddReplicationDialog extends DataEntryDialog<Util.Triple<Util.Triple<String, String, String>, Integer, Integer>> {
        private final TextBox hostnameEntryField;
        private final TextBox exchangeHostnameEntryField;
        private final TextBox exchangenameEntryField;
        private final IntegerBox messagingPortField;
        private final IntegerBox servletPortField;
        
        public AddReplicationDialog(final Validator<Util.Triple<Util.Triple<String, String, String>, Integer, Integer>> validator,
                final DialogCallback<Util.Triple<Util.Triple<String, String, String>, Integer, Integer>> callback) {
            super(stringMessages.connect(), stringMessages.enterMaster(),
                    stringMessages.ok(), stringMessages.cancel(), validator, callback);
            hostnameEntryField = createTextBox("localhost");
            exchangeHostnameEntryField = createTextBox("localhost");
            exchangenameEntryField = createTextBox("sapsailinganalytics");
            messagingPortField = createIntegerBox(0, /* visible length */ 5);
            servletPortField = createIntegerBox(8888, /* visibleLength */ 5);
        }
        
        /**
         * Can contribute an additional widget to be displayed underneath the text entry field. If <code>null</code> is
         * returned, no additional widget will be displayed. This is the default behavior of this default implementation.
         */
        @Override
        protected Widget getAdditionalWidget() {
            Grid grid = new Grid(10, 2);
            grid.setWidget(0, 0, new Label(stringMessages.hostname()));
            grid.setWidget(0, 1, hostnameEntryField);
            grid.setWidget(1, 0, new Label(stringMessages.explainReplicationHostname()));
            
            grid.setWidget(2, 0, new Label(stringMessages.exchangeHost()));
            grid.setWidget(2, 1, exchangeHostnameEntryField);
            grid.setWidget(3, 0, new Label(stringMessages.explainExchangeHostName()));
            
            grid.setWidget(4, 0, new Label(stringMessages.exchangeName()));
            grid.setWidget(4, 1, exchangenameEntryField);
            grid.setWidget(5, 0, new Label(stringMessages.explainReplicationExchangeName()));
            
            grid.setWidget(6, 0, new Label(stringMessages.messagingPortNumber()));
            grid.setWidget(6, 1, messagingPortField);
            grid.setWidget(7, 0, new Label(stringMessages.explainReplicationExchangePort()));
            
            grid.setWidget(8, 0, new Label(stringMessages.servletPortNumber()));
            grid.setWidget(8, 1, servletPortField);
            grid.setWidget(9, 0, new Label(stringMessages.explainReplicationServletPort()));
            return grid;
        }
        
        @Override
        protected FocusWidget getInitialFocusWidget() {
            return hostnameEntryField;
        }

        @Override
        protected Util.Triple<Util.Triple<String, String, String>, Integer, Integer> getResult() {
            return new Util.Triple<Util.Triple<String, String, String>, Integer, Integer>(
                    new Util.Triple<String, String, String>(exchangeHostnameEntryField.getText(), hostnameEntryField.getText(), exchangenameEntryField.getText()),
                    messagingPortField.getValue(), servletPortField.getValue());
        }
    }

}
