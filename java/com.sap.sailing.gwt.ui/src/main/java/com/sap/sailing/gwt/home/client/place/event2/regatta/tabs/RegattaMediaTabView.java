package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.regatta.RegattaTabView;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaMediaTabView extends Composite implements RegattaTabView<RegattaMediaPlace> {

    public RegattaMediaTabView() {

    }

    @Override
    public Class<RegattaMediaPlace> getPlaceClassForActivation() {
        return RegattaMediaPlace.class;
    }

    @Override
    public void start(RegattaMediaPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaMediaTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private Presenter currentPresenter;

    @Override
    public RegattaMediaPlace placeToFire() {
        return new RegattaMediaPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }

}