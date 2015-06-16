package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.net.URL;
import java.util.Date;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public abstract class NewsEntryDTO implements DTO {
    
    private Date timestamp;
    
    private String title;

    private String externalURL;

    protected NewsEntryDTO() {
    }
    
    @GwtIncompatible
    public NewsEntryDTO(String title, Date timestamp, URL externalURL) {
        this(title, timestamp, externalURL == null ? null : externalURL.toString());
    }
    
    @GwtIncompatible
    private NewsEntryDTO(String title, Date timestamp, String externalURL) {
        this.title = title;
        this.timestamp = timestamp;
        this.externalURL = externalURL;
    }

    public String getTitle() {
        return title;
    }

    public abstract String getMessage();
    
    public abstract String getBoatClass();
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public String getExternalURL() {
        return externalURL;
    }
}
