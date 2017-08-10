package com.sap.sailing.gwt.home.desktop.partials.old.competitorcharts;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.home.desktop.partials.old.EventRegattaLeaderboardResources;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiCompetitorLeaderboardChartSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sse.common.Util;

public class OldCompetitorCharts extends Composite {
    private static OldCompetitorChartsUiBinder uiBinder = GWT.create(OldCompetitorChartsUiBinder.class);

    interface OldCompetitorChartsUiBinder extends UiBinder<Widget, OldCompetitorCharts> {
    }

    @UiField HTMLPanel oldCompetitorChartsPanel;
    @UiField ListBox chartTypeSelectionListBox;
    @UiField Anchor fullscreenAnchor;
    @UiField DivElement competitorSelectionStateUi;
    
    private final List<DetailType> availableDetailsTypes;
    private MultiCompetitorLeaderboardChart multiCompetitorChart;
    private final CompetitorChartsDelegate delegate;
    
    public OldCompetitorCharts() {
        this(null);
    }

    public OldCompetitorCharts(CompetitorChartsDelegate delegate) {
        this.availableDetailsTypes = new ArrayList<DetailType>();
        this.multiCompetitorChart = null;
        EventRegattaLeaderboardResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        fullscreenAnchor.setTitle(StringMessages.INSTANCE.openFullscreenView());
        this.delegate = delegate;
        this.setupFullscreenDelegate();
    }
    
    private void setupFullscreenDelegate() {
        if (delegate == null) {
            fullscreenAnchor.removeFromParent();
            return;
        }
        delegate.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (multiCompetitorChart != null) {
                    multiCompetitorChart.removeFromParent();
                    oldCompetitorChartsPanel.add(multiCompetitorChart);
                    multiCompetitorChart.forceChartToClientHeight();
                }
            }
        });
        delegate.getChartTypeSelectionControl().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                synchronizeChartTypeSelection(delegate.getChartTypeSelectionControl(), chartTypeSelectionListBox);
                OldCompetitorCharts.this.updateChartType();
                multiCompetitorChart.forceMaximumChartSize();
            }
        });
    }
    
    private void synchronizeChartTypeSelection(ListBox from, ListBox to) {
        int selectedIndex = from.getSelectedIndex();
        to.setSelectedIndex(selectedIndex);
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
        if (delegate != null) delegate.getChartTypeSelectionControl().clear();
        int i = 0;
        for (DetailType detailType : availableDetailsTypes) {
            String formattedDetailType = DetailTypeFormatter.format(detailType);
            chartTypeSelectionListBox.addItem(formattedDetailType, detailType.name());
            if (delegate != null) delegate.getChartTypeSelectionControl().addItem(formattedDetailType, detailType.name());
            if (detailType == initialDetailType) {
                chartTypeSelectionListBox.setSelectedIndex(i);
                if (delegate != null) delegate.getChartTypeSelectionControl().setSelectedIndex(i);
            }
            i++;
        }
    }

    @UiHandler("chartTypeSelectionListBox")
    void chartTypeSelectionChanged(ChangeEvent event) {
        if (delegate != null) synchronizeChartTypeSelection(chartTypeSelectionListBox, delegate.getChartTypeSelectionControl());
        updateChartType();
    }
    
    private void updateChartType() {
        DetailType selectedChartDetailType = getSelectedChartDetailType();
        multiCompetitorChart.updateSettings(new MultiCompetitorLeaderboardChartSettings(selectedChartDetailType));
    }
    
    @UiHandler("fullscreenAnchor")
    void fullscreenClicked(ClickEvent event) {
        if(multiCompetitorChart != null && delegate != null) {
            multiCompetitorChart.removeFromParent();
            delegate.setCompetitorChart(multiCompetitorChart);
        }
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
