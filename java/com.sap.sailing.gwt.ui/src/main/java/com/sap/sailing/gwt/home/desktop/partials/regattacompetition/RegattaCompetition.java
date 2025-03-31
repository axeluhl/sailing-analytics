package com.sap.sailing.gwt.home.desktop.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatSeriesDTO;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView;

public class RegattaCompetition extends Composite implements RegattaCompetitionView {

    private static RegattaCompetitionUiBinder uiBinder = GWT.create(RegattaCompetitionUiBinder.class);

    interface RegattaCompetitionUiBinder extends UiBinder<Widget, RegattaCompetition> {
    }

    @UiField FlowPanel regattaSeriesContainerUi;
    
    public RegattaCompetition() {
        RegattaCompetitionResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void clearContent() {
        regattaSeriesContainerUi.clear(); 
    }

    @Override
    public RegattaCompetitionSeriesView addSeriesView(RaceCompetitionFormatSeriesDTO series) {
        RegattaCompetitionSeries seriesView = new RegattaCompetitionSeries(series);
        regattaSeriesContainerUi.add(seriesView);
        return seriesView;
    }

}
