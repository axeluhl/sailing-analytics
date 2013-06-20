package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.IntegerBox;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ReplicaDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationMasterDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationStateDTO;

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
    
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    
    private final Button addButton;
    private final Button stopReplicationButton;
    
    public ReplicationPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter, StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        
        add(new Label(stringMessages.explainReplicasRegistered()));
        registeredReplicas = new Grid();
        registeredReplicas.resizeColumns(3);
        add(registeredReplicas);
        
        add(new Label(stringMessages.explainConnectionsToMaster()));
        registeredMasters = new Grid();
        registeredMasters.resizeColumns(3);
        add(registeredMasters);
        
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateReplicaList();
            }
        });
        add(refreshButton);
        addButton = new Button(stringMessages.connectToMaster());
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addReplication();
            }
        });
        add(addButton);
        stopReplicationButton = new Button(stringMessages.stopConnectionToMaster());
        stopReplicationButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                stopReplication();
              };
        });
        stopReplicationButton.setEnabled(false);
        add(stopReplicationButton);
        updateReplicaList();
    }

    private void stopReplication() {
        sailingService.stopReplicatingFromMaster(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
            @Override
            public void onSuccess(Void result) {
                updateReplicaList();
            }
        });
    }
    
    private void addReplication() {
        AddReplicationDialog dialog = new AddReplicationDialog(null,
                new DialogCallback<Triple<Pair<String, String>, Integer, Integer>>() {
                    @Override
                    public void ok(final Triple<Pair<String, String>, Integer, Integer> masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber) {
                        registeredMasters.removeRow(0);
                        registeredMasters.insertRow(0);
                        registeredMasters.setWidget(0, 0, new Label(stringMessages.loading()));
                        sailingService.startReplicatingFromMaster(masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getA(),
                                masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getB(),
                                masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getC(),
                                masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getB(), new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable e) {
                                errorReporter.reportError(stringMessages.errorStartingReplication(
                                        masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getA(),
                                        masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getB(), e.getMessage()));
                            }

                            @Override
                            public void onSuccess(Void arg0) {
                                updateReplicaList();
                                addButton.setEnabled(false);
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

    private void updateReplicaList() {
        sailingService.getReplicaInfo(new AsyncCallback<ReplicationStateDTO>() {
            @Override
            public void onSuccess(ReplicationStateDTO replicas) {
                while (registeredMasters.getRowCount() > 0) {
                    registeredMasters.removeRow(0);
                }
                int i=0;
                final ReplicationMasterDTO replicatingFromMaster = replicas.getReplicatingFromMaster();
                if (replicatingFromMaster != null) {
                    registeredMasters.insertRow(i);
                    registeredMasters.setWidget(i, 0, new Label(stringMessages.replicatingFromMaster(replicatingFromMaster.getHostname(),
                            replicatingFromMaster.getMessagingPort(), replicatingFromMaster.getServletPort())));
                    i++;
                    addButton.setEnabled(false);
                    stopReplicationButton.setEnabled(true);
                } else {
                    registeredMasters.insertRow(i);
                    registeredMasters.setWidget(i, 0, new Label(stringMessages.explainNoConnectionsToMaster()));
                    addButton.setEnabled(true);
                    stopReplicationButton.setEnabled(false);
                }
                
                while (registeredReplicas.getRowCount() > 0) {
                    registeredReplicas.removeRow(0);
                }
                i = 0; boolean replicaRegistered = false;
                for (ReplicaDTO replica : replicas.getReplicas()) {
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 0, new Label(replica.getHostname()));
                    registeredReplicas.setWidget(i, 1, new Label(stringMessages.registeredAt(replica.getRegistrationTime().toString())));
                    i++;
                    for (Map.Entry<String, Integer> e : replica.getOperationCountByOperationClassName().entrySet()) {
                        registeredReplicas.insertRow(i);
                        registeredReplicas.setWidget(i, 1, new Label(e.getKey()));
                        registeredReplicas.setWidget(i, 2, new Label(e.getValue().toString()));
                        i++;
                    }
                    replicaRegistered = true;
                }
                
                if (!replicaRegistered) {
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 0, new Label(stringMessages.explainNoConnectionsFromReplicas()));
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
     * {@link #getAdditionalWidget()} to add a widget below the text field, e.g., for capturing additional data.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private class AddReplicationDialog extends DataEntryDialog<Triple<Pair<String, String>, Integer, Integer>> {
        private final TextBox hostnameEntryField;
        private final TextBox exchangenameEntryField;
        private final IntegerBox messagingPortField;
        private final IntegerBox servletPortField;
        
        public AddReplicationDialog(final Validator<Triple<Pair<String, String>, Integer, Integer>> validator,
                final DialogCallback<Triple<Pair<String, String>, Integer, Integer>> callback) {
            super(stringMessages.connect(), stringMessages.enterMaster(),
                    stringMessages.ok(), stringMessages.cancel(), validator, callback);
            hostnameEntryField = createTextBox("localhost");
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
            Grid grid = new Grid(8, 2);
            grid.setWidget(0, 0, new Label(stringMessages.hostname()));
            grid.setWidget(0, 1, hostnameEntryField);
            grid.setWidget(1, 0, new Label(stringMessages.explainReplicationHostname()));
            grid.setWidget(2, 0, new Label(stringMessages.exchangeName()));
            grid.setWidget(2, 1, exchangenameEntryField);
            grid.setWidget(3, 0, new Label(stringMessages.explainReplicationExchangeName()));
            grid.setWidget(4, 0, new Label(stringMessages.messagingPortNumber()));
            grid.setWidget(4, 1, messagingPortField);
            grid.setWidget(5, 0, new Label(stringMessages.explainReplicationExchangePort()));
            grid.setWidget(6, 0, new Label(stringMessages.servletPortNumber()));
            grid.setWidget(6, 1, servletPortField);
            grid.setWidget(7, 0, new Label(stringMessages.explainReplicationServletPort()));
            return grid;
        }
        
        @Override
        public void show() {
            super.show();
            hostnameEntryField.setFocus(true);
        }

        @Override
        protected Triple<Pair<String, String>, Integer, Integer> getResult() {
            return new Triple<Pair<String, String>, Integer, Integer>(
                    new Pair<String, String>(hostnameEntryField.getText(), exchangenameEntryField.getText()),
                    messagingPortField.getValue(), servletPortField.getValue());
        }
    }

}
