package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesView.PlaceCallback;
import com.sap.sailing.gwt.home.client.place.series.SeriesClientFactory;

public abstract class AbstractSeriesActivity<PLACE extends AbstractSeriesPlace> extends AbstractActivity implements SeriesView.Presenter {

    protected final PLACE currentPlace;
    protected final SeriesContext ctx;
    protected final SeriesClientFactory clientFactory;

    public AbstractSeriesActivity(PLACE place, SeriesClientFactory clientFactory) {
        this.currentPlace = place;
        this.ctx = new SeriesContext(place.getCtx());

        this.clientFactory = clientFactory;
    }

    @Override
    public SeriesContext getCtx() {
        return ctx;
    }

    @Override
    public void handleTabPlaceSelection(TabView<?, ? extends SeriesView.Presenter> selectedActivity) {
        Place tabPlaceToGo = selectedActivity.placeToFire();
        clientFactory.getPlaceController().goTo(tabPlaceToGo);
    }
    
    public void navigateTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    @Override
    public void forPlaceSelection(PlaceCallback callback) {
//        EventDTO event = ctx.getEventDTO();
    }
    
    @Override
    public String getUrl(Place place) {
        // TODO implement
        return "TODO URL";
    }
    
    @Override
    public String getEventName() {
        return ctx.getEventDTO().getName();
    }
}
