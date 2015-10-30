package com.sap.sailing.gwt.home.mobile.places.event.overview.regatta;

import java.util.Collection;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.home.communication.event.EventReferenceDTO;
import com.sap.sailing.gwt.home.communication.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.home.communication.event.GetRegattaWithProgressAction;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.mobile.partials.eventsteps.EventSteps;
import com.sap.sailing.gwt.home.mobile.partials.liveraces.RegattaLiveRaces;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.AbstractEventOverview;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RegattaOverviewImpl extends AbstractEventOverview {
    
    private static final StringMessages MSG = StringMessages.INSTANCE;

    private EventSteps eventStepsUi;
    private RegattaLiveRaces liveRacesUi;

    public RegattaOverviewImpl(EventViewBase.Presenter presenter) {
        super(presenter, presenter.isMultiRegattaEvent(), presenter.isMultiRegattaEvent());
        FlowPanel container = new FlowPanel();
        this.setupProgress(container);
        this.setupLiveRaces(container);
        if (!isMultiRegattaEvent()) {
            this.setupOverviewStage(container);
        }
        this.setupMiniLeaderboard(container);
        if (ExperimentalFeatures.SHOW_REGATTA_OVERVIEW_AND_RACES_ON_MOBILE) {
            initRacesNavigation(container);
        }
        if (!isMultiRegattaEvent()) {
            this.setupUpdateBox(container);
            this.setupImpressions(container);
        }
        this.setupStatisticsBox(container);
        setViewContent(container);
    }
    
    private void setupProgress(Panel container) {
        eventStepsUi = new EventSteps();
        if (ExperimentalFeatures.SHOW_REGATTA_PROGRESS_ON_MOBILE) {
            refreshManager.add(eventStepsUi, new GetRegattaWithProgressAction(getEventId(), getRegattaId()));
            container.add(eventStepsUi);
        }
    }
    
    private void setupLiveRaces(Panel container) {
        if (ExperimentalFeatures.SHOW_REGATTA_LIVE_RACES_ON_MOBILE) {
            liveRacesUi = new RegattaLiveRaces();
            refreshManager.add(liveRacesUi, new GetLiveRacesForRegattaAction(getEventId(), getRegattaId()));
            container.add(liveRacesUi);
        }
    }
    
    private void setupMiniLeaderboard(Panel container) {
        MinileaderboardBox miniLeaderboard = new MinileaderboardBox(false);
        miniLeaderboard.setAction(MSG.showAll(), currentPresenter.getRegattaMiniLeaderboardNavigation(getRegattaId()));
        refreshManager.add(miniLeaderboard, new GetMiniLeaderbordAction(getEventId(), getRegattaId(), 3));
        container.add(miniLeaderboard);
    }
    
    @Override
    protected void setQuickFinderValues(Quickfinder quickfinder, Collection<RegattaMetadataDTO> regattaMetadatas) {
        QuickfinderPresenter.getForRegattaOverview(quickfinder, currentPresenter, regattaMetadatas);
    }
    
    @Override
    protected void setQuickFinderValues(Quickfinder quickfinder, String seriesName, Collection<EventReferenceDTO> eventsOfSeries) {
        QuickfinderPresenter.getForSeriesEventOverview(quickfinder, seriesName, currentPresenter, eventsOfSeries);
    }

}
