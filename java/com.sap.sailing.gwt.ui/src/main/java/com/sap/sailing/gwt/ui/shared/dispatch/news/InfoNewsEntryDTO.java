package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

public class InfoNewsEntryDTO extends NewsEntryDTO {
    
    private String title;
    private String subtitle;
    
    @SuppressWarnings("unused")
    private InfoNewsEntryDTO() {
    }

    public InfoNewsEntryDTO(String title, String subtitle, Date timestamp) {
        super(timestamp);
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSubtitle() {
        return subtitle;
    }

    @Override
    public String getBoatClass() {
        return null;
    }
}
