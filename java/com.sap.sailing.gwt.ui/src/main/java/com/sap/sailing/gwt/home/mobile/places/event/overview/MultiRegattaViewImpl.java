package com.sap.sailing.gwt.home.mobile.places.event.overview;

import java.util.Collection;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.home.communication.event.EventReferenceDTO;
import com.sap.sailing.gwt.home.communication.event.EventState;
import com.sap.sailing.gwt.home.communication.event.GetRegattasAndLiveRacesForEventAction;
import com.sap.sailing.gwt.home.communication.event.eventoverview.GetEventOverviewStageAction;
import com.sap.sailing.gwt.home.communication.event.news.GetEventOverviewNewsAction;
import com.sap.sailing.gwt.home.communication.event.statistics.GetEventStatisticsAction;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.mobile.partials.impressions.Impressions;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatus;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsBox;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.UpdatesBox;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;

public class MultiRegattaViewImpl extends AbstractEventView<EventView.Presenter> implements EventView {
    
    private EventOverviewStage overviewStageUi;
    private RegattaStatus regattaStatusUi;
    private UpdatesBox updatesBoxUi;
    private Impressions impressionsUi;
    private StatisticsBox statisticsBoxUi;
    
    public MultiRegattaViewImpl(EventView.Presenter presenter) {
        super(presenter, false, false);
        FlowPanel container = new FlowPanel();
        this.setupOverviewStage(container);
        this.setupRegattaStatusList(container);
        this.setupUpdateBox(container);
        this.setupImpressions(container);
        this.setupStatisticsBox(container);
        setViewContent(container);
    }

    private void setupOverviewStage(Panel container) {
        overviewStageUi = new EventOverviewStage(currentPresenter);
        refreshManager.add(overviewStageUi, new GetEventOverviewStageAction(getEventId(), true));
        container.add(overviewStageUi);
    }
    
    private void setupRegattaStatusList(Panel container) {
        regattaStatusUi = new RegattaStatus(currentPresenter);
        container.add(regattaStatusUi);
        refreshManager.add(regattaStatusUi, new GetRegattasAndLiveRacesForEventAction(getEventId()));
    }
    
    private void setupUpdateBox(Panel container) {
        updatesBoxUi = new UpdatesBox(currentPresenter, refreshManager);
        if (currentPresenter.getEventDTO().getState() == EventState.RUNNING) {
            refreshManager.add(updatesBoxUi, new GetEventOverviewNewsAction(getEventId(), 2));
            container.add(updatesBoxUi);
        }
    }
    
    private void setupImpressions(Panel container) {
        impressionsUi = new Impressions();
        impressionsUi.getElement().getStyle().setDisplay(Display.NONE);
        container.add(impressionsUi);
    }
    
    private void setupStatisticsBox(Panel container) {
        statisticsBoxUi = new StatisticsBox(isMultiRegattaEvent());
        refreshManager.add(statisticsBoxUi, new GetEventStatisticsAction(getEventId()));
        container.add(statisticsBoxUi);
    }
    
    @Override
    protected void setQuickFinderValues(Quickfinder quickfinder, String seriesName, Collection<EventReferenceDTO> eventsOfSeries) {
        QuickfinderPresenter.getForSeriesEventOverview(quickfinder, seriesName, currentPresenter, eventsOfSeries);
    }

    @Override
    public void setMediaForImpressions(int nrOfImages, int nrOfVideos, Collection<SailingImageDTO> images) {
        impressionsUi.getElement().getStyle().setDisplay(Display.BLOCK);
        impressionsUi.setStatistis(nrOfImages, nrOfVideos);
        impressionsUi.addImages(images);
        // TODO: desktop media navigation
        impressionsUi.setClickDestinaton(currentPresenter.getMediaPageNavigation());
    }

}
