package com.sap.sailing.dashboards.gwt.client.startanalysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.startanalysis.rankingtable.StartAnalysisStartRankTable;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public class StartlineAnalysisCard extends Composite implements HasWidgets, StartAnalysisPageChangeListener {

    private static StartlineAnalysisCardUiBinder uiBinder = GWT.create(StartlineAnalysisCardUiBinder.class);

    interface StartlineAnalysisCardUiBinder extends UiBinder<Widget, StartlineAnalysisCard> {
    }

    interface StartlineAnalysis extends CssResource {
    }

    @UiField
    StartlineAnalysis style;

    @UiField
    HTMLPanel startanalysis_card;
    
    @UiField
    HTMLPanel startanalysis_card_table;

    @UiField
    DivElement startanalysis_card_line_advantage;

    private int cardid;
    private StartAnalysisDTO startAnalysisDTO;
    private RaceMap raceMap;

    private SailingServiceAsync sailingServiceAsync;
    private final CompetitorSelectionModel competitorSelectionModel;

    private final double WIND_LINE_ADVANTAGE_DIV_WIDTH_IN_PT = 185;
    private final double GEOMETRIC_LINE_ADVANTAGE_DIV_WIDTH_IN_PT = 210;
    private final int TAIL_LENGHT_IN_MILLISECONDS = 900000;
    

    private static StartAnalysisResources resources = GWT.create(StartAnalysisResources.class);

    public StartlineAnalysisCard(double leftCSSProperty, int cardId, StartAnalysisDTO startAnalysisDTO,
            SailingServiceAsync sailingServiceAsync) {
        resources.combinedWindPanelStyle().ensureInjected();
        this.sailingServiceAsync = sailingServiceAsync;
        competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */true);

        initWidget(uiBinder.createAndBindUi(this));
        startanalysis_card.getElement().getStyle().setLeft(leftCSSProperty, Unit.PCT);
        this.startAnalysisDTO = startAnalysisDTO;
        this.cardid = cardId;
        startanalysis_card_table.add(new StartAnalysisStartRankTable(startAnalysisDTO.startAnalysisCompetitorDTOs));
        fillWindAndStartLineData(this.startAnalysisDTO);
    }

    private void fillWindAndStartLineData(StartAnalysisDTO startAnalysisDTO) {
        if (startAnalysisDTO.startAnalysisWindLineInfoDTO != null) {
            setLineAdvantageDivWidth(startAnalysisDTO.startAnalysisWindLineInfoDTO.startLineAdvantage.startLineAdvatageType);
            startanalysis_card_line_advantage
                    .setInnerHTML(startAnalysisDTO.startAnalysisWindLineInfoDTO.startLineAdvantage.startLineAdvatageType
                            .getDisplayName()
                            + ": "
                            + NumberFormat
                                    .getFormat("#0.0")
                                    .format(startAnalysisDTO.startAnalysisWindLineInfoDTO.startLineAdvantage.startLineAdvantage)
                            + " m");
        }
    }

    private void setLineAdvantageDivWidth(StartlineAdvantageType startlineAdvantageType) {
        if (startAnalysisDTO.startAnalysisWindLineInfoDTO.startLineAdvantage.startLineAdvatageType
                .equals(StartlineAdvantageType.GEOMETRIC)) {
            startanalysis_card_line_advantage.getStyle().setWidth(GEOMETRIC_LINE_ADVANTAGE_DIV_WIDTH_IN_PT, Unit.PT);
        } else {
            startanalysis_card_line_advantage.getStyle().setWidth(WIND_LINE_ADVANTAGE_DIV_WIDTH_IN_PT, Unit.PT);
        }
    }

    private void addMap(final int cardID, final StartAnalysisDTO startAnalysisDTO) {
        AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
        com.sap.sse.gwt.client.player.Timer timer = new com.sap.sse.gwt.client.player.Timer(PlayModes.Live, 1000l);
        timer.setTime(startAnalysisDTO.timeOfStartInMilliSeconds);
        timer.pause();

        RaceTimesInfoProvider raceTimesInfoProvider = new RaceTimesInfoProvider(sailingServiceAsync,
                asyncActionsExecutor, null, Collections.singletonList(startAnalysisDTO.regattaAndRaceIdentifier), 5000l /* requestInterval */);

        raceMap = new RaceMap(sailingServiceAsync, asyncActionsExecutor, null, timer, competitorSelectionModel,
                StringMessages.INSTANCE, false, false, false, startAnalysisDTO.regattaAndRaceIdentifier,
                resources.combinedWindPanelStyle());
        competitorSelectionModel.setCompetitors(startAnalysisDTO.getCompetitorDTOsFromStartAnaylsisCompetitorDTOs(),
                raceMap);
        raceMap.onRaceSelectionChange(Collections.singletonList(startAnalysisDTO.regattaAndRaceIdentifier));
        raceMap.getSettings().setZoomSettings(new RaceMapZoomSettings(ZoomTypes.TAILS));
        raceMap.getSettings().setHelpLinesSettings(getHelpLineSettings());
        raceMap.getSettings().setTailLengthInMilliseconds(TAIL_LENGHT_IN_MILLISECONDS);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(raceMap);
        startanalysis_card.add(raceMap);
        /**
         * Executes onResize() after the reflow of the DOM. Otherwise it has no effect.
         * Needs to resize the map because google maps are not shown loaded fully when they are hidden.
         * */
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            
            @Override
            public void execute() {
                raceMap.onResize();
            }
        });
    }

    private RaceMapHelpLinesSettings getHelpLineSettings() {
        HashSet<HelpLineTypes> visibleHelpLines = new HashSet<HelpLineTypes>();
        visibleHelpLines.add(HelpLineTypes.STARTLINE);
        visibleHelpLines.add(HelpLineTypes.BOATTAILS);
        visibleHelpLines.add(HelpLineTypes.STARTLINETOFIRSTMARKTRIANGLE);
        return new RaceMapHelpLinesSettings(visibleHelpLines);
    }

    private void removeMap() {
        if (raceMap != null && raceMap.isAttached() == true) {
            startanalysis_card.remove(raceMap);
        }
    }

    @Override
    public void startAnalysisComponentPageChangedToIndexAndStartAnalysis(final int newPageIndex,
            final StartAnalysisDTO startAnalysisDTO) {
        removeMap();
        if (cardid == newPageIndex) {
            /**
             * Prevents UI to animate startanalysis card animation with small interruptions.
             * The map gets added just after the DOM manipulation was executed.
             * */
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    addMap(newPageIndex, startAnalysisDTO);
                }
            });
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
