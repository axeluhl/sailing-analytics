package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationWithSecurityDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.ui.client.UserService;

public class DeviceConfigurationPanel extends SimplePanel implements DeviceConfigurationDetailComposite.DeviceConfigurationFactory {
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    
    private Button addConfigurationButton;
    private Button removeConfigurationButton;
    private Button refreshConfigurationsButton;
    private DeviceConfigurationListComposite listComposite;
    private DeviceConfigurationDetailComposite detailComposite;
    
    private final RefreshableMultiSelectionModel<DeviceConfigurationWithSecurityDTO> refreshableMultiSelectionModel;
    
    public DeviceConfigurationPanel(SailingServiceAsync sailingService, UserService userService,
            StringMessages stringMessages, ErrorReporter reporter) {
        this.sailingService = sailingService;
        this.userService = userService;
        this.stringMessages = stringMessages;
        this.errorReporter = reporter;
        setupUi();
        refreshableMultiSelectionModel = listComposite.getSelectionModel();
        refreshableMultiSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<DeviceConfigurationWithSecurityDTO> selectedConfigurations = refreshableMultiSelectionModel.getSelectedSet();
                if (selectedConfigurations.size() == 1 && selectedConfigurations.iterator().hasNext()) {
                    detailComposite.setConfiguration(selectedConfigurations.iterator().next());
                } else {
                    detailComposite.setConfiguration(null);
                }
                removeConfigurationButton.setEnabled(!selectedConfigurations.isEmpty());
            }
        }); 
    }
    
    protected UserService getUserService() {
        return userService;
    }

    private void setupUi() {
        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");
        setupControlPanel(mainPanel);
        setupConfigurationPanels(mainPanel);
    }

    private void setupControlPanel(VerticalPanel mainPanel) {
        HorizontalPanel deviceManagementControlPanel = new HorizontalPanel();
        deviceManagementControlPanel.setSpacing(5);
        addConfigurationButton = new Button(stringMessages.addConfiguration());
        addConfigurationButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createConfiguration();
            }
        });
        if (userService.hasCreatePermission(SecuredDomainType.RACE_MANAGER_APP_DEVICE_CONFIGURATION)) {
            deviceManagementControlPanel.add(addConfigurationButton);
        }
        removeConfigurationButton = new Button(stringMessages.remove());
        removeConfigurationButton.setEnabled(false);
        removeConfigurationButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeConfiguration();
            }
        });
        deviceManagementControlPanel.add(removeConfigurationButton);
        refreshConfigurationsButton = new Button(stringMessages.refresh());
        refreshConfigurationsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                listComposite.refreshTable();
            }
        });
        deviceManagementControlPanel.add(refreshConfigurationsButton);
        mainPanel.add(deviceManagementControlPanel);
    }

    private void setupConfigurationPanels(VerticalPanel mainPanel) {
        Grid grid = new Grid(1 ,2);
        mainPanel.add(grid);
        listComposite = new DeviceConfigurationListComposite(sailingService, errorReporter, stringMessages, userService);
        grid.setWidget(0, 0, listComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(1).getStyle().setPaddingTop(2.0, Unit.EM);
        detailComposite = new DeviceConfigurationDetailComposite(sailingService, getUserService(), errorReporter, stringMessages, this);
        detailComposite.setVisible(false);
        grid.setWidget(0, 1, detailComposite);
    }
    
    private void createConfiguration() {
        final DeviceConfigurationDTO newConfiguration = new DeviceConfigurationDTO();
        newConfiguration.id = UUID.randomUUID(); // the name will be obtained by the following call
        obtainAndSetNameForConfigurationAndAdd(newConfiguration);
    }

    /**
     * Asks the user for a new name for {@code configurationToObtainAndSetNameForAndAdd} that is not used yet by any of
     * the configurations in all the configurations obtained by
     * {@link SailingServiceAsync#getDeviceConfigurations(AsyncCallback)}. If such a name has been provided by the user
     * it is set on {@code configurationToObtainAndSetNameForAndAdd}, and
     * {@code configurationToObtainAndSetNameForAndAdd} is submitted to the server for creation. This method assumes
     * that the {@link DeviceConfigurationDTO.id ID} of the {@code configurationToObtainAndSetNameForAndAdd} is unique.
     */
    @Override
    public void obtainAndSetNameForConfigurationAndAdd(
            final DeviceConfigurationDTO configurationToObtainAndSetNameForAndAdd) {
        sailingService.getDeviceConfigurations(
                new MarkedAsyncCallback<>(new AsyncCallback<List<DeviceConfigurationWithSecurityDTO>>() {
            @Override
                    public void onSuccess(List<DeviceConfigurationWithSecurityDTO> allConfigurations) {
                new SelectNameForNewDeviceConfigurationDialog(stringMessages, new SelectNameForNewDeviceConfigurationDialog.MatcherValidator(allConfigurations, stringMessages), new DialogCallback<String>() {
                    @Override
                    public void ok(String nameForNewDeviceConfiguration) {
                        configurationToObtainAndSetNameForAndAdd.name = nameForNewDeviceConfiguration;
                        sailingService.createOrUpdateDeviceConfiguration(configurationToObtainAndSetNameForAndAdd, 
                                new MarkedAsyncCallback<>(new AsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                listComposite.refreshTable();
                                refreshableMultiSelectionModel.clear();
                            }
                            
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(caught.getMessage());
                            }
                        }));
                    }
                
                    @Override
                    public void cancel() { }
                }).show();
            }
    
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
        }));
    }
    
    private void removeConfiguration() {
        detailComposite.setConfiguration(null);
        for (DeviceConfigurationDTO config : refreshableMultiSelectionModel.getSelectedSet()) {
            sailingService.removeDeviceConfiguration(config.id, new AsyncCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    listComposite.refreshTable();
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.getMessage());
                }                  
            });
        }
    }

}
