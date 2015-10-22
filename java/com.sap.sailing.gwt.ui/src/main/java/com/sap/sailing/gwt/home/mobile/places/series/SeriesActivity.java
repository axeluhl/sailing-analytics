package com.sap.sailing.gwt.home.mobile.places.series;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.overallleaderboardtab.EventSeriesOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.series.minileaderboard.SeriesMiniOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.app.ActivityCallback;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;

public class SeriesActivity extends AbstractActivity implements SeriesView.Presenter {
    private final MobileApplicationClientFactory clientFactory;
    private final AbstractSeriesPlace place;
    
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
            clientFactory.getHomeService().getEventSeriesViewById(seriesUUID, 
                    new ActivityCallback<EventSeriesViewDTO>(clientFactory, panel) {
                        @Override
                        public void onSuccess(EventSeriesViewDTO series) {
                            ctx.updateContext(series);
                            initUi(panel, eventBus);
                        }
                    });
        }
    }
    
    private void initUi(final AcceptsOneWidget panel, EventBus eventBus) {
        final SeriesView view = new SeriesViewImpl(this);
        EventSeriesViewDTO series = getCtx().getSeriesDTO();
        view.setQuickFinderValues(series.getDisplayName(), series.getEvents());
        panel.setWidget(view.asWidget());
    }
    
    @Override
    public SeriesContext getCtx() {
        return place.getCtx();
    }

    public SailingDispatchSystem getDispatch() {
        return clientFactory.getDispatch();
    }
    
    @Override
    public PlaceNavigation<?> getMiniLeaderboardNavigation(UUID eventId) {
        return clientFactory.getNavigator().getEventNavigation(new MiniLeaderboardPlace(eventId.toString(), null), null, false);
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
    public PlaceNavigation<?> getEventNavigation(String eventId) {
        return clientFactory.getNavigator().getEventNavigation(eventId, null, false);
    }
}
