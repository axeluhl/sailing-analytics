package com.sap.sailing.gwt.home.client.place.event2.model;

import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventReferenceDTO implements IsSerializable {
    private UUID id;
    private String displayName;
    public EventReferenceDTO() {
    }
    
    public EventReferenceDTO(UUID id, String name, String regattaName) {
        super();
        this.id = id;
        this.displayName = name;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String name) {
        this.displayName = name;
    }
}
