package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventDTO extends NamedDTO implements IsSerializable {
    public List<RegattaDTO> regattas;
    public VenueDTO venue;
    public String publicationUrl;
    public boolean isPublic;
    public String id;

    // maybe temporary: as long it's not clear how a leaderboard group relates to a sailing event
    public LeaderboardGroupDTO leaderboardGroup;
    
    public EventDTO() {}

    public EventDTO(String name) {
        super(name);
        regattas = new ArrayList<RegattaDTO>();
    }
}
