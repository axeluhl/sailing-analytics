package com.sap.sailing.gwt.home.mobile.partials.updatesBox;

import java.util.List;

import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public interface NewsItemLinkProvider {
    
    PlaceNavigation<?> getNewsPlaceNavigation(List<NewsEntryDTO> values);
    
    PlaceNavigation<?> getNewsEntryPlaceNavigation(NewsEntryDTO newsEntry);
}
