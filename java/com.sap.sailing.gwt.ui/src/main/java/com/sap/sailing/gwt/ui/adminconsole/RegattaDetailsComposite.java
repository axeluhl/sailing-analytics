package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
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
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnInSeriesDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;


public class RegattaDetailsComposite extends Composite {
    private RegattaDTO regatta;

    private final CaptionPanel mainPanel;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final RegattaRefresher regattaRefresher;
    
    private final Label regattaName;
    private final Label boatClassName;
    private final Label scoringSystem;
    private final Label defaultCourseArea;

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
        
        Grid grid = new Grid(4, 2);
        vPanel.add(grid);
        
        regattaName = new Label();
        grid.setWidget(0 , 0, new Label(stringMessages.regattaName() + ":"));
        grid.setWidget(0 , 1, regattaName);

        boatClassName = new Label();
        grid.setWidget(1 , 0, new Label(stringMessages.boatClass() + ":"));
        grid.setWidget(1 , 1, boatClassName);

        defaultCourseArea = new Label();
        grid.setWidget(2 , 0, new Label(stringMessages.courseArea() + ":"));
        grid.setWidget(2 , 1, defaultCourseArea);
        
        scoringSystem = new Label();
        grid.setWidget(3 , 0, new Label(stringMessages.scoringSystem() + ":"));
        grid.setWidget(3 , 1, scoringSystem);
        
        seriesTable = createRegattaSeriesTable();
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

    private CellTable<SeriesDTO> createRegattaSeriesTable() {
        CellTable<SeriesDTO> table = new CellTable<SeriesDTO>(/* pageSize */10000, tableRes);
        table.setWidth("100%");
        
        TextColumn<SeriesDTO> seriesNameColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                return series.name;
            }
        };

        TextColumn<SeriesDTO> isMedalSeriesColumn = new TextColumn<SeriesDTO>() {
            @Override
            public String getValue(SeriesDTO series) {
                return series.isMedal() ? stringMessages.yes() : stringMessages.no();
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
                    result += fleet.name;
                    result += "(" + fleet.getOrderNo() + ") ";
                    if (i < fleetsCount) {
                        result += ", ";
                    }
                    i++;
                }
                return result;
            }
        };

        ImagesBarColumn<SeriesDTO, SeriesConfigImagesBarCell> seriesActionColumn = new ImagesBarColumn<SeriesDTO, SeriesConfigImagesBarCell>(
                new SeriesConfigImagesBarCell(stringMessages));
        seriesActionColumn.setFieldUpdater(new FieldUpdater<SeriesDTO, String>() {
            @Override
            public void update(int index, SeriesDTO series, String value) {
                if (SeriesConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    editRacesOfRegattaSeries(regatta, series);
                } else if (SeriesConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    Window.alert("This function is not implemented yet. To delete a series you have to recreate the regatta!");
                }

            }
        });
        
        table.addColumn(seriesNameColumn, stringMessages.series());
        table.addColumn(isMedalSeriesColumn, stringMessages.medalSeries());
        table.addColumn(racesColumn, stringMessages.races());
        table.addColumn(fleetsColumn, stringMessages.fleets());
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
        RaceColumnInRegattaSeriesDialog raceDialog = new RaceColumnInRegattaSeriesDialog(regatta, series, stringMessages, 
                new DialogCallback<Pair<SeriesDTO, List<RaceColumnDTO>>>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final Pair<SeriesDTO, List<RaceColumnDTO>> result) {
                        updateRacesOfRegattaSeries(regatta, result.getA(), result.getB());
                    }
                });
        raceDialog.show();
    }

    private void updateRacesOfRegattaSeries(final RegattaDTO regatta, final SeriesDTO series, List<RaceColumnDTO> newRaceColumns) {
        final RegattaIdentifier regattaIdentifier = new RegattaName(regatta.name);
        
        List<RaceColumnDTO> existingRaceColumns = series.getRaceColumns();
        final List<String> raceColumnsToAdd = new ArrayList<String>();
        final List<String> raceColumnsToRemove = new ArrayList<String>();
        
        for(RaceColumnDTO newRaceColumn: newRaceColumns) {
            if(!existingRaceColumns.contains(newRaceColumn)) {
                raceColumnsToAdd.add(newRaceColumn.name);
            }
        }

        for(RaceColumnDTO existingRaceColumn: existingRaceColumns) {
            if(!newRaceColumns.contains(existingRaceColumn)) {
                raceColumnsToRemove.add(existingRaceColumn.name);
            }
        }

        sailingService.addRaceColumnsToSeries(regattaIdentifier, series.name, raceColumnsToAdd, new AsyncCallback<List<RaceColumnInSeriesDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to add race columns "
                            + raceColumnsToAdd + " to series " + series.name
                            + ": " + caught.getMessage());

                }

                @Override
                public void onSuccess(List<RaceColumnInSeriesDTO> raceColumns) {
                    regattaRefresher.fillRegattas();
                    updateRegattaDetails();
                }
            });
        
        sailingService.removeRaceColumnsFromSeries(regattaIdentifier, series.name, raceColumnsToRemove, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to remove race columns "
                        + raceColumnsToAdd + " from series " + series.name
                        + ": " + caught.getMessage());

            }

            @Override
            public void onSuccess(Void v) {
                regattaRefresher.fillRegattas();
                updateRegattaDetails();
            }
        });
    }

    private void updateRegattaDetails() {
        if(regatta != null) {
            regattaName.setText(regatta.name);
            boatClassName.setText(regatta.boatClass != null ? regatta.boatClass.name : "");
            defaultCourseArea.setText(regatta.defaultCourseAreaIdAsString == null ? "" : regatta.defaultCourseAreaName);

            ScoringSchemeType scoringScheme = regatta.scoringScheme;
            String scoringSystemText = scoringScheme == null ? "" : ScoringSchemeTypeFormatter.format(scoringScheme, stringMessages);               
            scoringSystem.setText(scoringSystemText);
            
            seriesListDataProvider.getList().clear();
            seriesListDataProvider.getList().addAll(regatta.series);
        } 
    }
}
