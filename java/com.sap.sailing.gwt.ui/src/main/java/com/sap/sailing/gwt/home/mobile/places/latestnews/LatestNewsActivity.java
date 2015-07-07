package com.sap.sailing.gwt.home.mobile.places.latestnews;

import java.util.List;
import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.error.ErrorPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.NewsItemLinkProvider;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsView.Presenter;
import com.sap.sailing.gwt.home.mobile.places.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventViewAction;
import com.sap.sailing.gwt.ui.shared.dispatch.news.LeaderboardNewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;

public class LatestNewsActivity extends AbstractActivity implements Presenter, NewsItemLinkProvider {
    private final MobileApplicationClientFactory clientFactory;
    private final LatestNewsPlace place;

    public LatestNewsActivity(LatestNewsPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        if(place.getCtx().getEventDTO() == null) {
            UUID currentEventUUId = UUID.fromString(place.getCtx().getEventId());
            clientFactory.getDispatch().execute(new GetEventViewAction(currentEventUUId), new AsyncCallback<EventViewDTO>() {
                @Override
                public void onSuccess(final EventViewDTO event) {
                    place.getCtx().updateContext(event);
                    initUi(panel, eventBus);
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    // TODO @FM: extract text?
                    ErrorPlace errorPlace = new ErrorPlace("Error while loading the event with service getEventViewById()");
                    // TODO @FM: reload sinnvoll hier?
                    errorPlace.setComingFrom(place);
                    clientFactory.getPlaceController().goTo(errorPlace);
                }
            });
        } else {
            initUi(panel, eventBus);
        }
    }
    
    private void initUi(AcceptsOneWidget panel, EventBus eventBus) {
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
    public PlaceNavigation<?> getNewsEntryPlaceNavigation(NewsEntryDTO entry) {
        if (entry instanceof LeaderboardNewsEntryDTO) {
            final LeaderboardNewsEntryDTO dto = (LeaderboardNewsEntryDTO) entry;
            return getRegattaLeaderboardNavigation(dto.getLeaderboardName());
        }
        return null;
    }

    public PlaceNavigation<?> getRegattaLeaderboardNavigation(String leaderboardName) {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(leaderboardName).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new MiniLeaderboardPlace(ctx), null, false);
    }

    @Override
    public PlaceNavigation<?> getNewsPlaceNavigation(List<NewsEntryDTO> values) {
        return clientFactory.getNavigator().getEventLastestNewsNavigation(getCtx(), values, null, false);
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
