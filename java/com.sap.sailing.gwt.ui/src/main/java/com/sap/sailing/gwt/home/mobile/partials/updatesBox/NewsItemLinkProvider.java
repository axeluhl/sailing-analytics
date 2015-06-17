package com.sap.sailing.gwt.home.mobile.partials.updatesBox;

import java.util.List;

import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public interface NewsItemLinkProvider {
    PlaceNavigation<?> getPlaceNavigation(NewsEntryDTO newsEntry);

    void gotoNewsPlace(List<NewsEntryDTO> values);
}
