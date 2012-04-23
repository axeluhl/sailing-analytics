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
