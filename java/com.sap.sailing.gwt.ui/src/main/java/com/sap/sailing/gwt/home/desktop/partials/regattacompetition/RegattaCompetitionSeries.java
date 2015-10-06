package com.sap.sailing.gwt.home.desktop.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.AbstractRegattaCompetitionSeries;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionFleetView;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatSeriesDTO;

public class RegattaCompetitionSeries extends AbstractRegattaCompetitionSeries {

    private static RegattaCompetitionSeriesUiBinder uiBinder = GWT.create(RegattaCompetitionSeriesUiBinder.class);

    interface RegattaCompetitionSeriesUiBinder extends UiBinder<HTMLPanel, RegattaCompetitionSeries> {
    }
    
    @UiField DivElement seriesNameUi;
    @UiField DivElement competitorCountUi;
    @UiField DivElement raceCountUi;
    @UiField HTMLPanel containerUi;

    public RegattaCompetitionSeries(RaceCompetitionFormatSeriesDTO series) {
        super(series);
        RegattaCompetitionResources.INSTANCE.css().ensureInjected();
    }
    
    @Override
    public RegattaCompetitionFleetView addFleetView(RaceCompetitionFormatFleetDTO fleet) {
        RegattaCompetitionFleet fleetView = new RegattaCompetitionFleet(fleet);
        containerUi.add(fleetView);
        return fleetView;
    }

    @Override
    protected Widget getMainUiWidget() {
        return uiBinder.createAndBindUi(this);
    }

    @Override
    protected void setSeriesName(String seriesName) {
        seriesNameUi.setInnerText(seriesName);
    }

    @Override
    protected void setRacesAndCompetitorInfo(String raceInfoText, String competitorInfoText) {
        raceCountUi.setInnerText(raceInfoText);
        if (competitorInfoText.isEmpty()) competitorCountUi.removeFromParent();
        else competitorCountUi.setInnerText(competitorInfoText);
    }

}
