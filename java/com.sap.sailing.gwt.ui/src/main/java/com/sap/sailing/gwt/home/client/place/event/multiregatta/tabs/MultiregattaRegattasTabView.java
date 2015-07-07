package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.MultiregattaTabView;
import com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList.MultiRegattaListItem;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLive;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForEventAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattaListViewAction;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaRegattasTabView extends Composite implements MultiregattaTabView<MultiregattaRegattasPlace> {
    
    @UiField(provided = true) RacesListLive racesListLive;
    @UiField FlowPanel regattasContainerUi;
    private Presenter currentPresenter;

    public MultiregattaRegattasTabView() {

    }

    @Override
    public Class<MultiregattaRegattasPlace> getPlaceClassForActivation() {
        return MultiregattaRegattasPlace.class;
    }
    
    @Override
    public void setPresenter(EventMultiregattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(final MultiregattaRegattasPlace myPlace, final AcceptsOneWidget contentArea) {
        racesListLive = new RacesListLive(currentPresenter, true);
        initWidget(ourUiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        refreshManager.add(racesListLive, new GetLiveRacesForEventAction(currentPresenter.getCtx().getEventDTO().getId()));
        contentArea.setWidget(this);
        
        UUID eventId = currentPresenter.getCtx().getEventDTO().getId();
        currentPresenter.getDispatch().execute(new GetRegattaListViewAction(eventId), new AsyncCallback<ResultWithTTL<SortedSetResult<RegattaWithProgressDTO>>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(ResultWithTTL<SortedSetResult<RegattaWithProgressDTO>> result) {
                regattasContainerUi.clear();
                for (RegattaWithProgressDTO regattaWithProgress : result.getDto().getValues()) {
                    regattasContainerUi.add(new MultiRegattaListItem(regattaWithProgress, currentPresenter));
                }
            }
        });        
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaRegattasTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public MultiregattaRegattasPlace placeToFire() {
        return new MultiregattaRegattasPlace(currentPresenter.getCtx());
    }

}