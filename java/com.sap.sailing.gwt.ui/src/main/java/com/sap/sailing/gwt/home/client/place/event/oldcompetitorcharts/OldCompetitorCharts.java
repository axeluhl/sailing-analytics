package com.sap.sailing.gwt.home.client.place.event.oldcompetitorcharts;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.home.client.place.event.regattaleaderboard.EventRegattaLeaderboardResources;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChartSettings;
import com.sap.sse.common.Util;

public class OldCompetitorCharts extends Composite {
    private static OldCompetitorChartsUiBinder uiBinder = GWT.create(OldCompetitorChartsUiBinder.class);

    interface OldCompetitorChartsUiBinder extends UiBinder<Widget, OldCompetitorCharts> {
    }

    @UiField HTMLPanel oldCompetitorChartsPanel;
    @UiField ListBox chartTypeSelectionListBox;
    @UiField DivElement competitorSelectionStateUi;
    
    private final List<DetailType> availableDetailsTypes;
    private MultiCompetitorLeaderboardChart multiCompetitorChart;

    public OldCompetitorCharts() {
        this.availableDetailsTypes = new ArrayList<DetailType>();
        this.multiCompetitorChart = null;

        EventRegattaLeaderboardResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setChart(MultiCompetitorLeaderboardChart multiCompetitorChart, List<DetailType> availableDetailsTypes, DetailType initialDetailType) {
        this.multiCompetitorChart = multiCompetitorChart;
        this.availableDetailsTypes.clear();
        this.availableDetailsTypes.addAll(availableDetailsTypes);
        
        fillChartTypeSelectionBox(initialDetailType);
        
        oldCompetitorChartsPanel.add(multiCompetitorChart);
    }

    private void fillChartTypeSelectionBox(DetailType initialDetailType) {
        chartTypeSelectionListBox.clear();
        int i = 0;
        for (DetailType detailType : availableDetailsTypes) {
            chartTypeSelectionListBox.addItem(DetailTypeFormatter.format(detailType), detailType.name());
            if (detailType == initialDetailType) {
                chartTypeSelectionListBox.setSelectedIndex(i);
            }
            i++;
        }
    }

    @UiHandler("chartTypeSelectionListBox")
    void chartTypeSelectionChanged(ChangeEvent event) {
        DetailType selectedChartDetailType = getSelectedChartDetailType();
        multiCompetitorChart.updateSettings(new MultiCompetitorLeaderboardChartSettings(selectedChartDetailType));
    }
    
    public DetailType getSelectedChartDetailType() {
        DetailType result = null;
        int selectedIndex = chartTypeSelectionListBox.getSelectedIndex();
        String selectedDetailType = chartTypeSelectionListBox.getValue(selectedIndex);
        for (DetailType detailType : availableDetailsTypes) {
            if (detailType.name().equals(selectedDetailType)) {
                result = detailType;
                break;
            }
        }
        return result;
    }

    public void updateSelectionState(CompetitorSelectionProvider competitorSelectionProvider) {
        int selectedCompetitorsCount = Util.size(competitorSelectionProvider.getSelectedCompetitors());
        if(selectedCompetitorsCount > 0) {
            int competitorsCount = Util.size(competitorSelectionProvider.getAllCompetitors());
            competitorSelectionStateUi.getStyle().setVisibility(Visibility.VISIBLE);
            competitorSelectionStateUi.setInnerText(selectedCompetitorsCount + "/" + competitorsCount + " competitors selected.");
        } else {
            competitorSelectionStateUi.getStyle().setVisibility(Visibility.HIDDEN);
        }
    }
}
