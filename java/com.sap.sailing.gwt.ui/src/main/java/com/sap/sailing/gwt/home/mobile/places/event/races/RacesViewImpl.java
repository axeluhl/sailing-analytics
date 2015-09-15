package com.sap.sailing.gwt.home.mobile.places.event.races;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.regattacompetition.RegattaCompetition;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetCompetitionFormatRacesAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public class RacesViewImpl extends AbstractEventView<RacesView.Presenter> implements RacesView {

    private static RacesViewImplUiBinder uiBinder = GWT.create(RacesViewImplUiBinder.class);

    interface RacesViewImplUiBinder extends UiBinder<Widget, RacesViewImpl> {
    }
    
    @UiField(provided = true) RegattaCompetition regattaCompetitionUi;

    public RacesViewImpl(RacesView.Presenter presenter) {
        super(presenter, presenter.getCtx().getEventDTO().getType() == EventType.MULTI_REGATTA, true);
        regattaCompetitionUi = new RegattaCompetition(presenter);
        refreshManager.add(regattaCompetitionUi, new GetCompetitionFormatRacesAction(getEventId(), getRegattaId()));
        setViewContent(uiBinder.createAndBindUi(this));
    }
    
    @Override
    protected void setQuickFinderValues(Quickfinder quickfinder, Collection<RegattaMetadataDTO> regattaMetadatas) {
        QuickfinderPresenter.getForRegattaRaces(quickfinder, currentPresenter, regattaMetadatas);
    }

}
