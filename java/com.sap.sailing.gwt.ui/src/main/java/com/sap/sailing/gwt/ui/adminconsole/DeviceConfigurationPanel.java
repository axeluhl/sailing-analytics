package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.List;

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
import com.sap.sailing.gwt.ui.adminconsole.DeviceConfigurationDetailComposite.DeviceConfigurationCloneListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SelectionProvider;
import com.sap.sailing.gwt.ui.client.SelectionProviderImpl;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

public class DeviceConfigurationPanel extends SimplePanel implements SelectionChangeListener<DeviceConfigurationMatcherDTO>, DeviceConfigurationCloneListener {

    public static String renderIdentifiers(List<String> clientIdentifiers) {
        if (clientIdentifiers.size() == 1) {
            return clientIdentifiers.get(0);
        } else if (clientIdentifiers.size() == 0) {
            return "[ANY]";
        } else {
            return clientIdentifiers.toString();
        }
    }
    
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    
    private Button addConfigurationButton;
    private Button removeConfigurationButton;
    private Button refreshConfigurationsButton;
    private DeviceConfigurationListComposite listComposite;
    private DeviceConfigurationDetailComposite detailComposite;
    
    private final SelectionProvider<DeviceConfigurationMatcherDTO> selectionProvider;

    public DeviceConfigurationPanel(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter reporter) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = reporter;
        
        this.selectionProvider = new SelectionProviderImpl<DeviceConfigurationMatcherDTO>(true);
        this.selectionProvider.addSelectionChangeListener(this);
        
        setupUi();
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
                detailComposite.setConfiguration(null);
                detailComposite.setVisible(false);
                listComposite.refreshTable();
            }
        });
        deviceManagementControlPanel.add(refreshConfigurationsButton);
        
        mainPanel.add(deviceManagementControlPanel);
    }

    private void setupConfigurationPanels(VerticalPanel mainPanel) {
        Grid grid = new Grid(1 ,2);
        mainPanel.add(grid);
        
        listComposite = createListComposite(sailingService, selectionProvider, errorReporter, stringMessages);
        grid.setWidget(0, 0, listComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(1).getStyle().setPaddingTop(2.0, Unit.EM);
        
        detailComposite = createDetailComposite(sailingService, errorReporter, stringMessages, this);
        detailComposite.setVisible(false);
        grid.setWidget(0, 1, detailComposite);
    }

    @Override
    public void onSelectionChange(List<DeviceConfigurationMatcherDTO> selectedConfigurations) {
        if (selectedConfigurations.size() == 1 && selectedConfigurations.iterator().hasNext()) {
            detailComposite.setConfiguration(selectedConfigurations.iterator().next());
            detailComposite.setVisible(true);
        } else {
            detailComposite.setConfiguration(null);
            detailComposite.setVisible(false);
        }
        removeConfigurationButton.setEnabled(!selectedConfigurations.isEmpty());
    }
    
    private void createConfiguration() {
        createConfiguration(new DeviceConfigurationDTO());
    }

    private void createConfiguration(final DeviceConfigurationDTO configuration) {
        sailingService.getDeviceConfigurationMatchers(new AsyncCallback<List<DeviceConfigurationMatcherDTO>>() {
            @Override
            public void onSuccess(List<DeviceConfigurationMatcherDTO> allMatchers) {
                createConfiguration(allMatchers, configuration);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
        });
    }

    private void createConfiguration(final List<DeviceConfigurationMatcherDTO> allMatchers, final DeviceConfigurationDTO configuration) {
        getCreateDialog(stringMessages, 
                new DeviceConfigurationCreateMatcherDialog.MatcherValidator(allMatchers), 
                new DialogCallback<DeviceConfigurationMatcherDTO>() {
            @Override
            public void ok(DeviceConfigurationMatcherDTO createdMatcher) {
                sailingService.createOrUpdateDeviceConfiguration(createdMatcher, configuration, 
                        new AsyncCallback<DeviceConfigurationMatcherDTO>() {
                    @Override
                    public void onSuccess(DeviceConfigurationMatcherDTO newMatcher) {
                        listComposite.refreshTable();
                        onSelectionChange(Arrays.asList(newMatcher));
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }
                });
            }

            @Override
            public void cancel() { }
        }).show();
    }
    
    protected DataEntryDialog<DeviceConfigurationMatcherDTO> getCreateDialog(final StringMessages stringMessages, 
            final Validator<DeviceConfigurationMatcherDTO> validator,
            final DialogCallback<DeviceConfigurationMatcherDTO> callback) {
        return new DeviceConfigurationCreateMatcherDialog(stringMessages, validator, callback);
    }

    protected DeviceConfigurationListComposite createListComposite(SailingServiceAsync sailingService, SelectionProvider<DeviceConfigurationMatcherDTO> selectionProvider, 
            ErrorReporter errorReporter, StringMessages stringMessages) {
        return new DeviceConfigurationListComposite(sailingService, selectionProvider, 
                errorReporter, stringMessages);
    }

    protected DeviceConfigurationDetailComposite createDetailComposite(SailingServiceAsync sailingService,
            ErrorReporter errorReporter, StringMessages stringMessages, DeviceConfigurationCloneListener cloneListener) {
        return new DeviceConfigurationDetailComposite(sailingService, errorReporter, stringMessages, cloneListener);
    }

    private void removeConfiguration() {
        detailComposite.setConfiguration(null);
        detailComposite.setVisible(false);
        for (DeviceConfigurationMatcherDTO identifier : selectionProvider.getSelectedItems()) {
            sailingService.removeDeviceConfiguration(identifier.type, identifier.clients, new AsyncCallback<Boolean>() {
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
