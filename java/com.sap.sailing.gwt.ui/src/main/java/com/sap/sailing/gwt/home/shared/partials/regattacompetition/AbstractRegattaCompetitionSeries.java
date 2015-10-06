package com.sap.sailing.gwt.home.shared.partials.regattacompetition;

import static com.sap.sailing.domain.common.LeaderboardNameConstants.DEFAULT_SERIES_NAME;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionSeriesView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatSeriesDTO;

public abstract class AbstractRegattaCompetitionSeries extends Composite implements RegattaCompetitionSeriesView {
    
    private static final StringMessages I18N = StringMessages.INSTANCE;
    
    protected AbstractRegattaCompetitionSeries(RaceCompetitionFormatSeriesDTO series) {
        initWidget(getMainUiWidget());
        setSeriesName(DEFAULT_SERIES_NAME.equals(series.getSeriesName()) ? I18N.races() : series.getSeriesName());
        String raceInfoText = series.getRaceCount() > 0 ? I18N.racesCount(series.getRaceCount()) : "";
        String competitorInfoText = series.getCompetitorCount() > 0 ? I18N.competitorsCount(series.getCompetitorCount()) : "";
        setRacesAndCompetitorInfo(raceInfoText, competitorInfoText);
    }
    
    @Override
    public void doFilter(boolean filter) {
        setVisible(!filter);
    }
    
    protected abstract Widget getMainUiWidget();
    
    protected abstract void setSeriesName(String seriesName);
    
    protected abstract void setRacesAndCompetitorInfo(String raceInfoText, String competitorInfoText);

}
