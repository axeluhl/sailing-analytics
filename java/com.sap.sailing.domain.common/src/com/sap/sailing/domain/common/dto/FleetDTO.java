package com.sap.sailing.domain.common.dto;

import com.sap.sailing.domain.common.Color;

public class FleetDTO extends NamedDTO {
    private static final long serialVersionUID = 1336494392278190103L;
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
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
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
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        if (orderNo != other.orderNo)
            return false;
        return true;
    }

}
