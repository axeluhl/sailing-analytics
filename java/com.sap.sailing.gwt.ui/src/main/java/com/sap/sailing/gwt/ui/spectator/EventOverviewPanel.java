package com.sap.sailing.gwt.ui.spectator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.PlacemarkOrderDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.URLEncoder;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

/**
 * An overview panel for sailing events
 * @author Frank Mittag (C5163874)
 */
public class EventOverviewPanel extends FormPanel {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml anchor(String url, String displayName);
    }

    public static final String STYLE_NAME_PREFIX = "eventOverviewPanel-";
    private static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private TextBox locationTextBox;
    private TextBox nameTextBox;
    private TextBox fromTextBox;
    private TextBox untilTextBox;
    private CheckBox onlyLiveCheckBox;
    
    private CellTable<EventDTO> eventsTable;
    private ListDataProvider<EventDTO> eventsDataProvider;
    private SingleSelectionModel<EventDTO> eventSelectionModel;
    
    private FlowPanel eventDetailsPanel;
    private HTML eventDescriptionHTML;
    private CellTable<StrippedLeaderboardDTO> leaderboardsTable;
    private ListDataProvider<StrippedLeaderboardDTO> leaderboardsDataProvider;
    private SingleSelectionModel<StrippedLeaderboardDTO> leaderboardsSelectionModel;
    
    private FlowPanel leaderboardDetailsPanel;
    private CellTable<RaceColumnDTO> racesTable;
    private NoSelectionModel<RaceColumnDTO> racesSelectionModel;
    private ListDataProvider<RaceColumnDTO> racesDataProvider;
    
    private List<EventDTO> availableEvents;
    private final boolean showRaceDetails;
    private final Timer timerForClientServerOffset;

    private final LeaderboardGroupOverviewTableResources tableResources = GWT.create(LeaderboardGroupOverviewTableResources.class);

    public EventOverviewPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, boolean showRaceDetails) {
        this.showRaceDetails = showRaceDetails;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.timerForClientServerOffset = new Timer(PlayModes.Replay);
        availableEvents = new ArrayList<EventDTO>();
        
        FlowPanel mainPanel = new FlowPanel();
        this.setWidget(mainPanel);
        mainPanel.setSize("100%", "100%");
        
        mainPanel.add(createSearchGUI());
        mainPanel.add(createEventsGUI(tableResources));
        mainPanel.add(createEventDetailsGUI(tableResources));
        
        loadEvents();
    }
    
    private Panel createSearchGUI() {
        FlowPanel searchPanel = new FlowPanel();
        searchPanel.setWidth("100%");

        FlowPanel header = new FlowPanel();
        searchPanel.add(header);
        Label headerLabel = new Label(stringMessages.searchEvents());
        headerLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        header.add(headerLabel);
         
        Label locationLabel = new Label(this.stringMessages.location() + ":");
        locationLabel.setStyleName(STYLE_NAME_PREFIX + "searchLabel");
        locationLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        searchPanel.add(locationLabel);
        locationTextBox = new TextBox();
        locationTextBox.setStyleName(STYLE_NAME_PREFIX + "searchInputField");
        locationTextBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        locationTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchPanel.add(locationTextBox);
        
        Label nameLabel = new Label(this.stringMessages.name() + ":");
        nameLabel.setStyleName(STYLE_NAME_PREFIX + "searchLabel");
        nameLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        searchPanel.add(nameLabel);
        nameTextBox = new TextBox();
        nameTextBox.setStyleName(STYLE_NAME_PREFIX + "searchInputField");
        nameTextBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        nameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchPanel.add(nameTextBox);
        
        onlyLiveCheckBox = new CheckBox(this.stringMessages.onlyLiveEvents());
        onlyLiveCheckBox.setStyleName(STYLE_NAME_PREFIX + "searchCheckBox");
        onlyLiveCheckBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        onlyLiveCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                onCheckBoxLiveChange();
            }
        });
        searchPanel.add(onlyLiveCheckBox);
        
        Label fromDateLabel = new Label(this.stringMessages.from() + ":");
        fromDateLabel.setStyleName(STYLE_NAME_PREFIX + "searchLabel");
        fromDateLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        searchPanel.add(fromDateLabel);
        fromTextBox = new TextBox();
        fromTextBox.setStyleName(STYLE_NAME_PREFIX + "searchInputField");
        fromTextBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        fromTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchPanel.add(fromTextBox);
        
        Label toDateLabel = new Label(this.stringMessages.until() + ":");
        toDateLabel.setStyleName(STYLE_NAME_PREFIX + "searchLabel");
        toDateLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        searchPanel.add(toDateLabel);
        untilTextBox = new TextBox();
        untilTextBox.setStyleName(STYLE_NAME_PREFIX + "searchInputField");
        untilTextBox.getElement().setPropertyString("clear", "right");
        untilTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchPanel.add(untilTextBox);
        
        return searchPanel;
    }
    
    private Panel createEventsGUI(LeaderboardGroupOverviewTableResources tableResources) {
        FlowPanel eventsPanel = new FlowPanel();
        eventsPanel.setStyleName(STYLE_NAME_PREFIX + "eventsPanel"); 

        FlowPanel header = new FlowPanel();
        eventsPanel.add(header);
        Label headerLabel = new Label(stringMessages.events());
        headerLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        header.add(headerLabel);
        
        AnchorCell eventNameAnchorCell = new AnchorCell();
        Column<EventDTO, SafeHtml> eventNameColumn = new Column<EventDTO, SafeHtml>(eventNameAnchorCell) {
            @Override
            public SafeHtml getValue(EventDTO event) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                builder.appendHtmlConstant(event.getName());
                return builder.toSafeHtml();
            }
        };
        eventNameColumn.setSortable(true);

        TextColumn<EventDTO> eventVenueColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                return event.venue.getName();
            }
        };
        eventVenueColumn.setSortable(true);
                
        TextColumn<EventDTO> eventLeaderboardsColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                String result = "";
                if(event.leaderboardGroup != null) {
                    StringBuilder sb = new StringBuilder();
                    boolean first = true;
                    for (StrippedLeaderboardDTO leaderboard : event.leaderboardGroup.leaderboards) {
                        if (!first) {
                            sb.append(", ");
                        }
                        sb.append(leaderboard.name);
                        first = false;
                    }
                    result = sb.toString();
                } 
                return result;
            }
        };
        eventLeaderboardsColumn.setSortable(false);
        
        TextColumn<EventDTO> eventStartDateColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                if(event.leaderboardGroup != null) {
                    Date startDate = event.leaderboardGroup.getGroupStartDate();
                    return startDate == null ? EventOverviewPanel.this.stringMessages.untracked()
                            : DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(startDate);
                } else {
                    return "";
                }
            }
        };
        eventStartDateColumn.setSortable(true);
        
        eventsTable = new CellTable<EventDTO>(10000, tableResources);
        eventsTable.setWidth("100%");
        eventSelectionModel = new SingleSelectionModel<EventDTO>();
        eventSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                onEventSelectionChanged();
            }
        });
        eventsTable.setSelectionModel(eventSelectionModel);
        
        eventsTable.addColumn(eventNameColumn, this.stringMessages.name());
        eventsTable.addColumn(eventVenueColumn, this.stringMessages.venue());
        eventsTable.addColumn(eventLeaderboardsColumn, this.stringMessages.leaderboards());
        eventsTable.addColumn(eventStartDateColumn, this.stringMessages.startDate());
        eventsPanel.add(eventsTable);
        
        eventsDataProvider = new ListDataProvider<EventDTO>();
        eventsDataProvider.addDataDisplay(eventsTable);

        ListHandler<EventDTO> eventsListHandler = new ListHandler<EventDTO>(eventsDataProvider.getList());
        eventsListHandler.setComparator(eventNameColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return new NaturalComparator().compare(e1.getName(), e2.getName());
            }
        });
        eventsListHandler.setComparator(eventVenueColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return e1.venue.getName().compareTo(e2.venue.getName());
            }
        });
        eventsListHandler.setComparator(eventStartDateColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                Date d1 = e1.leaderboardGroup != null ? e1.leaderboardGroup.getGroupStartDate() : null;
                Date d2 = e2.leaderboardGroup != null ? e2.leaderboardGroup.getGroupStartDate() : null;
                if (d1 == null || d2 == null) {
                    return d1 == null && d2 == null ? 0 : d1 == null ? -1 : 1;
                } else {
                    return d1.compareTo(d2);
                }
            }
        });
        eventsTable.addColumnSortHandler(eventsListHandler);
        
        return eventsPanel;
    }

    private Panel createEventDetailsGUI(LeaderboardGroupOverviewTableResources tableResources) {
        eventDetailsPanel = new FlowPanel();
        eventDetailsPanel.setStyleName(STYLE_NAME_PREFIX + "eventDetailsPanel");
        eventDetailsPanel.setVisible(false);
        
        FlowPanel header = new FlowPanel();
        eventDetailsPanel.add(header);
        Label headerLabel = new Label(stringMessages.details());
        headerLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        header.add(headerLabel);
        
        //Build group details GUI
        FlowPanel detailsPanel = new FlowPanel();
        detailsPanel.setVisible(false);
        detailsPanel.setSize("100%", "100%");
        detailsPanel.setStyleName(STYLE_NAME_PREFIX + "detailsPanel");
        eventDetailsPanel.add(detailsPanel);
        
        eventDescriptionHTML = new HTML();
        eventDescriptionHTML.setStyleName(STYLE_NAME_PREFIX + "eventDescription");
        detailsPanel.add(eventDescriptionHTML);
        
        Label eventDetailsLabel = new Label(stringMessages.leaderboards()+ ": ");
        eventDetailsLabel.getElement().getStyle().setPadding(5, Unit.PX);
        detailsPanel.add(eventDetailsLabel);
        
        TextColumn<StrippedLeaderboardDTO> leaderboardsLocationColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                PlacemarkOrderDTO leaderboardPlaces = leaderboard.getPlaces();
                return leaderboardPlaces == null ? EventOverviewPanel.this.stringMessages
                        .notAvailable() : leaderboardPlaces.placemarksAsString();
            }
        };
        
        AnchorCell leaderboardsNameAnchorCell = new AnchorCell();
        Column<StrippedLeaderboardDTO, SafeHtml> leaderboardsNameColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(leaderboardsNameAnchorCell) {
            @Override
            public SafeHtml getValue(StrippedLeaderboardDTO leaderboard) {
                EventDTO selectedGroup = eventSelectionModel.getSelectedObject();
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLEncoder.encode("/gwt/Leaderboard.html?name=" + leaderboard.name
                        + (showRaceDetails ? "&showRaceDetails=true" : "")
                        + "&leaderboardGroupName=" + selectedGroup.getName() + "&root=overview"
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                return ANCHORTEMPLATE.anchor(link, leaderboard.name);
            }
        };
        
        TextColumn<StrippedLeaderboardDTO> leaderboardsRacesColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (RaceColumnDTO race : leaderboard.getRaceList()) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(race.getRaceColumnName());
                    first = false;
                }
                return sb.toString();
            }
        };
        
        TextColumn<StrippedLeaderboardDTO> leaderboardsStartDateColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                Date leaderboardStart = leaderboard.getStartDate();
                return leaderboardStart == null ? EventOverviewPanel.this.stringMessages.untracked()
                        : DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(leaderboardStart);
            }
        };
        
        leaderboardsTable = new CellTable<StrippedLeaderboardDTO>(10000, tableResources);
        leaderboardsTable.setWidth("100%");
        leaderboardsSelectionModel = new SingleSelectionModel<StrippedLeaderboardDTO>();
        leaderboardsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                onLeaderboardSelectionChanged();
            }
        });
        leaderboardsTable.setSelectionModel(leaderboardsSelectionModel);
        
        leaderboardsTable.addColumn(leaderboardsLocationColumn, this.stringMessages.location());
        leaderboardsTable.addColumn(leaderboardsNameColumn, this.stringMessages.name());
        leaderboardsTable.addColumn(leaderboardsRacesColumn, this.stringMessages.races());
        leaderboardsTable.addColumn(leaderboardsStartDateColumn, this.stringMessages.startDate());
        detailsPanel.add(leaderboardsTable);
        
        leaderboardsDataProvider = new ListDataProvider<StrippedLeaderboardDTO>();
        leaderboardsDataProvider.addDataDisplay(leaderboardsTable);

        //Build leaderboard details GUI
        leaderboardDetailsPanel = new FlowPanel();
        leaderboardDetailsPanel.setSize("100%", "100%");
        leaderboardDetailsPanel.setStyleName(STYLE_NAME_PREFIX + "leaderboardDetailsPanel");
        leaderboardDetailsPanel.setVisible(false);
        leaderboardDetailsPanel.getElement().setPropertyString("clear", "right");
        eventDetailsPanel.add(leaderboardDetailsPanel);
        
        Label leaderboardDetailsLabel = new Label(stringMessages.racesInLeaderboard() + ":");
        leaderboardDetailsLabel.getElement().getStyle().setPadding(5, Unit.PX);
        leaderboardDetailsPanel.add(leaderboardDetailsLabel);
        
        TextColumn<RaceColumnDTO> racesLocationColumn = new TextColumn<RaceColumnDTO>() {
            @Override
            public String getValue(RaceColumnDTO race) {
                PlacemarkOrderDTO racePlaces = race.getPlaces();
                return racePlaces == null ? EventOverviewPanel.this.stringMessages.locationNotAvailable() : racePlaces.placemarksAsString();
            }
        };
        
        AnchorCell racesNameAnchorCell = new AnchorCell();
        Column<RaceColumnDTO, SafeHtml> racesNameColumn = new Column<RaceColumnDTO, SafeHtml>(racesNameAnchorCell) {
            @Override
            public SafeHtml getValue(RaceColumnDTO race) {
                SafeHtml name = null;
                Iterable<FleetDTO> fleets = race.getFleets();
                boolean singleFleet = Util.size(fleets) < 2;
                for (FleetDTO fleet : fleets) {
                    String raceDisplayName;
                    if (singleFleet) {
                        raceDisplayName = race.getRaceColumnName();
                    } else {
                        raceDisplayName = race.getRaceColumnName() + "("+fleet+")";
                    }
                    if (race.getRaceIdentifier(fleet) != null) {
                        EventDTO selectedGroup = eventSelectionModel.getSelectedObject();
                        StrippedLeaderboardDTO selectedLeaderboard = leaderboardsSelectionModel.getSelectedObject();
                        RegattaNameAndRaceName raceId = (RegattaNameAndRaceName) race.getRaceIdentifier(fleet);
                        String debugParam = Window.Location.getParameter("gwt.codesvr");
                        String link = URLEncoder.encode("/gwt/RaceBoard.html?leaderboardName="
                                + selectedLeaderboard.name + "&raceName=" + raceId.getRaceName() + "&regattaName="
                                + raceId.getRegattaName() + "&leaderboardGroupName=" + selectedGroup.getName()
                                + "&root=overview"
                                + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                        name = ANCHORTEMPLATE.anchor(link, raceDisplayName);
                    } else {
                        name = new SafeHtmlBuilder().appendHtmlConstant(raceDisplayName).toSafeHtml();
                    }
                }
                return name;
            }
        };
        
        TextColumn<RaceColumnDTO> racesStartDateColumn = new TextColumn<RaceColumnDTO>() {
            @Override
            public String getValue(RaceColumnDTO race) {
                StringBuilder result = new StringBuilder();
                boolean first = true;
                for (FleetDTO fleet : race.getFleets()) {
                    Date raceStart = race.getStartDate(fleet);
                    if (first) {
                        first = false;
                    } else {
                        result.append(", ");
                    }
                    result.append(raceStart == null ? EventOverviewPanel.this.stringMessages.untracked()
                            : DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(raceStart));
                }
                return result.toString();
            }
        };
        
        racesTable = new CellTable<RaceColumnDTO>(200, tableResources);
        racesTable.setWidth("100%");
        racesSelectionModel = new NoSelectionModel<RaceColumnDTO>();
        racesTable.setSelectionModel(racesSelectionModel);
        
        racesTable.addColumn(racesLocationColumn, this.stringMessages.location());
        racesTable.addColumn(racesNameColumn, this.stringMessages.name());
        racesTable.addColumn(racesStartDateColumn, this.stringMessages.startDate());
        leaderboardDetailsPanel.add(racesTable);
        
        racesDataProvider = new ListDataProvider<RaceColumnDTO>();
        racesDataProvider.addDataDisplay(racesTable);
        
        return eventDetailsPanel;
    }

    private void loadEvents() {
        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onSuccess(List<EventDTO> result) {
                final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                if (result != null && !result.isEmpty()) {
                    timerForClientServerOffset.adjustClientServerOffset(clientTimeWhenRequestWasSent,
                            result.get(result.size() - 1).getCurrentServerTime(), clientTimeWhenResponseWasReceived);
                }
                availableEvents = new ArrayList<EventDTO>(result);
                eventsDataProvider.getList().clear();
                eventsDataProvider.getList().addAll(availableEvents);
            }
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to obtain the data: " + t.getMessage());
            }
        });
    }
    
    private void onEventSelectionChanged() {
        EventDTO selectedEvent = eventSelectionModel.getSelectedObject();
        StrippedLeaderboardDTO selectedLeaderboard = leaderboardsSelectionModel.getSelectedObject();
        
        if (selectedEvent != null) {
            eventDetailsPanel.setVisible(true);
            if (selectedLeaderboard != null) {
                setLeaderboardDetailsVisible(selectedEvent.leaderboardGroup.leaderboards.contains(selectedLeaderboard));
            }
            fillEventDetails(selectedEvent);
        } else {
            eventDetailsPanel.setVisible(false);
            setLeaderboardDetailsVisible(false);
        }
    }
    
    private void fillEventDetails(EventDTO event) {
        if(event.leaderboardGroup != null) {
            eventDescriptionHTML.setHTML(new SafeHtmlBuilder().appendEscapedLines(event.leaderboardGroup.description).toSafeHtml());
            leaderboardsDataProvider.getList().clear();
            leaderboardsDataProvider.getList().addAll(event.leaderboardGroup.leaderboards);
        }
    }
    
    private void onLeaderboardSelectionChanged() {
        StrippedLeaderboardDTO selectedLeaderboard = leaderboardsSelectionModel.getSelectedObject();
        setLeaderboardDetailsVisible(selectedLeaderboard != null);
        if (selectedLeaderboard != null) {
            fillLeaderboardDetails(selectedLeaderboard);
        }
    }
    
    private void setLeaderboardDetailsVisible(boolean visible) {
        leaderboardDetailsPanel.setVisible(visible);
    }
    
    private void fillLeaderboardDetails(StrippedLeaderboardDTO leaderboard) {
        racesDataProvider.getList().clear();
        racesDataProvider.getList().addAll(leaderboard.getRaceList());
    }
    
    private void onCheckBoxLiveChange() {
        if (onlyLiveCheckBox.getValue()) {
            String today = DateTimeFormat.getFormat("dd.MM.yyyy").format(new Date()).toString();
            
            fromTextBox.setText(today);
            fromTextBox.setEnabled(false);
            untilTextBox.setText("");
            untilTextBox.setEnabled(false);
        } else {
            fromTextBox.setText("");
            fromTextBox.setEnabled(true);
            untilTextBox.setText("");
            untilTextBox.setEnabled(true);
        }
        onSearchCriteriaChange();
    }
    
    private void onSearchCriteriaChange() {
        //Get search criteria
        String location = locationTextBox.getText();
        String name = nameTextBox.getText();
        boolean onlyLive = onlyLiveCheckBox.getValue();
        
        Date from = null;
        Date until = null;
        try {
            from = DateTimeFormat.getFormat("dd.MM.yyyy").parse(fromTextBox.getText());
            until = DateTimeFormat.getFormat("dd.MM.yyyy").parse(untilTextBox.getText());
            //Adding 24 hours to until, so that it doesn't result in an empty table if 'from' and 'until' are equal.
            //Instead you'll have a 24 hour range
            long time = until.getTime() + 24 * 60 * 60 * 1000;
            until = new Date(time);
        } catch (IllegalArgumentException e) {}

        eventsDataProvider.getList().clear();
        for (EventDTO group : availableEvents) {
            if (checkSearchCriteria(group, location, name, onlyLive, from, until)) {
                eventsDataProvider.getList().add(group);
            }
        }
        //Now sort again according to selected criterion
        ColumnSortEvent.fire(eventsTable, eventsTable.getColumnSortList());
    }
    
    private boolean checkSearchCriteria(EventDTO event, String location, String name, boolean onlyLive, Date from, Date until) {
        boolean result = true;
        if (result && location != null && location.length() > 0) {
            result = textContainsStringsToCheck(
                    PlacemarkOrderDTO.placemarksOfAllOrderAsSeperatedString(event.leaderboardGroup.getGroupPlaces(), true),
                    location.split("\\s"));
        }
        if (result && name != null && name.length() > 0) {
            result = textContainsStringsToCheck(event.getName(), name.split("\\s"));
        }
        if (result && onlyLiveCheckBox.getValue()) {
            result = event.leaderboardGroup.hasLiveRace(timerForClientServerOffset.getLiveTimePointInMillis());
        }else if (result) {
            Date startDate = event.leaderboardGroup.getGroupStartDate();
            if (startDate != null) {
                if (from != null && until != null) {
                    result = from.before(startDate) && until.after(startDate);
                } else if (from != null) {
                    result = from.before(startDate);
                } else if (until != null) {
                    result = until.after(startDate);
                }
            }
        }
        
        return result;
    }
    
    private boolean textContainsStringsToCheck(String text, String[] stringsToCheck) {
        boolean contains = false;
        for (String stringToCheck : stringsToCheck) {
            contains = text.toLowerCase().contains(stringToCheck.toLowerCase());
            if (!contains) {
                break;
            }
        }
        return contains;
    }

}
