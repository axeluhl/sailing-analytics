package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.sap.sailing.domain.common.impl.Util.NaturalComparator;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
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
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RegattaOverviewTableComposite extends Composite {

    private List<RegattaOverviewEntryDTO> allEntries;
    private boolean isFilterActive;

    private final SelectionModel<RegattaOverviewEntryDTO> raceSelectionModel;
    private final CellTable<RegattaOverviewEntryDTO> regattaOverviewTable;
    private ListDataProvider<RegattaOverviewEntryDTO> regattaOverviewDataProvider;
    private final SimplePanel mainPanel;
    private final VerticalPanel panel;
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");

    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final String eventIdAsString;
    private final RegattaOverviewRaceSelectionProvider raceSelectionProvider;

    private final FlagImageResolver flagImageResolver;

    private static RegattaOverviewTableResources tableRes = GWT.create(RegattaOverviewTableResources.class);

    public RegattaOverviewTableComposite(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages, final String eventIdAsString,
            final RegattaOverviewRaceSelectionProvider raceSelectionProvider) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.eventIdAsString = eventIdAsString;
        this.raceSelectionProvider = raceSelectionProvider;
        this.flagImageResolver = new FlagImageResolver();

        this.isFilterActive = true;

        mainPanel = new SimplePanel();
        panel = new VerticalPanel();
        mainPanel.setWidget(panel);
        mainPanel.setWidth("100%");

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

        panel.add(regattaOverviewTable);

        initWidget(mainPanel);

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

    public boolean switchFilter() {
        this.isFilterActive = !isFilterActive;
        updateTable(allEntries);
        return isFilterActive;
    }

    private void updateTable(List<RegattaOverviewEntryDTO> newEntries) {
        allEntries = newEntries;
        regattaOverviewDataProvider.getList().clear();
        regattaOverviewDataProvider.getList().addAll(filterAndSort(allEntries));
        // now sort again according to selected criterion
        ColumnSortEvent.fire(regattaOverviewTable, regattaOverviewTable.getColumnSortList());
    }

    private Collection<? extends RegattaOverviewEntryDTO> filterAndSort(List<RegattaOverviewEntryDTO> raceList) {
        List<RegattaOverviewEntryDTO> reversedUnfilterted = new ArrayList<RegattaOverviewEntryDTO>(raceList);

        // This has grown to an ugly hack. Consider moving this filtering to a better place and
        // change the handling of alphanum-like race names!
        Collections.sort(reversedUnfilterted, new Comparator<RegattaOverviewEntryDTO>() {
            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                int result = left.courseAreaName.compareTo(right.courseAreaName);
                if (result == 0) {
                    if (left.regattaName != null && right.regattaName != null) {
                        result = left.regattaName.compareTo(right.regattaName);
                    }
                    if (result == 0) {
                        result = left.raceInfo.fleet.compareTo(right.raceInfo.fleet);
                        if (result == 0) {
                            result = new NaturalComparator().compare(right.raceInfo.raceName, left.raceInfo.raceName);
                        }
                    }
                }
                return result;
            }
        });

        // And now we are going to filter
        if (!isFilterActive) {
            return reversedUnfilterted;
        }

        List<RegattaOverviewEntryDTO> filtered = new ArrayList<RegattaOverviewEntryDTO>();
        int maxAddtionalRacesCount = 2;
        for (RegattaOverviewEntryDTO entry : reversedUnfilterted) {
            RaceLogRaceStatus status = entry.raceInfo.lastStatus;
            if (status != null && isRaceActive(status)) {
                filtered.add(entry);
                continue;
            }

            if (maxAddtionalRacesCount == 0) {
                continue;
            }

            if (entry.raceInfo.hasEvents) {
                maxAddtionalRacesCount--;
                filtered.add(entry);
            }
        }
        
        // Add the "next race" to the list of races
        // TODO: Should be done for each course area, for each regatta, for each fleet...
        if (filtered.size() > 0 && filtered.size() != reversedUnfilterted.size()) {
            RegattaOverviewEntryDTO topMostEntry = filtered.get(0);
            int topMostIndex = reversedUnfilterted.indexOf(topMostEntry);
            if (topMostIndex >= 1) {
                filtered.add(0, reversedUnfilterted.get(topMostIndex - 1));
            }
        }
        return filtered;
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
                return entryDTO.regattaName;
            }
        };
        regattaNameColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(regattaNameColumn, new Comparator<RegattaOverviewEntryDTO>() {

            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return left.regattaName.compareTo(right.regattaName);
            }

        });

        TextColumn<RegattaOverviewEntryDTO> fleetNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.raceInfo.fleet;
            }
        };
        fleetNameColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(fleetNameColumn, new Comparator<RegattaOverviewEntryDTO>() {

            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return left.raceInfo.fleet.compareTo(right.raceInfo.fleet);
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
                    return flagImageResolver.resolveFlagDirectionToImage(entryDTO.raceInfo.displayed);
                else
                    return null;
            }
        };

        TextColumn<RegattaOverviewEntryDTO> raceAddditionalInformationColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                StringBuffer additionalInformation = new StringBuffer();
                if (entryDTO.raceInfo.hasEvents && !isRaceActive(entryDTO.raceInfo.lastStatus)) {
                    additionalInformation.append("Race has been aborted before. ");
                }
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

        table.addColumn(courseAreaColumn, stringMessages.courseArea());
        table.addColumn(regattaNameColumn, stringMessages.boatClass()); // For sailors the boat class also contains
                                                                        // additional infos such as woman/man, e.g.
                                                                        // Laser Radial Woman or Laser Radial Men
        table.addColumn(fleetNameColumn, stringMessages.fleet());
        table.addColumn(raceNameColumn, stringMessages.race());
        table.addColumn(raceStartTimeColumn, stringMessages.startTime());
        table.addColumn(raceStatusColumn, stringMessages.status());
        table.addColumn(lastUpperFlagColumn, stringMessages.lastUpperFlag());
        table.addColumn(lastLowerFlagColumn, stringMessages.lastLowerFlag());
        table.addColumn(lastFlagDirectionColumn, stringMessages.flagStatus());
        table.addColumn(raceAddditionalInformationColumn, stringMessages.additionalInformation());
        table.addColumnSortHandler(regattaOverviewListHandler);

        return table;
    }

    public List<RegattaOverviewEntryDTO> getAllRaces() {
        return allEntries;
    }

    private void showSelectedRaces() {
        List<RegattaOverviewEntryDTO> selectedRaces = getSelectedRaces();
        RegattaOverviewTableComposite.this.raceSelectionProvider.setSelection(selectedRaces);
    }

    private String getStatusText(RaceInfoDTO raceInfo) {
        String statusText = "";
        if (raceInfo.lastStatus.equals(RaceLogRaceStatus.RUNNING) && raceInfo.lastUpperFlag.equals(Flags.XRAY)
                && raceInfo.displayed) {
            statusText = "Race is running (had early starters)";
        } else if (raceInfo.lastStatus.equals(RaceLogRaceStatus.RUNNING) && raceInfo.lastUpperFlag.equals(Flags.XRAY)
                && !raceInfo.displayed) {
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
        } else if (raceInfo.lastUpperFlag.equals(Flags.AP) && raceInfo.displayed) {
            statusText = "Start postponed";
        } else if (raceInfo.lastUpperFlag.equals(Flags.NOVEMBER) && raceInfo.displayed) {
            statusText = "Start abandoned";
        }
        return statusText;
    }
}
