package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SelectionProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class DeviceConfigurationUserListComposite extends DeviceConfigurationListComposite {

    public DeviceConfigurationUserListComposite(SailingServiceAsync sailingService,
            SelectionProvider<DeviceConfigurationMatcherDTO> selectionProvider, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        super(sailingService, selectionProvider, errorReporter, stringMessages);
    }
    
    @Override
    protected CellTable<DeviceConfigurationMatcherDTO> createConfigurationTable() {
        CellTable<DeviceConfigurationMatcherDTO> table = new CellTable<DeviceConfigurationMatcherDTO>(
                /* pageSize */10000, tableResource);
        configurationsDataProvider.addDataDisplay(table);
        table.setWidth("100%");

        ListHandler<DeviceConfigurationMatcherDTO> columnSortHandler = 
                new ListHandler<DeviceConfigurationMatcherDTO>(configurationsDataProvider.getList());
        table.addColumnSortHandler(columnSortHandler);

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

        table.addColumn(identifierNameColumn, "Device");
        return table;
    }

}
