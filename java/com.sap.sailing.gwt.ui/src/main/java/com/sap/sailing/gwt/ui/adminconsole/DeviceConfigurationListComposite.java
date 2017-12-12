package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;

public abstract class DeviceConfigurationListComposite extends Composite {

    protected static AdminConsoleTableResources tableResource = GWT.create(AdminConsoleTableResources.class);
    
    private final RefreshableMultiSelectionModel<DeviceConfigurationMatcherDTO> refreshableConfigurationSelectionModel;
    private final CellTable<DeviceConfigurationMatcherDTO> configurationTable;
    protected ListDataProvider<DeviceConfigurationMatcherDTO> configurationsDataProvider;
    
    private final SimplePanel mainPanel;
    private final VerticalPanel panel;

    private final Label noConfigurationsLabel;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    protected final StringMessages stringMessages;

    public DeviceConfigurationListComposite(final SailingServiceAsync sailingService,
            final ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        mainPanel = new SimplePanel();
        panel = new VerticalPanel();
        mainPanel.setWidget(panel);

        noConfigurationsLabel = new Label(stringMessages.noConfigurations());
        noConfigurationsLabel.setWordWrap(false);
        panel.add(noConfigurationsLabel);

        configurationsDataProvider = new ListDataProvider<DeviceConfigurationMatcherDTO>();
        refreshTable();
        
        configurationTable = createConfigurationTable();
        configurationTable.setVisible(true);
        refreshableConfigurationSelectionModel = new RefreshableMultiSelectionModel<>(new EntityIdentityComparator<DeviceConfigurationMatcherDTO>() {
            @Override
            public boolean representSameEntity(DeviceConfigurationMatcherDTO dto1, DeviceConfigurationMatcherDTO dto2) {
                return Util.equalsWithNull(dto1.clients, dto2.clients);
            }

            @Override
            public int hashCode(DeviceConfigurationMatcherDTO t) {
                return t.clients == null ? 0 : t.clients.hashCode();
            }
        }, configurationsDataProvider);
        configurationTable.setSelectionModel(refreshableConfigurationSelectionModel);
        panel.add(configurationTable);

        initWidget(mainPanel);
    }

    public void refreshTable() {
        sailingService.getDeviceConfigurationMatchers(new AsyncCallback<List<DeviceConfigurationMatcherDTO>>() {
            
            @Override
            public void onSuccess(List<DeviceConfigurationMatcherDTO> result) {
                if (configurationsDataProvider.getList().isEmpty()) {
                    configurationTable.getColumnSortList().clear();
                    configurationTable.getColumnSortList().push(configurationTable.getColumn(0));
                }
                configurationsDataProvider.getList().clear();
                configurationsDataProvider.getList().addAll(result);
                ColumnSortEvent.fire(configurationTable, configurationTable.getColumnSortList());
                
                noConfigurationsLabel.setVisible(false);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                noConfigurationsLabel.setText(stringMessages.errorRetrievingConfiguration());
                noConfigurationsLabel.setVisible(true);
                configurationTable.setVisible(false);
                errorReporter.reportError("Error retrieving configuration data from server: " + caught.getMessage());
                refreshableConfigurationSelectionModel.clear();
            }
        });
    }

    abstract protected CellTable<DeviceConfigurationMatcherDTO> createConfigurationTable();
    
    public RefreshableMultiSelectionModel<DeviceConfigurationMatcherDTO> getSelectionModel() {
        return refreshableConfigurationSelectionModel;
    }
}
