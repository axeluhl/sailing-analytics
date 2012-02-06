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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.sap.sailing.domain.common.EventNameAndRaceName;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.HasWelcomeWidget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.panels.BreadcrumbPanel;
import com.sap.sailing.gwt.ui.shared.panels.WelcomeWidget;

public class LeaderboardGroupPanel extends FormPanel implements HasWelcomeWidget {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a class=\"{2}\" href=\"{0}\">{1}</a>")
        SafeHtml anchor(String url, String displayName, String styleClass);
    }
    
    interface TextWithClassTemplate extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<span class=\"{1}\">{0}</span>")
        SafeHtml textWithClass(String text, String styleClass);
    }

    private static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    private static final TextWithClassTemplate TEXTTEMPLATE = GWT.create(TextWithClassTemplate.class);
    private static final int MAX_COLUMNS_IN_ROW = 10;
    private static final String STYLE_NAME_PREFIX = "leaderboardGroupPanel-";
    
    private SailingServiceAsync sailingService;
    private StringMessages stringConstants;
    private ErrorReporter errorReporter;
    private LeaderboardGroupDTO group;
    private String root;
    
    private FlowPanel mainPanel;
    private Widget welcomeWidget = null;
    
    public LeaderboardGroupPanel(SailingServiceAsync sailingService, StringMessages stringConstants,
            ErrorReporter errorReporter, final String groupName, String root) {
        super();
        this.sailingService = sailingService;
        this.stringConstants = stringConstants;
        this.errorReporter = errorReporter;
        this.root = root;
        if (this.root == null || this.root.length() == 0) {
            this.root = "leaderboardGroupPanel";
        }

        mainPanel = new FlowPanel();
        mainPanel.setWidth("95%");
        add(mainPanel);
        loadGroup(groupName);
    }

    private void loadGroup(final String groupName) {
        sailingService.getLeaderboardGroupByName(groupName, new AsyncCallback<LeaderboardGroupDTO>() {
            @Override
            public void onSuccess(LeaderboardGroupDTO group) {
                if (group != null) {
                    LeaderboardGroupPanel.this.group = group;
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
        //Create breadcrumb panel
        BreadcrumbPanel breadcrumbPanel = createBreadcrumbPanel();
        if (breadcrumbPanel != null) {
            mainPanel.add(breadcrumbPanel);
        }
        
        //Create group details GUI
        FlowPanel groupDetailsPanel = new FlowPanel();
        groupDetailsPanel.setStyleName(STYLE_NAME_PREFIX + "GroupDetailsPanel");
        mainPanel.add(groupDetailsPanel);

        Label groupNameLabel = new Label(group.name + ":");
        groupNameLabel.setStyleName(STYLE_NAME_PREFIX + "GroupName");
        groupDetailsPanel.add(groupNameLabel);
        
        //Using HTML to display the line breaks in the description
        HTML groupDescriptionLabel = new HTML(new SafeHtmlBuilder().appendEscapedLines(group.description).toSafeHtml());
        groupDescriptionLabel.setStyleName(STYLE_NAME_PREFIX + "GroupDescription");
        groupDetailsPanel.add(groupDescriptionLabel);
        
        //Create group leaderboards GUI
        Label leaderboardsTableLabel = new Label(stringConstants.leaderboards());
        leaderboardsTableLabel.setStyleName(STYLE_NAME_PREFIX + "LeaderboardsTableLabel");
        mainPanel.add(leaderboardsTableLabel);
        
        if (group.leaderboards.size() <= MAX_COLUMNS_IN_ROW) {
            LeaderboardGroupFullTableResources tableResources = GWT.create(LeaderboardGroupFullTableResources.class);
            CellTable<Integer> leaderboardsTable = new CellTable<Integer>(200, tableResources);
            leaderboardsTable.setWidth("100%");
            leaderboardsTable.setSelectionModel(new NoSelectionModel<Integer>());
            
            int maxRacesNum = 0;
            for (final LeaderboardDTO leaderboard : group.leaderboards) {
                SafeHtmlCell leaderboardCell = new SafeHtmlCell();
                Column<Integer, SafeHtml> leaderboardColumn = new Column<Integer, SafeHtml>(leaderboardCell) {
                    @Override
                    public SafeHtml getValue(Integer raceNum) {
                        List<RaceInLeaderboardDTO> races = leaderboard.getRaceInLeaderboardList();
                        return raceNum < races.size() ? raceToRaceBoardLink(leaderboard, races.get(raceNum))
                                : SafeHtmlUtils.fromString("");
                    }
                };
                leaderboardsTable.addColumn(leaderboardColumn, leaderboard.name);
                leaderboard.addRaceAt(stringConstants.overview(), false, null, 0);
                maxRacesNum = maxRacesNum < leaderboard.getRaceInLeaderboardList().size() ? leaderboard
                        .getRaceInLeaderboardList().size() : maxRacesNum;
            }

            ArrayList<Integer> tableData = new ArrayList<Integer>();
            for (int i = 0; i < maxRacesNum; i++) {
                tableData.add(i);
            }
            
            leaderboardsTable.setRowData(tableData);
            mainPanel.add(leaderboardsTable);
        } else {
            AnchorCell nameAnchorCell = new AnchorCell();
            Column<LeaderboardDTO, SafeHtml> nameColumn = new Column<LeaderboardDTO, SafeHtml>(nameAnchorCell) {
                @Override
                public SafeHtml getValue(LeaderboardDTO leaderboard) {
                    String debugParam = Window.Location.getParameter("gwt.codesvr");
                    return ANCHORTEMPLATE.anchor("/gwt/Leaderboard.html?name=" + leaderboard.name
                            + "&leaderboardGroupName=" + group.name + "&root=" + root
                            + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""),
                            leaderboard.name, STYLE_NAME_PREFIX + "ActiveLeaderboard");
                }
            };

            SafeHtmlCell racesCell = new SafeHtmlCell();
            Column<LeaderboardDTO, SafeHtml> racesColumn = new Column<LeaderboardDTO, SafeHtml>(racesCell) {
                @Override
                public SafeHtml getValue(LeaderboardDTO leaderboard) {
                    return leaderboardRacesToHtml(leaderboard);
                }
            };

            LeaderboardGroupCompactTableResources tableResources = GWT.create(LeaderboardGroupCompactTableResources.class);
            CellTable<LeaderboardDTO> leaderboardsTable = new CellTable<LeaderboardDTO>(200, tableResources);
            leaderboardsTable.setWidth("100%");
            leaderboardsTable.setSelectionModel(new NoSelectionModel<LeaderboardDTO>());
            
            leaderboardsTable.addColumn(nameColumn, stringConstants.name());
            leaderboardsTable.setColumnWidth(nameColumn, "30%");
            leaderboardsTable.addColumn(racesColumn, stringConstants.races());
            leaderboardsTable.setColumnWidth(racesColumn, "70%");
            
            leaderboardsTable.setRowData(group.leaderboards);
            mainPanel.add(leaderboardsTable);
        }
    }

    private SafeHtml raceToRaceBoardLink(LeaderboardDTO leaderboard, RaceInLeaderboardDTO race) {
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        String displayName = race.getRaceColumnName();
        if (race.getRaceColumnName().equals(stringConstants.overview())) {
            String link = "/gwt/Leaderboard.html?name=" + leaderboard.name + "&leaderboardGroupName=" + group.name
                    + "&root=" + root
                    + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : "");
            b.append(ANCHORTEMPLATE.anchor(link, displayName, STYLE_NAME_PREFIX + "ActiveLeaderboard"));
        } else {
            if (race.getRaceIdentifier() != null) {
                EventNameAndRaceName raceId = (EventNameAndRaceName) race.getRaceIdentifier();
                String link = "/gwt/RaceBoard.html?leaderboardName=" + leaderboard.name + "&raceName=" + raceId.getRaceName()
                        + "&eventName=" + raceId.getEventName() + "&leaderboardGroupName=" + group.name + "&root=" + root
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : "");
                b.append(ANCHORTEMPLATE.anchor(link, displayName, STYLE_NAME_PREFIX + "ActiveRace"));
            } else {
                b.append(TEXTTEMPLATE.textWithClass(race.getRaceColumnName(), STYLE_NAME_PREFIX + "InactiveRace"));
            }
        }
        return b.toSafeHtml();
    }
    
    private SafeHtml leaderboardRacesToHtml(LeaderboardDTO leaderboard) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        boolean first = true;
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
            if (!first) {
                b.appendHtmlConstant(", ");
            }
            if (race.getRaceIdentifier() != null) {
                EventNameAndRaceName raceId = (EventNameAndRaceName) race.getRaceIdentifier();
                String link = "/gwt/RaceBoard.html?leaderboardName=" + leaderboard.name + "&raceName=" + "&root=" + root
                        + raceId.getRaceName() + "&eventName=" + raceId.getEventName() + "&leaderboardGroupName="
                        + group.name + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : "");
                b.append(ANCHORTEMPLATE.anchor(link, race.getRaceColumnName(), STYLE_NAME_PREFIX + "ActiveRace"));
            } else {
                b.append(TEXTTEMPLATE.textWithClass(race.getRaceColumnName(), STYLE_NAME_PREFIX + "InactiveRace"));
            }
            first = false;
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
            String actualBreadcrumbName = stringConstants.leaderboardGroup() + ": " + group.name;
            breadcrumbPanel = new BreadcrumbPanel(breadcrumbLinksData, actualBreadcrumbName);
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
        welcomeWidget.setWidth("100%");
        if (needsToBeInserted) {
            mainPanel.insert(welcomeWidget, 0);
        }
    }

}
