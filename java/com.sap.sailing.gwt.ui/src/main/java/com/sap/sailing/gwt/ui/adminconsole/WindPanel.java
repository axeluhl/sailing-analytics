package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;

/**
 * Displays a table of currently tracked races. The user can configure whether a race
 * is assumed to start with an upwind leg and exclude specific
 * wind sources from the overall (combined) wind computation, e.g., for performance reasons.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WindPanel extends FormPanel implements RegattaDisplayer, WindShower, RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final IdentityColumn<WindDTO> removeColumn;
    private final TextColumn<WindDTO> timeColumn;
    private final TextColumn<WindDTO> speedInKnotsColumn;
    private final TextColumn<WindDTO> windDirectionInDegColumn;
    private final TextColumn<WindDTO> positionColumn;
    private final TrackedRacesListComposite trackedRacesListComposite;
    private final RaceSelectionProvider raceSelectionProvider;
    private final WindSourcesToExcludeSelectorPanel windSourcesToExcludeSelectorPanel;
    private final CheckBox raceIsKnownToStartUpwindBox;
    private final CaptionPanel windCaptionPanel;
    private final VerticalPanel windFixesDisplayPanel;
    private final Label windSourceLabel;
    private final ListDataProvider<WindDTO> rawWindFixesDataProvider;
    private final CellTable<WindDTO> rawWindFixesTable;
    
    public WindPanel(final SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            ErrorReporter errorReporter, RegattaRefresher regattaRefresher, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.raceSelectionProvider = new RaceSelectionModel();
        windSourcesToExcludeSelectorPanel = new WindSourcesToExcludeSelectorPanel(sailingService, stringMessages, errorReporter);
        
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSize("100%", "100%");
        this.setWidget(mainPanel);

        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, regattaRefresher,
                raceSelectionProvider, stringMessages, false);
        mainPanel.add(trackedRacesListComposite);
        raceSelectionProvider.addRaceSelectionChangeListener(this);

        windCaptionPanel = new CaptionPanel(stringMessages.wind());
        windCaptionPanel.setVisible(false);
        mainPanel.add(windCaptionPanel);

        TabPanel tabPanel = new TabPanel();
        tabPanel.setAnimationEnabled(true);
        windCaptionPanel.add(tabPanel);
        tabPanel.setSize("95%", "95%");

        windFixesDisplayPanel = new VerticalPanel();
        Button addWindFixButton = new Button(stringMessages.actionAddWindData()+ "...");
        addWindFixButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RegattaAndRaceIdentifier selectedRace = getSelectedRace(); 
                if(selectedRace != null) {
                    final RaceDTO race = trackedRacesListComposite.getRaceByIdentifier(selectedRace);
                    sailingService.getCoursePositions(selectedRace, race.trackedRace.startOfTracking, new AsyncCallback<CoursePositionsDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            showWindSettingDialog(race, null);
                        }

                        @Override
                        public void onSuccess(final CoursePositionsDTO result) {
                            showWindSettingDialog(race, result);
                        }
                    });
                }
            }
        });
        windFixesDisplayPanel.add(addWindFixButton);

        VerticalPanel windSourcesPanel = new VerticalPanel();
        windSourcesPanel.setSpacing(10);
        tabPanel.add(windSourcesPanel, stringMessages.windSourcesUsed());
        tabPanel.add(windFixesDisplayPanel, "Wind fixes");
        tabPanel.selectTab(0);
        
        raceIsKnownToStartUpwindBox = new CheckBox(stringMessages.raceIsKnownToStartUpwind());
        windSourcesPanel.add(raceIsKnownToStartUpwindBox);

        windSourcesPanel.add(windSourcesToExcludeSelectorPanel);
        raceIsKnownToStartUpwindBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                setRaceIsKnownToStartUpwind();
            }
        });
        
        windSourceLabel = new Label();
        windFixesDisplayPanel.add(windSourceLabel);

        // table for the raw wind fixes
        removeColumn = new IdentityColumn<WindDTO>(new ActionCell<WindDTO>(stringMessages.remove(), new Delegate<WindDTO>() {
            @Override
            public void execute(final WindDTO wind) {
                List<RegattaAndRaceIdentifier> selectedRaces = raceSelectionProvider.getSelectedRaces();
                final RegattaAndRaceIdentifier raceIdentifier = selectedRaces.get(selectedRaces.size()-1);
                sailingService.removeWind(raceIdentifier, wind, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // remove row from underlying list:
                        rawWindFixesDataProvider.getList().remove(wind);
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                                        WindPanel.this.errorReporter.reportError(
                                                WindPanel.this.stringMessages.errorSettingWindForRace()+ " "+raceIdentifier
                                                + ": "+ caught.getMessage());
                                    }
                });
            }
        }));
        timeColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return DateAndTimeFormatterUtil.formatDateAndTime(new Date(object.measureTimepoint));
            }
        };
        speedInKnotsColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return ""+object.trueWindSpeedInKnots;
            }
        };
        windDirectionInDegColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return ""+object.trueWindFromDeg;
            }
        };
        positionColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                String result = "";
                if(object.position != null) {
                    result = "Lat: " + object.position.latDeg + ", Lon: " + object.position.lngDeg;  
                }
                return result;
            }
        };
        timeColumn.setSortable(true);
        speedInKnotsColumn.setSortable(true);
        windDirectionInDegColumn.setSortable(true);

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        rawWindFixesTable = new CellTable<WindDTO>(/* pageSize */10000, tableRes);
        rawWindFixesTable.addColumn(timeColumn, "Time");
        rawWindFixesTable.addColumn(speedInKnotsColumn, "Speed (kn)");
        rawWindFixesTable.addColumn(windDirectionInDegColumn, "From (deg)");
        rawWindFixesTable.addColumn(positionColumn, "Position");
        rawWindFixesTable.addColumn(removeColumn, stringMessages.actions());
        rawWindFixesDataProvider = new ListDataProvider<WindDTO>();
        rawWindFixesDataProvider.addDataDisplay(rawWindFixesTable);
        Handler columnSortHandler = getWindTableColumnSortHandler(rawWindFixesDataProvider.getList(), timeColumn,
                speedInKnotsColumn, windDirectionInDegColumn);
        rawWindFixesTable.addColumnSortHandler(columnSortHandler);
        rawWindFixesTable.getColumnSortList().push(timeColumn);
        windFixesDisplayPanel.add(rawWindFixesTable);
    }

    private void showWindSettingDialog(RaceDTO race, CoursePositionsDTO course) {
        AddWindFixDialog windSettingDialog = new AddWindFixDialog(race, course, stringMessages, 
                new DialogCallback<WindDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final WindDTO result) {
                        addWindFix(result);
                    }
                });
        windSettingDialog.show();
    }

    private void addWindFix(final WindDTO wind) {
        final RegattaAndRaceIdentifier raceIdentifier = getSelectedRace();
        sailingService.setWind(raceIdentifier, wind, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showWind(raceIdentifier);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error adding a wind fix for race " + raceIdentifier + ": " + caught.getMessage());
            }
        });
    }
    
    @Override
    public void fillRegattas(List<RegattaDTO> result) {
        trackedRacesListComposite.fillRegattas(result);
    }

    @Override
    public void showWind(final RegattaAndRaceIdentifier raceIdentifier) {
        sailingService.getWindSourcesInfo(raceIdentifier, 
                new AsyncCallback<WindInfoForRaceDTO>() {
                    @Override
                    public void onSuccess(final WindInfoForRaceDTO result) {
                        if (result != null) {
                            updateWindSourcesToExclude(result, raceIdentifier);
                            raceIsKnownToStartUpwindBox.setValue(result.raceIsKnownToStartUpwind);
                            
                            // load the raw wind fixes
                            sailingService.getRawWindFixes(raceIdentifier, null,
                                    new AsyncCallback<WindInfoForRaceDTO>() {
                                        @Override
                                        public void onSuccess(WindInfoForRaceDTO result) {
                                            if (result != null) {
                                                udapteRawWindFixes(result);
                                            } else {
                                                // no wind fixes known for untracked race
                                                clearWindSources();
                                                clearWindFixes();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            errorReporter.reportError("Error reading wind fixes: " + caught.getMessage());
                                        }
                                    });
                            
                        } else {
                            // no wind sources known for untracked race
                            clearWindSources();
                            clearWindFixes();
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error reading wind source information: " + caught.getMessage());
                    }
                });
    }

    private void updateWindSourcesToExclude(WindInfoForRaceDTO result, RegattaAndRaceIdentifier raceIdentifier) {
        windSourcesToExcludeSelectorPanel.update(raceIdentifier, result.windTrackInfoByWindSource.keySet(), result.windSourcesToExclude);
    }

    private void udapteRawWindFixes(WindInfoForRaceDTO result) {
        for (Map.Entry<WindSource, WindTrackInfoDTO> e : result.windTrackInfoByWindSource.entrySet()) {
            if (e.getKey().getType() == WindSourceType.WEB || e.getKey().getType() == WindSourceType.EXPEDITION) {
                windSourceLabel.setText(stringMessages.windSource() + ": " + e.getKey());
                if (e.getKey().getType() == WindSourceType.WEB) {
                    // only the WEB wind source is editable, hence has a "Remove" column
                    // rawWindFixesTable.removeColumn(col);
                }
                rawWindFixesDataProvider.getList().clear();
                rawWindFixesDataProvider.getList().addAll(e.getValue().windFixes);
            }
        }
    }
    
    private Handler getWindTableColumnSortHandler(List<WindDTO> list, TextColumn<WindDTO> timeColumn,
            TextColumn<WindDTO> speedInKnotsColumn, TextColumn<WindDTO> windDirectionInDegColumn) {
        ListHandler<WindDTO> result = new ListHandler<WindDTO>(list);
        result.setComparator(timeColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.measureTimepoint < o2.measureTimepoint ? -1 : o1.measureTimepoint == o2.measureTimepoint ? 0 : 1;
            }
        });
        result.setComparator(speedInKnotsColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.trueWindSpeedInKnots < o2.trueWindSpeedInKnots ? -1 :
                    o1.trueWindSpeedInKnots == o2.trueWindSpeedInKnots ? 0 : 1;
            }
        });
        result.setComparator(windDirectionInDegColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.trueWindFromDeg < o2.trueWindFromDeg ? -1 :
                    o1.trueWindFromDeg == o2.trueWindFromDeg ? 0 : 1;
            }
        });
        return result;
    }

    private void setRaceIsKnownToStartUpwind() {
        final RegattaAndRaceIdentifier selectedRace = getSelectedRace();
        sailingService.setRaceIsKnownToStartUpwind(selectedRace,
        raceIsKnownToStartUpwindBox.getValue(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(WindPanel.this.stringMessages.errorWhileTryingToSetWindSourceForRace()+
                            " "+selectedRace+": "+caught.getMessage());
                }
                @Override
                public void onSuccess(Void result) {
                }
            });
    }

    private RegattaAndRaceIdentifier getSelectedRace() {
        List<RegattaAndRaceIdentifier> selectedRaces = raceSelectionProvider.getSelectedRaces();
        if(selectedRaces.isEmpty()) 
            return null;
        
        return selectedRaces.get(0);
    }

    private void clearWindSources() {
        final Set<WindSource> emptySet = Collections.emptySet();
        windSourcesToExcludeSelectorPanel.update(null, emptySet, emptySet);
    }

    private void clearWindFixes() {
    }
    
    private void updateWindDisplay() {
        RegattaAndRaceIdentifier selectedRace = getSelectedRace();
        RaceDTO raceDTO = selectedRace != null ? trackedRacesListComposite.getRaceByIdentifier(selectedRace) : null;

        if (selectedRace != null && raceDTO != null && raceDTO.trackedRace != null) {
            windCaptionPanel.setVisible(true);
            windCaptionPanel.setCaptionText(stringMessages.wind() + ": " + selectedRace.getRaceName());

            showWind(selectedRace);
        } else {
            windCaptionPanel.setVisible(false);
            windCaptionPanel.setCaptionText(stringMessages.wind());

            clearWindSources();
            clearWindFixes();
        }
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        updateWindDisplay();
    }
}
