package com.sap.sailing.gwt.home.shared.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class EventDTO extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = -7100030301376959817L;
    public List<RegattaDTO> regattas;
    public VenueDTO venue;
    public Date startDate;
    public Date endDate;
    public boolean isPublic;
    public String uuid;

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
