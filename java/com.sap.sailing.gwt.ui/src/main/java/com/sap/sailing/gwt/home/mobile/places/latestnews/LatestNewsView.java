package com.sap.sailing.gwt.home.mobile.places.latestnews;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.NewsItemLinkProvider;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public interface LatestNewsView {

    public interface Presenter extends NewsItemLinkProvider {
        PlaceNavigation<?> getEventNavigation();
        
        void gotoEvents();

        DispatchSystem getDispatch();

        EventContext getCtx();
    }

    Widget asWidget();

    void showNews(List<NewsEntryDTO> news);
}
