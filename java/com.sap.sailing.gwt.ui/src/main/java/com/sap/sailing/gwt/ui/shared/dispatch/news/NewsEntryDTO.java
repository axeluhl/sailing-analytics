package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.net.URL;
import java.util.Date;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public abstract class NewsEntryDTO implements DTO {
    
    private Date newsTimestamp;
    
    private Date currentTimestamp;
    
    private String title;

    private String externalURL;

    protected NewsEntryDTO() {
    }
    
    @GwtIncompatible
    public NewsEntryDTO(String title, Date timestamp, Date currentTimestamp, URL externalURL) {
        this(title, timestamp, currentTimestamp, externalURL == null ? null : externalURL.toString());
    }
    
    @GwtIncompatible
    private NewsEntryDTO(String title, Date newsTimestamp, Date currentTimestamp, String externalURL) {
        this.title = title;
        this.newsTimestamp = newsTimestamp;
        this.currentTimestamp = currentTimestamp;
        this.externalURL = externalURL;
    }

    public String getTitle() {
        return title;
    }

    public abstract String getMessage();
    
    public abstract String getBoatClass();
    
    public Date getTimestamp() {
        return newsTimestamp;
    }
    
    public String getExternalURL() {
        return externalURL;
    }

    public Date getCurrentTimestamp() {
        return currentTimestamp;
    }
}
