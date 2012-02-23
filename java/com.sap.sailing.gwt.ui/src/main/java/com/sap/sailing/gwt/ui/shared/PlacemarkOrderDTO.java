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
    
}
