package com.sap.sailing.gwt.home.mobile.places.event.overview.multiregatta;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.home.communication.event.GetRegattasAndLiveRacesForEventAction;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatus;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.AbstractEventOverview;

public class MultiRegattaViewImpl extends AbstractEventOverview {
    
    private RegattaStatus regattaStatusUi;
    
    public MultiRegattaViewImpl(EventViewBase.Presenter presenter) {
        super(presenter, false);
        FlowPanel container = new FlowPanel();
        this.setupOverviewStage(container);
        this.setupRegattaStatusList(container);
        this.setupUpdateBox(container);
        this.setupImpressions(container);
        this.setupStatisticsBox(container);
        setViewContent(container);
    }
    
    private void setupRegattaStatusList(Panel container) {
        regattaStatusUi = new RegattaStatus(currentPresenter);
        container.add(regattaStatusUi);
        refreshManager.add(regattaStatusUi, new GetRegattasAndLiveRacesForEventAction(getEventId()));
    }

}
