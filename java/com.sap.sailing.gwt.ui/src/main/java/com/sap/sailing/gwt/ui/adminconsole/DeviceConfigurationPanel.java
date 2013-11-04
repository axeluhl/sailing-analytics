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
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SelectionProvider;
import com.sap.sailing.gwt.ui.client.SelectionProviderImpl;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;

public class DeviceConfigurationPanel extends SimplePanel implements SelectionChangeListener<DeviceConfigurationMatcherDTO> {
    
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    
    private Button addConfigurationButton;
    private Button removeConfigurationButton;
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
                addConfiguration();
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
        mainPanel.add(deviceManagementControlPanel);
    }

    private void setupConfigurationPanels(VerticalPanel mainPanel) {
        Grid grid = new Grid(1 ,2);
        mainPanel.add(grid);
        
        listComposite = new DeviceConfigurationListComposite(sailingService, selectionProvider, 
                errorReporter, stringMessages);
        grid.setWidget(0, 0, listComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(1).getStyle().setPaddingTop(2.0, Unit.EM);
        
        detailComposite = new DeviceConfigurationDetailComposite(sailingService, errorReporter, stringMessages);
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

    private void addConfiguration() {
        sailingService.getDeviceConfigurationMatchers(new AsyncCallback<List<DeviceConfigurationMatcherDTO>>() {
            @Override
            public void onSuccess(List<DeviceConfigurationMatcherDTO> allMatchers) {
                DeviceConfigurationCreateDialog dialog = new DeviceConfigurationCreateDialog(
                        stringMessages, 
                        new DeviceConfigurationCreateDialog.DuplicationValidator(allMatchers), 
                        new DialogCallback<DeviceConfigurationMatcherDTO>() {
                    @Override
                    public void ok(DeviceConfigurationMatcherDTO createdMatcher) {
                        sailingService.addDeviceConfiguration(createdMatcher.type, createdMatcher.clients, 
                                null, null, null, null, new AsyncCallback<DeviceConfigurationMatcherDTO>() {
                            @Override
                            public void onSuccess(DeviceConfigurationMatcherDTO matcher) {
                                listComposite.refreshTable();
                                onSelectionChange(Arrays.asList(matcher));
                            }
                            
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(caught.getMessage());
                            }
                        });
                    }

                    @Override
                    public void cancel() { }
                });
                dialog.show();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
        });
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

}
