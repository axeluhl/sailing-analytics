package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.AbstractRegattaCompetitionSeries;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionFleetView;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatSeriesDTO;

public class RegattaCompetitionSeries extends AbstractRegattaCompetitionSeries {

    private static RegattaCompetitionSeriesUiBinder uiBinder = GWT.create(RegattaCompetitionSeriesUiBinder.class);

    interface RegattaCompetitionSeriesUiBinder extends UiBinder<MobileSection, RegattaCompetitionSeries> {
    }
    
    @UiField SectionHeaderContent sectionHeaderUi;
    @UiField FlowPanel fleetContainerUi;
    @UiField MobileSection seriesUi;

    public RegattaCompetitionSeries(RaceCompetitionFormatSeriesDTO series) {
        super(series);
        seriesUi.setEdgeToEdgeContent(true);
        sectionHeaderUi.initCollapsibility(fleetContainerUi.getElement(), true);
    }
    
    @Override
    public RegattaCompetitionFleetView addFleetView(RaceCompetitionFormatFleetDTO fleet) {
        RegattaCompetitionFleet fleetView = new RegattaCompetitionFleet(fleet);
        fleetContainerUi.add(fleetView);
        return fleetView;
    }

    @Override
    protected Widget getMainUiWidget() {
        return uiBinder.createAndBindUi(this);
    }

    @Override
    protected void setSeriesName(String seriesName) {
        sectionHeaderUi.setSectionTitle(seriesName);
    }

    @Override
    protected void setRacesAndCompetitorInfo(String raceInfoText, String competitorInfoText) {
        String separator = raceInfoText.isEmpty() || competitorInfoText.isEmpty() ? "" : " | ";
        sectionHeaderUi.setSubtitle(competitorInfoText + separator + raceInfoText);
    }

}
