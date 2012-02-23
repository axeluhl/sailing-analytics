package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PlacemarkOrderDTO extends NamedDTO implements IsSerializable {
    
    private List<PlacemarkDTO> placemarks;
    
    public PlacemarkOrderDTO() {
        placemarks = new ArrayList<PlacemarkDTO>();
    }
    
    public PlacemarkOrderDTO(Collection<PlacemarkDTO> placemarks) {
        this.placemarks = new ArrayList<PlacemarkDTO>(placemarks);
    }
    
    public List<PlacemarkDTO> getPlacemarks() {
        return placemarks;
    }
    
    public void setPlacemarks(Collection<PlacemarkDTO> placemarks) {
        this.placemarks = new ArrayList<PlacemarkDTO>(placemarks);;
    }
    
    /**
     * Builds a string with all {@link PlacemarkDTO placemarks} of this order. Each string representation of a placemark has the format given by {@link PlacemarkDTO#asString()}.<br />
     * If consecutive placemarks are equal, only one string representation is added ('... -> DE, Kiel -> DE, Kiel -> ...' will be represented by '... -> DE, Kiel -> ...').
     */
    public String placemarksAsString() {
        StringBuilder sb = new StringBuilder();
        if (!placemarks.isEmpty()) {
            PlacemarkDTO placemarkBefore = placemarks.get(0);
            sb.append(placemarkBefore.asString());
            for (int i = 1; i < placemarks.size(); i++) {
                PlacemarkDTO placemark = placemarks.get(i);
                if (!placemarkBefore.equals(placemark)) {
                    sb.append(" -> ");
                    sb.append(placemark.asString());
                    placemarkBefore = placemark;
                }
            }
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return placemarks.toString();
    }
    
    /**
     * Like {@link PlacemarkOrderDTO#placemarksAsString()}, but builds a representation of all placemarks in the
     * PlacemarkOrderDTOs of <code>orders</code>.<br />
     * The order of the PlacemarkOrderDTOs in <code>orders</code> has a direct impact of the returning representation.
     * 
     * @param orders
     *            The PlacemarkOrderDTOs which should be represented as string.
     * @return A representation of <code>orders</code>
     */
    public static String placemarksOfAllOrdersAsString(Iterable<PlacemarkOrderDTO> orders) {
        PlacemarkOrderDTO superOrder = new PlacemarkOrderDTO();
        for (PlacemarkOrderDTO order : orders) {
            superOrder.placemarks.addAll(order.placemarks);
        }
        return superOrder.placemarksAsString();
    }
    
}
