package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.Color;

public class FleetDTO extends NamedDTO implements IsSerializable {
    private Color color;
    private int orderNo;
    
    public FleetDTO() {}

    public FleetDTO(String fleetName, int orderNo, Color color) {
        super(fleetName);
        this.orderNo = orderNo;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public int getOrderNo() {
        return orderNo;
    }
}
