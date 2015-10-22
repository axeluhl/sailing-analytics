package com.sap.sailing.gwt.home.desktop.places.event.regatta.mediatab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.desktop.partials.media.MediaPage;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaView.Presenter;

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
    public TabView.State getState() {
        return currentPresenter.hasMedia() ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }

    @Override
    public void start(RegattaMediaPlace myPlace, final AcceptsOneWidget contentArea) {
        final MediaPage mediaPage = new MediaPage(currentPresenter.getErrorAndBusyClientFactory().createBusyView());
        initWidget(mediaPage);
        
        currentPresenter.ensureMedia(new AsyncCallback<MediaDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onSuccess(MediaDTO media) {
                contentArea.setWidget(RegattaMediaTabView.this);
                mediaPage.setMedia(media);
            }
        });
    }

    @Override
    public void stop() {

    }

 // TODO delete UiBinder if we do not need it for the new media page implementation
    interface MyBinder extends UiBinder<HTMLPanel, RegattaMediaTabView> {
    }

    @SuppressWarnings("unused")
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