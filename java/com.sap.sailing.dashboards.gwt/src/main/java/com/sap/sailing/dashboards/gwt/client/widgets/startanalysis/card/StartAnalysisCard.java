package com.sap.sailing.dashboards.gwt.client.widgets.startanalysis.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.widgets.startanalysis.StartAnalysisPageChangeListener;
import com.sap.sailing.dashboards.gwt.client.widgets.startanalysis.rankingtable.StartAnalysisStartRankTable;
import com.sap.sailing.dashboards.gwt.shared.StartlineAdvantageType;
import com.sap.sailing.dashboards.gwt.shared.dto.StartAnalysisDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.gwt.ui.client.RaceCompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.DefaultQuickRanksDTOProvider;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceCompetitorSet;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapResources;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public class StartAnalysisCard extends Composite implements HasWidgets, StartAnalysisPageChangeListener {

    private static StartlineAnalysisCardUiBinder uiBinder = GWT.create(StartlineAnalysisCardUiBinder.class);

    interface StartlineAnalysisCardUiBinder extends UiBinder<Widget, StartAnalysisCard> {
    }

    interface StartlineAnalysis extends CssResource {
    }

    @UiField
    HTMLPanel startanalysis_card;
    
    @UiField
    HTMLPanel card_map_container;
    
    @UiField
    HTMLPanel startanalysis_card_table;

    @UiField
    DivElement startanalysis_card_line_advantage;
    
    @UiField
    DivElement startanalysis_card_race_time;

    private int cardid;
    private StartAnalysisDTO startAnalysisDTO;
    private RaceMap raceMap;

    private final SailingServiceAsync sailingServiceAsync;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final RaceCompetitorSelectionModel competitorSelectionModel;
    
    private final double WIND_LINE_ADVANTAGE_DIV_WIDTH_IN_PT = 185;
    private final double GEOMETRIC_LINE_ADVANTAGE_DIV_WIDTH_IN_PT = 210;
    private final String RACE_TIME_START = "00:00:00";
    
    private RaceMapResources raceMapResources;
    
    public StartAnalysisCard(double leftCSSProperty, int cardId, StartAnalysisDTO startAnalysisDTO,
            SailingServiceAsync sailingServiceAsync, ErrorReporter errorReporter, RaceMapResources raceMapResources) {
        stringMessages = StringMessages.INSTANCE;
        this.sailingServiceAsync = sailingServiceAsync;
        this.errorReporter = errorReporter;
        this.raceMapResources = raceMapResources;
        final Map<CompetitorDTO, BoatDTO> competitorsToBoats = startAnalysisDTO.startAnalysisCompetitorDTOs.stream()
                .collect(Collectors.toMap(c -> c.competitorDTO, c -> c.boatDTO));
        competitorSelectionModel = new RaceCompetitorSelectionModel(/* hasMultiSelection */true, competitorsToBoats);
        competitorSelectionModel.setCompetitors(startAnalysisDTO.getCompetitorDTOsFromStartAnaylsisCompetitorDTOs(), raceMap);
        initWidget(uiBinder.createAndBindUi(this));
        startanalysis_card.getElement().getStyle().setLeft(leftCSSProperty, Unit.PCT);
        this.startAnalysisDTO = startAnalysisDTO;
        this.cardid = cardId;
        startanalysis_card_table.add(new StartAnalysisStartRankTable(startAnalysisDTO.startAnalysisCompetitorDTOs, competitorSelectionModel));
        fillWindAndStartLineData(this.startAnalysisDTO);
    }

    private void fillWindAndStartLineData(StartAnalysisDTO startAnalysisDTO) {
        if (startAnalysisDTO.startAnalysisWindLineInfoDTO != null) {
            setLineAdvantageDivWidth(startAnalysisDTO.startAnalysisWindLineInfoDTO.startLineAdvantage.startLineAdvatageType);
            final String startLineAdvantageType;
            if (startAnalysisDTO.startAnalysisWindLineInfoDTO.startLineAdvantage.startLineAdvatageType
                    .equals(StartlineAdvantageType.GEOMETRIC)) {
                startLineAdvantageType = stringMessages.dashboardStartlineAdvantageByGeometryHeader();
            } else {
                startLineAdvantageType = stringMessages.dashboardStartlineAdvantagesByWindHeader();
            }
            startanalysis_card_line_advantage
                    .setInnerHTML(SafeHtmlUtils.fromString(startLineAdvantageType
                            + ": "
                            + NumberFormat
                                    .getFormat("#0.0")
                                    .format(startAnalysisDTO.startAnalysisWindLineInfoDTO.startLineAdvantage.startLineAdvantage)
                            + " m").asString());
            String formattedTimeSinceStart;
            if (startAnalysisDTO.racingProcedureType.equals(RacingProcedureType.GateStart)) {
                formattedTimeSinceStart = getRaceTimeStringFromMilliseconds(startAnalysisDTO.tailLenghtInMilliseconds);
            } else {
                formattedTimeSinceStart = RACE_TIME_START;
            }
            startanalysis_card_race_time.setInnerHTML("Elapsed Time: "+formattedTimeSinceStart);
        }
    }
    
    private String getRaceTimeStringFromMilliseconds(long milliseconds){
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
        return NumberFormat.getFormat("00").format(hours)+":"+NumberFormat.getFormat("00").format(minutes)+":"+NumberFormat.getFormat("00").format(seconds);
    }

    private void setLineAdvantageDivWidth(StartlineAdvantageType startlineAdvantageType) {
        if (startAnalysisDTO.startAnalysisWindLineInfoDTO.startLineAdvantage.startLineAdvatageType
                .equals(StartlineAdvantageType.GEOMETRIC)) {
            startanalysis_card_line_advantage.getStyle().setWidth(GEOMETRIC_LINE_ADVANTAGE_DIV_WIDTH_IN_PT, Unit.PT);
        } else {
            startanalysis_card_line_advantage.getStyle().setWidth(WIND_LINE_ADVANTAGE_DIV_WIDTH_IN_PT, Unit.PT);
        }
    }

    private void addMap(final int cardID, final StartAnalysisDTO startAnalysisDTO, RaceMapResources raceMapResources) {
        com.sap.sse.gwt.client.player.Timer timer = new com.sap.sse.gwt.client.player.Timer(PlayModes.Live, 1000l);
        timer.pause();
        ArrayList<ZoomTypes> zoomTypes = new ArrayList<ZoomTypes>();
        if (startAnalysisDTO.racingProcedureType.equals(RacingProcedureType.GateStart)) {
            timer.setTime(startAnalysisDTO.timeOfStartInMilliSeconds + startAnalysisDTO.tailLenghtInMilliseconds);
            zoomTypes.add(ZoomTypes.BUOYS);
        } else {
            timer.setTime(startAnalysisDTO.timeOfStartInMilliSeconds);
            zoomTypes.add(ZoomTypes.BUOYS);
        }
        RaceMapZoomSettings raceMapZoomSettings = new RaceMapZoomSettings(zoomTypes, false);
        
        AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
        RaceMapSettings defaultRaceMapSettings = RaceMapSettings.readSettingsFromURL(
                /* defaultForShowMapControls */ true, /* defaultForShowCourseGeometry */ false,
                /* defaultForMapOrientationWindUp */ false, /* defaultForViewShowStreamlets */ false,
                /* defaultForViewShowStreamletColors */ false, /* defaultForViewShowSimulation */ false);
        final RaceMapSettings raceMapSettings = new RaceMapSettings(raceMapZoomSettings, getHelpLineSettings(),
                defaultRaceMapSettings.getTransparentHoverlines(), defaultRaceMapSettings.getHoverlineStrokeWeight(), 
                startAnalysisDTO.tailLenghtInMilliseconds, defaultRaceMapSettings.isWindUp(),
                defaultRaceMapSettings.getBuoyZoneRadius(), defaultRaceMapSettings.isShowOnlySelectedCompetitors(),
                defaultRaceMapSettings.isShowSelectedCompetitorsInfo(), defaultRaceMapSettings.isShowWindStreamletColors(),
                defaultRaceMapSettings.isShowWindStreamletOverlay(), defaultRaceMapSettings.isShowSimulationOverlay(),
                defaultRaceMapSettings.isShowMapControls(), defaultRaceMapSettings.getManeuverTypesToShow(),
                defaultRaceMapSettings.isShowDouglasPeuckerPoints(), defaultRaceMapSettings.isShowEstimatedDuration(),
                defaultRaceMapSettings.getStartCountDownFontSizeScaling(), defaultRaceMapSettings.isShowManeuverLossVisualization());

        
        RaceTimesInfoProvider raceTimesInfoProvider = new RaceTimesInfoProvider(sailingServiceAsync,
                asyncActionsExecutor, errorReporter,
                Collections.singletonList(startAnalysisDTO.regattaAndRaceIdentifier), 5000l /* requestInterval */);
        raceMap = new RaceMap(null, null, new RaceMapLifecycle(StringMessages.INSTANCE), raceMapSettings,
                sailingServiceAsync,
                asyncActionsExecutor, errorReporter, timer, competitorSelectionModel,
                new RaceCompetitorSet(competitorSelectionModel), StringMessages.INSTANCE,
                startAnalysisDTO.regattaAndRaceIdentifier, raceMapResources, /* showHeaderPanel */ true, new DefaultQuickRanksDTOProvider());
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(raceMap);
        raceMap.setSize("100%", "100%");
        card_map_container.getElement().getStyle().setHeight(getHeightForRaceMapInPixels(), Unit.PX);
        card_map_container.add(raceMap);
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
    
    private double getHeightForRaceMapInPixels(){
        return this.getElement().getOffsetHeight() - startanalysis_card_table.getElement().getOffsetHeight() - 30;
    }

    private RaceMapHelpLinesSettings getHelpLineSettings() {
        HashSet<HelpLineTypes> visibleHelpLines = new HashSet<HelpLineTypes>();
        visibleHelpLines.add(HelpLineTypes.STARTLINE);
        visibleHelpLines.add(HelpLineTypes.BOATTAILS);
        visibleHelpLines.add(HelpLineTypes.STARTLINETOFIRSTMARKTRIANGLE);
        visibleHelpLines.add(HelpLineTypes.ADVANTAGELINE);
        return new RaceMapHelpLinesSettings(visibleHelpLines);
    }

    private void removeMap() {
        if (raceMap != null && raceMap.isAttached() == true) {
            card_map_container.remove(raceMap);
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
                    addMap(newPageIndex, startAnalysisDTO, raceMapResources);
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
