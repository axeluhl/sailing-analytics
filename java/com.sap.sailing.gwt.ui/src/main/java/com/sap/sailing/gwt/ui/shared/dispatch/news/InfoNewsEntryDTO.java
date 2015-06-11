package com.sap.sailing.gwt.ui.shared.dispatch.news;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.news.impl.InfoEventNewsItem;

public class InfoNewsEntryDTO extends NewsEntryDTO {
    
    private String title;
    private String subtitle;
    
    @SuppressWarnings("unused")
    private InfoNewsEntryDTO() {
    }

    @GwtIncompatible
    public InfoNewsEntryDTO(InfoEventNewsItem item) {
        super(item.getCreatedAtDate());
        this.title = item.getTitle();
        this.subtitle = item.getMessage();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getMessage() {
        return subtitle;
    }

    @Override
    public String getBoatClass() {
        return null;
    }
}
