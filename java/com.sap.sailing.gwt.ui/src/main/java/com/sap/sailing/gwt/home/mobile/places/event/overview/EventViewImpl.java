package com.sap.sailing.gwt.home.mobile.places.event.overview;

import java.util.Collection;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.home.mobile.partials.eventsteps.EventSteps;
import com.sap.sailing.gwt.home.mobile.partials.impressions.Impressions;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatus;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsBox;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.UpdatesBox;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewNewsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewStageAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventStatisticsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattaWithProgressAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattasAndLiveRacesForEventAction;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;

public class EventViewImpl extends AbstractEventView<EventView.Presenter> implements EventView {
    
    private static final StringMessages MSG = StringMessages.INSTANCE;

    private EventOverviewStage overviewStageUi;
    private EventSteps eventStepsUi;
    private Impressions impressionsUi;
    private StatisticsBox statisticsBoxUi;
    private UpdatesBox updatesBoxUi;

    public EventViewImpl(EventView.Presenter presenter) {
        super(presenter, false, false);
        FlowPanel container = new FlowPanel();
        this.setupOverviewStage(container);
        this.setupProgress(container);
        this.setupListContent(container);
        this.setupUpdateBox(container);
        this.setupImpressions(container);
        this.setupStatisticsBox(container);
        setViewContent(container);
    }
    
    private void setupProgress(Panel container) {
        eventStepsUi = new EventSteps();
        boolean showRegattaProgress = !isMultiRegattaEvent() && ExperimentalFeatures.SHOW_REGATTA_PROGRESS_ON_MOBILE;
        if (showRegattaProgress) {
            refreshManager.add(eventStepsUi, new GetRegattaWithProgressAction(getEventId(), getRegattaId()));
            container.add(eventStepsUi);
        }
    }
    
    private void setupOverviewStage(Panel container) {
        overviewStageUi = new EventOverviewStage(currentPresenter);
        refreshManager.add(overviewStageUi, new GetEventOverviewStageAction(getEventId()));
        container.add(overviewStageUi);
    }
    
    private void setupListContent(Panel container) {
        if (isMultiRegattaEvent()) {
            RegattaStatus regattaStatus = new RegattaStatus(currentPresenter);
            container.add(regattaStatus);
            refreshManager.add(regattaStatus, new GetRegattasAndLiveRacesForEventAction(getEventId()));
        } else {
            MinileaderboardBox miniLeaderboard = new MinileaderboardBox(false);
            miniLeaderboard.setAction(MSG.showAll(), currentPresenter.getRegattaMiniLeaderboardNavigation(getRegattaId()));
            container.add(miniLeaderboard);
            refreshManager.add(miniLeaderboard, new GetMiniLeaderbordAction(getEventId(), getRegattaId(), 3));
            if (ExperimentalFeatures.SHOW_REGATTA_OVERVIEW_AND_RACES_ON_MOBILE) {
                initRacesNavigation(container);
            }
        }
    }
    
    private void setupUpdateBox(Panel container) {
        updatesBoxUi = new UpdatesBox(currentPresenter, refreshManager);
        if (currentPresenter.getCtx().getEventDTO().getState() == EventState.RUNNING) {
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
    public void setMediaForImpressions(int nrOfImages, int nrOfVideos, Collection<SailingImageDTO> images) {
        impressionsUi.getElement().getStyle().setDisplay(Display.BLOCK);
        impressionsUi.setStatistis(nrOfImages, nrOfVideos);
        impressionsUi.addImages(images);
        // TODO: desktop media navigation
        impressionsUi.setClickDestinaton(currentPresenter.getMediaPageNavigation());
    }
}
