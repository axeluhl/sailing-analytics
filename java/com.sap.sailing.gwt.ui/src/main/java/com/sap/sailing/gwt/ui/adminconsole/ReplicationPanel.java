package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TextfieldEntryDialog;

/**
 * Allows administrators to manage all aspects of server instance replication such as showing whether the instance
 * is a master or a replica and for a master showing the replicas to which the master is currently replicating.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ReplicationPanel extends FlowPanel {
    private final ListBox registeredReplicas;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    
    public ReplicationPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter, StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        registeredReplicas = new ListBox();
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
        TextfieldEntryDialog dialog = new TextfieldEntryDialog(stringMessages.add(), stringMessages.enterMaster(),
                stringMessages.ok(), stringMessages.cancel(), "", null, new AsyncCallback<String>() {
                    @Override
                    public void onSuccess(final String masterName) {
                        sailingService.startReplicatingFromMaster(masterName, /* TODO servlet port */ 8888,
                                /* TODO JMS port */ 61616, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable e) {
                                errorReporter.reportError(stringMessages.errorStartingReplication(masterName, e.getMessage()));
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
        sailingService.getHostnamesOfReplica(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> hostnames) {
                registeredReplicas.clear();
                for (String hostname : hostnames) {
                    registeredReplicas.addItem(hostname);
                }
            }
            
            @Override
            public void onFailure(Throwable e) {
                errorReporter.reportError(stringMessages.errorFetchingReplicaData(e.getMessage()));
            }
        });
    }
}
