package com.sap.sailing.gwt.home.desktop.partials.old.competitorcharts;

import java.util.Optional;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sap.sailing.gwt.home.shared.partials.fullscreen.FullscreenContainer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sse.common.Color;

public class OldCompetitorChartsDelegateFullscreenViewer extends FullscreenContainer<MultiCompetitorLeaderboardChart>
        implements CompetitorChartsDelegate {

    private final ListBox chartTypeSelection = new ListBox();
    private HandlerRegistration redrawCallback;

    public OldCompetitorChartsDelegateFullscreenViewer() {
        showLogo();
        showBorder();
        setHeaderWidget(createHeaderPanel());
        addToolbarInfo(createLabeledChartTypeSelectionControl());
    }

    @Override
    protected void onShow() {
        final Runnable resizeCallback = () -> {
            getContentWidget().forceMaximumChartSize();
            getContentWidget().onResize();
        };
        this.redrawCallback = getContentWidget().addChartDataUpdatedHandler(resizeCallback);
        resizeCallback.run();
    }

    @Override
    protected void onClose() {
        Optional.ofNullable(redrawCallback).ifPresent(HandlerRegistration::removeHandler);
    }

    @Override
    public void setCompetitorChart(final MultiCompetitorLeaderboardChart competitorChart) {
        showContent(competitorChart);
    }

    @Override
    public ListBox getChartTypeSelectionControl() {
        return chartTypeSelection;
    }

    private Widget createHeaderPanel() {
        final Widget heading = getBoldLabel(StringMessages.INSTANCE.competitorsAnalytics());
        heading.getElement().getStyle().setFontSize(18, Unit.PX);
        heading.getElement().getStyle().setMarginTop(-.2, Unit.EM);
        return createPanel(heading, getBoldLabel(StringMessages.INSTANCE.chartSelectionHint()));
    }

    private Widget createLabeledChartTypeSelectionControl() {
        final Widget panel = createPanel(chartTypeSelection);
        panel.getElement().getStyle().setLineHeight(32, Unit.PX);
        chartTypeSelection.getElement().getStyle().setColor(Color.BLACK.getAsHtml());
        return panel;
    }

    private Widget getBoldLabel(final String text) {
        final Label label = new Label(text);
        label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        return label;
    }

}
