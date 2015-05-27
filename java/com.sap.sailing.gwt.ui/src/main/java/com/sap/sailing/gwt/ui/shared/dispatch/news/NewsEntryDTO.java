package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public abstract class NewsEntryDTO implements DTO {
    
    private Date timestamp;

    protected NewsEntryDTO() {
    }
    
    public NewsEntryDTO(Date timestamp) {
        this.timestamp = timestamp;
    }

    public abstract String getTitle();

    public abstract String getMessage();
    
    public abstract String getBoatClass();
    
    public Date getTimestamp() {
        return timestamp;
    }
}
