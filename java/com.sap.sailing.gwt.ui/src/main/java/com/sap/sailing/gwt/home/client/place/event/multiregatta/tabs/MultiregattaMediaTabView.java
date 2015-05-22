package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.shared.media.MediaPage;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaMediaTabView extends Composite implements MultiregattaTabView<MultiregattaMediaPlace> {

    public MultiregattaMediaTabView() {

    }

    @Override
    public Class<MultiregattaMediaPlace> getPlaceClassForActivation() {
        return MultiregattaMediaPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return currentPresenter.hasMedia() ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }

    @Override
    public void start(MultiregattaMediaPlace myPlace, AcceptsOneWidget contentArea) {
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

    // TODO delete UiBinder if we do not need it for the new media page implementation
    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaMediaTabView> {
    }

    @SuppressWarnings("unused")
    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private Presenter currentPresenter;

    @Override
    public MultiregattaMediaPlace placeToFire() {
        return new MultiregattaMediaPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(EventMultiregattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;

    }

}