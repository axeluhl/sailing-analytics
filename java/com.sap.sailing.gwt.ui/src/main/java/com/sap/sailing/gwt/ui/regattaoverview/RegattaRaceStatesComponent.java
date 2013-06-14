package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MarkedAsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

/**
 * This component shows a table displaying the current state of races for a given event. 
 * Which races are shown depends on the setting {@link RegattaRaceStatesSettings}.
 * Each entry shows what flags are currently displayed, what start time the race has and additional information, e.g. for Gate start.
 */
public class RegattaRaceStatesComponent extends SimplePanel implements Component<RegattaRaceStatesSettings>, EventAndRaceGroupAvailabilityListener {

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
    private final FlagImageResolver flagImageResolver;
    
    private EventDTO eventDTO;
    private List<RaceGroupDTO> raceGroupDTOs;
    private TextColumn<RegattaOverviewEntryDTO> seriesNameColumn;
    private TextColumn<RegattaOverviewEntryDTO> fleetNameColumn;

    private final RegattaRaceStatesSettings settings; 
    
    private static RegattaRaceStatesTableResources tableRes = GWT.create(RegattaRaceStatesTableResources.class);

    public RegattaRaceStatesComponent(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages, final String eventIdAsString, final RegattaOverviewRaceSelectionProvider raceSelectionProvider, RegattaRaceStatesSettings settings) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.eventIdAsString = eventIdAsString;
        this.raceSelectionProvider = raceSelectionProvider;
        this.flagImageResolver = new FlagImageResolver();
        this.allEntries = new ArrayList<RegattaOverviewEntryDTO>();
        
        this.eventDTO = null;
        this.raceGroupDTOs = null;

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
        
        Collections.sort(allEntries, new RegattaRaceStatesComparator()); //sort entries
        
        regattaOverviewDataProvider.getList().clear();
        regattaOverviewDataProvider.getList().addAll(allEntries);
        // now sort again according to selected criterion
        ColumnSortEvent.fire(regattaOverviewTable, regattaOverviewTable.getColumnSortList());
    }

    protected void loadAndUpdateEventLog() {
        if (eventIdAsString == null || eventDTO == null || raceGroupDTOs == null) {
            return;
        }
        sailingService.getRaceStateEntriesForRaceGroup(eventIdAsString, settings.getVisibleCourseAreas(), settings.getVisibleRegattas(), 
                settings.isShowOnlyCurrentlyRunningRaces(), settings.isShowOnlyRacesOfSameDay(), new MarkedAsyncCallback<List<RegattaOverviewEntryDTO>>() {

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
        
        seriesNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.raceInfo.seriesName.equals(LeaderboardNameConstants.DEFAULT_SERIES_NAME) ? "-" : entryDTO.raceInfo.seriesName;
            }
        };
        seriesNameColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        fleetNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.raceInfo.fleetName.equals(LeaderboardNameConstants.DEFAULT_FLEET_NAME) ? "-" : entryDTO.raceInfo.fleetName;
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
        table.addColumn(regattaNameColumn, stringMessages.regatta());
        table.addColumn(seriesNameColumn, stringMessages.series());
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
    
    private boolean hasAnyRaceGroupASeries() {
        boolean result = false;
        if (raceGroupDTOs != null) {
            for (RaceGroupDTO raceGroup : raceGroupDTOs) {
                for (RaceGroupSeriesDTO series : raceGroup.getSeries()) {
                    if (!series.getName().equals(LeaderboardNameConstants.DEFAULT_SERIES_NAME)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    private boolean hasAnyRaceGroupAFleet() {
        boolean result = false;
        if (raceGroupDTOs != null) {
            for (RaceGroupDTO raceGroup : raceGroupDTOs) {
                for (RaceGroupSeriesDTO series : raceGroup.getSeries()) {
                    for (FleetDTO fleet : series.getFleets()) {
                        if (!fleet.getName().equals(LeaderboardNameConstants.DEFAULT_FLEET_NAME)) {
                            result = true;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }
 
    private void showSelectedRaces() {
        List<RegattaOverviewEntryDTO> selectedRaces = getSelectedRaces();
        RegattaRaceStatesComponent.this.raceSelectionProvider.setSelection(selectedRaces);
    }

    private String getStatusText(RaceInfoDTO raceInfo) {
        //TODO i8n
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
                Collections.unmodifiableList(eventDTO.venue.getCourseAreas()),
                Collections.unmodifiableList(raceGroupDTOs));
    }

    @Override
    public void updateSettings(RegattaRaceStatesSettings newSettings) {
        if (settings.getVisibleCourseAreas().isEmpty() || !settings.getVisibleCourseAreas().equals(newSettings.getVisibleCourseAreas())) {
            settings.getVisibleCourseAreas().clear();
            settings.getVisibleCourseAreas().addAll(newSettings.getVisibleCourseAreas());
            
            fillVisibleCourseAreasInSettingsIfEmpty();
        }
        
        if (settings.getVisibleRegattas().isEmpty() || !settings.getVisibleRegattas().equals(newSettings.getVisibleRegattas())) {
            settings.getVisibleRegattas().clear();
            settings.getVisibleRegattas().addAll(newSettings.getVisibleRegattas());
            
            fillVisibleRegattasInSettingsIfEmpty();
        }
        
        if (settings.isShowOnlyRacesOfSameDay() != newSettings.isShowOnlyRacesOfSameDay()) {
            settings.setShowOnlyRaceOfSameDay(newSettings.isShowOnlyRacesOfSameDay());
        }
        
        if (settings.isShowOnlyCurrentlyRunningRaces() != newSettings.isShowOnlyCurrentlyRunningRaces()) {
            settings.setShowOnlyCurrentlyRunningRaces(newSettings.isShowOnlyCurrentlyRunningRaces());
        }
        
        refreshTableWithNewSettings();
    }

    private void refreshTableWithNewSettings() {
        if (eventDTO != null && raceGroupDTOs != null) {
            loadAndUpdateEventLog();
        }
    }

    private void fillVisibleRegattasInSettingsIfEmpty() {
        if (settings.getVisibleRegattas().isEmpty() && raceGroupDTOs != null) {
            for (RaceGroupDTO raceGroup : raceGroupDTOs) {
                settings.getVisibleRegattas().add(raceGroup.getName());
            }
        }
    }

    private void fillVisibleCourseAreasInSettingsIfEmpty() {
        if (settings.getVisibleCourseAreas().isEmpty() && eventDTO != null) {
            for (CourseAreaDTO courseArea : eventDTO.venue.getCourseAreas()) {
                settings.getVisibleCourseAreas().add(courseArea.id);
            }
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

    @Override
    public void onEventUpdated(EventDTO event) {
        eventDTO = event;
        fillVisibleCourseAreasInSettingsIfEmpty();
        refreshTableWithNewSettings();
    }

    @Override
    public void onRaceGroupsUpdated(List<RaceGroupDTO> raceGroups) {
        raceGroupDTOs = raceGroups;
        
        if (!hasAnyRaceGroupASeries()) {
            regattaOverviewTable.removeColumn(seriesNameColumn);
        }
        if (!hasAnyRaceGroupAFleet()) {
            regattaOverviewTable.removeColumn(fleetNameColumn);
        }
        fillVisibleRegattasInSettingsIfEmpty();
        refreshTableWithNewSettings();
    }
}
