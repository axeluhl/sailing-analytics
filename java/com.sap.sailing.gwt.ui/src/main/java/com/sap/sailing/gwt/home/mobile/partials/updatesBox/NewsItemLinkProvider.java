package com.sap.sailing.gwt.home.mobile.partials.updatesBox;

import java.util.List;

import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public interface NewsItemLinkProvider {
    void gotoNewsPlace(List<NewsEntryDTO> values);

    PlaceNavigation<?> getNewsEntryPlaceNavigation(NewsEntryDTO newsEntry);
}
