package com.sap.sailing.gwt.home.mobile.places.event.overview.regatta;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.home.communication.event.EventReferenceWithStateDTO;
import com.sap.sailing.gwt.home.communication.event.GetLiveRacesForRegattaAction;
import com.sap.sailing.gwt.home.communication.event.GetRegattaWithProgressAction;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.mobile.partials.eventsteps.EventSteps;
import com.sap.sailing.gwt.home.mobile.partials.liveraces.RegattaLiveRaces;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.AbstractEventOverview;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RegattaOverviewImpl extends AbstractEventOverview {
    
    private static final StringMessages MSG = StringMessages.INSTANCE;

    private EventSteps eventStepsUi;
    private RegattaLiveRaces liveRacesUi;

    private final FlagImageResolver flagImageResolver;

    public RegattaOverviewImpl(EventViewBase.Presenter presenter, FlagImageResolver flagImageResolver) {
        super(presenter, presenter.isMultiRegattaEvent(), presenter.isMultiRegattaEvent());
        this.flagImageResolver = flagImageResolver;
        FlowPanel container = new FlowPanel();
        if (presenter.getRegatta() != null) {
            this.setupProgress(container);
            this.setupLiveRaces(container);
        }
        if (!isMultiRegattaEvent()) {
            this.setupOverviewStage(container);
            this.setupEventDescription(container);
        }
        this.setupMiniLeaderboard(container);
        initRacesNavigation(container);
        if (!isMultiRegattaEvent()) {
            this.setupUpdateBox(container);
            this.setupImpressions(container);
        }
        this.setupStatisticsBox(container, !presenter.isSingleRegattaEvent());
        setViewContent(container);
    }
    
    private void setupProgress(Panel container) {
        final Function<String, PlaceNavigation<?>> racesNavigationFactory = prefSeriesName -> currentPresenter
                .getRegattaRacesNavigation(getRegattaId(), prefSeriesName);
        eventStepsUi = new EventSteps(currentPresenter.getRegatta(), racesNavigationFactory);
        refreshManager.add(eventStepsUi, new GetRegattaWithProgressAction(getEventId(), getRegattaId()));
        container.add(eventStepsUi);
    }
    
    private void setupLiveRaces(Panel container) {
        liveRacesUi = new RegattaLiveRaces(currentPresenter);
        refreshManager.add(liveRacesUi, new GetLiveRacesForRegattaAction(getEventId(), getRegattaId()));
        container.add(liveRacesUi);
    }
    
    private void setupMiniLeaderboard(Panel container) {
        MinileaderboardBox miniLeaderboard = new MinileaderboardBox(false, flagImageResolver);
        miniLeaderboard.setAction(MSG.showAll(), currentPresenter.getRegattaMiniLeaderboardNavigation(getRegattaId()));
        if (currentPresenter.getRegatta() != null) {
            refreshManager.add(miniLeaderboard, new GetMiniLeaderbordAction(getEventId(), getRegattaId(), 3));
        } else {
            // This forces the "There are no results available yet" message to show
            miniLeaderboard.setData(new GetMiniLeaderboardDTO());
        }
        container.add(miniLeaderboard);
    }
    
    @Override
    protected void setQuickFinderValues(Quickfinder quickfinder, Map<String, Set<RegattaMetadataDTO>> regattasByLeaderboardGroupName) {
        QuickfinderPresenter.getForRegattaOverview(quickfinder, currentPresenter, regattasByLeaderboardGroupName);
    }
    
    @Override
    protected void setQuickFinderValues(Quickfinder quickfinder, String seriesName, Collection<EventReferenceWithStateDTO> eventsOfSeries) {
        QuickfinderPresenter.getForSeriesEventOverview(quickfinder, seriesName, currentPresenter, eventsOfSeries);
    }

}
