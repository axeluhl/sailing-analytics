package com.sap.sailing.gwt.home.mobile.places.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.simpleinfoblock.SimpleInfoBlock;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventOverviewStage;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewStageAction;

public class EventViewImpl extends Composite implements EventView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, EventViewImpl> {
    }

    @UiField(provided = true) EventHeader eventHeaderUi;
    @UiField Quickfinder quickFinderUi;
    @UiField SimpleInfoBlock sailorInfoUi;
    @UiField(provided = true)
    EventOverviewStage overviewStageUi;
    
    private Presenter currentPresenter;

    public EventViewImpl(Presenter presenter) {
        this.currentPresenter = presenter;
        eventHeaderUi = new EventHeader(presenter.getCtx().getEventDTO());
        overviewStageUi = new EventOverviewStage(currentPresenter);
        initWidget(uiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, currentPresenter.getDispatch());
        refreshManager.add(overviewStageUi, new GetEventOverviewStageAction(currentPresenter.getCtx().getEventDTO()
                .getId()));
    }

    @Override
    public void setSailorInfos(String description, String buttonLabel, String url) {
        sailorInfoUi.setDescription(SafeHtmlUtils.fromString(description.replace("\n", " ")));
        sailorInfoUi.setAction(buttonLabel, url);
    }

    
}
