package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabsView;
import com.sap.sailing.gwt.home.client.shared.media.MediaPage;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;

public class SeriesMediaTabView extends Composite implements SeriesTabView<SeriesMediaPlace> {

    interface MyBinder extends UiBinder<HTMLPanel, SeriesMediaTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private SeriesTabsView.Presenter currentPresenter;

    public SeriesMediaTabView() {
    }

    @Override
    public Class<SeriesMediaPlace> getPlaceClassForActivation() {
        return SeriesMediaPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return currentPresenter.hasMedia() ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }

    @Override
    public void start(SeriesMediaPlace myPlace, AcceptsOneWidget contentArea) {
        final MediaPage mediaPage = new MediaPage();
        
        currentPresenter.ensureMedia(new AsyncCallback<MediaDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onSuccess(MediaDTO media) {
                mediaPage.setMedia(media);
            }
        });

        initWidget(mediaPage);
        
        // TODO do we need UiBinder here?
//        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    @Override
    public SeriesMediaPlace placeToFire() {
        return new SeriesMediaPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(SeriesTabsView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
}