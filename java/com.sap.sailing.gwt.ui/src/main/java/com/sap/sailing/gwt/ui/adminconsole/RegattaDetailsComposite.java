package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.RankingMetricTypeFormatter;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;


public class RegattaDetailsComposite extends Composite {
    private RegattaDTO regatta;

    private final CaptionPanel mainPanel;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final RegattaRefresher regattaRefresher;

    private final Label regattaId;
    private final Label regattaName;
    private final Label startDate;
    private final Label endDate;
    private final Label boatClassName;
    private final Label scoringSystem;
    private final Label rankingMetric;
    private final Label defaultCourseArea;
    private final Label useStartTimeInference;
    private final Label controlTrackingFromStartAndFinishTimes;
    private final Label canBoatsOfCompetitorsChangePerRace;
    private final Label competitorRegistrationType;
    private final Label configuration;
    private final Label buoyZoneRadiusInHullLengths;
    private final Label openRegattaRegistrationLink;
    
    private final SelectionModel<SeriesDTO> seriesSelectionModel;
    private final CellTable<SeriesDTO> seriesTable;
    private ListDataProvider<SeriesDTO> seriesListDataProvider;

    private static AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public RegattaDetailsComposite(final SailingServiceAsync sailingService, final RegattaRefresher regattaRefresher,  
            final ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.regattaRefresher = regattaRefresher;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        regatta = null;
        mainPanel = new CaptionPanel(stringMessages.regatta());
        VerticalPanel vPanel = new VerticalPanel();
        mainPanel.add(vPanel);

        int rows = 15;
        Grid grid = new Grid(rows, 2);
        vPanel.add(grid);
        
        int currentRow = 0;
        regattaId = createLabelAndValueWidget(grid, currentRow++, stringMessages.id(), "RegattaIdLabel");
        regattaName = createLabelAndValueWidget(grid, currentRow++, stringMessages.regattaName(), "NameLabel");
        startDate = createLabelAndValueWidget(grid, currentRow++, stringMessages.startDate(), "StartDateLabel");
        endDate = createLabelAndValueWidget(grid, currentRow++, stringMessages.endDate(), "EndDateLabel");
        boatClassName = createLabelAndValueWidget(grid, currentRow++, stringMessages.boatClass(), "BoatClassLabel");
        defaultCourseArea = createLabelAndValueWidget(grid, currentRow++, stringMessages.courseArea(), "CourseAreaLabel");
        useStartTimeInference = createLabelAndValueWidget(grid, currentRow++, stringMessages.useStartTimeInference(), "UseStartTimeInferenceLabel");
        controlTrackingFromStartAndFinishTimes = createLabelAndValueWidget(grid, currentRow++, stringMessages.controlTrackingFromStartAndFinishTimes(), "UseStartTimeInferenceLabel");
        canBoatsOfCompetitorsChangePerRace = createLabelAndValueWidget(grid, currentRow++, stringMessages.canBoatsOfCompetitorsChangePerRace(), "CanBoatsOfCompetitorsChangePerRaceLabel");
        competitorRegistrationType = createLabelAndValueWidget(grid, currentRow++, stringMessages.competitorRegistrationType(), "CompetitorRegistrationTypeLabel");
        buoyZoneRadiusInHullLengths = createLabelAndValueWidget(grid, currentRow++, stringMessages.buoyZoneRadiusInHullLengths(), "BuoyZoneRadiusInHullLengthsLabel");
        configuration = createLabelAndValueWidget(grid, currentRow++, stringMessages.racingProcedureConfiguration(), "RacingProcedureLabel");
        scoringSystem = createLabelAndValueWidget(grid, currentRow++, stringMessages.scoringSystem(), "ScoringSystemLabel");
        rankingMetric = createLabelAndValueWidget(grid, currentRow++, stringMessages.rankingMetric(), "RankingMetricLabel");
        openRegattaRegistrationLink = createLabelAndValueWidget(grid, currentRow++, "Registration Link", "OpenRegattaRegistrationLinkLabel") ;
        
        seriesTable = createRegattaSeriesTable();
        seriesTable.ensureDebugId("SeriesCellTable");
        seriesSelectionModel = new SingleSelectionModel<SeriesDTO>();
        seriesSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
            }
        });
        seriesTable.setSelectionModel(seriesSelectionModel);
        seriesListDataProvider = new ListDataProvider<SeriesDTO>();
        seriesListDataProvider.addDataDisplay(seriesTable);
        vPanel.add(seriesTable);
        initWidget(mainPanel);
    }

    private Label createLabelAndValueWidget(Grid grid, int row, String label, String debugId) {
        Label valueLabel = new Label();
        valueLabel.ensureDebugId(debugId);
        grid.setWidget(row , 0, new Label(label + ":"));
        grid.setWidget(row , 1, valueLabel);
        return valueLabel;
    }

    private CellTable<SeriesDTO> createRegattaSeriesTable() {
        CellTable<SeriesDTO> table = new BaseCelltable<SeriesDTO>(/* pageSize */10000, tableRes);
        table.setWidth("100%");
        TextColumn<SeriesDTO> seriesNameColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                return series.getName();
            }
        };
        TextColumn<SeriesDTO> isMedalSeriesColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                return series.isMedal() ? stringMessages.yes() : stringMessages.no();
            }
        };
        TextColumn<SeriesDTO> startsWithZeroScoreColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                return series.isStartsWithZeroScore() ? stringMessages.yes() : stringMessages.no();
            }
        };
        TextColumn<SeriesDTO> hasSplitFleetContiguousScoringColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                return series.hasSplitFleetContiguousScoring() ? stringMessages.yes() : stringMessages.no();
            }
        };
        TextColumn<SeriesDTO> isFirstColumnIsNonDiscardableCarryForwardColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                return series.isFirstColumnIsNonDiscardableCarryForward() ? stringMessages.yes() : stringMessages.no();
            }
        };
        TextColumn<SeriesDTO> isFleetsCanRunInParallelColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                return series.isFleetsCanRunInParallel() ? stringMessages.yes() : stringMessages.no();
            }
        };
        TextColumn<SeriesDTO> maximumNumberOfDiscardsColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                return series.getMaximumNumberOfDiscards() == null ? "" : (""+series.getMaximumNumberOfDiscards());
            }
        };

        TextColumn<SeriesDTO> racesColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                String result = "";
                if(!series.getRaceColumns().isEmpty()) {
                    int raceColumnsCount = series.getRaceColumns().size();
                    int i = 1;
                    for(RaceColumnDTO raceColumn: series.getRaceColumns()) {
                        result += raceColumn.getRaceColumnName();
                        if(i < raceColumnsCount) {
                            result += ", ";
                        }
                        i++;
                    }
                } else {
                    result = stringMessages.noRacesYet();
                }
                return result;
            }
        };
        
        TextColumn<SeriesDTO> fleetsColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                String result = "";
                int fleetsCount = series.getFleets().size();
                int i = 1;
                for(FleetDTO fleet: series.getFleets()) {
                    result += fleet.getName();
                    result += "(" + fleet.getOrderNo() + ") ";
                    if (i < fleetsCount) {
                        result += ", ";
                    }
                    i++;
                }
                return result;
            }
        };

        TextColumn<SeriesDTO> discardsColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                final String result;
                if (series.getDiscardThresholds() == null) {
                    result = stringMessages.no();
                } else {
                    StringBuilder sb = new StringBuilder();
                    boolean first = true;
                    for (int threshold : series.getDiscardThresholds()) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(", ");
                        }
                        sb.append(threshold);
                    }
                    result = sb.toString();
                }
                return result;
            }
        };

        ImagesBarColumn<SeriesDTO, SeriesConfigImagesBarCell> seriesActionColumn = new ImagesBarColumn<SeriesDTO, SeriesConfigImagesBarCell>(
                new SeriesConfigImagesBarCell(stringMessages));
        seriesActionColumn.setFieldUpdater(new FieldUpdater<SeriesDTO, String>() {
            @Override
            public void update(int index, final SeriesDTO series, String value) {
                if (SeriesConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    editRacesOfRegattaSeries(regatta, series);
                } else if (SeriesConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    RegattaIdentifier identifier = new RegattaName(regatta.getName());
                    if (Window.confirm(stringMessages.reallyRemoveSeries(series.getName()))) {
                        sailingService.removeSeries(identifier, series.getName(),
                                new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                                    @Override
                                    public void onFailure(Throwable cause) {
                                        errorReporter.reportError("Error trying to remove series " + series.getName()
                                                + ": " + cause.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        regattaRefresher.fillRegattas();
                                    }
                                }));
                    }
                }

            }
        });
        
        table.addColumn(seriesNameColumn, stringMessages.series());
        table.addColumn(isMedalSeriesColumn, stringMessages.medalSeries());
        table.addColumn(racesColumn, stringMessages.races());
        table.addColumn(fleetsColumn, stringMessages.fleets());
        table.addColumn(discardsColumn, stringMessages.discarding());
        table.addColumn(isFirstColumnIsNonDiscardableCarryForwardColumn, stringMessages.firstRaceIsNonDiscardableCarryForward());
        table.addColumn(startsWithZeroScoreColumn, stringMessages.startsWithZeroScore());
        table.addColumn(hasSplitFleetContiguousScoringColumn, stringMessages.hasSplitFleetContiguousScoring());
        table.addColumn(isFleetsCanRunInParallelColumn, stringMessages.canFleetsRunInParallel());
        table.addColumn(maximumNumberOfDiscardsColumn, stringMessages.maximumNumberOfDiscards());
        table.addColumn(seriesActionColumn, stringMessages.actions());
        
        return table;
    }
    
    public RegattaDTO getRegatta() {
        return regatta;
    }

    public void setRegatta(RegattaDTO regatta) {
        this.regatta = regatta;
        updateRegattaDetails();
    }

    private void editRacesOfRegattaSeries(final RegattaDTO regatta, final SeriesDTO series) {
        SeriesEditDialog raceDialog = new SeriesEditDialog(regatta, series, stringMessages, 
                new DialogCallback<SeriesDescriptor>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final SeriesDescriptor result) {
                        updateRacesOfRegattaSeries(regatta, result);
                    }
                });
        raceDialog.ensureDebugId("SeriesEditDialog");
        raceDialog.show();
    }

    private void updateRacesOfRegattaSeries(final RegattaDTO regatta, final SeriesDescriptor seriesDescriptor) {
        final SeriesDTO series = seriesDescriptor.getSeries();
        final List<RaceColumnDTO> newRaceColumns = seriesDescriptor.getRaces();
        final boolean isMedalChanged = series.isMedal() != seriesDescriptor.isMedal();
        final boolean isFleetsCanRunInParallelChanged = series.isFleetsCanRunInParallel() != seriesDescriptor.isFleetsCanRunInParallel();
        final boolean isStartsWithZeroScoreChanged = series.isStartsWithZeroScore() != seriesDescriptor.isStartsWithZeroScore();
        final boolean isFirstColumnIsNonDiscardableCarryForwardChanged = series.isFirstColumnIsNonDiscardableCarryForward() != seriesDescriptor.isFirstColumnIsNonDiscardableCarryForward();
        final boolean hasSplitFleetContiguousScoringChanged = series.hasSplitFleetContiguousScoring() != seriesDescriptor.hasSplitFleetContiguousScoring();
        final boolean seriesResultDiscardingThresholdsChanged = !Arrays.equals(series.getDiscardThresholds(),
                seriesDescriptor.getResultDiscardingThresholds());
        final boolean maximumNumberOfDiscardsChanged = series.getMaximumNumberOfDiscards() != seriesDescriptor.getMaximumNumberOfDiscards();
        final boolean seriesNameChanged = !series.getName().equals(seriesDescriptor.getSeriesName());
        final RegattaIdentifier regattaIdentifier = new RegattaName(regatta.getName());
        List<RaceColumnDTO> existingRaceColumns = series.getRaceColumns();
        final List<Pair<String, Integer>> raceColumnNamesToAddWithInsertIndex = new ArrayList<>();
        final List<String> raceColumnsToRemove = new ArrayList<>();
        // TODO see bug 1447: the resulting order currently doesn't necessarily match the order of races in this dialog!
        int insertIndex = 0;
        for (RaceColumnDTO newRaceColumn : newRaceColumns) {
            if (!existingRaceColumns.contains(newRaceColumn)) {
                raceColumnNamesToAddWithInsertIndex.add(new Pair<>(newRaceColumn.getName(), insertIndex));
            }
            insertIndex++;
        }
        for (RaceColumnDTO existingRaceColumn : existingRaceColumns) {
            if (!newRaceColumns.contains(existingRaceColumn)) {
                raceColumnsToRemove.add(existingRaceColumn.getName());
            }
        }
        StringBuilder racesToRemove = new StringBuilder();
        boolean first = true;
        for (String raceColumnToRemove : raceColumnsToRemove) {
            if (first) {
                first = false;
            } else {
                racesToRemove.append(", ");
            }
            racesToRemove.append(raceColumnToRemove);
        }
        if (raceColumnsToRemove.isEmpty() || Window.confirm(stringMessages.reallyRemoveRace(racesToRemove.toString()))) {
            // first remove:
            sailingService.removeRaceColumnsFromSeries(regattaIdentifier, series.getName(), raceColumnsToRemove,
                    new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error trying to remove race columns " + raceColumnNamesToAddWithInsertIndex
                                    + " from series " + series.getName() + ": " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Void v) {
                            // when successfully removed, insert:
                            sailingService.addRaceColumnsToSeries(regattaIdentifier, series.getName(), raceColumnNamesToAddWithInsertIndex,
                                    new AsyncCallback<List<RaceColumnInSeriesDTO>>() {
                                        @Override
                                        public void onFailure(Throwable caught) {
                                            errorReporter.reportError("Error trying to add race columns " + raceColumnNamesToAddWithInsertIndex
                                                    + " to series " + series.getName() + ": " + caught.getMessage());
                                        }

                                        @Override
                                        public void onSuccess(List<RaceColumnInSeriesDTO> raceColumns) {
                                            regattaRefresher.fillRegattas();
                                        }
                                    });
                        }
                    });
            if (isMedalChanged || isFleetsCanRunInParallelChanged || seriesResultDiscardingThresholdsChanged || isStartsWithZeroScoreChanged
                    || isFirstColumnIsNonDiscardableCarryForwardChanged || hasSplitFleetContiguousScoringChanged
                    || seriesNameChanged || maximumNumberOfDiscardsChanged) {
                sailingService.updateSeries(regattaIdentifier, series.getName(), seriesDescriptor.getSeriesName(),
                        seriesDescriptor.isMedal(), seriesDescriptor.isFleetsCanRunInParallel(), seriesDescriptor.getResultDiscardingThresholds(),
                        seriesDescriptor.isStartsWithZeroScore(),
                        seriesDescriptor.isFirstColumnIsNonDiscardableCarryForward(),
                        seriesDescriptor.hasSplitFleetContiguousScoring(), seriesDescriptor.getMaximumNumberOfDiscards(),
                        series.getFleets(), new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to update series " + series.getName() + ": "
                                        + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void result) {
                                regattaRefresher.fillRegattas();
                            }
                        });
            }
        }
    }

    private void updateRegattaDetails() {
        if (regatta != null) {
            mainPanel.setCaptionText(stringMessages.regatta() + " " + regatta.getName());
            regattaId.setText(regatta.getName());
            regattaName.setText(regatta.getName());
            startDate.setText(regatta.startDate != null ? regatta.startDate.toString() : "");
            endDate.setText(regatta.endDate != null ? regatta.endDate.toString() : "");
            boatClassName.setText(regatta.boatClass != null ? regatta.boatClass.getName() : "");
            defaultCourseArea.setText(regatta.defaultCourseAreaUuid == null ? "" : regatta.defaultCourseAreaName);
            useStartTimeInference.setText(regatta.useStartTimeInference ? stringMessages.yes() : stringMessages.no());
            controlTrackingFromStartAndFinishTimes.setText(regatta.controlTrackingFromStartAndFinishTimes ? stringMessages.yes() : stringMessages.no());
            canBoatsOfCompetitorsChangePerRace.setText(regatta.canBoatsOfCompetitorsChangePerRace ? stringMessages.yes() : stringMessages.no());
            competitorRegistrationType.setText(regatta.competitorRegistrationType.getLabel(stringMessages));
            openRegattaRegistrationLink.setText(regatta.competitorRegistrationType.isOpen()
                    ? "http://www.sapsailing.com"    //TODO: get correct URL for branch.io
                    : "-");
            buoyZoneRadiusInHullLengths.setText(String.valueOf(regatta.buoyZoneRadiusInHullLengths));
            
            if (regatta.configuration != null) {
                configuration.setText(stringMessages.configured());
            } else {
                configuration.setText(stringMessages.none());
            }
            ScoringSchemeType scoringScheme = regatta.scoringScheme;
            String scoringSystemText = scoringScheme == null ? "" : ScoringSchemeTypeFormatter.format(scoringScheme, stringMessages);
            scoringSystem.setText(scoringSystemText);
            RankingMetrics rankingMetricType = regatta.rankingMetricType;
            String rankingMetricText = rankingMetricType == null ? "" : RankingMetricTypeFormatter.getDescription(rankingMetricType, stringMessages);
            rankingMetric.setText(rankingMetricText);
            seriesListDataProvider.getList().clear();
            seriesListDataProvider.getList().addAll(regatta.series);
        } 
    }
}
