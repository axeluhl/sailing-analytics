package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MarkedAsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

/**
 * This component shows a table displaying the current state of races for a given event. 
 * Which races are shown depends on the setting {@link RegattaRaceStatesSettings}.
 * Each entry shows what flags are currently displayed, what start time the race has and additional information, e.g. for Gate start.
 */
public class RegattaRaceStatesComponent extends SimplePanel implements Component<RegattaRaceStatesSettings> {

    private List<RegattaOverviewEntryDTO> allEntries;

    private final SelectionModel<RegattaOverviewEntryDTO> raceSelectionModel;
    private final CellTable<RegattaOverviewEntryDTO> regattaOverviewTable;
    private ListDataProvider<RegattaOverviewEntryDTO> regattaOverviewDataProvider;
    private final VerticalPanel mainPanel;
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");

    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final String eventIdAsString;
    private final RegattaOverviewRaceSelectionProvider raceSelectionProvider;
    private final EventProvider eventProvider;

    private final FlagImageResolver flagImageResolver;

    private final RegattaRaceStatesSettings settings; 
    private Map<String, String> regattaNames;
    
    private static RegattaRaceStatesTableResources tableRes = GWT.create(RegattaRaceStatesTableResources.class);

    public RegattaRaceStatesComponent(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages, final String eventIdAsString, final RegattaOverviewRaceSelectionProvider raceSelectionProvider,
            final EventProvider eventProvider, RegattaRaceStatesSettings settings) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.eventIdAsString = eventIdAsString;
        this.raceSelectionProvider = raceSelectionProvider;
        this.eventProvider = eventProvider;
        this.flagImageResolver = new FlagImageResolver();
        regattaNames = new HashMap<String, String>();
        this.allEntries = new ArrayList<RegattaOverviewEntryDTO>();

        this.settings = new RegattaRaceStatesSettings();
        updateSettings(settings);
        
        mainPanel = new VerticalPanel();
        setWidth("100%");

        regattaOverviewDataProvider = new ListDataProvider<RegattaOverviewEntryDTO>();
        regattaOverviewTable = createRegattaTable();

        raceSelectionModel = new SingleSelectionModel<RegattaOverviewEntryDTO>();
        raceSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                showSelectedRaces();
            }

        });

        regattaOverviewTable.setSelectionModel(raceSelectionModel);

        mainPanel.add(regattaOverviewTable);
        setWidget(mainPanel);

        loadAndUpdateEventLog();
    }

    protected List<RegattaOverviewEntryDTO> getSelectedRaces() {
        List<RegattaOverviewEntryDTO> result = new ArrayList<RegattaOverviewEntryDTO>();
        if (regattaOverviewDataProvider != null) {
            for (RegattaOverviewEntryDTO race : regattaOverviewDataProvider.getList()) {
                if (raceSelectionModel.isSelected(race)) {
                    result.add(race);
                }
            }
        }
        return result;
    }

    public void onUpdateServer(Date time) {
        loadAndUpdateEventLog();
    }

    private void updateTable(List<RegattaOverviewEntryDTO> newEntries) {
        allEntries = newEntries;
        List<RegattaOverviewEntryDTO> filteredRegattaRaces = getFilteredRegattaRaces(allEntries);
        Collections.sort(filteredRegattaRaces, new RegattaRaceStatesComparator()); //sort entries after filtering
        List<RegattaOverviewEntryDTO> racesToBeShown = getRacesToBeShown(filteredRegattaRaces);
        //TODO group races by same day
        regattaOverviewDataProvider.getList().clear();
        regattaOverviewDataProvider.getList().addAll(racesToBeShown);
        // now sort again according to selected criterion
        ColumnSortEvent.fire(regattaOverviewTable, regattaOverviewTable.getColumnSortList());
    }

    private List<RegattaOverviewEntryDTO> getRacesToBeShown(List<RegattaOverviewEntryDTO> filteredEntries) {
        List<RegattaOverviewEntryDTO> racesToBeShown = new ArrayList<RegattaOverviewEntryDTO>(filteredEntries);
        if (settings.isShowOnlyCurrentlyRunningRaces()) {
            String currentRegattaName = "";
            String currentFleetName = "";
            int numberOfFinishedRacesOfCurrentRegattaFleet = 0;
            for (RegattaOverviewEntryDTO entry : filteredEntries) {
                if (!currentRegattaName.equals(entry.regattaDisplayName) || !currentFleetName.equals(entry.raceInfo.fleetName)) {
                    currentRegattaName = entry.regattaDisplayName;
                    currentFleetName = entry.raceInfo.fleetName;
                    numberOfFinishedRacesOfCurrentRegattaFleet = 0;
                }
                if (!isRaceActive(entry.raceInfo.lastStatus)) {
                    if (entry.raceInfo.lastStatus.equals(RaceLogRaceStatus.FINISHED)) {
                        if (numberOfFinishedRacesOfCurrentRegattaFleet > 0) {
                            racesToBeShown.remove(entry);
                        }
                        numberOfFinishedRacesOfCurrentRegattaFleet++;
                    } else if (entry.raceInfo.lastStatus.equals(RaceLogRaceStatus.UNSCHEDULED)) {
                        //don't filter when the race is unscheduled and aborted before
                        if (!entry.raceInfo.isRaceAbortedInPassBefore) {
                            racesToBeShown.remove(entry);
                        }
                        
                    }
                }
            }
        }
        return racesToBeShown;
    }

    private List<RegattaOverviewEntryDTO> getFilteredRegattaRaces(List<RegattaOverviewEntryDTO> raceList) {
        List<RegattaOverviewEntryDTO> filteredEntries = new ArrayList<RegattaOverviewEntryDTO>(raceList);
        
        if (!settings.getVisibleCourseAreas().isEmpty() || !settings.getVisibleRegattas().isEmpty()) {
            for (RegattaOverviewEntryDTO entry : raceList) {
                if (!settings.getVisibleCourseAreas().isEmpty()) {
                    if (!settings.getVisibleCourseAreas().contains(entry.courseAreaIdAsString)) {
                        filteredEntries.remove(entry);
                        continue;
                    }
                }

                if (!settings.getVisibleRegattas().isEmpty()) {
                    if (!settings.getVisibleRegattas().contains(entry.regattaName)) {
                        filteredEntries.remove(entry);
                        continue;
                    }
                }
            }
        }
        
        return filteredEntries;
    }

    private boolean isRaceActive(RaceLogRaceStatus status) {
        return status.equals(RaceLogRaceStatus.SCHEDULED) || status.equals(RaceLogRaceStatus.STARTPHASE)
                || status.equals(RaceLogRaceStatus.RUNNING) || status.equals(RaceLogRaceStatus.FINISHING);
    }

    protected void loadAndUpdateEventLog() {
        if (eventIdAsString == null) {
            return;
        }
        sailingService.getRegattaOverviewEntriesForEvent(eventIdAsString,
                new MarkedAsyncCallback<List<RegattaOverviewEntryDTO>>() {

                    @Override
                    protected void handleFailure(Throwable cause) {

                    }

                    @Override
                    protected void handleSuccess(List<RegattaOverviewEntryDTO> result) {
                        updateTable(result);
                    }
                });
    }

    private CellTable<RegattaOverviewEntryDTO> createRegattaTable() {
        CellTable<RegattaOverviewEntryDTO> table = new CellTable<RegattaOverviewEntryDTO>(/* pageSize */10000, tableRes);
        regattaOverviewDataProvider.addDataDisplay(table);
        table.setWidth("100%");

        ListHandler<RegattaOverviewEntryDTO> regattaOverviewListHandler = new ListHandler<RegattaOverviewEntryDTO>(
                regattaOverviewDataProvider.getList());

        TextColumn<RegattaOverviewEntryDTO> courseAreaColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.courseAreaName;
            }
        };
        courseAreaColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(courseAreaColumn, new Comparator<RegattaOverviewEntryDTO>() {

            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return left.courseAreaName.compareTo(right.courseAreaName);
            }

        });

        TextColumn<RegattaOverviewEntryDTO> regattaNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.regattaDisplayName;
            }
        };
        regattaNameColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(regattaNameColumn, new Comparator<RegattaOverviewEntryDTO>() {

            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return left.regattaDisplayName.compareTo(right.regattaDisplayName);
            }

        });

        TextColumn<RegattaOverviewEntryDTO> fleetNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                //TODO Fleet column shall be not shown when the races of this event have no fleets
                return entryDTO.raceInfo.fleetName.equals("Default") ? "-" : entryDTO.raceInfo.fleetName;
            }
        };
        fleetNameColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        fleetNameColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(fleetNameColumn, new Comparator<RegattaOverviewEntryDTO>() {

            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return left.raceInfo.fleetName.compareTo(right.raceInfo.fleetName);
            }

        });

        TextColumn<RegattaOverviewEntryDTO> raceNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.raceInfo.raceName;
            }
        };
        raceNameColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(raceNameColumn, new Comparator<RegattaOverviewEntryDTO>() {

            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return right.raceInfo.raceName.compareTo(left.raceInfo.raceName);
            }

        });

        TextColumn<RegattaOverviewEntryDTO> raceStartTimeColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                String result = "-";
                if (entryDTO.raceInfo.startTime != null) {
                    result = timeFormatter.format(entryDTO.raceInfo.startTime);
                }
                return result;
            }
        };
        raceStartTimeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        raceStartTimeColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(raceStartTimeColumn, new Comparator<RegattaOverviewEntryDTO>() {

            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                int result = 0;
                if (left.raceInfo.startTime != null && right.raceInfo.startTime != null) {
                    result = left.raceInfo.startTime.compareTo(right.raceInfo.startTime);
                } else if (left.raceInfo.startTime == null && right.raceInfo.startTime == null) {
                    result = 0;
                } else if (left.raceInfo.startTime == null) {
                    result = 1;
                } else if (right.raceInfo.startTime == null) {
                    result = -1;
                } else {
                    result = 0;
                }
                return result;
            }

        });

        TextColumn<RegattaOverviewEntryDTO> raceStatusColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return getStatusText(entryDTO.raceInfo);
            }
        };
        raceStatusColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(raceStatusColumn, new Comparator<RegattaOverviewEntryDTO>() {

            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return left.raceInfo.lastStatus.compareTo(right.raceInfo.lastStatus);
            }

        });

        Column<RegattaOverviewEntryDTO, ImageResource> lastUpperFlagColumn = new Column<RegattaOverviewEntryDTO, ImageResource>(
                new ImageResourceCell()) {
            @Override
            public ImageResource getValue(RegattaOverviewEntryDTO entryDTO) {
                return flagImageResolver.resolveFlagToImage(entryDTO.raceInfo.lastUpperFlag);
            }
        };

        Column<RegattaOverviewEntryDTO, ImageResource> lastLowerFlagColumn = new Column<RegattaOverviewEntryDTO, ImageResource>(
                new ImageResourceCell()) {
            @Override
            public ImageResource getValue(RegattaOverviewEntryDTO entryDTO) {
                return flagImageResolver.resolveFlagToImage(entryDTO.raceInfo.lastLowerFlag);
            }
        };

        Column<RegattaOverviewEntryDTO, ImageResource> lastFlagDirectionColumn = new Column<RegattaOverviewEntryDTO, ImageResource>(
                new ImageResourceCell()) {
            @Override
            public ImageResource getValue(RegattaOverviewEntryDTO entryDTO) {
                if (entryDTO.raceInfo.lastUpperFlag != null)
                    return flagImageResolver.resolveFlagDirectionToImage(entryDTO.raceInfo.isLastFlagDisplayed);
                else
                    return null;
            }
        };

        TextColumn<RegattaOverviewEntryDTO> raceAdditionalInformationColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                StringBuffer additionalInformation = new StringBuffer();
                if (entryDTO.raceInfo.pathfinderId != null) {
                    additionalInformation.append("Pathfinder: " + entryDTO.raceInfo.pathfinderId);
                }
                if (entryDTO.raceInfo.pathfinderId != null && entryDTO.raceInfo.gateLineOpeningTime != null) {
                    additionalInformation.append("  /  ");
                }
                if (entryDTO.raceInfo.gateLineOpeningTime != null) {
                    additionalInformation.append("GateLineOpeningTime: "
                            + (entryDTO.raceInfo.gateLineOpeningTime / (60 * 1000)) + " minutes");
                }

                return additionalInformation.toString();
            }
        };
        
        //TODO add a link to a raceboard when a trackedrace is available for a given race column

        table.addColumn(courseAreaColumn, stringMessages.courseArea());
        table.addColumn(regattaNameColumn, stringMessages.regatta()); // For sailors the boat class also contains
                                                                        // additional infos such as woman/man, e.g.
                                                                        // Laser Radial Woman or Laser Radial Men
        //TODO Show Series when available
        table.addColumn(fleetNameColumn, stringMessages.fleet());
        table.addColumn(raceNameColumn, stringMessages.race());
        table.addColumn(raceStartTimeColumn, stringMessages.startTime());
        table.addColumn(raceStatusColumn, stringMessages.status());
        table.addColumn(lastUpperFlagColumn, stringMessages.lastUpperFlag());
        table.addColumn(lastLowerFlagColumn, stringMessages.lastLowerFlag());
        table.addColumn(lastFlagDirectionColumn, stringMessages.flagStatus());
        table.addColumn(raceAdditionalInformationColumn, stringMessages.additionalInformation());
        table.addColumnSortHandler(regattaOverviewListHandler);

        return table;
    }

    public List<RegattaOverviewEntryDTO> getAllRaces() {
        return allEntries;
    }
    
    public Map<String, String> getRegattaNames() {
        if (this.regattaNames.isEmpty()) {
            for (RegattaOverviewEntryDTO entry : allEntries) {
                if (!regattaNames.containsValue(entry.regattaDisplayName)) {
                    regattaNames.put(entry.regattaName, entry.regattaDisplayName);
                }
            }
        }
        return regattaNames;
    }
 
    private void showSelectedRaces() {
        List<RegattaOverviewEntryDTO> selectedRaces = getSelectedRaces();
        RegattaRaceStatesComponent.this.raceSelectionProvider.setSelection(selectedRaces);
    }

    private String getStatusText(RaceInfoDTO raceInfo) {
        String statusText = "";
        if (raceInfo.lastStatus.equals(RaceLogRaceStatus.RUNNING) && raceInfo.lastUpperFlag.equals(Flags.XRAY)
                && raceInfo.isLastFlagDisplayed) {
            statusText = "Race is running (had early starters)";
        } else if (raceInfo.lastStatus.equals(RaceLogRaceStatus.RUNNING) && raceInfo.lastUpperFlag.equals(Flags.XRAY)
                && !raceInfo.isLastFlagDisplayed) {
            statusText = "Race is running";
        } else if (raceInfo.lastStatus.equals(RaceLogRaceStatus.RUNNING)) {
            statusText = "Race is running";
        } else if (raceInfo.lastStatus.equals(RaceLogRaceStatus.FINISHING)) {
            statusText = "Race is finishing";
        } else if (raceInfo.lastStatus.equals(RaceLogRaceStatus.FINISHED)) {
            statusText = "Race is finished";
        } else if (raceInfo.lastStatus.equals(RaceLogRaceStatus.SCHEDULED)) {
            statusText = "Race is scheduled";
        } else if (raceInfo.lastStatus.equals(RaceLogRaceStatus.STARTPHASE)) {
            statusText = "Race in start phase";
        } else if (raceInfo.lastStatus.equals(RaceLogRaceStatus.UNSCHEDULED)) {
            statusText = stringMessages.noStarttimeAnnouncedYet();
        } else if (raceInfo.lastUpperFlag == null) {
            statusText = "";
        } else if (raceInfo.lastUpperFlag.equals(Flags.FIRSTSUBSTITUTE)) {
            statusText = "General recall";
        } else if (raceInfo.lastUpperFlag.equals(Flags.AP) && raceInfo.isLastFlagDisplayed) {
            statusText = "Start postponed";
        } else if (raceInfo.lastUpperFlag.equals(Flags.NOVEMBER) && raceInfo.isLastFlagDisplayed) {
            statusText = "Start abandoned";
        }
        return statusText;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<RegattaRaceStatesSettings> getSettingsDialogComponent() {
        return new RegattaRaceStatesSettingsDialogComponent(settings, stringMessages, 
                Collections.unmodifiableList(eventProvider.getEvent().venue.getCourseAreas()),
                Collections.unmodifiableMap(getRegattaNames()));
    }

    @Override
    public void updateSettings(RegattaRaceStatesSettings newSettings) {
        if (!settings.getVisibleCourseAreas().equals(newSettings.getVisibleCourseAreas())) {
            settings.getVisibleCourseAreas().clear();
            settings.getVisibleCourseAreas().addAll(newSettings.getVisibleCourseAreas());
            
            if (settings.getVisibleCourseAreas().isEmpty()) {
                for (CourseAreaDTO courseArea : eventProvider.getEvent().venue.getCourseAreas()) {
                    settings.getVisibleCourseAreas().add(courseArea.id);
                }
            }
        }
        
        if (!settings.getVisibleRegattas().equals(newSettings.getVisibleRegattas())) {
            settings.getVisibleRegattas().clear();
            settings.getVisibleRegattas().addAll(newSettings.getVisibleRegattas());
            
            if (settings.getVisibleRegattas().isEmpty()) {
                settings.getVisibleRegattas().addAll(getRegattaNames().values());
            }
        }
        
        if (settings.isShowOnlyRacesOfSameDay() != newSettings.isShowOnlyRacesOfSameDay()) {
            settings.setShowOnlyRaceOfSameDay(newSettings.isShowOnlyRacesOfSameDay());
        }
        
        if (settings.isShowOnlyCurrentlyRunningRaces() != newSettings.isShowOnlyCurrentlyRunningRaces()) {
            settings.setShowOnlyCurrentlyRunningRaces(newSettings.isShowOnlyCurrentlyRunningRaces());
        }
        //do not update the table when the entries are not loaded yet
        if (!allEntries.isEmpty()) {
            updateTable(allEntries);
        }
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.regattaOverview();
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }
}
