package com.sap.sailing.gwt.home.desktop.places.event.multiregatta.mediatab;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.desktop.partials.media.MediaPage;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.shared.app.ActivityCallback;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.security.shared.HasPermissions;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaMediaTabView extends Composite implements MultiregattaTabView<MultiregattaMediaPlace> {
    
    private Presenter currentPresenter;
    
    public MultiregattaMediaTabView() {
        super();
    }
    
    @Override
    public void setPresenter(EventMultiregattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }

    @Override
    public Class<MultiregattaMediaPlace> getPlaceClassForActivation() {
        return MultiregattaMediaPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return currentPresenter.hasMedia() || currentPresenter.getUserService()
                .hasPermission(currentPresenter.getEventDTO(), HasPermissions.DefaultActions.UPDATE)
                        ? TabView.State.VISIBLE
                        : TabView.State.INVISIBLE;
    }

    @Override
    public void start(MultiregattaMediaPlace myPlace, final AcceptsOneWidget contentArea) {
        ErrorAndBusyClientFactory errorAndBusyClientFactory = currentPresenter.getErrorAndBusyClientFactory();
        final MediaPage mediaPage = new MediaPage(errorAndBusyClientFactory.createBusyView(), currentPresenter.getEventBus(), 
                currentPresenter.getUserService(), currentPresenter.getEventDTO());
        initWidget(mediaPage);
        currentPresenter.ensureMedia(new ActivityCallback<MediaDTO>(errorAndBusyClientFactory, contentArea) {
            @Override
            public void onSuccess(MediaDTO media) {
                contentArea.setWidget(MultiregattaMediaTabView.this);
                mediaPage.setMedia(media);
            }
        });
    }

    @Override
    public void stop() {
    }

    @Override
    public MultiregattaMediaPlace placeToFire() {
        return new MultiregattaMediaPlace(currentPresenter.getCtx());
    }
}
