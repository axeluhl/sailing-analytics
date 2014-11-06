package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class DeviceMappingTableWrapper extends TableWrapper<DeviceMappingDTO, SingleSelectionModel<DeviceMappingDTO>> {
    static interface FilterChangedHandler {
        void onFilterChanged(List<DeviceMappingDTO> filteredList);
    }
    private List<DeviceMappingDTO> allMappings = new ArrayList<>();
    private final CheckBox showPingMappingsCb;
    
    private final List<FilterChangedHandler> filterHandlers = new ArrayList<>();

    public DeviceMappingTableWrapper(SailingServiceAsync sailingService, final StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter, new SingleSelectionModel<DeviceMappingDTO>(), true);
        
        showPingMappingsCb = new CheckBox(stringMessages.showPingMarkMappings());
        showPingMappingsCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                filterPingMappings();
            }
        });
        mainPanel.insert(showPingMappingsCb, 0);
        
        table.setWidth("1000px", true);
        table.addStyleName("wrap-cols");
        
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
        table.setColumnWidth(deviceIdCol, 400, Unit.PX);
        
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
    }
    
    private void notifyFilterHandlers() {
        for (FilterChangedHandler handler : filterHandlers) {
            handler.onFilterChanged(getDataProvider().getList());
        }
    }
    
    private void filterPingMappings() {
        boolean show = showPingMappingsCb.getValue();
        getDataProvider().getList().clear();
        for (DeviceMappingDTO mapping : allMappings) {
            if (show || ! "PING".equals(mapping.deviceIdentifier.deviceType)) {
                getDataProvider().getList().add(mapping);
            }
        }
        notifyFilterHandlers();
    }
    
    public void refresh(List<DeviceMappingDTO> mappings) {
        allMappings = mappings;
        filterPingMappings();
    }
    
    @Override
    public Widget asWidget() {
        return  mainPanel;
    }
    
    public void addFilterChangedHandler(FilterChangedHandler handler) {
        filterHandlers.add(handler);
    }
}
