package com.sap.sailing.gwt.home.mobile.places.series.minileaderboard;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.EventSeriesOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.desktop.places.error.ErrorPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;

public class SeriesMiniOverallLeaderboardActivity extends AbstractActivity implements SeriesMiniOverallLeaderboardView.Presenter {
    private final MobileApplicationClientFactory clientFactory;
    private final SeriesMiniOverallLeaderboardPlace place;

    public SeriesMiniOverallLeaderboardActivity(SeriesMiniOverallLeaderboardPlace place, MobileApplicationClientFactory clientFactory) {
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

    private void initUi(AcceptsOneWidget panel, EventBus eventBus) {
        final SeriesMiniOverallLeaderboardView view = new SeriesMiniOverallLeaderboardViewImpl(this);
        EventSeriesViewDTO series = getCtx().getSeriesDTO();
        view.setQuickFinderValues(series.getDisplayName(), series.getEvents());
        panel.setWidget(view.asWidget());
    }

    @Override
    public SeriesContext getCtx() {
        return place.getCtx();
    }

    @Override
    public DispatchSystem getDispatch() {
        return clientFactory.getDispatch();
    }
    
    @Override
    public PlaceNavigation<?> getOverallLeaderboardNavigation() {
        return clientFactory.getNavigator().getSeriesNavigation(new EventSeriesOverallLeaderboardPlace(getCtx()), null, false);
    }
    
    @Override
    public PlaceNavigation<?> getMiniOverallLeaderboardNavigation() {
        return clientFactory.getNavigator().getSeriesNavigation(new SeriesMiniOverallLeaderboardPlace(getCtx()), null, false);
    }
    
    @Override
    public PlaceNavigation<?> getMiniLeaderboardNavigation(UUID eventId) {
        return clientFactory.getNavigator().getEventNavigation(new MiniLeaderboardPlace(eventId.toString(), null), null, false);
    }
    
    @Override
    public PlaceNavigation<?> getSeriesNavigation() {
        return clientFactory.getNavigator().getSeriesNavigation(new SeriesDefaultPlace(getCtx()), null, false);
    }
}
