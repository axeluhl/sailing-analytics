package com.sap.sailing.gwt.home.mobile.places.minileaderboard;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.error.ErrorPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.minileaderboard.MiniLeaderboardView.Presenter;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventViewAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;

public class MiniLeaderboardActivity extends AbstractActivity implements Presenter {
    private final MobileApplicationClientFactory clientFactory;
    private final MiniLeaderboardPlace place;

    public MiniLeaderboardActivity(MiniLeaderboardPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        if (place.getCtx().getEventDTO() == null) {
            UUID currentEventUUId = UUID.fromString(place.getCtx().getEventId());
            clientFactory.getDispatch().execute(new GetEventViewAction(currentEventUUId),
                    new AsyncCallback<EventViewDTO>() {
                        @Override
                        public void onSuccess(final EventViewDTO event) {
                            place.getCtx().updateContext(event);
                            initUi(panel, eventBus);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            // TODO @FM: extract text?
                            ErrorPlace errorPlace = new ErrorPlace(
                                    "Error while loading the event with service getEventViewById()");
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
        final MiniLeaderboardView view = new MiniLeaderboardViewImpl(this);
        view.setQuickFinderValues(place.getCtx().getEventDTO().getRegattas());
        panel.setWidget(view.asWidget());
    }

    @Override
    public EventContext getCtx() {
        return place.getCtx();
    }

    @Override
    public PlaceNavigation<?> getRegattaLeaderboardNavigation(String leaderboardName) {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(leaderboardName).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new RegattaLeaderboardPlace(ctx), null, false);
    }

    @Override
    public DispatchSystem getDispatch() {
        return clientFactory.getDispatch();
    }

    @Override
    public PlaceNavigation<?> getRegattaMiniLeaderboardNavigation(String leaderboardName) {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(leaderboardName).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new MiniLeaderboardPlace(ctx), null, false);
    }

}
