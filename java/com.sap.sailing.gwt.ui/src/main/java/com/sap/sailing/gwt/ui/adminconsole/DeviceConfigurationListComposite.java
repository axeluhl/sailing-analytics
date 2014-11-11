package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SelectionProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class DeviceConfigurationListComposite extends Composite {

    protected static AdminConsoleTableResources tableResource = GWT.create(AdminConsoleTableResources.class);
    
    private final SelectionModel<DeviceConfigurationMatcherDTO> configurationSelectionModel;
    private final CellTable<DeviceConfigurationMatcherDTO> configurationTable;
    protected ListDataProvider<DeviceConfigurationMatcherDTO> configurationsDataProvider;
    
    private final SimplePanel mainPanel;
    private final VerticalPanel panel;

    private final Label noConfigurationsLabel;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    public DeviceConfigurationListComposite(final SailingServiceAsync sailingService, 
            final SelectionProvider<DeviceConfigurationMatcherDTO> selectionProvider, 
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

        configurationSelectionModel = new MultiSelectionModel<DeviceConfigurationMatcherDTO>();
        configurationSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                selectionProvider.setSelection(getSelectedConfigurations());
            }
        });
        configurationTable.setSelectionModel(configurationSelectionModel);
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
            }
        });
    }

    protected CellTable<DeviceConfigurationMatcherDTO> createConfigurationTable() {
        CellTable<DeviceConfigurationMatcherDTO> table = new CellTable<DeviceConfigurationMatcherDTO>(
                /* pageSize */10000, tableResource);
        configurationsDataProvider.addDataDisplay(table);
        table.setWidth("100%");

        ListHandler<DeviceConfigurationMatcherDTO> columnSortHandler = 
                new ListHandler<DeviceConfigurationMatcherDTO>(configurationsDataProvider.getList());
        table.addColumnSortHandler(columnSortHandler);

        TextColumn<DeviceConfigurationMatcherDTO> identifierTypeColumn = 
                new TextColumn<DeviceConfigurationMatcherDTO>() {
            @Override
            public String getValue(DeviceConfigurationMatcherDTO identifier) {
                return identifier.type.name();
            }
        };
        identifierTypeColumn.setSortable(true);
        columnSortHandler.setComparator(identifierTypeColumn, new Comparator<DeviceConfigurationMatcherDTO>() {
            @Override
            public int compare(DeviceConfigurationMatcherDTO r1, DeviceConfigurationMatcherDTO r2) {
                return new Integer(r1.rank).compareTo(r2.rank);
            }
        });

        TextColumn<DeviceConfigurationMatcherDTO> identifierNameColumn = 
                new TextColumn<DeviceConfigurationMatcherDTO>() {
            @Override
            public String getValue(DeviceConfigurationMatcherDTO identifier) {
                return DeviceConfigurationPanel.renderIdentifiers(identifier.clients);
            }
        };
        identifierNameColumn.setSortable(true);
        columnSortHandler.setComparator(identifierNameColumn, new Comparator<DeviceConfigurationMatcherDTO>() {
            @Override
            public int compare(DeviceConfigurationMatcherDTO r1, DeviceConfigurationMatcherDTO r2) {
                return r1.toString().compareTo(r2.toString());
            }
        });

        table.addColumn(identifierTypeColumn, "Matcher");
        table.addColumn(identifierNameColumn, "Device");
        return table;
    }

    private List<DeviceConfigurationMatcherDTO> getSelectedConfigurations() {
        List<DeviceConfigurationMatcherDTO> result = new ArrayList<DeviceConfigurationMatcherDTO>();
        if (configurationsDataProvider != null) {
            for (DeviceConfigurationMatcherDTO regatta : configurationsDataProvider.getList()) {
                if (configurationSelectionModel.isSelected(regatta)) {
                    result.add(regatta);
                }
            }
        }
        return result;
    }
}
