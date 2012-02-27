package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;

public class StrippedRaceDTO extends NamedDTO implements IsSerializable {

    public RaceIdentifier identifier;
    public PlacemarkOrderDTO places;
    
    public Date startOfRace;
    public Date startOfTracking;
    public Date endOfTracking;
    public Date endOfRace;
    
    /**
     * Empty default constructor for GWT-Serialization
     */
    StrippedRaceDTO() {}
    
    public StrippedRaceDTO(String name, RaceIdentifier identifier, PlacemarkOrderDTO places) {
        super(name);
        this.identifier = identifier;
        this.places = places;
    }

}
