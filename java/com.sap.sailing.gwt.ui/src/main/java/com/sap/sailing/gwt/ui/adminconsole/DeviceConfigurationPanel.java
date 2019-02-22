package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;
import java.util.Set;

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
import com.sap.sailing.gwt.ui.adminconsole.DeviceConfigurationDetailComposite.DeviceConfigurationCloneListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.security.ui.client.UserService;

public abstract class DeviceConfigurationPanel extends SimplePanel implements DeviceConfigurationCloneListener {

    public static String renderIdentifiers(List<String> clientIdentifiers, StringMessages stringMessages) {
        if (clientIdentifiers.size() == 1) {
            return clientIdentifiers.get(0);
        } else if (clientIdentifiers.size() == 0) {
            return "["+stringMessages.any()+"]";
        } else {
            return clientIdentifiers.toString();
        }
    }
    
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    
    private Button addConfigurationButton;
    private Button removeConfigurationButton;
    private Button refreshConfigurationsButton;
    private DeviceConfigurationListComposite listComposite;
    private DeviceConfigurationDetailComposite detailComposite;
    
    private final RefreshableMultiSelectionModel<DeviceConfigurationMatcherDTO> refreshableMultiSelectionModel;

    public DeviceConfigurationPanel(SailingServiceAsync sailingService, UserService userService, StringMessages stringMessages, ErrorReporter reporter) {
        this.sailingService = sailingService;
        this.userService = userService;
        this.stringMessages = stringMessages;
        this.errorReporter = reporter;
        setupUi();
        refreshableMultiSelectionModel = listComposite.getSelectionModel();
        refreshableMultiSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<DeviceConfigurationMatcherDTO> selectedConfigurations = refreshableMultiSelectionModel
                        .getSelectedSet();
                if (selectedConfigurations.size() == 1 && selectedConfigurations.iterator().hasNext()) {
                    detailComposite.setConfiguration(selectedConfigurations.iterator().next());
                    detailComposite.setVisible(true);
                } else {
                    detailComposite.setConfiguration(null);
                    detailComposite.setVisible(false);
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
        deviceManagementControlPanel.add(addConfigurationButton);
        
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
        
        listComposite = createListComposite(sailingService, errorReporter, stringMessages);
        grid.setWidget(0, 0, listComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(1).getStyle().setPaddingTop(2.0, Unit.EM);
        
        detailComposite = createDetailComposite(sailingService, errorReporter, stringMessages, this);
        detailComposite.setVisible(false);
        grid.setWidget(0, 1, detailComposite);
    }
    
    private void createConfiguration() {
        createConfiguration(new DeviceConfigurationDTO());
    }

    private void createConfiguration(final DeviceConfigurationDTO configuration) {
        sailingService.getDeviceConfigurationMatchers(new MarkedAsyncCallback<>(new AsyncCallback<List<DeviceConfigurationMatcherDTO>>() {
            @Override
            public void onSuccess(List<DeviceConfigurationMatcherDTO> allMatchers) {
                createConfiguration(allMatchers, configuration);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
        }));
    }

    private void createConfiguration(final List<DeviceConfigurationMatcherDTO> allMatchers, final DeviceConfigurationDTO configuration) {
        getCreateDialog(stringMessages, 
                new DeviceConfigurationCreateSingleMatcherDialog.MatcherValidator(allMatchers, stringMessages), 
                new DialogCallback<DeviceConfigurationMatcherDTO>() {
            @Override
            public void ok(DeviceConfigurationMatcherDTO createdMatcher) {
                sailingService.createOrUpdateDeviceConfiguration(createdMatcher, configuration, 
                        new MarkedAsyncCallback<>(new AsyncCallback<DeviceConfigurationMatcherDTO>() {
                    @Override
                    public void onSuccess(DeviceConfigurationMatcherDTO newMatcher) {
                        listComposite.refreshTable();
                        refreshableMultiSelectionModel.clear();
                        refreshableMultiSelectionModel.setSelected(newMatcher, true);
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
    
    abstract protected DataEntryDialog<DeviceConfigurationMatcherDTO> getCreateDialog(final StringMessages stringMessages, 
            final Validator<DeviceConfigurationMatcherDTO> validator,
            final DialogCallback<DeviceConfigurationMatcherDTO> callback);

    abstract protected DeviceConfigurationListComposite createListComposite(SailingServiceAsync sailingService,
            ErrorReporter errorReporter, StringMessages stringMessages);

    abstract protected DeviceConfigurationDetailComposite createDetailComposite(SailingServiceAsync sailingService,
            ErrorReporter errorReporter, StringMessages stringMessages, DeviceConfigurationCloneListener cloneListener);

    private void removeConfiguration() {
        detailComposite.setConfiguration(null);
        detailComposite.setVisible(false);
        for (DeviceConfigurationMatcherDTO identifier : refreshableMultiSelectionModel.getSelectedSet()) {
            sailingService.removeDeviceConfiguration(identifier.clients, new AsyncCallback<Boolean>() {
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

    @Override
    public void onCloneRequested(DeviceConfigurationMatcherDTO matcher, DeviceConfigurationDTO configuration) {
        createConfiguration(configuration);
    }

}
