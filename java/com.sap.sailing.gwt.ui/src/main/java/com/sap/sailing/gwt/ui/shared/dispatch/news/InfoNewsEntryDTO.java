package com.sap.sailing.gwt.ui.shared.dispatch.news;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.news.impl.InfoEventNewsItem;

public class InfoNewsEntryDTO extends NewsEntryDTO {
    
    private String subtitle;
    
    @SuppressWarnings("unused")
    private InfoNewsEntryDTO() {
    }

    @GwtIncompatible
    public InfoNewsEntryDTO(InfoEventNewsItem item) {
        super(item.getTitle(), item.getCreatedAtDate());
        this.subtitle = item.getMessage();
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
