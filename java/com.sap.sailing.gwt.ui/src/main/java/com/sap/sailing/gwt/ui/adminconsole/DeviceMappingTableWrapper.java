package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;
import java.util.List;

import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;

public class DeviceMappingTableWrapper extends TableWrapper<DeviceMappingDTO, SingleSelectionModel<DeviceMappingDTO>> {

    public DeviceMappingTableWrapper(SailingServiceAsync sailingService, final StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter, new SingleSelectionModel<DeviceMappingDTO>());
        
        ListHandler<DeviceMappingDTO> listHandler = new ListHandler<DeviceMappingDTO>(dataProvider.getList());
        
        TextColumn<DeviceMappingDTO> itemTypeCol = new TextColumn<DeviceMappingDTO>() {
            @Override
            public String getValue(DeviceMappingDTO mapping) {
                if (mapping.mappedTo instanceof CompetitorDTO) return stringMessages.competitor();
                else return stringMessages.mark();
            }
        };
        itemTypeCol.setSortable(true);
        listHandler.setComparator(itemTypeCol, new Comparator<DeviceMappingDTO>() {
            @Override
            public int compare(DeviceMappingDTO o1, DeviceMappingDTO o2) {
                return o1.mappedTo.getClass().getName().compareTo(o2.mappedTo.getClass().getName());
            }
        });
        table.addColumn(itemTypeCol, stringMessages.mappedToType());
        
        TextColumn<DeviceMappingDTO> itemCol = new TextColumn<DeviceMappingDTO>() {
            @Override
            public String getValue(DeviceMappingDTO mapping) {
                return mapping.mappedTo.toString();
            }
        };
        itemCol.setSortable(true);
        listHandler.setComparator(itemCol, new Comparator<DeviceMappingDTO>() {
            @Override
            public int compare(DeviceMappingDTO o1, DeviceMappingDTO o2) {
                return o1.mappedTo.toString().compareTo(o2.mappedTo.toString());
            }
        });
        table.addColumn(itemCol, stringMessages.mappedTo());
        
        TextColumn<DeviceMappingDTO> deviceTypeCol = new TextColumn<DeviceMappingDTO>() {
            @Override
            public String getValue(DeviceMappingDTO mapping) {
                return mapping.deviceIdentifier.deviceType;
            }
        };
        deviceTypeCol.setSortable(true);
        listHandler.setComparator(deviceTypeCol, new Comparator<DeviceMappingDTO>() {
            @Override
            public int compare(DeviceMappingDTO o1, DeviceMappingDTO o2) {
                return o1.deviceIdentifier.deviceType.compareTo(o2.deviceIdentifier.deviceType);
            }
        });
        table.addColumn(deviceTypeCol, stringMessages.deviceType());
        
        TextColumn<DeviceMappingDTO> deviceIdCol = new TextColumn<DeviceMappingDTO>() {
            @Override
            public String getValue(DeviceMappingDTO mapping) {
                return mapping.deviceIdentifier.deviceId;
            }
        };
        deviceIdCol.setSortable(true);
        listHandler.setComparator(deviceIdCol, new Comparator<DeviceMappingDTO>() {
            @Override
            public int compare(DeviceMappingDTO o1, DeviceMappingDTO o2) {
                return o1.deviceIdentifier.deviceId.compareTo(o2.deviceIdentifier.deviceId);
            }
        });
        table.addColumn(deviceIdCol, stringMessages.deviceId());
        
        TextColumn<DeviceMappingDTO> fromCol = new TextColumn<DeviceMappingDTO>() {
            @Override
            public String getValue(DeviceMappingDTO mapping) {
                return DateAndTimeFormatterUtil.formatDateAndTime(mapping.from);
            }
        };
        fromCol.setSortable(true);
        listHandler.setComparator(fromCol, new Comparator<DeviceMappingDTO>() {
            @Override
            public int compare(DeviceMappingDTO o1, DeviceMappingDTO o2) {
                return o1.from.compareTo(o2.from);
            }
        });
        table.addColumn(fromCol, stringMessages.from());
        
        TextColumn<DeviceMappingDTO> toCol = new TextColumn<DeviceMappingDTO>() {
            @Override
            public String getValue(DeviceMappingDTO mapping) {
                return DateAndTimeFormatterUtil.formatDateAndTime(mapping.to);
            }
        };
        toCol.setSortable(true);
        listHandler.setComparator(toCol, new Comparator<DeviceMappingDTO>() {
            @Override
            public int compare(DeviceMappingDTO o1, DeviceMappingDTO o2) {
                return o1.to.compareTo(o2.to);
            }
        });
        table.addColumn(toCol, stringMessages.to());

        table.addColumnSortHandler(listHandler);
        mainPanel.add(table);
    }
    
    public void refresh(List<DeviceMappingDTO> mappings) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(mappings);
    }
}
