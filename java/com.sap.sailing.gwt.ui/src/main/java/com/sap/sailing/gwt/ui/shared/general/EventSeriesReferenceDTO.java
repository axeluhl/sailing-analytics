package com.sap.sailing.gwt.ui.shared.general;

import java.util.UUID;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class EventSeriesReferenceDTO implements DTO {

    private UUID id;
    private String displayName;

    protected EventSeriesReferenceDTO() {
    }

    public EventSeriesReferenceDTO(UUID id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

}
