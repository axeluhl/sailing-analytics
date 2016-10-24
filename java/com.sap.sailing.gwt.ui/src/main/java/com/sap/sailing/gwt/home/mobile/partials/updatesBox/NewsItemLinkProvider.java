package com.sap.sailing.gwt.home.mobile.partials.updatesBox;

import java.util.List;

import com.sap.sailing.gwt.home.communication.event.news.NewsEntryDTO;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;

public interface NewsItemLinkProvider {
    
    PlaceNavigation<?> getNewsPlaceNavigation(List<NewsEntryDTO> values);
    
    PlaceNavigation<?> getNewsEntryPlaceNavigation(NewsEntryDTO newsEntry);
}
