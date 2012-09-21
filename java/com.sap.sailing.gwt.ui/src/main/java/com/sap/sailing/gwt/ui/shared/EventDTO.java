package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventDTO extends NamedDTO implements IsSerializable {
    public List<RegattaDTO> regattas;
    public VenueDTO venue;
    public String publicationUrl;
    
    EventDTO() {}

    public EventDTO(String name) {
        super(name);
    }
}
