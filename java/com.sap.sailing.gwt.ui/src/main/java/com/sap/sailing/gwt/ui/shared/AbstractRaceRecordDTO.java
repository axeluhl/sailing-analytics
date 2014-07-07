package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.gwt.ui.adminconsole.RaceRecordDTO;

public abstract class AbstractRaceRecordDTO implements RaceRecordDTO {
    private String name;
    private boolean hasRememberedRegatta;
    
    protected AbstractRaceRecordDTO() {} // for serialization only
    
    protected AbstractRaceRecordDTO(String name, boolean hasRememberedRegatta) {
        super();
        this.name = name;
        this.hasRememberedRegatta = hasRememberedRegatta;
    }

    @Override
    public boolean hasRememberedRegatta() {
        return hasRememberedRegatta;
    }

    @Override
    public String getName() {
        return name;
    }

}
