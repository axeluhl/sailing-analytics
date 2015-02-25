package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.regatta.RegattaTabView;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaRacesTabView extends Composite implements RegattaTabView<RegattaRacesPlace> {

    private Presenter currentPresenter;

    public RegattaRacesTabView() {

    }

    @Override
    public Class<RegattaRacesPlace> getPlaceClassForActivation() {
        return RegattaRacesPlace.class;
    }
    
    @Override
    public void setPresenter(Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        
    }

    @Override
    public void start(RegattaRacesPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaRacesTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public RegattaRacesPlace placeToFire() {
        return new RegattaRacesPlace(currentPresenter.getCtx());
    }

}