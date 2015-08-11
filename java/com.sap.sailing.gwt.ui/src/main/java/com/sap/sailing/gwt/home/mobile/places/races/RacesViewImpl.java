package com.sap.sailing.gwt.home.mobile.places.races;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.mobile.partials.regattacompetition.RegattaCompetition;
import com.sap.sailing.gwt.home.mobile.places.QuickfinderPresenter;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class RacesViewImpl extends Composite implements RacesView {

    private static RacesViewImplUiBinder uiBinder = GWT.create(RacesViewImplUiBinder.class);

    interface RacesViewImplUiBinder extends UiBinder<Widget, RacesViewImpl> {
    }
    
    private Presenter currentPresenter;
    @UiField(provided = true) EventHeader eventHeaderUi;
    @UiField RegattaCompetition regattaCompetitionUi;
    @UiField Quickfinder quickFinderUi;

    public RacesViewImpl(Presenter presenter) {
        currentPresenter = presenter;
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setQuickFinderValues(Collection<RegattaMetadataDTO> regattaMetadatas) {
        new QuickfinderPresenter(quickFinderUi, currentPresenter, regattaMetadatas);
    }
    
    @Override
    public void setQuickFinderValues(String seriesName, Collection<EventReferenceDTO> eventsOfSeries) {
        new QuickfinderPresenter(quickFinderUi, currentPresenter, seriesName, eventsOfSeries);
    }
    
    @Override
    public void hideQuickfinder() {
        quickFinderUi.removeFromParent();
    }

}
