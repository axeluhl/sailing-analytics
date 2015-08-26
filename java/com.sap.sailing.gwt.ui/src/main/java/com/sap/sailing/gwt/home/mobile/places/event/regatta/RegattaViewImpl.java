package com.sap.sailing.gwt.home.mobile.places.event.regatta;

import java.util.Collection;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.home.mobile.partials.eventsteps.EventSteps;
import com.sap.sailing.gwt.home.mobile.partials.liveraces.RegattaLiveRaces;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsBox;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.UpdatesBox;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventOverviewNewsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventStatisticsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetRegattaWithProgressAction;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;

public class RegattaViewImpl extends AbstractEventView<RegattaView.Presenter> implements RegattaView {

    private EventSteps regattaProgressUi;
    private RegattaLiveRaces liveRacesUi;
    private MinileaderboardBox leaderboardUi;
    private UpdatesBox latestNewsUi;
    private StatisticsBox statisticsUi;
    
    public RegattaViewImpl(RegattaView.Presenter presenter) {
        super(presenter, true, true);
        Panel container = new FlowPanel();
        this.initRegattaProgressUi(container);
        this.initLiveRaces(container);
        this.initLeaderboardUi(container);
        this.initLatestNewsUi(container);
        this.initRacesNavigation(container);
        this.initStatisticsUi(container);
        setViewContent(container);
    }

    private void initRegattaProgressUi(Panel container) {
        regattaProgressUi = new EventSteps();
        refreshManager.add(regattaProgressUi, new GetRegattaWithProgressAction(getEventId(), getRegattaId()));
        container.add(regattaProgressUi);
    }
    
    private void initLiveRaces(Panel container) {
        liveRacesUi = new RegattaLiveRaces();
        refreshManager.add(liveRacesUi, new GetLiveRacesForRegattaAction(getEventId(), getRegattaId()));
        container.add(liveRacesUi);
    }
    
    private void initLeaderboardUi(Panel container) {
        leaderboardUi = new MinileaderboardBox(false);
        leaderboardUi.setAction(StringMessages.INSTANCE.showAll(), currentPresenter.getRegattaMiniLeaderboardNavigation(getRegattaId()));
        refreshManager.add(leaderboardUi, new GetMiniLeaderbordAction(getEventId(), getRegattaId(), 3));
        container.add(leaderboardUi);
    }
    
    private void initLatestNewsUi(Panel container) {
        latestNewsUi = new UpdatesBox(currentPresenter, refreshManager);
        if (currentPresenter.getCtx().getEventDTO().getState() == EventState.RUNNING) {
            refreshManager.add(latestNewsUi, new GetEventOverviewNewsAction(getEventId(), 2));
            container.add(latestNewsUi);
        }
    }
    
    private void initStatisticsUi(Panel container) {
        statisticsUi = new StatisticsBox(false);
        // FIXME only show statistics of the regatta
        refreshManager.add(statisticsUi, new GetEventStatisticsAction(getEventId(), true));
        container.add(statisticsUi);
    }
    
    @Override
    protected void setQuickFinderValues(Quickfinder quickfinder, Collection<RegattaMetadataDTO> regattaMetadatas) {
        QuickfinderPresenter.getForRegattaOverview(quickfinder, currentPresenter, regattaMetadatas);
    }

}
