package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.dispatch.client.ListResult;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatSeriesDTO;
import com.sap.sailing.gwt.home.mobile.places.event.races.RacesView.Presenter;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;

public class RegattaCompetition extends Composite implements RefreshableWidget<ListResult<RaceCompetitionFormatSeriesDTO>> {

    private static RegattaCompetitionUiBinder uiBinder = GWT.create(RegattaCompetitionUiBinder.class);

    interface RegattaCompetitionUiBinder extends UiBinder<Widget, RegattaCompetition> {
    }

    @UiField FlowPanel regattaSeriesContainerUi;
    private final Presenter presenter;
    
    public RegattaCompetition(Presenter presenter) {
        this.presenter = presenter;
        RegattaCompetitionResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setData(ListResult<RaceCompetitionFormatSeriesDTO> data) {
        regattaSeriesContainerUi.clear(); 
        for (RaceCompetitionFormatSeriesDTO series : data.getValues()) {
            RegattaCompetitionSeries regattaCompetitionSeries = new RegattaCompetitionSeries(series);
            int fleetCount = series.getFleets().size();
            for (RaceCompetitionFormatFleetDTO fleet : series.getFleets()) {
                regattaCompetitionSeries.addFleet(presenter, fleet, fleetCount);
            }
            regattaSeriesContainerUi.add(regattaCompetitionSeries);
        }
    }

}
