package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
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
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    
    public ReplicationPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter, StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        registeredReplicas = new Grid();
        registeredReplicas.resizeColumns(3);
        add(registeredReplicas);
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateReplicaList();
            }
        });
        add(refreshButton);
        Button addButton = new Button(stringMessages.add());
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addReplication();
            }
        });
        add(addButton);
    }
    
    private void addReplication() {
        AddReplicationDialog dialog = new AddReplicationDialog(null,
                new AsyncCallback<Triple<Pair<String, String>, Integer, Integer>>() {
                    @Override
                    public void onSuccess(final Triple<Pair<String, String>, Integer, Integer> masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber) {
                        sailingService.startReplicatingFromMaster(masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getA(),
                                masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getB(),
                                /* TODO servlet port */ masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getC(),
                                /* TODO JMS port */ masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getB(), new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable e) {
                                errorReporter.reportError(stringMessages.errorStartingReplication(
                                        masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getA(),
                                        masterNameAndExchangeNameAndMessagingPortNumberAndServletPortNumber.getA().getB(), e.getMessage()));
                            }

                            @Override
                            public void onSuccess(Void arg0) {
                                updateReplicaList();
                            }
                        });
                    }
                    
                    @Override
                    public void onFailure(Throwable arg0) {
                        // simply don't add replication
                    }
                });
        dialog.show();
    }

    private void updateReplicaList() {
        sailingService.getReplicaInfo(new AsyncCallback<ReplicationStateDTO>() {
            @Override
            public void onSuccess(ReplicationStateDTO replicas) {
                while (registeredReplicas.getRowCount() > 0) {
                    registeredReplicas.removeRow(0);
                }
                int i=0;
                final ReplicationMasterDTO replicatingFromMaster = replicas.getReplicatingFromMaster();
                if (replicatingFromMaster != null) {
                    registeredReplicas.insertRow(i);
                    registeredReplicas.setWidget(i, 0, new Label(stringMessages.replicatingFromMaster(replicatingFromMaster.getHostname(),
                            replicatingFromMaster.getJmsPort(), replicatingFromMaster.getServletPort())));
                    i++;
                }
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
                final AsyncCallback<Triple<Pair<String, String>, Integer, Integer>> callback) {
            super(stringMessages.add(), stringMessages.enterMaster(),
                    stringMessages.ok(), stringMessages.cancel(), validator, callback);
            hostnameEntryField = createTextBox("");
            exchangenameEntryField = createTextBox("");
            messagingPortField = createIntegerBox(61616, /* visible length */ 5);
            servletPortField = createIntegerBox(8888, /* visibleLength */ 5);
        }
        
        /**
         * Can contribute an additional widget to be displayed underneath the text entry field. If <code>null</code> is
         * returned, no additional widget will be displayed. This is the default behavior of this default implementation.
         */
        @Override
        protected Widget getAdditionalWidget() {
            Grid grid = new Grid(4, 2);
            grid.setWidget(0, 0, new Label(stringMessages.hostname()));
            grid.setWidget(0, 1, hostnameEntryField);
            grid.setWidget(0, 0, new Label(stringMessages.exchangeName()));
            grid.setWidget(0, 1, exchangenameEntryField);
            grid.setWidget(2, 0, new Label(stringMessages.jmsPortNumber()));
            grid.setWidget(2, 1, messagingPortField);
            grid.setWidget(3, 0, new Label(stringMessages.servletPortNumber()));
            grid.setWidget(4, 1, servletPortField);
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
