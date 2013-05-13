package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.Color;

public class FleetDTO extends NamedDTO implements IsSerializable {
    private Color color;
    private int orderNo;
    
    /** the name of the series the fleet belongs to */
    private String seriesName;
    
    public FleetDTO() {}

    public FleetDTO(String fleetName, String seriesName, int orderNo, Color color) {
        super(fleetName);
        this.seriesName = seriesName;
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

    public String getSeriesName() {
        return seriesName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + orderNo;
        result = prime * result + ((seriesName == null) ? 0 : seriesName.hashCode());
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
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (orderNo != other.orderNo)
            return false;
        if (seriesName == null) {
            if (other.seriesName != null)
                return false;
        } else if (!seriesName.equals(other.seriesName))
            return false;
        return true;
    }

}
