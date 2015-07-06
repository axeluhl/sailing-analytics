package com.sap.sailing.gwt.home.mobile.places.series;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.error.ErrorPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;

public class SeriesActivity extends AbstractActivity implements SeriesView.Presenter {
    private final MobileApplicationClientFactory clientFactory;
    private final AbstractSeriesPlace place;
    private UUID currentEventUUId;
    
    public SeriesActivity(AbstractSeriesPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        final SeriesContext ctx = place.getCtx();
        if (ctx.getSeriesDTO() != null) {
            initUi(panel, eventBus);
        } else {
            final UUID seriesUUID = UUID.fromString(ctx.getSeriesId());
            clientFactory.getHomeService().getEventSeriesViewById(seriesUUID, new AsyncCallback<EventSeriesViewDTO>() {
                @Override
                public void onSuccess(final EventSeriesViewDTO event) {
                    ctx.updateContext(event);
                    initUi(panel, eventBus);
                }
                @Override
                public void onFailure(Throwable caught) {
                    // TODO @FM: extract text?
                    ErrorPlace errorPlace = new ErrorPlace(
                            "Error while loading the series with service getEventSeriesViewById()");
                    // TODO @FM: reload sinnvoll hier?
                    errorPlace.setComingFrom(place);
                    clientFactory.getPlaceController().goTo(errorPlace);
                }
            });
        }
    }
    
    private void initUi(final AcceptsOneWidget panel, EventBus eventBus) {
        final SeriesView view = new SeriesViewImpl(this);
        panel.setWidget(view.asWidget());
    }
    
    @Override
    public SeriesContext getCtx() {
        return place.getCtx();
    }

    public DispatchSystem getDispatch() {
        return clientFactory.getDispatch();
    }
    
//    @Override
//    public PlaceNavigation<?> getRegattaLeaderboardNavigation(String leaderboardName) {
//        EventContext ctx = new EventContext(getCtx()).withRegattaId(leaderboardName).withRegattaAnalyticsManager(null);
//        return clientFactory.getNavigator().getEventNavigation(new RegattaLeaderboardPlace(ctx), null, false);
//    }
//
//    @Override
//    public PlaceNavigation<?> getRegattaMiniLeaderboardNavigation(String leaderboardName) {
//        EventContext ctx = new EventContext(getCtx()).withRegattaId(leaderboardName).withRegattaAnalyticsManager(null);
//        return clientFactory.getNavigator().getEventNavigation(new MiniLeaderboardPlace(ctx), null, false);
//    }
}
