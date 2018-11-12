package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ServerConfigurationDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;

public class LocalServerManagementPanel extends SimplePanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final CaptionPanel serverInfoPanel;
    private final CaptionPanel serverConfigurationPanel;
    private CheckBox isStandaloneServerCheckbox;
    private Label serverNameLabel;
    private Label buildVersionLabel;
    private CheckBox isPublicServer;
    private CheckBox isSelfServiceServer;

    public LocalServerManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");

        serverInfoPanel = new CaptionPanel(stringMessages.serverInformation());
        mainPanel.add(serverInfoPanel);

        serverConfigurationPanel = new CaptionPanel(stringMessages.serverConfiguration());
        mainPanel.add(serverConfigurationPanel);
        
        createServerInfoUI();
        createServerConfigurationUI();
    }
    
    private void createServerInfoUI() {
        VerticalPanel serverInfoContentPanel = new VerticalPanel();
        serverInfoPanel.setContentWidget(serverInfoContentPanel);
        
        serverNameLabel = new Label();
        buildVersionLabel = new Label();
        
        Grid grid = new Grid(2, 2);
        grid.setWidget(0, 0, new Label(stringMessages.name() + ":"));
        grid.setWidget(0, 1, serverNameLabel);
        grid.setWidget(1, 0, new Label(stringMessages.buildVersion() + ":"));
        grid.setWidget(1, 1, buildVersionLabel);
        serverInfoContentPanel.add(grid);
        
        refreshServerInfo();
    }
    
    private void createServerConfigurationUI() {
        VerticalPanel serverConfigurationContentPanel = new VerticalPanel();
        serverConfigurationPanel.setContentWidget(serverConfigurationContentPanel);
        
        isStandaloneServerCheckbox = new CheckBox();
        isStandaloneServerCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                serverConfigurationChanged();
            }
        });

        isPublicServer = new CheckBox();
        isPublicServer.setEnabled(false);
        isSelfServiceServer = new CheckBox();
        isSelfServiceServer.setEnabled(false);
    
        Grid grid = new Grid(3, 2);
        grid.setWidget(0, 0, new Label(stringMessages.standaloneServer() + ":"));
        grid.setWidget(0, 1, isStandaloneServerCheckbox);

        grid.setWidget(1, 0, new Label(stringMessages.isPublic() + ":"));
        grid.setWidget(1, 1, isPublicServer);

        grid.setWidget(2, 0, new Label(stringMessages.selfServiceServer() + ":"));
        grid.setWidget(2, 1, isSelfServiceServer);

        serverConfigurationContentPanel.add(grid);
        
        refreshServerConfiguration();
    }    

    private void serverConfigurationChanged() {
        ServerConfigurationDTO serverConfig = new ServerConfigurationDTO(isStandaloneServerCheckbox.getValue(),
                isPublicServer.getValue(), isSelfServiceServer.getValue());
        
        sailingService.updateServerConfiguration(serverConfig, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                refreshServerConfiguration();
            }
        });
    }

    private void refreshServerInfo() {
        sailingService.getServerInfo(new AsyncCallback<ServerInfoDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(ServerInfoDTO result) {
                updateServerInfo(result);
            }
        });
    }

    private void refreshServerConfiguration() {
        sailingService.getServerConfiguration(new AsyncCallback<ServerConfigurationDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(ServerConfigurationDTO result) {
                updateServerConfiguration(result);
            }
        });
    }
    
    private void updateServerInfo(ServerInfoDTO result) {
        serverNameLabel.setText(result.getServerName());
        buildVersionLabel.setText(result.getBuildVersion() != null ? result.getBuildVersion() : "Unknown");
    }
    
    private void updateServerConfiguration(ServerConfigurationDTO result) {
        isStandaloneServerCheckbox.setValue(result.isStandaloneServer(), true); 
        isPublicServer.setValue(result.isPublic(), true);
        isSelfServiceServer.setValue(result.isSelfService(), true);
    }
}
