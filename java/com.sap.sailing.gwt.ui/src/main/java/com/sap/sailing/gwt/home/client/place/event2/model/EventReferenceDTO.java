package com.sap.sailing.gwt.home.client.place.event2.model;

import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventReferenceDTO implements IsSerializable {
    private UUID id;
    private String name;
    private String regattaName;
    public EventReferenceDTO() {
    }
    
    public EventReferenceDTO(UUID id, String name, String regattaName) {
        super();
        this.id = id;
        this.name = name;
        this.regattaName = regattaName;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRegattaName() {
        return regattaName;
    }
    public void setRegattaName(String regattaName) {
        this.regattaName = regattaName;
    }
}
