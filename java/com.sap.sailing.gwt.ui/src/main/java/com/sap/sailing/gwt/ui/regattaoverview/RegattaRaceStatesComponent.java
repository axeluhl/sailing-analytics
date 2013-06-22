package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.AnchorCell;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.GwtJsonDeSerializer;
import com.sap.sailing.gwt.ui.client.MarkedAsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;

/**
 * This component shows a table displaying the current state of races for a given event. 
 * Which races are shown depends on the setting {@link RegattaRaceStatesSettings}.
 * Each entry shows what flags are currently displayed, what start time the race has and additional information, e.g. for Gate start.
 */
public class RegattaRaceStatesComponent extends SimplePanel implements Component<RegattaRaceStatesSettings>, EventAndRaceGroupAvailabilityListener {

    public interface EntryClickedHandler {
        void onEntryClicked(RegattaOverviewEntryDTO entry);
    }
    
    private List<RegattaOverviewEntryDTO> allEntries;

    private final CellTable<RegattaOverviewEntryDTO> regattaOverviewTable;
    private ListDataProvider<RegattaOverviewEntryDTO> regattaOverviewDataProvider;
    private final VerticalPanel mainPanel;
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");

    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final String eventIdAsString;
    private final FlagImageResolver flagImageResolver;

    private EventDTO eventDTO;
    private List<RaceGroupDTO> raceGroupDTOs;
    private TextColumn<RegattaOverviewEntryDTO> seriesNameColumn;
    private TextColumn<RegattaOverviewEntryDTO> fleetNameColumn;

    private final RegattaRaceStatesSettings settings;
    private final FlagAlphabetInterpreter flagInterpreter;
    
    private final String localStorageRegattaOverviewEventKey;

    private final Timer timerToSynchronize;

    private static RegattaRaceStatesTableResources tableRes = GWT.create(RegattaRaceStatesTableResources.class);

    private final static String LOCAL_STORAGE_REGATTA_OVERVIEW_KEY = "sailingAnalytics.regattaOverview.settings.";
    
    private static final String STYLE_NAME_PREFIX = "RegattaOverview-";
    private static final String STYLE_CIRCLE = STYLE_NAME_PREFIX + "circle";
    private static final String STYLE_CIRCLE_BLUE = "circleBlue";
    private static final String STYLE_CIRCLE_YELLOW = "circleYellow";
    private static final String STYLE_CIRCLE_GREEN = "circleGreen";
    private static final String STYLE_CIRCLE_GREY = "circleGrey";
    
    private EntryClickedHandler entryClickedHandler;
    
    public void setEntryClickedHandler(EntryClickedHandler handler) {
        this.entryClickedHandler = handler;
    }

    /**
     * @param timerToSynchronize
     *            Whenever this component makes a service call and receives an update on the current server time, the
     *            timer passed for this argument will be synchronized.
     */
    public RegattaRaceStatesComponent(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages, final String eventIdAsString, RegattaRaceStatesSettings settings, Timer timerToSynchronize) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.eventIdAsString = eventIdAsString;
        this.flagImageResolver = new FlagImageResolver();
        this.allEntries = new ArrayList<RegattaOverviewEntryDTO>();
        this.timerToSynchronize = timerToSynchronize;

        this.eventDTO = null;
        this.raceGroupDTOs = null;
        
        this.localStorageRegattaOverviewEventKey = LOCAL_STORAGE_REGATTA_OVERVIEW_KEY + eventIdAsString;

        this.flagInterpreter = new FlagAlphabetInterpreter(stringMessages);

        this.settings = new RegattaRaceStatesSettings();
        loadAndSetSettings(settings);

        mainPanel = new VerticalPanel();
        setWidth("100%");

        regattaOverviewDataProvider = new ListDataProvider<RegattaOverviewEntryDTO>();
        regattaOverviewTable = createRegattaTable();
        regattaOverviewTable.addCellPreviewHandler(new Handler<RegattaOverviewEntryDTO>() {
            @Override
            public void onCellPreview(CellPreviewEvent<RegattaOverviewEntryDTO> event) {
                if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType()) && entryClickedHandler != null){
                    entryClickedHandler.onEntryClicked(event.getValue());
                }
            }
        });

        mainPanel.add(regattaOverviewTable);
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

    private void updateTable(List<RegattaOverviewEntryDTO> newEntries) {
        allEntries = newEntries;

        regattaOverviewDataProvider.getList().clear();
        regattaOverviewDataProvider.getList().addAll(allEntries);
        // now sort again according to selected criterion
        ColumnSortEvent.fire(regattaOverviewTable, regattaOverviewTable.getColumnSortList());
    }

    /**
     */
    protected void loadAndUpdateEventLog() {
        if (eventIdAsString == null || eventDTO == null || raceGroupDTOs == null) {
            return;
        }
        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
        sailingService.getRaceStateEntriesForRaceGroup(eventIdAsString, settings.getVisibleCourseAreas(), settings.getVisibleRegattas(), 
                settings.isShowOnlyCurrentlyRunningRaces(), settings.isShowOnlyRacesOfSameDay(), new MarkedAsyncCallback<List<RegattaOverviewEntryDTO>>() {

            @Override
            protected void handleFailure(Throwable cause) {

            }

            @Override
            protected void handleSuccess(List<RegattaOverviewEntryDTO> result) {
                final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                Date serverTimeDuringRequest = null;
                for (RegattaOverviewEntryDTO entryDTO : result) {
                    if (entryDTO.currentServerTime != null) {
                        serverTimeDuringRequest = entryDTO.currentServerTime;
                    }
                }
                updateTable(result);
                timerToSynchronize.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
            }

        });
    }

    private CellTable<RegattaOverviewEntryDTO> createRegattaTable() {
        CellTable<RegattaOverviewEntryDTO> table = new CellTable<RegattaOverviewEntryDTO>(/* pageSize */10000, tableRes);
        regattaOverviewDataProvider.addDataDisplay(table);
        table.setWidth("100%");

        ListHandler<RegattaOverviewEntryDTO> regattaOverviewListHandler = new ListHandler<RegattaOverviewEntryDTO>(
                regattaOverviewDataProvider.getList());
        
        SafeHtmlCell circleCell = new SafeHtmlCell();
        Column<RegattaOverviewEntryDTO, SafeHtml> circleColumn = new Column<RegattaOverviewEntryDTO, SafeHtml>(circleCell) {

            @Override
            public SafeHtml getValue(RegattaOverviewEntryDTO entryDTO) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                builder.append(SafeHtmlUtils.fromTrustedString("<div class=\"" + STYLE_CIRCLE + " "));
                switch (entryDTO.raceInfo.lastStatus) {
                case RUNNING:
                    builder.append(SafeHtmlUtils.fromTrustedString(STYLE_CIRCLE_GREEN));
                    break;
                case FINISHED:
                    builder.append(SafeHtmlUtils.fromTrustedString(STYLE_CIRCLE_BLUE));
                    break;
                case SCHEDULED:
                case STARTPHASE:
                    builder.append(SafeHtmlUtils.fromTrustedString(STYLE_CIRCLE_YELLOW));
                    break;
                default:
                    builder.append(SafeHtmlUtils.fromTrustedString(STYLE_CIRCLE_GREY));
                    break;
                }
                
                builder.append(SafeHtmlUtils.fromTrustedString("\"></div>"));
                return builder.toSafeHtml();
            }
            
        };

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
                return new NaturalComparator().compare(left.courseAreaName, right.courseAreaName);
            }

        });

        AnchorCell leaderboardAnchorCell = new AnchorCell();
        Column<RegattaOverviewEntryDTO, Anchor> regattaNameColumn = new Column<RegattaOverviewEntryDTO, Anchor>(leaderboardAnchorCell) {
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

        AnchorCell raceCell = new AnchorCell();
        Column<RegattaOverviewEntryDTO, Anchor> raceNameColumn = new Column<RegattaOverviewEntryDTO, Anchor>(raceCell) {
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

        TextColumn<RegattaOverviewEntryDTO> raceStatusColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return flagInterpreter.getMeaningOfRaceStateAndFlags(entryDTO.raceInfo.lastStatus, entryDTO.raceInfo.lastUpperFlag, 
                        entryDTO.raceInfo.lastLowerFlag, entryDTO.raceInfo.isLastFlagDisplayed);
            }
        };

        Column<RegattaOverviewEntryDTO, String> raceCourseColumn = new Column<RegattaOverviewEntryDTO, String>(new ClickableTextCell()) {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                String courseName = "-";
                if (entryDTO.raceInfo.lastCourseName != null) {
                    courseName = entryDTO.raceInfo.lastCourseName;
                }
                return courseName;
            }
        };
        raceCourseColumn.setFieldUpdater(new FieldUpdater<RegattaOverviewEntryDTO, String>() {

            @Override
            public void update(int index, RegattaOverviewEntryDTO object, String value) {
                if (object.raceInfo.lastCourseDesign.waypoints.size() > 0) {
                    DialogBox courseViewDialogBox = createCourseViewDialogBox(object.raceInfo);
                    courseViewDialogBox.center();
                    courseViewDialogBox.setGlassEnabled(true);
                    courseViewDialogBox.setAnimationEnabled(true);
                    courseViewDialogBox.show();
                }
            }

        });
        raceCourseColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

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
                boolean isInfoBefore = false;
                if (entryDTO.raceInfo.lastUpdateTime != null) {
                    additionalInformation.append("Last update at " + timeFormatter.format(entryDTO.raceInfo.lastUpdateTime));
                    isInfoBefore = true;
                }
                if (entryDTO.raceInfo.pathfinderId != null) {
                    if (isInfoBefore) {
                        additionalInformation.append("  /  ");
                    }
                    additionalInformation.append("Pathfinder: " + entryDTO.raceInfo.pathfinderId);
                    isInfoBefore = true;
                }
                if (entryDTO.raceInfo.gateLineOpeningTime != null) {
                    if (isInfoBefore) {
                        additionalInformation.append("  /  ");
                    }
                    additionalInformation.append("GateLineOpeningTime: "
                            + (entryDTO.raceInfo.gateLineOpeningTime / (60 * 1000)) + " minutes");
                    isInfoBefore = true;
                }
                if (entryDTO.raceInfo.protestFinishTime != null) {
                    if (isInfoBefore) {
                        additionalInformation.append("  /  ");
                    }
                    Date now = new Date();
                    String text = "";
                    if (entryDTO.raceInfo.protestFinishTime.after(now)) {
                        text = stringMessages.protestTimeFinishesAt();
                    } else {
                        text = stringMessages.protestTimeFinishedAt();
                    }
                    additionalInformation.append(text + timeFormatter.format(entryDTO.raceInfo.protestFinishTime));
                }

                return additionalInformation.toString();
            }
        };

        table.addColumn(circleColumn, "");
        table.addColumn(courseAreaColumn, stringMessages.courseArea());
        table.addColumn(regattaNameColumn, stringMessages.regatta());
        table.addColumn(seriesNameColumn, stringMessages.series());
        table.addColumn(fleetNameColumn, stringMessages.fleet());
        table.addColumn(raceNameColumn, stringMessages.race());
        table.addColumn(raceStartTimeColumn, stringMessages.startTime());
        table.addColumn(raceStatusColumn, stringMessages.status());
        table.addColumn(raceCourseColumn, stringMessages.course());
        table.addColumn(lastUpperFlagColumn, stringMessages.lastUpperFlag());
        table.addColumn(lastLowerFlagColumn, stringMessages.lastLowerFlag());
        table.addColumn(lastFlagDirectionColumn, stringMessages.flagStatus());
        table.addColumn(raceAdditionalInformationColumn, stringMessages.additionalInformation());
        
        table.addColumnSortHandler(regattaOverviewListHandler);
        table.getColumnSortList().push(courseAreaColumn);
        
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

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<RegattaRaceStatesSettings> getSettingsDialogComponent() {
        return new RegattaRaceStatesSettingsDialogComponent(settings, stringMessages, eventIdAsString,
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

        if (!hasAnyRaceGroupASeries()) {
            regattaOverviewTable.removeColumn(seriesNameColumn);
        }
        if (!hasAnyRaceGroupAFleet()) {
            regattaOverviewTable.removeColumn(fleetNameColumn);
        }
        fillVisibleRegattasInSettingsIfEmpty();
        refreshTableWithNewSettings();
    }

    private void storeRegattaRaceStatesSettings(RegattaRaceStatesSettings settings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            // delete old value
            localStorage.removeItem(localStorageRegattaOverviewEventKey);

            //store settings
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
                if(value.isObject() != null) {
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
        dialogContents.setCellHorizontalAlignment(
                closeButton, HasHorizontalAlignment.ALIGN_CENTER);

        return dialogBox;
    }

    private String getWaypointNameLabel(WaypointDTO waypointDTO) {
        String result = waypointDTO.getName();
        result += (waypointDTO.passingSide == null) ? "" : ", to " + getNauticalSideAsText(waypointDTO.passingSide);
        return result;
    }

    private String getNauticalSideAsText(NauticalSide passingSide) {
        switch (passingSide) {
        case PORT:
            return stringMessages.portSide();
        case STARBOARD:
            return stringMessages.starboardSide();
        default:
            return "";
        }
    }
}
