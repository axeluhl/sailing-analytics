package com.sap.sailing.gwt.home.mobile.places.event.races;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.communication.event.EventReferenceDTO;
import com.sap.sailing.gwt.home.communication.event.GetCompetitionFormatRacesAction;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.regattacompetition.RegattaCompetition;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterPresenter;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterValueChangeHandler;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterWidget;
import com.sap.sailing.gwt.home.shared.partials.filter.RacesByCompetitorTextBoxFilter;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionPresenter;

public class RacesViewImpl extends AbstractEventView<RacesView.Presenter> implements RacesView {

    private static RacesViewImplUiBinder uiBinder = GWT.create(RacesViewImplUiBinder.class);

    interface RacesViewImplUiBinder extends UiBinder<Widget, RacesViewImpl> {
    }
    
    @UiField RacesByCompetitorTextBoxFilter competitorFilterUi;
    @UiField RegattaCompetition regattaCompetitionUi;

    public RacesViewImpl(final RacesView.Presenter presenter) {
        super(presenter, presenter.isMultiRegattaEvent(), true);
        setViewContent(uiBinder.createAndBindUi(this));
        RegattaCompetitionPresenter competitionPresenter = new MobileRegattaCompetitionPresenter();
        RacesViewImplFilterPresenter filterPresenter = new RacesViewImplFilterPresenter(competitorFilterUi, competitionPresenter);
        refreshManager.add(filterPresenter.getRefreshableWidgetWrapper(competitionPresenter), new GetCompetitionFormatRacesAction(getEventId(), getRegattaId()));
        if (!ExperimentalFeatures.SHOW_RACES_BY_COMPETITOR_FILTER) {
            competitorFilterUi.removeFromParent();
        }
    }
    
    @Override
    protected void setQuickFinderValues(Quickfinder quickfinder, Collection<RegattaMetadataDTO> regattaMetadatas) {
        QuickfinderPresenter.getForRegattaRaces(quickfinder, currentPresenter, regattaMetadatas);
    }
    
    @Override
    protected void setQuickFinderValues(Quickfinder quickfinder, String seriesName, Collection<EventReferenceDTO> eventsOfSeries) {
        QuickfinderPresenter.getForSeriesEventRaces(quickfinder, seriesName, currentPresenter, eventsOfSeries);
    }
    
    private class MobileRegattaCompetitionPresenter extends RegattaCompetitionPresenter {
        public MobileRegattaCompetitionPresenter() {
            super(regattaCompetitionUi);
        }

        @Override
        protected String getRaceViewerURL(String leaderboardName, RegattaAndRaceIdentifier raceIdentifier) {
            return null; // TODO No mobile "RaceViewer implemented yet;
        }
    }
    
    private class RacesViewImplFilterPresenter extends FilterPresenter<SimpleRaceMetadataDTO> {
        private final List<FilterValueChangeHandler<SimpleRaceMetadataDTO>> valueChangeHandler;

        public RacesViewImplFilterPresenter(FilterWidget<SimpleRaceMetadataDTO> filterWidget,
                FilterValueChangeHandler<SimpleRaceMetadataDTO> valueChangeHandler) {
            super(filterWidget);
            this.valueChangeHandler = Arrays.asList(valueChangeHandler);
            super.addHandler(valueChangeHandler);
        }

        @Override
        protected List<FilterValueChangeHandler<SimpleRaceMetadataDTO>> getCurrentValueChangeHandlers() {
            return valueChangeHandler;
        }
    }
    
}
