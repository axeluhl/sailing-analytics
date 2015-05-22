package com.sap.sailing.gwt.regattaoverview.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.gwt.ui.client.AnchorCell;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.GwtJsonDeSerializer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ClickableSafeHtmlCell;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * This component shows a table displaying the current state of races for a given event. Which races are shown depends
 * on the setting {@link RegattaRaceStatesSettings}. Each entry shows what flags are currently displayed, what start
 * time the race has and additional information, e.g. for Gate start.
 */
public class RegattaRaceStatesComponent extends SimplePanel implements Component<RegattaRaceStatesSettings>,
        EventAndRaceGroupAvailabilityListener {
    public interface EntryHandler {
        void onEntryClicked(RegattaOverviewEntryDTO entry);

        void onEntryUpdated(RegattaOverviewEntryDTO entry);
    }

    private List<RegattaOverviewEntryDTO> allEntries;
    // private CellTable<RegattaOverviewEntryDTO> regattaOverviewTable;
    private ListDataProvider<RegattaOverviewEntryDTO> regattaOverviewDataProvider;
    private final VerticalPanel mainPanel;
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");
    private final DateTimeFormat dateFormatter = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final UUID eventId;
    private EventDTO eventDTO;
    private List<RaceGroupDTO> raceGroupDTOs;
    private TextColumn<RegattaOverviewEntryDTO> seriesNameColumn;
    private TextColumn<RegattaOverviewEntryDTO> fleetNameColumn;
    private final RegattaRaceStatesSettings settings;
    private final RaceStateFlagsInterpreter flagInterpreter;
    private final String localStorageRegattaOverviewEventKey;
    private final Timer timerToSynchronize;
    private static RegattaRaceStatesTableResources tableRes = GWT.create(RegattaRaceStatesTableResources.class);
    private final static String LOCAL_STORAGE_REGATTA_OVERVIEW_KEY = "sailingAnalytics.regattaOverview.settings.";
    private EntryHandler entryClickedHandler;
    private TextColumn<RegattaOverviewEntryDTO> courseAreaColumn;
    private Column<RegattaOverviewEntryDTO, Anchor> regattaNameColumn;
    private Label repeatedInfoLabel;
    private Column<RegattaOverviewEntryDTO, SafeHtml> raceCourseColumn;
    private TextColumn<RegattaOverviewEntryDTO> boatClass;
    private TextColumn<RegattaOverviewEntryDTO> startTimeColumn;
    private SimplePanel tableHolder = new SimplePanel();
    private final long _1_HOUR = 60 /* seconds */* 60 /* minutes */* 60 /* hour */;
    private final long HIDE_COL_TIME_THRESHOLD = _1_HOUR * 12;
    private TextColumn<RegattaOverviewEntryDTO> lastUpdateColumn;
    private TextColumn<RegattaOverviewEntryDTO> endOfProtestTime;
    private TextColumn<RegattaOverviewEntryDTO> raceAdditionalInformationColumn;
    private Column<RegattaOverviewEntryDTO, SafeHtml> flagColumn;
    private Column<RegattaOverviewEntryDTO, SafeHtml> raceStatusColumn;
    private Column<RegattaOverviewEntryDTO, Anchor> raceNameColumn;
    private ListHandler<RegattaOverviewEntryDTO> regattaOverviewListHandler;
    private CellTable<RegattaOverviewEntryDTO> table;
    boolean hasAnyRaceGroupASeries = false;
    boolean hasAnyRaceGroupAFleet = false;

    public void setEntryClickedHandler(EntryHandler handler) {
        this.entryClickedHandler = handler;
    }

    /**
     * @param timerToSynchronize
     *            Whenever this component makes a service call and receives an update on the current server time, the
     *            timer passed for this argument will be synchronized.
     */
    public RegattaRaceStatesComponent(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages, final UUID eventId, RegattaRaceStatesSettings settings,
            Timer timerToSynchronize) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.eventId = eventId;
        this.allEntries = new ArrayList<RegattaOverviewEntryDTO>();
        this.timerToSynchronize = timerToSynchronize;
        this.eventDTO = null;
        this.raceGroupDTOs = null;
        this.localStorageRegattaOverviewEventKey = LOCAL_STORAGE_REGATTA_OVERVIEW_KEY + eventId.toString();
        this.flagInterpreter = new RaceStateFlagsInterpreter(stringMessages);
        this.settings = new RegattaRaceStatesSettings();
        loadAndSetSettings(settings);
        mainPanel = new VerticalPanel();
        mainPanel.getElement().getStyle().setWidth(100, Unit.PCT);
        getElement().getStyle().setWidth(100, Unit.PCT);
        regattaOverviewDataProvider = new ListDataProvider<RegattaOverviewEntryDTO>();
        // regattaOverviewTable = createRegattaTable(true);
        createColumns();
        mainPanel.add(tableHolder);
        setWidget(mainPanel);
    }

    private void loadAndSetSettings(RegattaRaceStatesSettings settings) {
        RegattaRaceStatesSettings loadedSettings = loadRegattaRaceStatesSettings();
        if (loadedSettings != null) {
            settings = loadedSettings;
        }
        updateSettings(settings);
    }

    public void onUpdateServer() {
        loadAndUpdateEventLog();
    }

    @SuppressWarnings("unchecked")
    private void updateTable(List<RegattaOverviewEntryDTO> newEntries) {
        allEntries = newEntries;
        String lastCourseAreaName = null;
        String lastRegattaName = null;
        String lastCourseName = null;
        String lastBoatClass = null;
        boolean canRemoveCourseArea = true;
        boolean canRemoveCourse = true;
        boolean canRemoveRegatta = true;
        boolean canRemoveBoatClass = true;
        boolean canRemoveLastUpdate = true;
        boolean canRemoveProtestTime = true;
        for (RegattaOverviewEntryDTO entryDTO : allEntries) {
            if (canRemoveLastUpdate && timePassedInSeconds(entryDTO.raceInfo.lastUpdateTime) <= HIDE_COL_TIME_THRESHOLD) {
                canRemoveLastUpdate = false;
            }
            if (canRemoveProtestTime && entryDTO.raceInfo.protestFinishTime != null
                    && timePassedInSeconds(entryDTO.raceInfo.protestFinishTime) <= HIDE_COL_TIME_THRESHOLD) {
                canRemoveProtestTime = false;
            }
            if (lastCourseAreaName == null) {
                lastCourseAreaName = entryDTO.courseAreaName;
            } else if (canRemoveCourseArea && !lastCourseAreaName.equals(entryDTO.courseAreaName)) {
                canRemoveCourseArea = false;
            }
            if (lastCourseName == null) {
                lastCourseName = entryDTO.raceInfo.lastCourseName;
            } else if (canRemoveCourse && !lastCourseName.equals(entryDTO.raceInfo.lastCourseName)) {
                canRemoveCourse = false;
            }
            if (lastRegattaName == null) {
                lastRegattaName = entryDTO.regattaDisplayName;
            } else if (canRemoveRegatta && !lastRegattaName.equals(entryDTO.regattaDisplayName)) {
                canRemoveRegatta = false;
            }
            if (lastBoatClass == null) {
                lastBoatClass = entryDTO.boatClassName;
            } else if (canRemoveBoatClass && !lastBoatClass.equals(entryDTO.boatClassName)) {
                canRemoveBoatClass = false;
            }
        }
        StringBuilder repeatedInfosBuilder = new StringBuilder();
        collectRepeatedInfos(stringMessages.regatta(), lastRegattaName, canRemoveRegatta, repeatedInfosBuilder);
        collectRepeatedInfos(stringMessages.courseArea(), lastCourseAreaName, canRemoveCourseArea, repeatedInfosBuilder);
        collectRepeatedInfos(stringMessages.course(), lastCourseName, canRemoveCourse, repeatedInfosBuilder);
        collectRepeatedInfos(stringMessages.boatClass(), lastBoatClass, canRemoveBoatClass, repeatedInfosBuilder);
        collectRepeatedInfos("", "", canRemoveLastUpdate, repeatedInfosBuilder);
        collectRepeatedInfos("", "", canRemoveProtestTime, repeatedInfosBuilder);
        if (repeatedInfoLabel != null) {
            repeatedInfoLabel.setText(repeatedInfosBuilder.toString());
        }
        LinkedList<ColumnSortInfo> sortInfos = new LinkedList<ColumnSortList.ColumnSortInfo>();
        if (table != null) {
            ColumnSortList columnSortList = table.getColumnSortList();
            for (int i = 0; i < columnSortList.size(); i++) {
                sortInfos.addFirst(columnSortList.get(i));
            }
        }
        table = new CellTable<RegattaOverviewEntryDTO>(/* pageSize */10000, tableRes);
        tableHolder.setWidget(table);
        regattaOverviewDataProvider.addDataDisplay(table);
        table.setWidth("100%");
        if (!canRemoveRegatta) {
            table.addColumn(regattaNameColumn, stringMessages.regatta());
        }
        if (!canRemoveCourseArea) {
            table.addColumn(courseAreaColumn, stringMessages.courseArea());
        }
        if (!canRemoveCourse) {
            table.addColumn(raceCourseColumn, stringMessages.course());
        }
        table.addColumn(raceNameColumn, stringMessages.race());
        if (!canRemoveBoatClass) {
            table.addColumn(boatClass, stringMessages.boatClass());
        }
        if (hasAnyRaceGroupAFleet) {
            table.addColumn(fleetNameColumn, stringMessages.fleet());
            table.setColumnWidth(fleetNameColumn, 115, Unit.PX);
        }
        table.addColumn(seriesNameColumn, stringMessages.series());
        table.addColumn(flagColumn, stringMessages.flags());
        table.setColumnWidth(flagColumn, 95, Unit.PX);
        table.addColumn(raceStatusColumn, stringMessages.status());
        if (!canRemoveLastUpdate) {
            table.addColumn(lastUpdateColumn, stringMessages.lastUpdate());
        }
        table.addColumn(startTimeColumn, stringMessages.startTime());
        if (!canRemoveProtestTime) {
            table.addColumn(endOfProtestTime, stringMessages.protestTime());
        }
        table.addColumn(raceAdditionalInformationColumn, stringMessages.additionalInformation());
        table.addColumnSortHandler(regattaOverviewListHandler);
        boolean isSortedOk = false;
        if (sortInfos.size() > 0) {
            GWT.log("Using existing sort info");
            for (ColumnSortInfo columnSortInfo : sortInfos) {
                if (table.getColumnIndex((Column<RegattaOverviewEntryDTO, ?>) columnSortInfo.getColumn()) >= 0) {
                    GWT.log("Pushed: "
                            + table.getHeader(
                                    table.getColumnIndex((Column<RegattaOverviewEntryDTO, ?>) columnSortInfo
                                            .getColumn())).getValue() + ", "
                            + (columnSortInfo.isAscending() ? "ascending" : "descending"));
                    table.getColumnSortList().push(columnSortInfo);
                    isSortedOk = true;
                }
            }
        }
        if (!isSortedOk) {
            ColumnSortInfo initialSortinfo = new ColumnSortInfo(courseAreaColumn, /* ascending */false);
            table.getColumnSortList().push(initialSortinfo);
        }
        regattaOverviewDataProvider.getList().clear();
        regattaOverviewDataProvider.getList().addAll(allEntries);
        if (entryClickedHandler != null) {
            for (RegattaOverviewEntryDTO entry : allEntries) {
                entryClickedHandler.onEntryUpdated(entry);
            }
        }
    }

    private void collectRepeatedInfos(String label, String info, boolean canRemove, StringBuilder repeatedInfosBuilder) {
        if (canRemove && info != null && !info.isEmpty()) {
            if (repeatedInfosBuilder.length() > 0) {
                repeatedInfosBuilder.append(", ");
            }
            repeatedInfosBuilder.append(label).append(": ").append(info);
        }
    }

    protected void loadAndUpdateEventLog() {
        if (eventId == null || eventDTO == null || raceGroupDTOs == null) {
            return;
        }
        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
        sailingService.getRaceStateEntriesForRaceGroup(eventId, settings.getVisibleCourseAreas(), settings
                .getVisibleRegattas(), settings.isShowOnlyCurrentlyRunningRaces(), settings.isShowOnlyRacesOfSameDay(),
                new MarkedAsyncCallback<List<RegattaOverviewEntryDTO>>(
                        new AsyncCallback<List<RegattaOverviewEntryDTO>>() {
                            @Override
                            public void onFailure(Throwable cause) {
                                // ignore errors as state can recover
                            }

                            @Override
                            public void onSuccess(List<RegattaOverviewEntryDTO> result) {
                                final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                                Date serverTimeDuringRequest = null;
                                for (RegattaOverviewEntryDTO entryDTO : result) {
                                    if (entryDTO.currentServerTime != null) {
                                        serverTimeDuringRequest = entryDTO.currentServerTime;
                                    }
                                }
                                updateTable(result);
                                timerToSynchronize.adjustClientServerOffset(clientTimeWhenRequestWasSent,
                                        serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
                            }
                        }));
    }

    private String formatTime(Date time) {
        String result = "-";
        if (time != null) {
            result = "";
            if (!CalendarUtil.isSameDate(time, new Date())) {
                result += dateFormatter.format(time) + " ";
            }
            result += timeFormatter.format(time);
        }
        return result;
    }

    private void createColumns() {
        regattaOverviewListHandler = new ListHandler<RegattaOverviewEntryDTO>(regattaOverviewDataProvider.getList());
        courseAreaColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.courseAreaName;
            }
        };
        courseAreaColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(courseAreaColumn, new Comparator<RegattaOverviewEntryDTO>() {
            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return new NaturalComparator().compare(left.courseAreaName, right.courseAreaName);
            }
        });
        AnchorCell leaderboardAnchorCell = new AnchorCell();
        regattaNameColumn = new Column<RegattaOverviewEntryDTO, Anchor>(leaderboardAnchorCell) {
            @Override
            public Anchor getValue(RegattaOverviewEntryDTO entryDTO) {
                Map<String, String> leaderboardLinkParameters = new HashMap<String, String>();
                leaderboardLinkParameters.put("name", entryDTO.regattaName);
                leaderboardLinkParameters.put("showRaceDetails", String.valueOf(true));
                leaderboardLinkParameters.put("displayName", entryDTO.regattaDisplayName);
                String leaderboardLink = EntryPointLinkFactory.createLeaderboardLink(leaderboardLinkParameters);
                Anchor result = new Anchor(entryDTO.regattaDisplayName, leaderboardLink);
                return result;
            }
        };
        regattaNameColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(regattaNameColumn, new Comparator<RegattaOverviewEntryDTO>() {
            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return new NaturalComparator().compare(left.regattaDisplayName, right.regattaDisplayName);
            }
        });
        seriesNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.raceInfo.seriesName.equals(LeaderboardNameConstants.DEFAULT_SERIES_NAME) ? "-"
                        : entryDTO.raceInfo.seriesName;
            }
        };
        // seriesNameColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        fleetNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.raceInfo.fleetName.equals(LeaderboardNameConstants.DEFAULT_FLEET_NAME) ? "-"
                        : entryDTO.raceInfo.fleetName;
            }
        };
        // fleetNameColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        AnchorCell raceCell = new AnchorCell();
        raceNameColumn = new Column<RegattaOverviewEntryDTO, Anchor>(raceCell) {
            @Override
            public Anchor getValue(RegattaOverviewEntryDTO entryDTO) {
                Anchor result = null;
                if (entryDTO.raceInfo.raceIdentifier != null && entryDTO.raceInfo.isTracked) {
                    Map<String, String> raceLinkParameters = new HashMap<String, String>();
                    raceLinkParameters.put("leaderboardName", entryDTO.regattaName);
                    raceLinkParameters.put("raceName", entryDTO.raceInfo.raceIdentifier.getRaceName());
                    raceLinkParameters.put("canReplayDuringLiveRaces", "true");
                    raceLinkParameters.put("regattaName", entryDTO.regattaName);
                    String raceBoardLink = EntryPointLinkFactory.createRaceBoardLink(raceLinkParameters);
                    result = new Anchor(entryDTO.raceInfo.raceName, raceBoardLink);
                } else {
                    result = new Anchor(entryDTO.raceInfo.raceName);
                    result.setEnabled(false);
                }
                return result;
            }
        };
        raceNameColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(raceNameColumn, new Comparator<RegattaOverviewEntryDTO>() {
            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return new NaturalComparator().compare(left.raceInfo.raceName, right.raceInfo.raceName);
            }
        });
        startTimeColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return formatTime(entryDTO.raceInfo.startTime);
            }
        };
        // raceStartTimeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        startTimeColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(startTimeColumn, new Comparator<RegattaOverviewEntryDTO>() {
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
        raceStatusColumn = new Column<RegattaOverviewEntryDTO, SafeHtml>(new ClickableSafeHtmlCell()) {
            @Override
            public SafeHtml getValue(final RegattaOverviewEntryDTO entryDTO) {
                String status = flagInterpreter.getMeaningOfRaceStateAndFlags(entryDTO.raceInfo.lastStatus,
                        entryDTO.raceInfo.lastUpperFlag, entryDTO.raceInfo.lastLowerFlag,
                        entryDTO.raceInfo.lastFlagsAreDisplayed);
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendHtmlConstant("<a class=\"pointeredLink\">");
                sb.appendEscaped(status);
                sb.appendHtmlConstant("</a>");
                return sb.toSafeHtml();
            }
        };
        raceStatusColumn.setFieldUpdater(new FieldUpdater<RegattaOverviewEntryDTO, SafeHtml>() {
            @Override
            public void update(int index, RegattaOverviewEntryDTO object, SafeHtml value) {
                entryClickedHandler.onEntryClicked(object);
            }
        });
        raceStatusColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(raceStatusColumn, new Comparator<RegattaOverviewEntryDTO>() {
            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                return left.raceInfo.lastStatus.compareTo(right.raceInfo.lastStatus);
            }
        });
        // raceStatusColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        raceCourseColumn = new Column<RegattaOverviewEntryDTO, SafeHtml>(new ClickableSafeHtmlCell()) {
            @Override
            public SafeHtml getValue(final RegattaOverviewEntryDTO entryDTO) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (entryDTO.raceInfo.lastCourseName != null) {
                    String courseName = entryDTO.raceInfo.lastCourseName;
                    sb.appendHtmlConstant("<a class=\"pointeredLink\">");
                    sb.appendEscaped(courseName);
                    sb.appendHtmlConstant("</a>");
                    return sb.toSafeHtml();
                } else {
                    return sb.toSafeHtml();
                }
            }
        };
        raceCourseColumn.setSortable(true);
        raceCourseColumn.setFieldUpdater(new FieldUpdater<RegattaOverviewEntryDTO, SafeHtml>() {
            @Override
            public void update(int index, RegattaOverviewEntryDTO object, SafeHtml value) {
                RaceCourseDTO courseDTO = object.raceInfo.lastCourseDesign;
                if (courseDTO != null && courseDTO.waypoints.size() > 0) {
                    DialogBox courseViewDialogBox = createCourseViewDialogBox(object.raceInfo);
                    courseViewDialogBox.center();
                    courseViewDialogBox.setGlassEnabled(true);
                    courseViewDialogBox.setAnimationEnabled(true);
                    courseViewDialogBox.show();
                }
            }
        });
        // raceCourseColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        regattaOverviewListHandler.setComparator(raceCourseColumn, new Comparator<RegattaOverviewEntryDTO>() {
            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                String cnLeft = left.raceInfo.lastCourseName == null ? "" : left.raceInfo.lastCourseName;
                String cnRight = right.raceInfo.lastCourseName == null ? "" : right.raceInfo.lastCourseName;
                int firstCompare = cnLeft.compareTo(cnRight);
                if (firstCompare != 0) {
                    return firstCompare;
                }
                return left.boatClassName.compareTo(right.boatClassName);
            }
        });
        SafeHtmlCell flagsCell = new SafeHtmlCell();
        flagColumn = new Column<RegattaOverviewEntryDTO, SafeHtml>(flagsCell) {
            @Override
            public SafeHtml getValue(RegattaOverviewEntryDTO entryDTO) {
                String tooltip = "";
                if (entryDTO.raceInfo.lastUpperFlag != null && entryDTO.raceInfo.lastLowerFlag != null) {
                    tooltip = entryDTO.raceInfo.lastUpperFlag.name();
                    if (!entryDTO.raceInfo.lastLowerFlag.equals(Flags.NONE)) {
                        tooltip += " over " + entryDTO.raceInfo.lastLowerFlag.name();
                    }
                    tooltip += " "
                            + FlagsMeaningExplanator.getFlagsMeaning(stringMessages, entryDTO.raceInfo.lastUpperFlag,
                                    entryDTO.raceInfo.lastLowerFlag, entryDTO.raceInfo.lastFlagsAreDisplayed);
                }
                return SailingFlagsBuilder.render(entryDTO.raceInfo.lastUpperFlag, entryDTO.raceInfo.lastLowerFlag,
                        entryDTO.raceInfo.lastFlagsAreDisplayed, entryDTO.raceInfo.lastFlagsDisplayedStateChanged,
                        tooltip);
            }
        };
        boatClass = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.boatClassName;
            }
        };
        lastUpdateColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            private final NumberFormat FMT = NumberFormat.getFormat("00");

            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                final long lastUpdateInSeconds = timePassedInSeconds(entryDTO.raceInfo.lastUpdateTime);
                if (lastUpdateInSeconds > HIDE_COL_TIME_THRESHOLD) {
                    return timeFormatter.format(entryDTO.raceInfo.lastUpdateTime);
                }
                final StringBuilder sb = new StringBuilder();
                if (lastUpdateInSeconds < 60) {
                    sb.append(lastUpdateInSeconds).append("s");
                } else if (lastUpdateInSeconds < _1_HOUR) {
                    sb.append(lastUpdateInSeconds / 60).append("m");
                } else {
                    long hours = lastUpdateInSeconds / 3600;
                    long minutes = (lastUpdateInSeconds - (hours * 3600)) / 60;
                    if (hours > HIDE_COL_TIME_THRESHOLD) {
                        return "";
                    }
                    sb.append(FMT.format(hours)).append(":").append(FMT.format(minutes));
                }
                return sb.toString();
            }
        };
        lastUpdateColumn.setSortable(true);
        regattaOverviewListHandler.setComparator(lastUpdateColumn, new Comparator<RegattaOverviewEntryDTO>() {
            @Override
            public int compare(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
                boolean nullGreater = false;
                Date leftLastUpdateTime = left.raceInfo.lastUpdateTime;
                Date rightLastUpdateTime = right.raceInfo.lastUpdateTime;
                if (leftLastUpdateTime == rightLastUpdateTime) {
                    return 0;
                } else if (leftLastUpdateTime == null) {
                    return (nullGreater ? 1 : -1);
                } else if (rightLastUpdateTime == null) {
                    return (nullGreater ? -1 : 1);
                }
                return leftLastUpdateTime.compareTo(rightLastUpdateTime);
            }
        });
        raceAdditionalInformationColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                StringBuffer additionalInformation = new StringBuffer();
                boolean isInfoBefore = false;
                if (entryDTO.raceInfo.lastLowerFlag != null && entryDTO.raceInfo.lastLowerFlag.equals(Flags.ALPHA)) {
                    additionalInformation.append(stringMessages.noMoreRacingToday());
                    isInfoBefore = true;
                } else if (entryDTO.raceInfo.lastLowerFlag != null
                        && entryDTO.raceInfo.lastLowerFlag.equals(Flags.HOTEL)) {
                    additionalInformation.append(stringMessages.furtherSignalsAshore());
                    isInfoBefore = true;
                } else if (entryDTO.raceInfo.lastUpperFlag != null
                        && entryDTO.raceInfo.lastUpperFlag.equals(Flags.XRAY)
                        && entryDTO.raceInfo.lastFlagsAreDisplayed) {
                    additionalInformation.append(stringMessages.earlyStarters());
                    isInfoBefore = true;
                }
                if (entryDTO.raceInfo.finishedTime != null) {
                    if (isInfoBefore) {
                        additionalInformation.append("  /  ");
                    }
                    additionalInformation.append("Finished at: "
                            + (timeFormatter.format(entryDTO.raceInfo.finishedTime)));
                    isInfoBefore = true;
                }
                return additionalInformation.toString();
            }
        };
        endOfProtestTime = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                StringBuffer additionalInformation = new StringBuffer();
                if (entryDTO.raceInfo.protestFinishTime != null) {
                    final Date now = new Date();
                    if (entryDTO.raceInfo.protestFinishTime.after(now)) {
                        additionalInformation.append(stringMessages.protestTimeFinishesAt());
                        additionalInformation.append(" ");
                    } else {
                        additionalInformation.append(stringMessages.protestTimeFinishedAt());
                        additionalInformation.append(" ");
                    }
                    additionalInformation.append(timeFormatter.format(entryDTO.raceInfo.protestFinishTime));
                }
                return additionalInformation.toString();
            }
        };
    }

    public List<RegattaOverviewEntryDTO> getAllRaces() {
        return allEntries;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<RegattaRaceStatesSettings> getSettingsDialogComponent() {
        return new RegattaRaceStatesSettingsDialogComponent(settings, stringMessages, eventId,
                Collections.unmodifiableList(eventDTO.venue.getCourseAreas()),
                Collections.unmodifiableList(raceGroupDTOs));
    }

    @Override
    public void updateSettings(RegattaRaceStatesSettings newSettings) {
        if (settings.getVisibleCourseAreas().isEmpty()
                || !settings.getVisibleCourseAreas().equals(newSettings.getVisibleCourseAreas())) {
            settings.getVisibleCourseAreas().clear();
            settings.getVisibleCourseAreas().addAll(newSettings.getVisibleCourseAreas());
            fillVisibleCourseAreasInSettingsIfEmpty();
        }
        if (settings.getVisibleRegattas().isEmpty()
                || !settings.getVisibleRegattas().equals(newSettings.getVisibleRegattas())) {
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
        storeRegattaRaceStatesSettings(settings);
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
        GWT.log("onRaceGroupsUpdated");
        if (raceGroupDTOs != null) {
            search: for (RaceGroupDTO raceGroup : raceGroupDTOs) {
                for (RaceGroupSeriesDTO series : raceGroup.getSeries()) {
                    if (!series.getName().equals(LeaderboardNameConstants.DEFAULT_SERIES_NAME)) {
                        hasAnyRaceGroupASeries = true;
                    }
                    if (!hasAnyRaceGroupAFleet) { // don't re-check fleet, we already found one
                        for (FleetDTO fleet : series.getFleets()) {
                            if (!fleet.getName().equals(LeaderboardNameConstants.DEFAULT_FLEET_NAME)) {
                                hasAnyRaceGroupAFleet = true;
                                break;
                            }
                        }
                    }
                    if (hasAnyRaceGroupASeries && hasAnyRaceGroupAFleet) {
                        break search;
                    }
                }
            }
        }
        // if (!hasAnyRaceGroupASeries) {
        // if (regattaOverviewTable.getColumnIndex(seriesNameColumn) >= 0) {
        // regattaOverviewTable.removeColumn(seriesNameColumn);
        // }
        // }
        // if (!hasAnyRaceGroupAFleet) {
        // if (regattaOverviewTable.getColumnIndex(fleetNameColumn) >= 0) {
        // regattaOverviewTable.removeColumn(fleetNameColumn);
        // }
        // }
        // regattaOverviewTable.redraw();
        fillVisibleRegattasInSettingsIfEmpty();
        refreshTableWithNewSettings();
    }

    private void storeRegattaRaceStatesSettings(RegattaRaceStatesSettings settings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null && eventId != null) {
            // delete old value
            localStorage.removeItem(localStorageRegattaOverviewEventKey);
            // store settings
            GwtJsonDeSerializer<RegattaRaceStatesSettings> serializer = new RegattaRaceStatesSettingsJsonDeSerializer();
            JSONObject settingsAsJson = serializer.serialize(settings);
            localStorage.setItem(localStorageRegattaOverviewEventKey, settingsAsJson.toString());
        }
    }

    private RegattaRaceStatesSettings loadRegattaRaceStatesSettings() {
        RegattaRaceStatesSettings loadedSettings = null;
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            String jsonAsLocalStore = localStorage.getItem(localStorageRegattaOverviewEventKey);
            if (jsonAsLocalStore != null && !jsonAsLocalStore.isEmpty()) {
                GwtJsonDeSerializer<RegattaRaceStatesSettings> deserializer = new RegattaRaceStatesSettingsJsonDeSerializer();
                JSONValue value = JSONParser.parseStrict(jsonAsLocalStore);
                if (value.isObject() != null) {
                    loadedSettings = deserializer.deserialize((JSONObject) value);
                }
            }
        }
        return loadedSettings;
    }

    private DialogBox createCourseViewDialogBox(RaceInfoDTO raceInfoDTO) {
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText(stringMessages.courseLayout());
        // Create a table to layout the content
        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        dialogBox.setWidget(dialogContents);
        Label courseNameLabel = new Label(raceInfoDTO.lastCourseName);
        dialogContents.add(courseNameLabel);
        Grid waypointGrid = new Grid(raceInfoDTO.lastCourseDesign.waypoints.size(), 1);
        dialogContents.add(waypointGrid);
        for (int i = 0; i < raceInfoDTO.lastCourseDesign.waypoints.size(); i++) {
            WaypointDTO waypoint = raceInfoDTO.lastCourseDesign.waypoints.get(i);
            waypointGrid.setText(i, 0, getWaypointNameLabel(waypoint));
        }
        Button closeButton = new Button(stringMessages.ok());
        closeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        dialogContents.add(closeButton);
        dialogContents.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_CENTER);
        return dialogBox;
    }

    private String getWaypointNameLabel(WaypointDTO waypointDTO) {
        String result = waypointDTO.getName();
        result += (waypointDTO.passingInstructions == null) ? "" : ","
                + getPassingInstructionsAsText(waypointDTO.passingInstructions);
        return result;
    }

    private String getPassingInstructionsAsText(PassingInstruction passingInstructions) {
        switch (passingInstructions) {
        case Port:
            return stringMessages.toSide() + " " + stringMessages.portSide();
        case Starboard:
            return stringMessages.toSide() + " " + stringMessages.starboardSide();
        case Gate:
            return stringMessages.gate();
        case Line:
            return stringMessages.line();
        case Offset:
            return stringMessages.offset();
        default:
            return "";
        }
    }

    private final long timePassedInSeconds(Date dateTime) {
        if (dateTime == null)
            return 0;
        return (new Date().getTime() - dateTime.getTime()) / 1000;
    }

    @Override
    public String getDependentCssClassName() {
        return "regattaRaceStates";
    }

    public void setRepeatedInfoLabel(Label repeatedInfoLabel) {
        this.repeatedInfoLabel = repeatedInfoLabel;
    }
}
