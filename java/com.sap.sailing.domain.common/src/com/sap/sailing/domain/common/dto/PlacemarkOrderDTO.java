package com.sap.sailing.domain.common.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.sap.sse.security.shared.NamedDTO;

public class PlacemarkOrderDTO extends NamedDTO {
    private static final long serialVersionUID = -4981887843843495494L;
    private List<PlacemarkDTO> placemarks;
    
    public PlacemarkOrderDTO() {
        placemarks = new ArrayList<PlacemarkDTO>();
    }
    
    public PlacemarkOrderDTO(Collection<PlacemarkDTO> placemarks) {
        this.placemarks = new ArrayList<PlacemarkDTO>(placemarks);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((placemarks == null) ? 0 : placemarks.hashCode());
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
        PlacemarkOrderDTO other = (PlacemarkOrderDTO) obj;
        if (placemarks == null) {
            if (other.placemarks != null)
                return false;
        } else if (!placemarks.equals(other.placemarks))
            return false;
        return true;
    }

    public List<PlacemarkDTO> getPlacemarks() {
        return placemarks;
    }
    
    /**
     * @return The list of placemarks contained by this order, consecutive placemarks which are equal will only be contained once.<br />
     *         The returning list is never <code>null</code>, but can be empty.
     */
    public List<PlacemarkDTO> getPlacemarksCompressed() {
        ArrayList<PlacemarkDTO> placemarksMerged = new ArrayList<PlacemarkDTO>();
        if (!placemarks.isEmpty()) {
            PlacemarkDTO placemarkBefore = placemarks.get(0);
            placemarksMerged.add(placemarkBefore);
            for (int i = 1; i < placemarks.size(); i++) {
                PlacemarkDTO placemark = placemarks.get(i);
                if (!placemarkBefore.equals(placemark)) {
                    placemarksMerged.add(placemark);
                    placemarkBefore = placemark;
                }
            }
        }
        return placemarksMerged;
    }
    
    public void setPlacemarks(Collection<PlacemarkDTO> placemarks) {
        this.placemarks = new ArrayList<PlacemarkDTO>(placemarks);;
    }

    public boolean isEmpty() {
        return placemarks.isEmpty();
    }
    
    /**
     * Builds a string with all {@link PlacemarkDTO placemarks} of this order. Each string representation of a placemark has the format given by {@link PlacemarkDTO#asString()}.<br />
     * If consecutive placemarks are equal, only one string representation is added ('... -> DE, Kiel -> DE, Kiel -> ...' will be represented by '... -> DE, Kiel -> ...').
     */
    public String placemarksAsString() {
        StringBuilder sb = new StringBuilder();
        List<PlacemarkDTO> placemarks = getPlacemarksCompressed();
        boolean first = true;
        if (!placemarks.isEmpty()) {
            for (PlacemarkDTO placemark : placemarks) {
                if (!first) {
                    sb.append(" -> ");
                }
                sb.append(placemark.asString());
                first = false;
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
     * @return A string representation of <code>orders</code>
     */
    public static String placemarksOfAllOrdersAsMergedString(Iterable<PlacemarkOrderDTO> orders) {
        PlacemarkOrderDTO superOrder = new PlacemarkOrderDTO();
        for (PlacemarkOrderDTO order : orders) {
            superOrder.placemarks.addAll(order.placemarks);
        }
        return superOrder.placemarksAsString();
    }
    
    /**
     * Like {@link PlacemarkOrderDTO#placemarksOfAllOrdersAsMergedString(orders)
     * PlacemarkOrderDTO.placemarksOfAllOrdersAsMergedString}, but the orders will be seperated by commas.<br />
     * The order of the PlacemarkOrderDTOs in <code>orders</code> has a direct impact of the returning representation.<br />
     * <br />
     * If the parameter <code>compressEqualOrders</code> is <code>true</code>, will consecutive orders with the same
     * {@link PlacemarkOrderDTO#getPlacemarksCompressed() stripped-down placemarks} only represented once.
     * 
     * @param orders
     *            The PlacemarkOrderDTOs which should be represented as string.
     * @param compressEqualOrders
     *            If <code>true</code>, cosecutive equal orders will be compressed
     * @return A string representation of <code>orders</code>
     */
    public static String placemarksOfAllOrderAsSeperatedString(Iterable<PlacemarkOrderDTO> orders, boolean compressEqualOrders) {
        StringBuilder sb = new StringBuilder();
        Iterator<PlacemarkOrderDTO> ordersIterator = orders.iterator();
        if (ordersIterator.hasNext()) {
            PlacemarkOrderDTO order = ordersIterator.next();
            sb.append(order.placemarksAsString());
            List<PlacemarkDTO> placemarksBefore = null;
            if (compressEqualOrders) {
                placemarksBefore = order.getPlacemarksCompressed();
            }
            
            while (ordersIterator.hasNext()) {
                order = ordersIterator.next();
                if (compressEqualOrders) {
                    List<PlacemarkDTO> placemarks = order.getPlacemarksCompressed();
                    if (!placemarksBefore.equals(placemarks)) {
                        sb.append(", ");
                        sb.append(order.placemarksAsString());
                        placemarksBefore = placemarks;
                    }
                } else {
                    sb.append(", ");
                    sb.append(order.placemarksAsString());
                }
            }
        }
        return sb.toString();
    }

    public void add(PlacemarkOrderDTO places) {
        placemarks.addAll(places.getPlacemarks());
    }
    
}
