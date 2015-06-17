package com.sap.sailing.gwt.home.mobile.places.latestnews;

import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.NewsItemLinkProvider;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsView.Presenter;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.shared.dispatch.news.AbstractRaceNewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.news.LeaderboardNewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public class LatestNewsActivity extends AbstractActivity implements Presenter, NewsItemLinkProvider {
    private final MobileApplicationClientFactory clientFactory;
    private final LatestNewsPlace place;

    public LatestNewsActivity(LatestNewsPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        final LatestNewsView view = new LatestNewsViewImpl(this);
        panel.setWidget(view.asWidget());
        Window.setTitle(place.getTitle());
        view.showNews(place.getNews());
    }

    @Override
    public void gotoEvents() {
        clientFactory //
                .getNavigator() //
                .getEventsNavigation()//
                .goToPlace();
    }

    @Override
    public PlaceNavigation<?> getPlaceNavigation(NewsEntryDTO entry) {
        MobilePlacesNavigator navigator = clientFactory.getNavigator();
        if (entry instanceof LeaderboardNewsEntryDTO) {
            final LeaderboardNewsEntryDTO dto = (LeaderboardNewsEntryDTO) entry;
            final String regattaId = dto.getLeaderboardName();
            navigator.getEventNavigation(new RegattaLeaderboardPlace(new EventContext().withRegattaId(regattaId)),
                    null, false);
        } else if (entry instanceof AbstractRaceNewsEntryDTO) {
            // TODO
        }
        return null;
    }

    @Override
    public void gotoNewsPlace(List<NewsEntryDTO> values) {
        clientFactory.getPlaceController().goTo(new LatestNewsPlace(place.getCtx(), values));
    }

    @Override
    public EventContext getCtx() {
        return place.getCtx();
    }

    @Override
    public DispatchSystem getDispatch() {
        return clientFactory.getDispatch();
    }
}
