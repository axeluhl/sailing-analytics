package com.sap.sailing.gwt.home.mobile.places.event.races;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.regattacompetition.RegattaCompetition;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.home.shared.partials.filter.RacesByCompetitorTextBoxFilter;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionPresenter;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetCompetitionFormatRacesAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class RacesViewImpl extends AbstractEventView<RacesView.Presenter> implements RacesView {

    private static RacesViewImplUiBinder uiBinder = GWT.create(RacesViewImplUiBinder.class);

    interface RacesViewImplUiBinder extends UiBinder<Widget, RacesViewImpl> {
    }
    
    @UiField RacesByCompetitorTextBoxFilter competitorFilterUi;
    @UiField RegattaCompetition regattaCompetitionUi;

    public RacesViewImpl(final RacesView.Presenter presenter) {
        super(presenter, presenter.getCtx().getEventDTO().getType() == EventType.MULTI_REGATTA, true);
        setViewContent(uiBinder.createAndBindUi(this));
        RegattaCompetitionPresenter competitionPresenter = new MobileRegattaCompetitionPresenter();
        competitorFilterUi.addFilterValueChangeHandler(competitionPresenter);
        refreshManager.add(competitionPresenter, new GetCompetitionFormatRacesAction(getEventId(), getRegattaId()));
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
    
}
