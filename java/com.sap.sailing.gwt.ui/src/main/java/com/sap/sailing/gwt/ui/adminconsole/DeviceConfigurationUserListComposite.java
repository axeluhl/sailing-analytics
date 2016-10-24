package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.BaseCelltable;

public class DeviceConfigurationUserListComposite extends DeviceConfigurationListComposite {

    public DeviceConfigurationUserListComposite(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        super(sailingService, errorReporter, stringMessages);
    }
    
    @Override
    protected CellTable<DeviceConfigurationMatcherDTO> createConfigurationTable() {
        CellTable<DeviceConfigurationMatcherDTO> table = new BaseCelltable<DeviceConfigurationMatcherDTO>(
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
                return DeviceConfigurationPanel.renderIdentifiers(identifier.clients, stringMessages);
            }
        };
        identifierNameColumn.setSortable(true);
        columnSortHandler.setComparator(identifierNameColumn, new Comparator<DeviceConfigurationMatcherDTO>() {
            @Override
            public int compare(DeviceConfigurationMatcherDTO r1, DeviceConfigurationMatcherDTO r2) {
                return r1.clients.toString().compareTo(r2.clients.toString());
            }
        });

        table.addColumn(identifierNameColumn, stringMessages.device());
        return table;
    }

}
