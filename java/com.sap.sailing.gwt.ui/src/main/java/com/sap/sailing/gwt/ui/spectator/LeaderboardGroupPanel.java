package com.sap.sailing.gwt.ui.spectator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
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
import com.sap.sailing.domain.common.Color;
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
        @SafeHtmlTemplates.Template("<div class=\"{1}\">{0}</div>")
        SafeHtml textWithClass(String text, String styleClass);

        @SafeHtmlTemplates.Template("<div class=\"{2}\" style=\"min-width:{1}px;\">{0}</div>")
        SafeHtml textWithClass(String text, int widthInPx, String styleClass);
    }

    interface ColorBoxTemplate extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div class=\"{1}\" style=\"background:{0};\">&nbsp;</div>")
        SafeHtml colorBox(String htmlColor, String styleClass);

        @SafeHtmlTemplates.Template("<div class=\"{0}\">&nbsp;</div>")
        SafeHtml nocolorBox(String styleClass);
    }
    
    private static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    private static final TextWithClassTemplate TEXTTEMPLATE = GWT.create(TextWithClassTemplate.class);
    private static final ColorBoxTemplate COLORBOXTEMPLATE = GWT.create(ColorBoxTemplate.class);
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
    private final boolean showRaceDetails;
    
    public LeaderboardGroupPanel(SailingServiceAsync sailingService, StringMessages stringConstants,
            ErrorReporter errorReporter, final String groupName, String root, String viewMode, boolean embedded, boolean showRaceDetails) {
        super();
        this.embedded = embedded;
        this.showRaceDetails = showRaceDetails;
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
        if (!embedded) {
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
            HTML groupDescriptionLabel = new HTML(new SafeHtmlBuilder().appendEscapedLines(group.description)
                    .toSafeHtml());
            groupDescriptionLabel.setStyleName(STYLE_NAME_PREFIX + "GroupDescription");
            groupDetailsPanel.add(groupDescriptionLabel);
            // Create group leaderboards GUI
            Label leaderboardsTableLabel = new Label(stringConstants.leaderboards());
            leaderboardsTableLabel.setStyleName(STYLE_NAME_PREFIX + "LeaderboardsTableLabel");
            mainPanel.add(leaderboardsTableLabel);
        }
        SafeHtmlCell leaderboardNameCell = new SafeHtmlCell();
        Column<StrippedLeaderboardDTO, SafeHtml> leaderboardNameColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(
                leaderboardNameCell) {
            @Override
            public SafeHtml getValue(StrippedLeaderboardDTO strippedLeaderboardDTO) {
                String text = "";
                if (allLeaderboardNamesStartWithGroupName) {
                    text = shortenLeaderboardName(group.name, strippedLeaderboardDTO.name);
                } else {
                    text = strippedLeaderboardDTO.name;
                }
                SafeHtmlBuilder b = new SafeHtmlBuilder();
                b.append(TEXTTEMPLATE.textWithClass(text, STYLE_NAME_PREFIX + "BoatClass"));
                return b.toSafeHtml();
            }
        };

        AnchorCell nameAnchorCell = new AnchorCell();
        Column<StrippedLeaderboardDTO, SafeHtml> overviewColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(
                nameAnchorCell) {
            @Override
            public SafeHtml getValue(StrippedLeaderboardDTO leaderboard) {
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLFactory.INSTANCE.encode("/gwt/Leaderboard.html?name=" + leaderboard.name
                        + (showRaceDetails ? "&showRaceDetails=true" : "")
                        + "&leaderboardGroupName=" + group.name + "&root=" + root
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                if (embedded) {
                    return ANCHORTEMPLATE.anchorWithTarget(link, stringConstants.overview(), STYLE_NAME_PREFIX
                            + "ActiveLeaderboard",
                    /* target */"_blank");
                } else {
                    return ANCHORTEMPLATE.anchor(link, stringConstants.overview(), STYLE_NAME_PREFIX
                            + "ActiveLeaderboard");
                }
            }
        };
        
        LeaderboardGroupFullTableResources tableResources = GWT.create(LeaderboardGroupFullTableResources.class);
        CellTable<StrippedLeaderboardDTO> leaderboardsTable = new CellTable<StrippedLeaderboardDTO>(200, tableResources);
        leaderboardsTable.setSelectionModel(new NoSelectionModel<StrippedLeaderboardDTO>());
        leaderboardsTable.addColumn(leaderboardNameColumn, stringConstants.name());
        leaderboardsTable.addColumn(overviewColumn, stringConstants.leaderboard());
        if (showRaceDetails) {
            SafeHtmlCell racesCell = new SafeHtmlCell();
            Column<StrippedLeaderboardDTO, SafeHtml> racesColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(
                    racesCell) {
                @Override
                public SafeHtml getValue(StrippedLeaderboardDTO leaderboard) {
                    return leaderboardRacesToHtml(leaderboard);
                }
            };
            leaderboardsTable.addColumn(racesColumn, stringConstants.races());
        }
        leaderboardsTable.setRowData(group.leaderboards);
        mainPanel.add(leaderboardsTable);
    }

    private String shortenLeaderboardName(String prefixToCut, String leaderboardName) {
        String result = leaderboardName.substring(prefixToCut.length(), leaderboardName.length());
        result = result.trim();
        if(result.startsWith("(") && result.endsWith(")")) {
            result = result.substring(1, result.length()-1);
        }
        return result.trim();
    }
    
    private SafeHtml leaderboardRacesToHtml(StrippedLeaderboardDTO leaderboard) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        
        List<Map<String,Pair<FleetDTO, List<RaceColumnDTO>>>> fleetGroups = new ArrayList<Map<String, Pair<FleetDTO, List<RaceColumnDTO>>>>();
        Map<String, Pair<FleetDTO, List<RaceColumnDTO>>> racesOrderedByFleets = null;
        RaceColumnDTO previousRaceColumn = null;
        for (RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
            if (previousRaceColumn == null || (previousRaceColumn != null && !hasSameFleets(raceColumn, previousRaceColumn))) {
                racesOrderedByFleets = new LinkedHashMap<String, Pair<FleetDTO, List<RaceColumnDTO>>>();
                fleetGroups.add(racesOrderedByFleets);
            }
            for (FleetDTO fleet : raceColumn.getFleets()) {
                Pair<FleetDTO, List<RaceColumnDTO>> pair = racesOrderedByFleets.get(fleet.name);
                if (pair == null) {
                    List<RaceColumnDTO> raceList = new ArrayList<RaceColumnDTO>();
                    raceList.add(raceColumn);
                    pair = new Pair<FleetDTO, List<RaceColumnDTO>>(fleet, raceList);
                    racesOrderedByFleets.put(fleet.name, pair);
                } else {
                    pair.getB().add(raceColumn);
                }
            }
            previousRaceColumn = raceColumn;
        }

        // the fleetGroups each are assumed to correspond to a fleet
        boolean hasMultipleFleetGroups = fleetGroups.size() > 1; 
        for (Map<String,Pair<FleetDTO, List<RaceColumnDTO>>> fleetGroup : fleetGroups) {
            b.appendHtmlConstant("<div style=\"float:left; margin-left:20px;\">");
            boolean hasMultipleFleet = fleetGroup.keySet().size() > 1;
            for (String fleetName : fleetGroup.keySet()) {
                Pair<FleetDTO, List<RaceColumnDTO>> pair = fleetGroup.get(fleetName);
                FleetDTO fleet = pair.getA();
                List<RaceColumnDTO> raceColumns = pair.getB();
                Color color = fleet.getColor();
                // show the "fleet" and the color only if there are more than one fleet in this fleet group and a color has been set
                if (hasMultipleFleet) {
                    if(color != null) {
                        b.append(COLORBOXTEMPLATE.colorBox(color.getAsHtml(), STYLE_NAME_PREFIX + "ColorBox"));
                    }
                    b.append(TEXTTEMPLATE.textWithClass(fleetName, 50, STYLE_NAME_PREFIX + "Fleet"));
                } else if(hasMultipleFleetGroups) {
                    String displayName = fleetName;
                    if("Default".equals(fleetName)) {
                        if(raceColumns.get(0) != null && raceColumns.get(0).isMedalRace()) {
                            displayName = stringConstants.medalRace(); 
                        } else {
                            displayName = stringConstants.race();
                        }
                    }
                    b.append(TEXTTEMPLATE.textWithClass(displayName, 50, STYLE_NAME_PREFIX + "Fleet"));
                }
                for (RaceColumnDTO race : raceColumns) {
                    String linkText = race.getRaceColumnName();
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
                        if (embedded) {
                            b.append(ANCHORTEMPLATE.anchorWithTarget(link, linkText, STYLE_NAME_PREFIX + "ActiveRace", "_blank"));
                        } else {
                            b.append(ANCHORTEMPLATE.anchor(link, linkText, STYLE_NAME_PREFIX + "ActiveRace"));
                        }
                    } else {
                        b.append(TEXTTEMPLATE.textWithClass(linkText, STYLE_NAME_PREFIX + "InactiveRace"));
                    }
                }
                b.appendHtmlConstant("<div style=\"clear:both\"/></div>");
            }
            b.appendHtmlConstant("</div>");
        }

        return b.toSafeHtml();
    }

    private boolean hasSameFleets(RaceColumnDTO raceColumn1, RaceColumnDTO raceColumn2) {
        Iterable<FleetDTO> raceColumn1Fleets = raceColumn1.getFleets();
        Iterable<FleetDTO> raceColumn2Fleets = raceColumn2.getFleets();

        if(Util.size(raceColumn1Fleets) != Util.size(raceColumn2Fleets))
            return false;
        
        for(int i = 0; i < Util.size(raceColumn1Fleets); i++) {
            FleetDTO next1 = raceColumn1Fleets.iterator().next();
            FleetDTO next2 = raceColumn2Fleets.iterator().next();
            if(!next1.name.equals(next2.name)) 
                return false;
        }
        
        return true;
    }
    
    private BreadcrumbPanel createBreadcrumbPanel() {
        BreadcrumbPanel breadcrumbPanel = null;
        if (root.equals("overview")) {
            String debugParam = Window.Location.getParameter("gwt.codesvr");
            String link = "/gwt/Spectator.html"
                    + (showRaceDetails ? "?showRaceDetails=true" : "")
                    + (debugParam != null && !debugParam.isEmpty() ? (showRaceDetails?"":"?")+"gwt.codesvr=" + debugParam : "");
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
