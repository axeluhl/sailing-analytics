package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class NewsEntryDTO implements IsSerializable {
    
    private Date timestamp;
    
    private String title;

    protected NewsEntryDTO() {
    }
    
    public NewsEntryDTO(String title, Date timestamp) {
        this.title = title;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public abstract String getMessage();
    
    public abstract String getBoatClass();
    
    public Date getTimestamp() {
        return timestamp;
    }
}
