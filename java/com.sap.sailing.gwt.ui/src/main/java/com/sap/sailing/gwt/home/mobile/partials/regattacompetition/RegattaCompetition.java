package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.places.races.RacesView.Presenter;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListSeriesDTO;

public class RegattaCompetition extends Composite {

    private static RegattaCompetitionUiBinder uiBinder = GWT.create(RegattaCompetitionUiBinder.class);

    interface RegattaCompetitionUiBinder extends UiBinder<Widget, RegattaCompetition> {
    }

    @UiField FlowPanel regattaSeriesContainerUi;
    
    public RegattaCompetition() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void showRaces(Presenter presenter, Collection<RaceListSeriesDTO> seriesList) {
        for (RaceListSeriesDTO series : seriesList) {
            RegattaCompetitionSeries regattaCompetitionSeries = new RegattaCompetitionSeries(series);
            int fleetCount = series.getFleets().size();
            for (RaceListFleetDTO fleet : series.getFleets()) {
                regattaCompetitionSeries.addFleet(presenter, fleet, fleetCount);
            }
            regattaSeriesContainerUi.add(regattaCompetitionSeries);
        }
    }

}
