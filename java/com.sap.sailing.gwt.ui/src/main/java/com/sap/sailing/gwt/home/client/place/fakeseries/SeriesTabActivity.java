package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.series.SeriesClientFactory;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;

public class SeriesTabActivity extends AbstractSeriesActivity<AbstractSeriesTabPlace> implements SeriesTabsView.Presenter {

    private SeriesTabsView currentView = new TabletAndDesktopSeriesView();

    public SeriesTabActivity(AbstractSeriesTabPlace place, SeriesClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        currentView.registerPresenter(this);
        panel.setWidget(currentView);
        currentView.navigateTabsTo(currentPlace);
    }
    
    @Override
    public boolean needsSelectionInHeader() {
        EventViewDTO event = ctx.getEventDTO();
        return (event.getType() == EventType.SERIES_EVENT || event.getType() == EventType.MULTI_REGATTA);
    }
    
    @Override
    public String getEventName() {
        if(ctx.getEventDTO().getType() == EventType.MULTI_REGATTA) {
            return ctx.getRegattaId();
        }
        return super.getEventName();
    }

    @Override
    public void gotoOverview() {
//        clientFactory.getPlaceController().goTo(new SeriesOverviewPlace(this.ctx));
    }
}
