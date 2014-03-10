package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.NamedDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.adminconsole.DisablableCheckboxCell.IsEnabled;
import com.sap.sailing.gwt.ui.adminconsole.RaceColumnInLeaderboardDialog.RaceColumnDescriptor;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MarkedAsyncCallback;
import com.sap.sailing.gwt.ui.client.ParallelExecutionCallback;
import com.sap.sailing.gwt.ui.client.ParallelExecutionHolder;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLEncoder;
import com.sap.sailing.gwt.ui.client.shared.panels.LabeledAbstractFilterablePanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardViewConfiguration;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.ui.DataEntryDialog.DialogCallback;

public class LeaderboardConfigPanel extends FormPanel implements SelectedLeaderboardProvider, RegattaDisplayer, RaceSelectionChangeListener,
    TrackedRaceChangedListener {

    private final TrackedRacesListComposite trackedRacesListComposite;

    private final StringMessages stringMessages;

    private final SailingServiceAsync sailingService;

    private final ListDataProvider<StrippedLeaderboardDTO> leaderboardList;

    private final ListDataProvider<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceColumnAndFleetList;

    private final ErrorReporter errorReporter;

    private final CellTable<StrippedLeaderboardDTO> leaderboardTable;

    private Button leaderboardRemoveButton;

    private final CellTable<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceColumnTable;

    private RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceInLeaderboard;

    private final Button addRaceColumnsButton;

    private final Button columnMoveUpButton;
    private final Button columnMoveDownButton;

    private final CaptionPanel selectedLeaderBoardPanel;
    private final CaptionPanel trackedRacesCaptionPanel;
    private final List<RegattaDTO> allRegattas;

    private LabeledAbstractFilterablePanel<StrippedLeaderboardDTO> filterLeaderboardPanel;

    final SingleSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceColumnTableSelectionModel;

    private List<StrippedLeaderboardDTO> availableLeaderboardList;

    private final MultiSelectionModel<StrippedLeaderboardDTO> leaderboardSelectionModel;

    private final RaceSelectionProvider raceSelectionProvider;

    public static class AnchorCell extends AbstractCell<SafeHtml> {

        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml cell(String url, String displayName);
    }

    private static AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);

    private static class RaceColumnDTOAndFleetDTOWithNameBasedEquality extends Pair<RaceColumnDTO, FleetDTO> {
        private static final long serialVersionUID = -8742476113296862662L;

        public RaceColumnDTOAndFleetDTOWithNameBasedEquality(RaceColumnDTO a, FleetDTO b) {
            super(a, b);
        }

        @Override
        public int hashCode() {
            return getA().getName().hashCode() ^ getB().getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else {
                if (obj == null) {
                    return false;
                } else {
                    return equalNamesOrBothNull(getA(), ((RaceColumnDTOAndFleetDTOWithNameBasedEquality) obj).getA())
                            && equalNamesOrBothNull(getB(), ((RaceColumnDTOAndFleetDTOWithNameBasedEquality) obj).getB());
                }
            }
        }

        private boolean equalNamesOrBothNull(NamedDTO a, NamedDTO b) {
            if (a == null) {
                return b == null;
            } else {
                if (b == null) {
                    return false;
                } else {
                    return Util.equalsWithNull(a.getName(), b.getName());
                }
            }
        }
    }

    public LeaderboardConfigPanel(final SailingServiceAsync sailingService, AdminConsoleEntryPoint adminConsole,
            final ErrorReporter errorReporter, StringMessages theStringConstants, final boolean showRaceDetails) {
        this.stringMessages = theStringConstants;
        this.sailingService = sailingService;
        leaderboardList = new ListDataProvider<StrippedLeaderboardDTO>();
        allRegattas = new ArrayList<RegattaDTO>();
        raceColumnAndFleetList = new ListDataProvider<RaceColumnDTOAndFleetDTOWithNameBasedEquality>();
        this.errorReporter = errorReporter;
        this.availableLeaderboardList = new ArrayList<StrippedLeaderboardDTO>();
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        this.setWidget(mainPanel);

        //Create leaderboards list and functionality
        CaptionPanel leaderboardsCaptionPanel = new CaptionPanel(stringMessages.leaderboards());
        leaderboardsCaptionPanel.setStyleName("bold");
        leaderboardsCaptionPanel.setWidth("75%");
        mainPanel.add(leaderboardsCaptionPanel);

        VerticalPanel leaderboardsPanel = new VerticalPanel();
        leaderboardsCaptionPanel.add(leaderboardsPanel);

        HorizontalPanel leaderboardConfigControlsPanel = new HorizontalPanel();
        Label lblFilterEvents = new Label(stringMessages.filterLeaderboardsByName() + ": ");
        leaderboardConfigControlsPanel.setSpacing(5);

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        leaderboardTable = new CellTable<StrippedLeaderboardDTO>(/* pageSize */10000, tableRes);
        leaderboardTable.ensureDebugId("LeaderboardsCellTable");
        filterLeaderboardPanel = new LabeledAbstractFilterablePanel<StrippedLeaderboardDTO>(lblFilterEvents, availableLeaderboardList, leaderboardTable, leaderboardList) {
            @Override
            public List<String> getSearchableStrings(StrippedLeaderboardDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.name);
                strings.add(t.displayName);
                return strings;
            }
        };
        filterLeaderboardPanel.getTextBox().ensureDebugId("LeaderboardsFilterTextBox");
        
        leaderboardRemoveButton = new Button(stringMessages.remove());
        leaderboardRemoveButton.ensureDebugId("LeaderboardsRemoveButton");
        leaderboardRemoveButton.setEnabled(false);
        leaderboardRemoveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm(stringMessages.doYouReallyWantToRemoveLeaderboards())) {
                    removeLeaderboards(leaderboardSelectionModel.getSelectedSet());
                }
            }
        });
        leaderboardConfigControlsPanel.add(filterLeaderboardPanel);
        leaderboardConfigControlsPanel.add(leaderboardRemoveButton);
        leaderboardsPanel.add(leaderboardConfigControlsPanel);
        ListHandler<StrippedLeaderboardDTO> leaderboardColumnListHandler = new ListHandler<StrippedLeaderboardDTO>(
                leaderboardList.getList());

        AnchorCell anchorCell = new AnchorCell();
        Column<StrippedLeaderboardDTO, SafeHtml> linkColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(anchorCell) {
            @Override
            public SafeHtml getValue(StrippedLeaderboardDTO object) {
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLEncoder.encode("/gwt/Leaderboard.html?name=" + object.name
                        + (showRaceDetails ? "&showRaceDetails=true" : "")
                        + (object.displayName != null ? "&displayName="+object.displayName : "")
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                return ANCHORTEMPLATE.cell(link, object.name);
            }

        };
        linkColumn.setSortable(true);
        leaderboardColumnListHandler.setComparator(linkColumn, new Comparator<StrippedLeaderboardDTO>() {

            @Override
            public int compare(StrippedLeaderboardDTO o1, StrippedLeaderboardDTO o2) {
                boolean ascending = isSortedAscending();
                if (o1.name.equals(o2.name)) {
                    return 0;
                }
                int val = -1;
                val = (o1 != null && o2 != null && ascending) ? (o1.name.compareTo(o2.name)) : -(o2.name
                        .compareTo(o1.name));
                        
                return val;
            }

            private boolean isSortedAscending() {
                ColumnSortList sortList = leaderboardTable.getColumnSortList();
                return sortList.size() > 0 & sortList.get(0).isAscending();
            }
        });

        TextColumn<StrippedLeaderboardDTO> leaderboardDisplayNameColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                return leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : "";
            }
        };

        TextColumn<StrippedLeaderboardDTO> discardingOptionsColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                String result = "";
                if (leaderboard.discardThresholds != null) {
                    for (int discardThreshold : leaderboard.discardThresholds) {
                        result += discardThreshold + " ";
                    }
                }
                return result;
            }
        };

        TextColumn<StrippedLeaderboardDTO> leaderboardTypeColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                String result = leaderboard.type.isRegattaLeaderboard() ? "Regatta" : "Flexible";
                if (leaderboard.type.isMetaLeaderboard()) {
                    result += " , Meta";
                }
                return result;
            }
        };

        TextColumn<StrippedLeaderboardDTO> scoringSystemColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                return leaderboard.scoringScheme == null ? "" : ScoringSchemeTypeFormatter.format(leaderboard.scoringScheme, stringMessages);
            }
        };

        TextColumn<StrippedLeaderboardDTO> courseAreaColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                return leaderboard.defaultCourseAreaId == null ? "" : leaderboard.defaultCourseAreaName;
            }
        };

        ImagesBarColumn<StrippedLeaderboardDTO, LeaderboardConfigImagesBarCell> leaderboardActionColumn = new ImagesBarColumn<StrippedLeaderboardDTO, LeaderboardConfigImagesBarCell>(
                new LeaderboardConfigImagesBarCell(stringMessages));
        leaderboardActionColumn.setFieldUpdater(new FieldUpdater<StrippedLeaderboardDTO, String>() {
            @Override
            public void update(int index, StrippedLeaderboardDTO leaderboardDTO, String value) {
                if (LeaderboardConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveLeaderboard(leaderboardDTO.name))) {
                        removeLeaderboard(leaderboardDTO);
                    }
                } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    final String oldLeaderboardName = leaderboardDTO.name;
                    List<StrippedLeaderboardDTO> otherExistingLeaderboard = new ArrayList<StrippedLeaderboardDTO>();
                    otherExistingLeaderboard.addAll(availableLeaderboardList);
                    otherExistingLeaderboard.remove(leaderboardDTO);
                    if (leaderboardDTO.type.isMetaLeaderboard()) {
                        Window.alert(stringMessages.metaLeaderboardCannotBeChanged());
                    } else {
                        if (leaderboardDTO.type.isRegattaLeaderboard()) {
                            LeaderboardDescriptor descriptor = new LeaderboardDescriptor(leaderboardDTO.name,
                                    leaderboardDTO.displayName, /* scoring scheme provided by regatta */ null,
                                    leaderboardDTO.discardThresholds, leaderboardDTO.regattaName,
                                    leaderboardDTO.defaultCourseAreaId);
                            AbstractLeaderboardDialog dialog = new RegattaLeaderboardEditDialog(Collections
                                    .unmodifiableCollection(otherExistingLeaderboard), Collections.unmodifiableCollection(allRegattas),
                                    descriptor, stringMessages, errorReporter,
                                    new DialogCallback<LeaderboardDescriptor>() {
                                @Override
                                public void cancel() {
                                }

                                @Override
                                public void ok(LeaderboardDescriptor result) {
                                    updateLeaderboard(oldLeaderboardName, result);
                                }
                            });
                            dialog.show();
                        } else {
                            LeaderboardDescriptor descriptor = new LeaderboardDescriptor(leaderboardDTO.name, leaderboardDTO.displayName, leaderboardDTO.scoringScheme, leaderboardDTO.discardThresholds, leaderboardDTO.defaultCourseAreaId);
                            openUpdateFlexibleLeaderboardDialog(leaderboardDTO, otherExistingLeaderboard, leaderboardDTO.name, descriptor);
                        }
                    }
                } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT_SCORES.equals(value)) {
                    String debugParam = Window.Location.getParameter("gwt.codesvr");
                    Window.open("/gwt/LeaderboardEditing.html?name=" + leaderboardDTO.name
                            + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""), "_blank", null);
                } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT_COMPETITORS.equals(value)) {
                    EditCompetitorsDialog editCompetitorsDialog = new EditCompetitorsDialog(sailingService, leaderboardDTO.name, stringMessages, 
                            errorReporter, new DialogCallback<List<CompetitorDTO>>() {
                        @Override
                        public void cancel() {
                        }

                        @Override
                        public void ok(final List<CompetitorDTO> result) {
                        }
                    });
                    editCompetitorsDialog.show();
                    
                } else if (LeaderboardConfigImagesBarCell.ACTION_CONFIGURE_URL.equals(value)) {
                    openLeaderboardUrlConfigDialog(leaderboardDTO, stringMessages);
                } else if (LeaderboardConfigImagesBarCell.ACTION_EXPORT_XML.equals(value)) {
                    Window.open("/export/xml?domain=leaderboard&name=" + leaderboardDTO.name, "", null);
                }
            }
        });
        leaderboardTable.addColumn(linkColumn, stringMessages.name());
        leaderboardTable.addColumn(leaderboardDisplayNameColumn, stringMessages.displayName());
        leaderboardTable.addColumn(discardingOptionsColumn, stringMessages.discarding());
        leaderboardTable.addColumn(leaderboardTypeColumn, stringMessages.type());
        leaderboardTable.addColumn(scoringSystemColumn, stringMessages.scoringSystem());
        leaderboardTable.addColumn(courseAreaColumn, stringMessages.courseArea());
        leaderboardTable.addColumn(leaderboardActionColumn, stringMessages.actions());
        leaderboardTable.addColumnSortHandler(leaderboardColumnListHandler);
        leaderboardTable.setWidth("100%");
        leaderboardSelectionModel = new MultiSelectionModel<StrippedLeaderboardDTO>();
        leaderboardTable.setSelectionModel(leaderboardSelectionModel);
        leaderboardSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                leaderboardSelectionChanged();
            }
        });
        leaderboardList.addDataDisplay(leaderboardTable);
        leaderboardsPanel.add(leaderboardTable);
        
        HorizontalPanel leaderboardButtonPanel = new HorizontalPanel();
        leaderboardButtonPanel.setSpacing(5);
        leaderboardsPanel.add(leaderboardButtonPanel);
        Button createFlexibleLeaderboardBtn = new Button(stringMessages.createFlexibleLeaderboard() + "...");
        createFlexibleLeaderboardBtn.ensureDebugId("CreateFlexibleLeaderboardButton");
        leaderboardButtonPanel.add(createFlexibleLeaderboardBtn);
        createFlexibleLeaderboardBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                createFlexibleLeaderboard();
            }
        });

        Button createRegattaLeaderboardBtn = new Button(stringMessages.createRegattaLeaderboard() + "...");
        createRegattaLeaderboardBtn.ensureDebugId("CreateRegattaLeaderboardButton");
        leaderboardButtonPanel.add(createRegattaLeaderboardBtn);
        createRegattaLeaderboardBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                createRegattaLeaderboard();
            }
        });

        mainPanel.add(new Grid(1, 1));

        // caption panels for the selected leaderboard and tracked races
        HorizontalPanel splitPanel = new HorizontalPanel();
        splitPanel.ensureDebugId("LeaderboardDetailsPanel");
        mainPanel.add(splitPanel);

        selectedLeaderBoardPanel = new CaptionPanel(stringMessages.leaderboard());
        selectedLeaderBoardPanel.setWidth("50%");
        splitPanel.add(selectedLeaderBoardPanel);

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        selectedLeaderBoardPanel.setContentWidget(vPanel);

        trackedRacesCaptionPanel = new CaptionPanel(stringMessages.trackedRaces());
        trackedRacesCaptionPanel.setWidth("50%");
        splitPanel.add(trackedRacesCaptionPanel);

        VerticalPanel trackedRacesPanel = new VerticalPanel();
        trackedRacesPanel.setWidth("100%");
        trackedRacesCaptionPanel.setContentWidget(trackedRacesPanel);
        trackedRacesCaptionPanel.setStyleName("bold");

        raceSelectionProvider = new RaceSelectionModel();
        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, adminConsole,
                raceSelectionProvider, stringMessages, /* multiselection */false);
        trackedRacesListComposite.ensureDebugId("TrackedRacesListComposite");
        trackedRacesPanel.add(trackedRacesListComposite);
        trackedRacesListComposite.addTrackedRaceChangeListener(this);
        raceSelectionProvider.addRaceSelectionChangeListener(this);

        Button reloadAllRaceLogs = new Button(stringMessages.reloadAllRaceLogs());
        reloadAllRaceLogs.ensureDebugId("ReloadAllRaceLogsButton");
        reloadAllRaceLogs.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                StrippedLeaderboardDTO leaderboard = getSelectedLeaderboard();
                for (RaceColumnDTO column : leaderboard.getRaceList()) {
                    for (FleetDTO fleet : column.getFleets()) {
                        refreshRaceLog(column, fleet, false);
                    }
                }
                Window.alert(stringMessages.raceLogReloaded());
            }
        });
        vPanel.add(reloadAllRaceLogs);

        // ------------ races of the selected leaderboard ----------------
        AnchorCell raceAnchorCell = new AnchorCell();
        Column<RaceColumnDTOAndFleetDTOWithNameBasedEquality, SafeHtml> raceLinkColumn = new Column<RaceColumnDTOAndFleetDTOWithNameBasedEquality, SafeHtml>(raceAnchorCell) {
            @Override
            public SafeHtml getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality raceInLeaderboardDTOAndFleetName) {
                if (raceInLeaderboardDTOAndFleetName.getA().getRaceIdentifier(raceInLeaderboardDTOAndFleetName.getB()) != null) {
                    RegattaNameAndRaceName raceIdentifier = (RegattaNameAndRaceName) raceInLeaderboardDTOAndFleetName
                            .getA().getRaceIdentifier(raceInLeaderboardDTOAndFleetName.getB());
                    String debugParam = Window.Location.getParameter("gwt.codesvr");
                    String link = URLEncoder.encode("/gwt/RaceBoard.html?leaderboardName="
                            + getSelectedLeaderboard().name + "&raceName=" + raceIdentifier.getRaceName() + "&regattaName="
                            + raceIdentifier.getRegattaName()
                            + "&"+RaceBoardViewConfiguration.PARAM_CAN_REPLAY_DURING_LIVE_RACES + "=true"
                            + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                    return ANCHORTEMPLATE.cell(link, raceInLeaderboardDTOAndFleetName.getA().getRaceColumnName());
                } else {
                    return SafeHtmlUtils.fromString(raceInLeaderboardDTOAndFleetName.getA().getRaceColumnName());
                }
            }
        };
        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> fleetNameColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality object) {
                return object.getB().getName();
            }
        };
        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> explicitFactorColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality object) {
                return object.getA().getExplicitFactor() == null ? "" : object.getA().getExplicitFactor().toString();
            }
        };

        Column<RaceColumnDTOAndFleetDTOWithNameBasedEquality, Boolean> isMedalRaceCheckboxColumn = new Column<RaceColumnDTOAndFleetDTOWithNameBasedEquality, Boolean>(
                new DisablableCheckboxCell(new IsEnabled() {
                    @Override
                    public boolean isEnabled() {
                        return getSelectedLeaderboard() != null && !getSelectedLeaderboard().type.isRegattaLeaderboard();
                                
                    }
                })) {
            @Override
            public Boolean getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality race) {
                return race.getA().isMedalRace();
            }
        };
        isMedalRaceCheckboxColumn.setFieldUpdater(new FieldUpdater<RaceColumnDTOAndFleetDTOWithNameBasedEquality, Boolean>() {
            @Override
            public void update(int index, RaceColumnDTOAndFleetDTOWithNameBasedEquality object, Boolean value) {
                setIsMedalRace(getSelectedLeaderboard().name, object.getA(), value);
            }
        });
        isMedalRaceCheckboxColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> isLinkedRaceColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnAndFleetName) {
                boolean isTrackedRace = raceColumnAndFleetName.getA().isTrackedRace(raceColumnAndFleetName.getB());
                return isTrackedRace ? stringMessages.yes() : stringMessages.no();
            }
        };
        ImagesBarColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality, LeaderboardRaceConfigImagesBarCell> raceActionColumn =
                new ImagesBarColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality, LeaderboardRaceConfigImagesBarCell>(
                        new LeaderboardRaceConfigImagesBarCell(this, stringMessages));
        raceActionColumn.setFieldUpdater(new FieldUpdater<RaceColumnDTOAndFleetDTOWithNameBasedEquality, String>() {
            @Override
            public void update(int index, RaceColumnDTOAndFleetDTOWithNameBasedEquality object, String value) {
                if (LeaderboardRaceConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.reallyRemoveRace(object.getA().getRaceColumnName()))) {
                        removeRaceColumn(object.getA());
                    }
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    editRaceColumnOfLeaderboard(object);
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_UNLINK.equals(value)) {
                    unlinkRaceColumnFromTrackedRace(object.getA().getRaceColumnName(), object.getB());
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_REFRESH_RACELOG.equals(value)) {
                    refreshRaceLog(object.getA(), object.getB(), true);
                } else if(LeaderboardRaceConfigImagesBarCell.ACTION_SET_STARTTIME.equals(value)) {
                    setStartTime(object.getA(), object.getB());
                } else if(LeaderboardRaceConfigImagesBarCell.ACTION_SHOW_RACELOG.equals(value)) {
                    showRaceLog(object.getA(), object.getB());
                }
            }
        });
        Label lblRaceNamesIn = new Label(stringMessages.races());
        vPanel.add(lblRaceNamesIn);
        raceColumnTable = new CellTable<RaceColumnDTOAndFleetDTOWithNameBasedEquality>(/* pageSize */200, tableRes);
        raceColumnTable.ensureDebugId("RacesCellTable");
        raceColumnTable.addColumn(raceLinkColumn, stringMessages.name());
        raceColumnTable.addColumn(fleetNameColumn, stringMessages.fleet());
        raceColumnTable.addColumn(isMedalRaceCheckboxColumn, stringMessages.medalRace());
        raceColumnTable.addColumn(isLinkedRaceColumn, stringMessages.islinked());
        raceColumnTable.addColumn(explicitFactorColumn, stringMessages.factor());
        raceColumnTable.addColumn(raceActionColumn, stringMessages.actions());
        raceColumnAndFleetList.addDataDisplay(raceColumnTable);
        raceColumnTable.setWidth("500px");
        raceColumnTableSelectionModel = new SingleSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>();
        raceColumnTable.setSelectionModel(raceColumnTableSelectionModel);
        raceColumnTableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                leaderboardRaceColumnSelectionChanged();
            }
        });
        vPanel.add(raceColumnTable);

        HorizontalPanel selectedLeaderboardRaceButtonPanel = new HorizontalPanel();
        selectedLeaderboardRaceButtonPanel.setSpacing(5);
        vPanel.add(selectedLeaderboardRaceButtonPanel);

        addRaceColumnsButton = new Button(stringMessages.actionAddRaces() + "...");
        addRaceColumnsButton.ensureDebugId("AddRacesButton");
        addRaceColumnsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getSelectedLeaderboard().type.isRegattaLeaderboard()) {
                    Window.alert(stringMessages.cannotAddRacesToRegattaLeaderboardButOnlyToRegatta());
                } else {
                    addRaceColumnsToLeaderboard();
                }
            }
        });
        selectedLeaderboardRaceButtonPanel.add(addRaceColumnsButton);

        columnMoveUpButton = new Button(stringMessages.columnMoveUp());
        columnMoveUpButton.ensureDebugId("MoveRaceUpButton");
        columnMoveUpButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelectedRaceColumnUp();
            }
        });
        selectedLeaderboardRaceButtonPanel.add(columnMoveUpButton);

        columnMoveDownButton = new Button(stringMessages.columnMoveDown());
        columnMoveDownButton.ensureDebugId("MoveRaceDownButton");
        columnMoveDownButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelectedRaceColumnDown();
            }
        });
        selectedLeaderboardRaceButtonPanel.add(columnMoveDownButton);

        loadLeaderboards();
    }

    protected void openUpdateFlexibleLeaderboardDialog(final StrippedLeaderboardDTO leaderboardDTO, final List<StrippedLeaderboardDTO> otherExistingLeaderboard,
            final String oldLeaderboardName, final LeaderboardDescriptor descriptor) {
        sailingService.getEvents(new MarkedAsyncCallback<List<EventDTO>>(
                new AsyncCallback<List<EventDTO>>() {
                    @Override
                    public void onSuccess(List<EventDTO> result) {
                        openUpdateFlexibleLeaderboardDialog(leaderboardDTO, otherExistingLeaderboard, oldLeaderboardName,
                                descriptor, result);
                    }
        
                    @Override
                    public void onFailure(Throwable caught) {
                        openUpdateFlexibleLeaderboardDialog(leaderboardDTO, otherExistingLeaderboard, oldLeaderboardName,
                                descriptor, new ArrayList<EventDTO>());
                    }
                }));
    }

    protected void openUpdateFlexibleLeaderboardDialog(StrippedLeaderboardDTO leaderboardDTO, List<StrippedLeaderboardDTO> otherExistingLeaderboard, 
            final String oldLeaderboardName, LeaderboardDescriptor descriptor, List<EventDTO> existingEvents) {
        FlexibleLeaderboardEditDialog dialog = new FlexibleLeaderboardEditDialog(
                Collections.unmodifiableCollection(otherExistingLeaderboard), descriptor, stringMessages,
                Collections.unmodifiableList(existingEvents), errorReporter,
                new DialogCallback<LeaderboardDescriptor>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(LeaderboardDescriptor result) {
                        updateLeaderboard(oldLeaderboardName, result);
                    }
                });
        dialog.show();
    }

    /**
     * Allow the user to combine the various URL parameters that exist for the {@link LeaderboardEntryPoint} and obtain the
     * resulting URL in a link. The link's reference target is updated dynamically as the user adjusts the settings. Therefore,
     * the link can be clicked, bookmarked or copied to the clipboard at any time. The OK / Cancel actions for the dialog shown
     * are no-ops.
     */
    private void openLeaderboardUrlConfigDialog(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        LeaderboardEntryPoint.getUrlConfigurationDialog(leaderboard, stringMessages).show();
    }

    public void loadLeaderboards() {
        sailingService.getLeaderboards(new MarkedAsyncCallback<List<StrippedLeaderboardDTO>>(
                new AsyncCallback<List<StrippedLeaderboardDTO>>() {
                    @Override
                    public void onSuccess(List<StrippedLeaderboardDTO> leaderboards) {
                        leaderboardList.getList().clear();
                        availableLeaderboardList.clear();
                        leaderboardList.getList().addAll(leaderboards);
                        availableLeaderboardList.addAll(leaderboards);
                        filterLeaderboardPanel.updateAll(availableLeaderboardList);
                        leaderboardSelectionChanged();
                        leaderboardRaceColumnSelectionChanged();
                    }
        
                    @Override
                    public void onFailure(Throwable t) {
                        LeaderboardConfigPanel.this.errorReporter.reportError("Error trying to obtain list of leaderboards: "
                                + t.getMessage());
                    }
                }));
    }

    /**
     * @param nameOfRaceColumnToSelect
     *            if not <code>null</code>, selects the first race column name with this name found in the leaderboard
     *            after the refresh has successfully completed. See {@link #selectRaceColumn(String)}.
     */
    public void loadAndRefreshLeaderboard(final String leaderboardName, final String nameOfRaceColumnToSelect) {
        sailingService.getLeaderboard(leaderboardName, new MarkedAsyncCallback<StrippedLeaderboardDTO>(
                new AsyncCallback<StrippedLeaderboardDTO>() {
                        @Override
                        public void onSuccess(StrippedLeaderboardDTO leaderboard) {
                            for (StrippedLeaderboardDTO leaderboardDTO : leaderboardSelectionModel.getSelectedSet()) {
                                if (leaderboardDTO.name.equals(leaderboardName)) {
                                    leaderboardSelectionModel.setSelected(leaderboardDTO, false);
                                    break;
                                }
                            }
            
                            replaceLeaderboardInList(leaderboardList.getList(), leaderboardName, leaderboard);
                            replaceLeaderboardInList(availableLeaderboardList, leaderboardName, leaderboard);
                            leaderboardSelectionModel.setSelected(leaderboard, true);
                            if (nameOfRaceColumnToSelect != null) {
                                selectRaceColumn(nameOfRaceColumnToSelect);
                            }
                            leaderboardSelectionChanged();
                        }
            
                        @Override
                        public void onFailure(Throwable t) {
                            LeaderboardConfigPanel.this.errorReporter.reportError("Error trying to update leaderboard with name " + leaderboardName + " : "
                                    + t.getMessage());
                        }
                }));
    }

    private void replaceLeaderboardInList(List<StrippedLeaderboardDTO> leaderboardList, String leaderboardToReplace, StrippedLeaderboardDTO newLeaderboard) {
        int index = -1;
        for (StrippedLeaderboardDTO existingLeaderboard : leaderboardList) {
            index++;
            if (existingLeaderboard.name.equals(leaderboardToReplace)) {
                break;
            }
        }
        if (index >= 0) {
            leaderboardList.set(index, newLeaderboard);
        }
    }

    private void unlinkRaceColumnFromTrackedRace(final String raceColumnName, final FleetDTO fleet) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        sailingService.disconnectLeaderboardColumnFromTrackedRace(selectedLeaderboardName, raceColumnName, fleet.getName(),
                new MarkedAsyncCallback<Void>(
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError("Error trying to unlink tracked race from column " + raceColumnName
                                        + " from leaderboard " + selectedLeaderboardName + ": " + t.getMessage());
                            }
                
                            @Override
                            public void onSuccess(Void arg0) {
                                trackedRacesListComposite.clearSelection();
                                getSelectedRaceColumnWithFleet().getA().setRaceIdentifier(fleet, null);
                                raceColumnAndFleetList.refresh();
                            }
                        }));
    }

    private void setStartTime(RaceColumnDTO raceColumnDTO, FleetDTO fleetDTO) {
        new SetStartTimeDialog(sailingService, errorReporter, getSelectedLeaderboardName(), raceColumnDTO.getName(), 
                fleetDTO.getName(), stringMessages, new DialogCallback<RaceLogSetStartTimeDTO>() {
            @Override
            public void ok(RaceLogSetStartTimeDTO editedObject) {
                sailingService.setStartTimeAndProcedure(editedObject, new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (!result) {
                            Window.alert(stringMessages.failedToSetNewStartTime());
                        }
                    }
                });
            }

            @Override
            public void cancel() { }
        }).show();
    }

    private void showRaceLog(final RaceColumnDTO raceColumnDTO, final FleetDTO fleetDTO) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        sailingService.getRaceLog(selectedLeaderboardName, raceColumnDTO, fleetDTO,
                new MarkedAsyncCallback<RaceLogDTO>(
                        new AsyncCallback<RaceLogDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(caught.getMessage(), true);
                            }
                            @Override
                            public void onSuccess(RaceLogDTO result) {
                                openRaceLogDialog(result);
                            }
                        }));
    }
    
    private void openRaceLogDialog(RaceLogDTO raceLogDTO) {
        RaceLogDialog dialog = new RaceLogDialog(raceLogDTO, stringMessages, new DialogCallback<RaceLogDTO>() { 
            @Override
            public void cancel() {
            }

            @Override
            public void ok(RaceLogDTO result) {
            }
        });
        dialog.show();
    }

    private void refreshRaceLog(final RaceColumnDTO raceColumnDTO, final FleetDTO fleet, final boolean showAlerts) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        sailingService.reloadRaceLog(selectedLeaderboardName, raceColumnDTO, fleet, new MarkedAsyncCallback<Void>(
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (showAlerts) {
                            errorReporter.reportError(caught.getMessage());
                        }
                    }
                    @Override
                    public void onSuccess(Void result) {
                        if (showAlerts) {
                            Window.alert(stringMessages.raceLogReloaded());
                        }
                    }
                }));
    }

    private void removeRaceColumn(final RaceColumnDTO raceColumnDTO) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String raceColumnString = raceColumnDTO.getRaceColumnName();
        sailingService.removeLeaderboardColumn(getSelectedLeaderboardName(), raceColumnString,
                new MarkedAsyncCallback<Void>(
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError("Error trying to remove leaderboard race column " + raceColumnDTO
                                        + " in leaderboard " + getSelectedLeaderboardName() + ": " + t.getMessage());
                            }
                
                            @Override
                            public void onSuccess(Void arg0) {
                                loadAndRefreshLeaderboard(selectedLeaderboardName, /* raceColumnNameToSelect */ null);
                            }
                        }));
    }

    /**
     * The selected row is potentially only one of several fleet-based rows of the same RaceColumn. In this case,
     * move all fleet-based rows of the same RaceColumn down.
     */
    private void moveSelectedRaceColumnDown() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String selectedRaceColumnName = raceColumnTableSelectionModel.getSelectedObject().getA().getRaceColumnName();
        sailingService.moveLeaderboardColumnDown(getSelectedLeaderboardName(), selectedRaceColumnName,
                new MarkedAsyncCallback<Void>(
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to move leaderboard race column "
                                        + selectedRaceColumnName + " in leaderboard " + getSelectedLeaderboardName()
                                        + " down: " + caught.getMessage());
                            }
                
                            @Override
                            public void onSuccess(Void result) {
                                loadAndRefreshLeaderboard(selectedLeaderboardName, selectedRaceColumnName);
                            }
                        }));
    }

    /**
     * The selected row is potentially only one of several fleet-based rows of the same RaceColumn. In this case,
     * move all fleet-based rows of the same RaceColumn down.
     */
    private void moveSelectedRaceColumnUp() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String selectedRaceColumnName = raceColumnTableSelectionModel.getSelectedObject().getA().getRaceColumnName();
        sailingService.moveLeaderboardColumnUp(getSelectedLeaderboardName(), selectedRaceColumnName,
                new MarkedAsyncCallback<Void>(
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to move leaderboard race column "
                                        + selectedRaceColumnName + " in leaderboard " + getSelectedLeaderboardName() + " up: "
                                        + caught.getMessage());
                            }
                
                            @Override
                            public void onSuccess(Void result) {
                                loadAndRefreshLeaderboard(selectedLeaderboardName, selectedRaceColumnName);
                            }
                        }));
    }

    private void leaderboardRaceColumnSelectionChanged() {
        selectedRaceInLeaderboard = getSelectedRaceColumnWithFleet();
        if (selectedRaceInLeaderboard != null) {
            columnMoveUpButton.setEnabled(true);
            columnMoveDownButton.setEnabled(true);
            selectTrackedRaceInRaceList();
        } else {
            columnMoveUpButton.setEnabled(false);
            columnMoveDownButton.setEnabled(false);
            trackedRacesListComposite.clearSelection();
        }
    }

    private void selectRaceColumn(String raceCoumnName) {
        List<RaceColumnDTOAndFleetDTOWithNameBasedEquality> list = raceColumnAndFleetList.getList();
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality pair : list) {
            if (pair.getA().getName().equals(raceCoumnName)) {
                raceColumnTableSelectionModel.setSelected(pair, true);
                break;
            }
        }
    }

    private void selectTrackedRaceInRaceList() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        if (selectedLeaderboardName != null) {
            final RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceColumnAndFleetNameInLeaderboard = getSelectedRaceColumnWithFleet();
            final String selectedRaceColumnName = selectedRaceColumnAndFleetNameInLeaderboard.getA().getRaceColumnName();
            final String selectedFleetName = selectedRaceColumnAndFleetNameInLeaderboard.getB().getName();
            sailingService.getRegattaAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(selectedLeaderboardName,
                    selectedRaceColumnName, new MarkedAsyncCallback<Map<String, RegattaAndRaceIdentifier>>(
                            new AsyncCallback<Map<String, RegattaAndRaceIdentifier>>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    errorReporter.reportError("Error trying to determine tracked race linked to race column "
                                            + selectedRaceColumnName + " in leaderboard " + selectedLeaderboardName + ": "
                                            + t.getMessage());
                                }
                                
                                @Override
                                public void onSuccess(Map<String, RegattaAndRaceIdentifier> regattaAndRaceNamesPerFleet) {
                                    if (regattaAndRaceNamesPerFleet != null && !regattaAndRaceNamesPerFleet.isEmpty()) {
                                        RegattaAndRaceIdentifier raceIdentifier = regattaAndRaceNamesPerFleet.get(selectedFleetName);
                                        
                                        if (raceIdentifier != null) {
                                            selectRaceInList(raceIdentifier.getRegattaName(), raceIdentifier.getRaceName());
                                        } else {
                                            trackedRacesListComposite.clearSelection();
                                        }
                                    } else {
                                        trackedRacesListComposite.clearSelection();
                                    }
                                }
                            }));
        }
    }

    private void selectRaceInList(String regattaName, String raceName) {
        RegattaNameAndRaceName raceIdentifier = new RegattaNameAndRaceName(regattaName, raceName);
        trackedRacesListComposite.selectRaceByIdentifier(raceIdentifier);
    }

    private RaceColumnDTOAndFleetDTOWithNameBasedEquality getSelectedRaceColumnWithFleet() {
        RaceColumnDTOAndFleetDTOWithNameBasedEquality raceInLeaderboardAndFleetName = raceColumnTableSelectionModel.getSelectedObject();
        return raceInLeaderboardAndFleetName;
    }

    private void editRaceColumnOfLeaderboard(final RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnWithFleet) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final boolean oldIsMedalRace = raceColumnWithFleet.getA().isMedalRace();
        final String oldRaceColumnName = raceColumnWithFleet.getA().getRaceColumnName();
        final Double oldExplicitFactor = raceColumnWithFleet.getA().getExplicitFactor();
        // use a set to avoid duplicates in the case of regatta leaderboards with multiple fleets per column
        Set<RaceColumnDTO> existingRacesWithoutThisRace = new HashSet<RaceColumnDTO>();
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality pair : raceColumnAndFleetList.getList()) {
            existingRacesWithoutThisRace.add(pair.getA());
        }
        existingRacesWithoutThisRace.remove(raceColumnWithFleet.getA());
        final RaceColumnInLeaderboardDialog raceDialog = new RaceColumnInLeaderboardDialog(existingRacesWithoutThisRace,
                raceColumnWithFleet.getA(), getSelectedLeaderboard().type.isRegattaLeaderboard(), stringMessages, new DialogCallback<RaceColumnDescriptor>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(final RaceColumnDescriptor result) {
                boolean rename = !oldRaceColumnName.equals(result.getName());
                boolean updateIsMedalRace = oldIsMedalRace != result.isMedalRace();
                boolean updateFactor = oldExplicitFactor != result.getExplicitFactor();
                List<ParallelExecutionCallback<Void>> callbacks = new ArrayList<ParallelExecutionCallback<Void>>();
                final ParallelExecutionCallback<Void> renameLeaderboardColumnCallback = new ParallelExecutionCallback<Void>();
                if (rename) {
                    callbacks.add(renameLeaderboardColumnCallback);
                }
                final ParallelExecutionCallback<Void> updateIsMedalRaceCallback = new ParallelExecutionCallback<Void>();
                if (updateIsMedalRace) {
                    callbacks.add(updateIsMedalRaceCallback);
                }
                final ParallelExecutionCallback<Void> updateLeaderboardColumnFactorCallback = new ParallelExecutionCallback<Void>();
                if (updateFactor) {
                    callbacks.add(updateLeaderboardColumnFactorCallback);
                }
                new ParallelExecutionHolder(callbacks.toArray(new ParallelExecutionCallback<?>[0])) {
                    @Override
                    public void handleSuccess() {
                        loadAndRefreshLeaderboard(selectedLeaderboardName, result.getName());
                    }
                    @Override
                    public void handleFailure(Throwable t) {
                        errorReporter.reportError("Error trying to update data of race column "
                                + oldRaceColumnName + " in leaderboard " + selectedLeaderboardName + ": "
                                + t.getMessage());
                    }
                };
                if (rename) {
                    sailingService.renameLeaderboardColumn(selectedLeaderboardName, oldRaceColumnName,
                            result.getName(), renameLeaderboardColumnCallback);
                }
                if (updateIsMedalRace) {
                    sailingService.updateIsMedalRace(selectedLeaderboardName, result.getName(),
                            result.isMedalRace(), updateIsMedalRaceCallback);
                }
                if (updateFactor) {
                    sailingService.updateLeaderboardColumnFactor(selectedLeaderboardName, result.getName(),
                            result.getExplicitFactor(), updateLeaderboardColumnFactorCallback);
                }
            }
        });
        raceDialog.show();
    }

    private void setIsMedalRace(String leaderboardName, final RaceColumnDTO raceInLeaderboard,
            final boolean isMedalRace) {
        sailingService.updateIsMedalRace(leaderboardName, raceInLeaderboard.getRaceColumnName(), isMedalRace,
                new MarkedAsyncCallback<Void>(
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.errorUpdatingIsMedalRace(caught.getMessage()));
                            }
                            
                            @Override
                            public void onSuccess(Void result) {
                                getSelectedLeaderboard().setIsMedalRace(raceInLeaderboard.getRaceColumnName(), isMedalRace);
                            }
                        }));
    }

    private void addRaceColumnsToLeaderboard() {
        final String leaderboardName = getSelectedLeaderboardName();
        final List<RaceColumnDTO> existingRaceColumns = new ArrayList<RaceColumnDTO>();
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality pair : raceColumnAndFleetList.getList()) {
            existingRaceColumns.add(pair.getA());
        }
        final RaceColumnsInLeaderboardDialog raceDialog = new RaceColumnsInLeaderboardDialog(existingRaceColumns,
                stringMessages, new DialogCallback<List<RaceColumnDTO>>() {
            @Override
            public void cancel() {
            }
            
            @Override
            public void ok(final List<RaceColumnDTO> result) {
                updateRaceColumnsOfLeaderboard(leaderboardName, existingRaceColumns, result);
            }
        });
        raceDialog.ensureDebugId("RaceColumnsInLeaderboardDialog");
        raceDialog.show();
    }

    private void updateRaceColumnsOfLeaderboard(final String leaderboardName, List<RaceColumnDTO> existingRaceColumns, List<RaceColumnDTO> newRaceColumns) {
        final List<Pair<String, Boolean>> raceColumnsToAdd = new ArrayList<Pair<String, Boolean>>();

        for (RaceColumnDTO newRaceColumn : newRaceColumns) {
            if (!existingRaceColumns.contains(newRaceColumn)) {
                raceColumnsToAdd.add(new Pair<String, Boolean>(newRaceColumn.getName(), newRaceColumn.isMedalRace()));
            }
        }

        sailingService.addColumnsToLeaderboard(leaderboardName, raceColumnsToAdd, new MarkedAsyncCallback<Void>(
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to add race columns to leaderboard " + leaderboardName
                                + ": " + caught.getMessage());
                    }
        
                    @Override
                    public void onSuccess(Void v) {
                        loadAndRefreshLeaderboard(leaderboardName, /* nameOfRaceColumnToSelect */ null);
                    }
                }));
    }

    private String getSelectedLeaderboardName() {
        return getSelectedLeaderboard() != null ? getSelectedLeaderboard().name : null;
    }

    private void leaderboardSelectionChanged() {
        // make sure that clearing the selection doesn't cause an unlinking of the selected tracked race
        raceSelectionProvider.removeRaceSelectionChangeListener(this);
        trackedRacesListComposite.clearSelection();
        // add listener again using a scheduled command which is executed when the browser's event loop re-gains
        // control; we assume that at that point in time the selection updates have already been performed
        Scheduler.get().scheduleFinally(new ScheduledCommand() {
            @Override
            public void execute() {
                raceSelectionProvider.addRaceSelectionChangeListener(LeaderboardConfigPanel.this);
            }
        });
        leaderboardRemoveButton.setEnabled(!leaderboardSelectionModel.getSelectedSet().isEmpty());
        StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard();
        if (leaderboardSelectionModel.getSelectedSet().size() == 1 && selectedLeaderboard != null) {
            raceColumnAndFleetList.getList().clear();
            for (RaceColumnDTO raceColumn : selectedLeaderboard.getRaceList()) {
                for (FleetDTO fleet : raceColumn.getFleets()) {
                    raceColumnAndFleetList.getList().add(new RaceColumnDTOAndFleetDTOWithNameBasedEquality(raceColumn, fleet));
                }
            }
            selectedLeaderBoardPanel.setVisible(true);
            selectedLeaderBoardPanel.setCaptionText("Details of leaderboard '" + selectedLeaderboard.name + "'");
            if (!selectedLeaderboard.type.isMetaLeaderboard()) {
                trackedRacesCaptionPanel.setVisible(true);
            }
            addRaceColumnsButton.setVisible(!selectedLeaderboard.type.isRegattaLeaderboard());
            columnMoveUpButton.setVisible(!selectedLeaderboard.type.isRegattaLeaderboard());
            columnMoveDownButton.setVisible(!selectedLeaderboard.type.isRegattaLeaderboard());
        } else {
            selectedLeaderBoardPanel.setVisible(false);
            trackedRacesCaptionPanel.setVisible(false);
            selectedRaceInLeaderboard = null;
        }
    }

    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        trackedRacesListComposite.fillRegattas(regattas);

        allRegattas.clear();
        allRegattas.addAll(regattas);
    }

    @Override
    public void changeTrackingRace(Iterable<? extends RegattaAndRaceIdentifier> regattaAndRaceIdentifiers, boolean isTracked) {
        for (RegattaAndRaceIdentifier regattaAndRaceIdentifier : regattaAndRaceIdentifiers) {
            for (RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnAndFleetName : raceColumnAndFleetList.getList()) {
                if (raceColumnAndFleetName.getA().getRaceColumnName().equals(regattaAndRaceIdentifier.getRaceName())) {
                    raceColumnAndFleetName.getA().setRaceIdentifier(raceColumnAndFleetName.getB(),
                            regattaAndRaceIdentifier);
                }
            }
            raceColumnAndFleetList.refresh();
        }
    }

    private void createFlexibleLeaderboard() {
        sailingService.getEvents(new MarkedAsyncCallback<List<EventDTO>>(
                new AsyncCallback<List<EventDTO>>() {
                    @Override
                    public void onSuccess(List<EventDTO> result) {
                        createFlexibleLeaderboard(result);
                    }
        
                    @Override
                    public void onFailure(Throwable caught) {
                        createFlexibleLeaderboard(new ArrayList<EventDTO>());
                    }
                }));
    }

    private void createFlexibleLeaderboard(List<EventDTO> existingEvents) {

        AbstractLeaderboardDialog dialog = new FlexibleLeaderboardCreateDialog(Collections.unmodifiableCollection(availableLeaderboardList),
                stringMessages, Collections.unmodifiableCollection(existingEvents), errorReporter, new DialogCallback<LeaderboardDescriptor>() {
            @Override
            public void cancel() {
            }
            
            @Override
            public void ok(final LeaderboardDescriptor newLeaderboard) {
                sailingService.createFlexibleLeaderboard(newLeaderboard.getName(), newLeaderboard.getDisplayName(),
                        newLeaderboard.getDiscardThresholds(), newLeaderboard.getScoringScheme(), newLeaderboard.getCourseAreaId(),
                        new MarkedAsyncCallback<StrippedLeaderboardDTO>(
                                new AsyncCallback<StrippedLeaderboardDTO>() {
                                    @Override
                                    public void onFailure(Throwable t) {
                                        errorReporter.reportError("Error trying to create the new flexible leaderboard " + newLeaderboard.getName()
                                                + ": " + t.getMessage());
                                    }
                                    
                                    @Override
                                    public void onSuccess(StrippedLeaderboardDTO result) {
                                        addLeaderboard(result);
                                    }
                                }));
            }
        });
        dialog.ensureDebugId("FlexibleLeaderboardCreateDialog");
        dialog.show();
    }

    private void createRegattaLeaderboard() {
        RegattaLeaderboardCreateDialog dialog = new RegattaLeaderboardCreateDialog(Collections.unmodifiableCollection(availableLeaderboardList),
                Collections.unmodifiableCollection(allRegattas), stringMessages, errorReporter, new DialogCallback<LeaderboardDescriptor>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(final LeaderboardDescriptor newLeaderboard) {
                RegattaIdentifier regattaIdentifier = new RegattaName(newLeaderboard.getRegattaName());
                sailingService.createRegattaLeaderboard(regattaIdentifier, newLeaderboard.getDisplayName(), newLeaderboard.getDiscardThresholds(),
                        new AsyncCallback<StrippedLeaderboardDTO>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to create the new regatta leaderboard " + newLeaderboard.getName()
                                + ": " + t.getMessage());
                    }
                    
                    @Override
                    public void onSuccess(StrippedLeaderboardDTO result) {
                        addLeaderboard(result);
                    }
                });
            }
        });
        dialog.ensureDebugId("RegattaLeaderboardCreateDialog");
        dialog.show();
    }

    private void addLeaderboard(StrippedLeaderboardDTO result) {
        leaderboardList.getList().add(result);
        availableLeaderboardList.add(result);
        leaderboardSelectionModel.clear();
        leaderboardSelectionModel.setSelected(result, true);
    }

    private void updateLeaderboard(final String oldLeaderboardName, final LeaderboardDescriptor leaderboardToUpdate) {
        sailingService.updateLeaderboard(oldLeaderboardName, leaderboardToUpdate.getName(), leaderboardToUpdate.getDisplayName(),
                leaderboardToUpdate.getDiscardThresholds(), leaderboardToUpdate.getCourseAreaId(),
                new MarkedAsyncCallback<StrippedLeaderboardDTO>(
                        new AsyncCallback<StrippedLeaderboardDTO>() {
                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError("Error trying to update leaderboard " + oldLeaderboardName + ": "
                                        + t.getMessage());
                            }
                            
                            @Override
                            public void onSuccess(StrippedLeaderboardDTO updatedLeaderboard) {
                                int indexOfLeaderboard = 0;
                                for (int i = 0; i < leaderboardList.getList().size(); i++) {
                                    StrippedLeaderboardDTO dao = leaderboardList.getList().get(i);
                                    if (dao.name.equals(oldLeaderboardName)) {
                                        indexOfLeaderboard = i;
                                        break;
                                    }
                                }
                                leaderboardList.getList().set(indexOfLeaderboard, updatedLeaderboard);
                                leaderboardList.refresh();
                            }
                        }));
    }

    private void removeLeaderboards(final Collection<StrippedLeaderboardDTO> leaderboards) {
        if (!leaderboards.isEmpty()) {
            Set<String> leaderboardNames = new HashSet<String>();
            for (StrippedLeaderboardDTO leaderboard : leaderboards) {
                leaderboardNames.add(leaderboard.name);
            }
            sailingService.removeLeaderboards(leaderboardNames, new MarkedAsyncCallback<Void>(
                    new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error trying to remove the leaderboards:" + caught.getMessage());
                                   
        
                        }
        
                        @Override
                        public void onSuccess(Void result) {
                            for (StrippedLeaderboardDTO leaderboard : leaderboards) {
                                removeLeaderboardFromTable(leaderboard);
                            }
                        }
                    }));
        }
    }

    private void removeLeaderboard(final StrippedLeaderboardDTO leaderBoard) {
        sailingService.removeLeaderboard(leaderBoard.name, new MarkedAsyncCallback<Void>(
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to remove leaderboard " + leaderBoard.name + ": "
                                + caught.getMessage());
                    }
        
                    @Override
                    public void onSuccess(Void result) {
                        removeLeaderboardFromTable(leaderBoard);
                    }
                }));
    }

    private void removeLeaderboardFromTable(final StrippedLeaderboardDTO leaderBoard) {
        leaderboardList.getList().remove(leaderBoard);
        availableLeaderboardList.remove(leaderBoard);
        leaderboardSelectionModel.setSelected(leaderBoard, false);
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        // if no leaderboard column is selected, ignore the race selection change
        RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceColumnAndFleetName = getSelectedRaceColumnWithFleet();
        if (selectedRaceColumnAndFleetName != null) {
            if (selectedRaces.isEmpty()) {
                if (selectedRaceColumnAndFleetName.getA().getRaceIdentifier(selectedRaceColumnAndFleetName.getB()) != null) {
                    unlinkRaceColumnFromTrackedRace(selectedRaceColumnAndFleetName.getA().getRaceColumnName(), selectedRaceColumnAndFleetName.getB());
                }
            } else {
                linkTrackedRaceToSelectedRaceColumn(selectedRaceColumnAndFleetName.getA(), selectedRaceColumnAndFleetName.getB(),
                        selectedRaces.iterator().next());
            }
        }
    }

    private void linkTrackedRaceToSelectedRaceColumn(final RaceColumnDTO selectedRaceInLeaderboard,
            final FleetDTO fleet, final RegattaAndRaceIdentifier selectedRace) {
        sailingService.connectTrackedRaceToLeaderboardColumn(getSelectedLeaderboardName(), selectedRaceInLeaderboard
                .getRaceColumnName(), fleet.getName(), selectedRace,
                new MarkedAsyncCallback<Boolean>(
                        new AsyncCallback<Boolean>() {
                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError("Error trying to link tracked race " + selectedRace + " to race column named "
                                        + selectedRaceInLeaderboard.getRaceColumnName() + " of leaderboard "
                                        + getSelectedLeaderboardName() + ": " + t.getMessage());
                                trackedRacesListComposite.clearSelection();
                            }
                
                            @Override
                            public void onSuccess(Boolean success) {
                                if (success) {
                                    // TODO consider enabling the Unlink button
                                    selectedRaceInLeaderboard.setRaceIdentifier(fleet, selectedRace);
                                    raceColumnAndFleetList.refresh();
                                }
                            }
                        }));
    }

    @Override
    public StrippedLeaderboardDTO getSelectedLeaderboard() {
        return leaderboardSelectionModel.getSelectedSet().isEmpty() ? null : leaderboardSelectionModel.getSelectedSet().iterator().next();
    }
}