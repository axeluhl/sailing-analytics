package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;

public class DeviceIdentifierTableWrapper extends TableWrapper<DeviceIdentifierDTO, RefreshableMultiSelectionModel<DeviceIdentifierDTO>> {

    public DeviceIdentifierTableWrapper(SailingServiceWriteAsync sailingServiceWrite, StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingServiceWrite, stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */ false,
                new EntityIdentityComparator<DeviceIdentifierDTO>() {
                    @Override
                    public boolean representSameEntity(DeviceIdentifierDTO dto1, DeviceIdentifierDTO dto2) {
                        return dto1.deviceId.equals(dto2.deviceId);
                    }
                    @Override
                    public int hashCode(DeviceIdentifierDTO t) {
                        return t.deviceId.hashCode();
                    }
                });
        
        TextColumn<DeviceIdentifierDTO> typeColumn = new TextColumn<DeviceIdentifierDTO>() {
            @Override
            public String getValue(DeviceIdentifierDTO object) {
                return object.deviceType;
            }
        };
        
        TextColumn<DeviceIdentifierDTO> idColumn = new TextColumn<DeviceIdentifierDTO>() {
            @Override
            public String getValue(DeviceIdentifierDTO object) {
                return object.deviceId;
            }
        };
        
        table.addColumn(typeColumn, stringMessages.deviceType());
        table.addColumn(idColumn, stringMessages.deviceId());
    }

}
