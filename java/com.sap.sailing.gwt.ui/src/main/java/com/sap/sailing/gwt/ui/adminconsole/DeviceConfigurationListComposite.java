package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;

public class DeviceConfigurationListComposite extends Composite  {
    protected static AdminConsoleTableResources tableResource = GWT.create(AdminConsoleTableResources.class);
    
    private final RefreshableMultiSelectionModel<DeviceConfigurationDTO> refreshableConfigurationSelectionModel;
    private final CellTable<DeviceConfigurationDTO> configurationTable;
    protected ListDataProvider<DeviceConfigurationDTO> configurationsDataProvider;
    
    private final SimplePanel mainPanel;
    private final VerticalPanel panel;

    private final Label noConfigurationsLabel;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    protected final StringMessages stringMessages;

    public DeviceConfigurationListComposite(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        mainPanel = new SimplePanel();
        panel = new VerticalPanel();
        mainPanel.setWidget(panel);
        noConfigurationsLabel = new Label(stringMessages.noConfigurations());
        noConfigurationsLabel.setWordWrap(false);
        panel.add(noConfigurationsLabel);
        configurationsDataProvider = new ListDataProvider<DeviceConfigurationDTO>();
        refreshTable();
        configurationTable = createConfigurationTable();
        configurationTable.setVisible(true);
        refreshableConfigurationSelectionModel = new RefreshableMultiSelectionModel<>(new EntityIdentityComparator<DeviceConfigurationDTO>() {
            @Override
            public boolean representSameEntity(DeviceConfigurationDTO dto1, DeviceConfigurationDTO dto2) {
                return Util.equalsWithNull(dto1.id, dto2.id);
            }

            @Override
            public int hashCode(DeviceConfigurationDTO t) {
                return t.id == null ? 0 : t.id.hashCode();
            }
        }, configurationsDataProvider);
        configurationTable.setSelectionModel(refreshableConfigurationSelectionModel);
        panel.add(configurationTable);
        initWidget(mainPanel);
    }

    public void refreshTable() {
        sailingService.getDeviceConfigurations(new AsyncCallback<List<DeviceConfigurationDTO>>() {
            @Override
            public void onSuccess(List<DeviceConfigurationDTO> result) {
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

    public RefreshableMultiSelectionModel<DeviceConfigurationDTO> getSelectionModel() {
        return refreshableConfigurationSelectionModel;
    }

    private CellTable<DeviceConfigurationDTO> createConfigurationTable() {
        CellTable<DeviceConfigurationDTO> table = new BaseCelltable<DeviceConfigurationDTO>(
                /* pageSize */10000, tableResource);
        configurationsDataProvider.addDataDisplay(table);
        table.setWidth("100%");
        ListHandler<DeviceConfigurationDTO> columnSortHandler = 
                new ListHandler<DeviceConfigurationDTO>(configurationsDataProvider.getList());
        table.addColumnSortHandler(columnSortHandler);
        TextColumn<DeviceConfigurationDTO> identifierNameColumn = new TextColumn<DeviceConfigurationDTO>() {
            @Override
            public String getValue(DeviceConfigurationDTO config) {
                return config.name;
            }
        };
        identifierNameColumn.setSortable(true);
        columnSortHandler.setComparator(identifierNameColumn, (r1, r2)->r1.name.compareTo(r2.name));
        table.addColumn(identifierNameColumn, stringMessages.device());
        return table;
    }

}
