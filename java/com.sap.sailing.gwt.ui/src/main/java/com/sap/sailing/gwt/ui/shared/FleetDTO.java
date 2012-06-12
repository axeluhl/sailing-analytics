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

    public void setColor(Color color) {
        this.color = color;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + orderNo;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FleetDTO other = (FleetDTO) obj;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.equals(other.color))
            return false;
        if (orderNo != other.orderNo)
            return false;
        return true;
    }
}
