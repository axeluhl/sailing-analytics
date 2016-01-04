package com.sap.sailing.gwt.home.mobile.places.event.latestnews;

import java.util.List;

import com.sap.sailing.gwt.home.communication.event.news.NewsEntryDTO;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.NewsItemLinkProvider;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;

public interface LatestNewsView extends EventViewBase {

    public interface Presenter extends EventViewBase.Presenter, NewsItemLinkProvider {
        
    }

    void showNews(List<NewsEntryDTO> news);
}
