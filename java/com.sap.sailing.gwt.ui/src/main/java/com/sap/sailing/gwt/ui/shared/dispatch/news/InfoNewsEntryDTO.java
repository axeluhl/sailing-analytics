package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Locale;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.news.impl.InfoEventNewsItem;

public class InfoNewsEntryDTO extends NewsEntryDTO {
    
    private String subtitle;
    
    @SuppressWarnings("unused")
    private InfoNewsEntryDTO() {
    }

    @GwtIncompatible
    public InfoNewsEntryDTO(InfoEventNewsItem item, Locale locale) {
        super(item.getTitle(locale), item.getCreatedAtDate(), item.getRelatedItemLink());
        this.subtitle = item.getMessage(locale);
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
