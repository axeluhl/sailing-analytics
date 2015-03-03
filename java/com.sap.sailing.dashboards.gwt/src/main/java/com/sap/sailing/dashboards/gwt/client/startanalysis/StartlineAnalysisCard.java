package com.sap.sailing.dashboards.gwt.client.startanalysis;

import java.util.Iterator;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.RibDashboardImageResources;
import com.sap.sailing.dashboards.gwt.client.startanalysis.rankingtable.StartAnalysisStartRankTable;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;

public class StartlineAnalysisCard extends Composite implements HasWidgets, StartAnalysisPageChangeListener {

    private StartAnalysisSimpleMap simpleMap;

    private static StartlineAnalysisCardUiBinder uiBinder = GWT.create(StartlineAnalysisCardUiBinder.class);

    interface StartlineAnalysisCardUiBinder extends UiBinder<Widget, StartlineAnalysisCard> {
    }

    interface StartlineAnalysis extends CssResource {
        public String startanalysis_card_wind_canvas();
        public String startanalysis_card_wind_arrow();
    }

    @UiField
    StartlineAnalysis style;

    @UiField
    HTMLPanel startanalysis_card;

    @UiField
    HTMLPanel winddirection;

    @UiField
    HTMLPanel startanalysis_card_table;

    @UiField
    DivElement startanalysis_card_wind_data_direction;

    @UiField
    DivElement startanalysis_card_wind_data_speed;

    @UiField
    DivElement startanalysis_card_line_advantage;

    Canvas winddirectionContainer;
    Image windarrow;

    private int cardid;
    private boolean containsMap = false;
    private StartAnalysisDTO startAnalysisDTO;

    public StartlineAnalysisCard(double leftCSSProperty, int cardID, StartAnalysisDTO startAnalysisDTO) {

        initWidget(uiBinder.createAndBindUi(this));

        startanalysis_card.getElement().getStyle().setLeft(leftCSSProperty, Unit.PCT);

        winddirectionContainer = Canvas.createIfSupported();
        this.startAnalysisDTO = startAnalysisDTO;

        winddirectionContainer.setStyleName(style.startanalysis_card_wind_canvas());

        windarrow = new Image();
        windarrow.setResource(RibDashboardImageResources.INSTANCE.windarrow());
        windarrow.setStyleName(style.startanalysis_card_wind_arrow());
        winddirection.add(windarrow);

        cardid = cardID;

        if (cardID == 0) {
            addMap(cardID);
        }

        startanalysis_card_table.add(new StartAnalysisStartRankTable(startAnalysisDTO.startAnalysisCompetitorDTOs));
        fillWindAndStartLineData(this.startAnalysisDTO);

        this.onLoad();
    }

    private void fillWindAndStartLineData(StartAnalysisDTO startAnalysisDTO) {

        if (startAnalysisDTO.startAnalysisWindLineInfoDTO != null) {
            String winddirectionformatted = NumberFormat.getFormat("#0").format(
                    startAnalysisDTO.startAnalysisWindLineInfoDTO.windDirectionInDegrees-90);
            windarrow.getElement().getStyle()
                    .setProperty("transform", "rotate(" + winddirectionformatted + "deg)");
            windarrow.getElement().getStyle().setProperty("webkitTransform", "rotate(" + winddirectionformatted + "deg)");
            startanalysis_card_wind_data_direction
                    .setInnerHTML(NumberFormat.getFormat("#0.0").format(startAnalysisDTO.startAnalysisWindLineInfoDTO.windDirectionInDegrees) + "°");
            startanalysis_card_wind_data_speed
                    .setInnerHTML(NumberFormat.getFormat("#0.0").format(startAnalysisDTO.startAnalysisWindLineInfoDTO.windSpeedInKnots) + " kts");
            startanalysis_card_line_advantage.setInnerHTML(startAnalysisDTO.startAnalysisWindLineInfoDTO.startLineAdvantage.startLineAdvatageType.getDisplayName()+": "+NumberFormat.getFormat("#0.0").format(startAnalysisDTO.startAnalysisWindLineInfoDTO.startLineAdvantage.startLineAdvantage)+" m");
        }
    }

    private void addMap(final int cardID) {
        Timer timer = new Timer()
        {
            @Override
            public void run()
            {
                simpleMap = new StartAnalysisSimpleMap(cardID, startAnalysisDTO);
                startanalysis_card.add(simpleMap);
            }
        };

        timer.schedule(800);
        containsMap = true;
    }

    private void removeMap() {
        if(simpleMap.isAttached()){
        startanalysis_card.remove(simpleMap);
        containsMap = false;
        }
    }

    @Override
    public void loadMapAndContent(int newPageIndex, String someDataDTO) {
        if (cardid == newPageIndex && containsMap == false) {
            addMap(newPageIndex);
        } else if (containsMap == true) {
            removeMap();
        }
    }
    
    @Override
    public void add(Widget w) {
        throw new UnsupportedOperationException("The method add(Widget w) is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("The method clear() is not supported.");
    }

    @Override
    public Iterator<Widget> iterator() {
        return null;
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }

}
