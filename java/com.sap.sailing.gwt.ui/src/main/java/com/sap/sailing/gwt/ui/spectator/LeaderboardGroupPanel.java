package com.sap.sailing.gwt.ui.spectator;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.HasWelcomeWidget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLFactory;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.panels.BreadcrumbPanel;
import com.sap.sailing.gwt.ui.shared.panels.WelcomeWidget;

public class LeaderboardGroupPanel extends FormPanel implements HasWelcomeWidget {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a class=\"{2}\" href=\"{0}\">{1}</a>")
        SafeHtml anchor(String url, String displayName, String styleClass);

        @SafeHtmlTemplates.Template("<a target=\"{3}\" class=\"{2}\" href=\"{0}\">{1}</a>")
        SafeHtml anchorWithTarget(String url, String displayName, String styleClass, String target);
    }
    
    interface TextWithClassTemplate extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<span class=\"{1}\">{0}</span>")
        SafeHtml textWithClass(String text, String styleClass);
    }

    private static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    private static final TextWithClassTemplate TEXTTEMPLATE = GWT.create(TextWithClassTemplate.class);
    private static final int MAX_COLUMNS_IN_ROW = 10;
    private static final String STYLE_NAME_PREFIX = "leaderboardGroupPanel-";
    
    private final SailingServiceAsync sailingService;
    private final StringMessages stringConstants;
    private final ErrorReporter errorReporter;
    private LeaderboardGroupDTO group;
    private final String root;
    private final String viewMode;
    
    private FlowPanel mainPanel;
    private Widget welcomeWidget = null;
    private boolean allLeaderboardNamesStartWithGroupName = false;
    private final boolean embedded;
    
    public LeaderboardGroupPanel(SailingServiceAsync sailingService, StringMessages stringConstants,
            ErrorReporter errorReporter, final String groupName, String root, String viewMode, boolean embedded) {
        super();
        this.embedded = embedded;
        this.sailingService = sailingService;
        this.stringConstants = stringConstants;
        this.errorReporter = errorReporter;
        this.root = (root == null || root.length() == 0) ? "leaderboardGroupPanel" : root;
        this.viewMode = viewMode;
        mainPanel = new FlowPanel();
        add(mainPanel);
        loadGroup(groupName);
    }

    private void loadGroup(final String groupName) {
        sailingService.getLeaderboardGroupByName(groupName, new AsyncCallback<LeaderboardGroupDTO>() {
            @Override
            public void onSuccess(LeaderboardGroupDTO group) {
                if (group != null) {
                    LeaderboardGroupPanel.this.group = group;
                    if(group.leaderboards.size() > 1) {
                        allLeaderboardNamesStartWithGroupName = true;
                        String groupName = group.name; 
                        for(StrippedLeaderboardDTO leaderboard: group.leaderboards) {
                            if(!leaderboard.name.startsWith(groupName)) {
                                allLeaderboardNamesStartWithGroupName = false;
                                break;
                            }
                        }
                    }
                    buildGUI();
                } else {
                    errorReporter.reportError(stringConstants.noLeaderboardGroupWithNameFound(groupName));
                }
            }
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to load the leaderboard group " + groupName + ": " + t.getMessage());
            }
        });
    }

    private void buildGUI() {
        // Create breadcrumb panel
        BreadcrumbPanel breadcrumbPanel = createBreadcrumbPanel();
        if (breadcrumbPanel != null) {
            mainPanel.add(breadcrumbPanel);
        }
        
        // Create group details GUI
        FlowPanel groupDetailsPanel = new FlowPanel();
        groupDetailsPanel.setStyleName(STYLE_NAME_PREFIX + "GroupDetailsPanel");
        mainPanel.add(groupDetailsPanel);

        Label groupNameLabel = new Label(group.name + ":");
        groupNameLabel.setStyleName(STYLE_NAME_PREFIX + "GroupName");
        groupDetailsPanel.add(groupNameLabel);
        
        // Using HTML to display the line breaks in the description
        HTML groupDescriptionLabel = new HTML(new SafeHtmlBuilder().appendEscapedLines(group.description).toSafeHtml());
        groupDescriptionLabel.setStyleName(STYLE_NAME_PREFIX + "GroupDescription");
        groupDetailsPanel.add(groupDescriptionLabel);
        
        // Create group leaderboards GUI
        Label leaderboardsTableLabel = new Label(stringConstants.leaderboards());
        leaderboardsTableLabel.setStyleName(STYLE_NAME_PREFIX + "LeaderboardsTableLabel");
        mainPanel.add(leaderboardsTableLabel);
        
        if (group.leaderboards.size() <= MAX_COLUMNS_IN_ROW) {
            LeaderboardGroupFullTableResources tableResources = GWT.create(LeaderboardGroupFullTableResources.class);
            CellTable<Integer> leaderboardsTable = new CellTable<Integer>(200, tableResources);
            leaderboardsTable.setSelectionModel(new NoSelectionModel<Integer>());
            int maxRacesNum = 0;
            for (final StrippedLeaderboardDTO leaderboard : group.leaderboards) {
                SafeHtmlCell leaderboardCell = new SafeHtmlCell();
                Column<Integer, SafeHtml> leaderboardColumn = new Column<Integer, SafeHtml>(leaderboardCell) {
                    @Override
                    public SafeHtml getValue(Integer raceNum) {
                        List<RaceColumnDTO> races = leaderboard.getRaceList();
                        return raceNum < races.size() ? raceToRaceBoardLink(leaderboard, races.get(raceNum))
                                : SafeHtmlUtils.fromString("");
                    }
                };
                leaderboardsTable.addColumn(leaderboardColumn, leaderboard.name);
                // add an artificial RaceColumn for the "Overview" link at position 0
                leaderboard.createRaceColumnAt(stringConstants.overview(), new FleetDTO("Default", 0, /* color */ null), /* medal race */ false, /* tracked race identifier */ null, 0);
                maxRacesNum = maxRacesNum < leaderboard.getRaceList().size() ? leaderboard
                        .getRaceList().size() : maxRacesNum;
            }

            ArrayList<Integer> tableData = new ArrayList<Integer>();
            for (int i = 0; i < maxRacesNum; i++) {
                tableData.add(i);
            }
            leaderboardsTable.setRowData(tableData);
            mainPanel.add(leaderboardsTable);
        } else {
            TextColumn<StrippedLeaderboardDTO> leaderboardNameColumn = new TextColumn<StrippedLeaderboardDTO>() {
                    @Override
                    public String getValue(StrippedLeaderboardDTO strippedLeaderboardDTO) {
                        if(allLeaderboardNamesStartWithGroupName) {
                            return shortenLeaderboardName(group.name, strippedLeaderboardDTO.name);
                        } else {
                            return strippedLeaderboardDTO.name;
                        }
                    }
                };
            
            AnchorCell nameAnchorCell = new AnchorCell();
            Column<StrippedLeaderboardDTO, SafeHtml> nameColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(nameAnchorCell) {
                @Override
                public SafeHtml getValue(StrippedLeaderboardDTO leaderboard) {
                    String debugParam = Window.Location.getParameter("gwt.codesvr");
                    String link = URLFactory.INSTANCE.encode("/gwt/Leaderboard.html?name=" + leaderboard.name
                            + "&leaderboardGroupName=" + group.name + "&root=" + root
                            + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                    if (embedded) {
                        return ANCHORTEMPLATE.anchor(link, stringConstants.overview(), STYLE_NAME_PREFIX + "ActiveLeaderboard");
                    } else {
                        return ANCHORTEMPLATE.anchorWithTarget(link, stringConstants.overview(), STYLE_NAME_PREFIX + "ActiveLeaderboard",
                                /* target */ "_blank");
                    }
                }
            };

            SafeHtmlCell racesCell = new SafeHtmlCell();
            Column<StrippedLeaderboardDTO, SafeHtml> racesColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(racesCell) {
                @Override
                public SafeHtml getValue(StrippedLeaderboardDTO leaderboard) {
                    return leaderboardRacesToHtml(leaderboard);
                }
            };

            LeaderboardGroupFullTableResources tableResources = GWT.create(LeaderboardGroupFullTableResources.class);
            CellTable<StrippedLeaderboardDTO> leaderboardsTable = new CellTable<StrippedLeaderboardDTO>(200, tableResources);
            leaderboardsTable.setSelectionModel(new NoSelectionModel<StrippedLeaderboardDTO>());
            leaderboardsTable.addColumn(leaderboardNameColumn, stringConstants.name());
            leaderboardsTable.addColumn(nameColumn, stringConstants.races());
            leaderboardsTable.addColumn(racesColumn);
            leaderboardsTable.setRowData(group.leaderboards);
            mainPanel.add(leaderboardsTable);
        }
    }

    private String shortenLeaderboardName(String prefixToCut, String leaderboardName) {
        String result = leaderboardName.substring(prefixToCut.length(), leaderboardName.length());
        return result.trim();
    }
    
    private SafeHtml raceToRaceBoardLink(StrippedLeaderboardDTO leaderboard, RaceColumnDTO race) {
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        String raceColumnDisplayName = race.getRaceColumnName();
        // special handling for artificial RaceColumn for "Overview" button
        if (race.getRaceColumnName().equals(stringConstants.overview())) {
            String link = URLFactory.INSTANCE.encode("/gwt/Leaderboard.html?name=" + leaderboard.name
                    + "&leaderboardGroupName=" + group.name + "&root=" + root);
            if (debugParam != null && !debugParam.isEmpty()) {
                link += "&gwt.codesvr=" + debugParam;
            }
            if (viewMode != null && !viewMode.isEmpty()) {
                link += "&viewMode=" + viewMode;
            }
            if (embedded) {
                b.append(ANCHORTEMPLATE.anchorWithTarget(link, raceColumnDisplayName, STYLE_NAME_PREFIX + "ActiveLeaderboard", "_blank"));
            } else {
                b.append(ANCHORTEMPLATE.anchor(link, raceColumnDisplayName, STYLE_NAME_PREFIX + "ActiveLeaderboard"));
            }
        } else {
            final Iterable<FleetDTO> fleets = race.getFleets();
            boolean singleFleet = Util.size(fleets) < 2;
            for (FleetDTO fleet: fleets) {
                String linkText;
                if (singleFleet) {
                    linkText = raceColumnDisplayName;
                } else {
                    linkText = raceColumnDisplayName +" ("+fleet.name+")";
                }
                if (race.getRaceIdentifier(fleet) != null) {
                    RegattaNameAndRaceName raceId = (RegattaNameAndRaceName) race.getRaceIdentifier(fleet);
                    String link = URLFactory.INSTANCE.encode("/gwt/RaceBoard.html?leaderboardName=" + leaderboard.name
                            + "&raceName=" + raceId.getRaceName() + "&regattaName=" + raceId.getRegattaName()
                            + "&leaderboardGroupName=" + group.name + "&root=" + root);
                    if (debugParam != null && !debugParam.isEmpty()) {
                        link += "&gwt.codesvr=" + debugParam;
                    }
                    if (viewMode != null && !viewMode.isEmpty()) {
                        link += "&viewMode=" + viewMode;
                    }
                    b.append(ANCHORTEMPLATE.anchor(link, linkText, STYLE_NAME_PREFIX + "ActiveRace"));
                } else {
                    b.append(TEXTTEMPLATE.textWithClass(linkText, STYLE_NAME_PREFIX + "InactiveRace"));
                }
            }
        }
        return b.toSafeHtml();
    }
    
    private SafeHtml leaderboardRacesToHtml(StrippedLeaderboardDTO leaderboard) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        for (RaceColumnDTO race : leaderboard.getRaceList()) {
            final Iterable<FleetDTO> fleets = race.getFleets();
            boolean singleFleet = Util.size(fleets) < 2;
            for (FleetDTO fleet : fleets) {
                String linkText;
                if (singleFleet) {
                    linkText = race.getRaceColumnName();
                } else {
                    linkText = race.getRaceColumnName() + " (" + fleet.name + ")";
                }
                if (race.getRaceIdentifier(fleet) != null) {
                    RegattaNameAndRaceName raceId = (RegattaNameAndRaceName) race.getRaceIdentifier(fleet);
                    String link = URLFactory.INSTANCE.encode("/gwt/RaceBoard.html?leaderboardName=" + leaderboard.name
                            + "&raceName=" + raceId.getRaceName() + "&root=" + root + raceId.getRaceName()
                            + "&regattaName=" + raceId.getRegattaName() + "&leaderboardGroupName=" + group.name);
                    if (debugParam != null && !debugParam.isEmpty()) {
                        link += "&gwt.codesvr=" + debugParam;
                    }
                    if (viewMode != null && !viewMode.isEmpty()) {
                        link += "&viewMode=" + viewMode;
                    }
                    b.append(ANCHORTEMPLATE.anchor(link, linkText, STYLE_NAME_PREFIX + "ActiveRace"));
                } else {
                    b.append(TEXTTEMPLATE.textWithClass(linkText, STYLE_NAME_PREFIX + "InactiveRace"));
                }
            }
        }
        return b.toSafeHtml();
    }
    
    private BreadcrumbPanel createBreadcrumbPanel() {
        BreadcrumbPanel breadcrumbPanel = null;
        if (root.equals("overview")) {
            String debugParam = Window.Location.getParameter("gwt.codesvr");
            String link = "/gwt/Spectator.html"
                    + (debugParam != null && !debugParam.isEmpty() ? "?gwt.codesvr=" + debugParam : "");
            ArrayList<Pair<String, String>> breadcrumbLinksData = new ArrayList<Pair<String, String>>();
            breadcrumbLinksData.add(new Pair<String, String>(link, stringConstants.home()));
            breadcrumbPanel = new BreadcrumbPanel(breadcrumbLinksData, group.name);
        }
        return breadcrumbPanel;
    }

    @Override
    public void setWelcomeWidgetVisible(boolean isVisible) {
        if (welcomeWidget != null) {
            welcomeWidget.setVisible(isVisible);
        }
    }

    @Override
    public void setWelcomeWidget(WelcomeWidget welcome) {
        boolean needsToBeInserted = welcomeWidget == null;
        welcomeWidget = welcome;
        if (needsToBeInserted) {
            mainPanel.insert(welcomeWidget, 0);
        }
    }

}
