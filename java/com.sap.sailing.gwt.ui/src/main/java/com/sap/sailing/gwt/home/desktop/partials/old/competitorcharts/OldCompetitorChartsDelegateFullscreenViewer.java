package com.sap.sailing.gwt.home.desktop.partials.old.competitorcharts;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.fullscreen.FullscreenContainer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sse.common.Color;

public class OldCompetitorChartsDelegateFullscreenViewer extends FullscreenContainer<MultiCompetitorLeaderboardChart>
        implements CompetitorChartsDelegate {
    
    private final ListBox chartTypeSelection = new ListBox();

    public OldCompetitorChartsDelegateFullscreenViewer() {
        showLogo();
        showBorder();
        addToolbarInfo(createLabeledChartTypeSelectionControl());
    }
    
    @Override
    protected void onShow() {
        getContentWidget().forceMaximumChartSize();
        getContentWidget().onResize();
    }
    
    @Override
    public void setCompetitorChart(MultiCompetitorLeaderboardChart competitorChart) {
        showContent(competitorChart);
    }

    @Override
    public ListBox getChartTypeSelectionControl() {
        return chartTypeSelection;
    }
    
    private Widget createLabeledChartTypeSelectionControl() {
        FlowPanel panel = getWidgetWithMarginTop(new FlowPanel(), -30, Unit.PX);
        panel.add(getWidgetWitDisplay(new Label(StringMessages.INSTANCE.chooseChartType() + " "), Display.INLINE));
        panel.add(getWidgetWithColor(chartTypeSelection, Color.BLACK));
        return panel;
    }
    
    private <T extends Widget> T getWidgetWithMarginTop(T widget, double value, Unit unit) {
        widget.getElement().getStyle().setMarginTop(value, unit);
        return widget; 
    }
    
    private <T extends Widget> T getWidgetWithColor(T widget, Color color) {
        widget.getElement().getStyle().setColor(color.getAsHtml());
        return widget; 
    }
    
    private <T extends Widget> T getWidgetWitDisplay(T widget, Display display) {
        widget.getElement().getStyle().setDisplay(display);
        return widget; 
    }

}
