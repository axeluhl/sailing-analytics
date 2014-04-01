package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class EventDTO extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = -7100030301376959817L;
    public List<RegattaDTO> regattas;
    public VenueDTO venue;
    public Date startDate;
    public Date endDate;
    public boolean isPublic;
    public UUID id;

    // maybe temporary: as long it's not clear how a leaderboard group relates to a sailing event
    public LeaderboardGroupDTO leaderboardGroup;

    private Date currentServerTime;
    
    public EventDTO() {
        initCurrentServerTime();
    }

    public EventDTO(String name) {
        super(name);
        initCurrentServerTime();
        regattas = new ArrayList<RegattaDTO>();
    }

    private void initCurrentServerTime() {
        currentServerTime = new Date();
    }

    public Date getCurrentServerTime() {
        return currentServerTime;
    }
}
