package com.sap.sailing.gwt.home.mobile.places.races;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.partials.listNavigation.RegattaNavigationResources;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshManager;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.regattacompetition.RegattaCompetition;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetCompetitionFormatRacesAction;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class RacesViewImpl extends Composite implements RacesView {

    private static RacesViewImplUiBinder uiBinder = GWT.create(RacesViewImplUiBinder.class);

    interface RacesViewImplUiBinder extends UiBinder<Widget, RacesViewImpl> {
    }
    
    private Presenter currentPresenter;
    @UiField(provided = true) EventHeader eventHeaderUi;
    @UiField(provided = true) RegattaCompetition regattaCompetitionUi;
    @UiField Quickfinder quickFinderUi;

    public RacesViewImpl(Presenter presenter) {
        currentPresenter = presenter;
        eventHeaderUi = new EventHeader(presenter.getCtx().getEventDTO(), presenter.getEventNavigation());
        RegattaNavigationResources.INSTANCE.css().ensureInjected();
        regattaCompetitionUi = new RegattaCompetition(presenter);
        initWidget(uiBinder.createAndBindUi(this));
        RefreshManager refreshManager = new RefreshManager(this, presenter.getDispatch());
        UUID eventId = presenter.getCtx().getEventDTO().getId();
        refreshManager.add(regattaCompetitionUi, new GetCompetitionFormatRacesAction(eventId, presenter.getCtx().getRegattaId()));
    }

    @Override
    public void setQuickFinderValues(Collection<RegattaMetadataDTO> regattaMetadatas) {
        QuickfinderPresenter.getForRegattaRaces(quickFinderUi, currentPresenter, regattaMetadatas);
    }
    
    @Override
    public void setQuickFinderValues(String seriesName, Collection<EventReferenceDTO> eventsOfSeries) {
        QuickfinderPresenter.getForSeriesLeaderboards(quickFinderUi, seriesName, currentPresenter, eventsOfSeries);
    }
    
    @Override
    public void hideQuickfinder() {
        quickFinderUi.removeFromParent();
    }

}
