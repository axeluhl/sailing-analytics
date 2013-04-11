package com.sap.sailing.gwt.ui.spectator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.HasWelcomeWidget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLEncoder;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.panels.WelcomeWidget;

public class LeaderboardGroupPanel extends SimplePanel implements HasWelcomeWidget {

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
    private static final String STYLE_INACTIVE_RACE = STYLE_NAME_PREFIX + "InactiveRace";
    private static final String STYLE_ACTIVE_RACE = STYLE_NAME_PREFIX + "ActiveRace";
    private static final String STYLE_LIVE_RACE = STYLE_NAME_PREFIX + "LiveRace";
    private static final String STYLE_ACTIVE_LEADERBOARD = STYLE_NAME_PREFIX + "ActiveLeaderboard";
    private static final String STYLE_LEGEND = STYLE_NAME_PREFIX + "Legend";
    private static final String STYLE_TABLE_TEXT = STYLE_NAME_PREFIX + "TableText";
    private static final String STYLE_COLORBOX = STYLE_NAME_PREFIX + "ColorBox";
    private static final String STYLE_BOATCLASS = STYLE_NAME_PREFIX + "BoatClass";
    private static final String STYLE_LEGEND_LIVE = STYLE_NAME_PREFIX + "LegendLive";
    private static final String STYLE_LEGEND_TRACKED = STYLE_NAME_PREFIX + "LegendTracked";
    
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private LeaderboardGroupDTO leaderboardGroup;
    private Map<String, RegattaDTO> regattasByName;
    private final String root;
    private final String viewMode;
    
    private VerticalPanel mainPanel;
    private Widget welcomeWidget = null;
    private final boolean isEmbedded;
    private final boolean showRaceDetails;
    
    public LeaderboardGroupPanel(SailingServiceAsync sailingService, StringMessages stringConstants,
            ErrorReporter errorReporter, final String groupName, String root, String viewMode, boolean embedded, boolean showRaceDetails) {
        super();
        this.isEmbedded = embedded;
        this.showRaceDetails = showRaceDetails;
        this.sailingService = sailingService;
        this.stringMessages = stringConstants;
        this.errorReporter = errorReporter;
        this.root = (root == null || root.length() == 0) ? "leaderboardGroupPanel" : root;
        this.viewMode = viewMode;
        setWidth("95%");
        regattasByName = new HashMap<String, RegattaDTO>(); 
        mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        mainPanel.addStyleName("mainPanel");
        add(mainPanel);
        loadLeaderboardGroup(groupName);
    }

    private void loadLeaderboardGroup(final String leaderboardGroupName) {
        sailingService.getLeaderboardGroupByName(leaderboardGroupName, false /*withGeoLocationData*/, new AsyncCallback<LeaderboardGroupDTO>() {
            @Override
            public void onSuccess(final LeaderboardGroupDTO leaderboardGroupDTO) {
                if (leaderboardGroupDTO != null) {
                    LeaderboardGroupPanel.this.leaderboardGroup = leaderboardGroupDTO;
                    // in case there is a regatta leaderboard in the leaderboard group 
                    // we need to know the corresponding regatta structure
                    if(leaderboardGroup.containsRegattaLeaderboard()) {
                        sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
                            @Override
                            public void onSuccess(List<RegattaDTO> regattaDTOs) {
                                for(RegattaDTO regattaDTO: regattaDTOs) {
                                    regattasByName.put(regattaDTO.name, regattaDTO);
                                }
                                createPageContent();
                            }
                            
                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError("Error trying to load corresponding regattas for leaderboard group " + leaderboardGroupName + ": " + t.getMessage());
                            }
                        });
                    } else {
                        createPageContent();
                    }
                } else {
                    errorReporter.reportError(stringMessages.noLeaderboardGroupWithNameFound(leaderboardGroupName));
                }
            }
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to load the leaderboard group " + leaderboardGroupName + ": " + t.getMessage());
            }
        });
    }

    private void createPageContent() {
        if (!isEmbedded) {
            Label groupNameLabel = new Label(leaderboardGroup.name + ":");
            groupNameLabel.setStyleName(STYLE_NAME_PREFIX + "GroupName");
            mainPanel.add(groupNameLabel);
            // Using HTML to display the line breaks in the description
            HTML leaderboardGroupDescriptionLabel = new HTML(new SafeHtmlBuilder().appendEscapedLines(leaderboardGroup.description).toSafeHtml());
            leaderboardGroupDescriptionLabel.setStyleName(STYLE_NAME_PREFIX + "GroupDescription");
            mainPanel.add(leaderboardGroupDescriptionLabel);
        }
        
        FlexTable flexTable = new FlexTable();
 
        Label leaderboardsLabel = new Label(stringMessages.leaderboards());
        leaderboardsLabel.addStyleName(STYLE_NAME_PREFIX + "LeaderboardsLabel");
        
        if(leaderboardGroup.hasOverallLeaderboard() && !isEmbedded) {
            mainPanel.add(leaderboardsLabel);
        } else {
            flexTable.setWidget(0, 0, leaderboardsLabel);
        }

        mainPanel.add(flexTable);
        flexTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        
        // legend
        HorizontalPanel legendPanel = createLegendPanel();
        flexTable.setWidget(0, 1, legendPanel);
        
        if (leaderboardGroup.hasOverallLeaderboard()) {
            String debugParam = Window.Location.getParameter("gwt.codesvr");
            String link = URLEncoder.encode("/gwt/Leaderboard.html?name=" + leaderboardGroup.name+" "+LeaderboardNameConstants.OVERALL
                    + (showRaceDetails ? "&showRaceDetails=true" : "")
                    + (isEmbedded ? "&embedded=true" : "")
                    + "&displayName=" + stringMessages.overallStandings() 
                    + "&leaderboardGroupName=" + leaderboardGroup.name + "&root=" + root
                    + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
            Anchor overallStandingsLink = new Anchor(stringMessages.overallStandings(), true, link);
            overallStandingsLink.setStyleName(STYLE_ACTIVE_LEADERBOARD);
            overallStandingsLink.addStyleName("overallStandings");
            flexTable.setWidget(0, 0, overallStandingsLink);
        }

        SafeHtmlCell leaderboardNameCell = new SafeHtmlCell();
        Column<StrippedLeaderboardDTO, SafeHtml> leaderboardNameColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(
                leaderboardNameCell) {
            @Override
            public SafeHtml getValue(StrippedLeaderboardDTO strippedLeaderboardDTO) {
                String text = strippedLeaderboardDTO.displayName != null ? strippedLeaderboardDTO.displayName : strippedLeaderboardDTO.name; 
                SafeHtmlBuilder b = new SafeHtmlBuilder();
                b.append(TEXTTEMPLATE.textWithClass(text, STYLE_BOATCLASS));
                return b.toSafeHtml();
            }
        };

        AnchorCell nameAnchorCell = new AnchorCell();
        Column<StrippedLeaderboardDTO, SafeHtml> overviewColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(
                nameAnchorCell) {
            @Override
            public SafeHtml getValue(StrippedLeaderboardDTO leaderboard) {
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLEncoder.encode("/gwt/Leaderboard.html?name=" + leaderboard.name
                        + (showRaceDetails ? "&showRaceDetails=true" : "")
                        + (leaderboard.displayName != null ? "&displayName="+leaderboard.displayName : "")
                        + (isEmbedded ? "&embedded=true" : "")
                        + "&leaderboardGroupName=" + leaderboardGroup.name + "&root=" + root
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                return getAnchor(link, stringMessages.leaderboard(), STYLE_ACTIVE_LEADERBOARD);
            }
        };
        
        LeaderboardGroupFullTableResources tableResources = GWT.create(LeaderboardGroupFullTableResources.class);
        CellTable<StrippedLeaderboardDTO> leaderboardsTable = new CellTable<StrippedLeaderboardDTO>(10000, tableResources);
        leaderboardsTable.setSelectionModel(new NoSelectionModel<StrippedLeaderboardDTO>());
        leaderboardsTable.addColumn(leaderboardNameColumn, stringMessages.regatta());
        leaderboardsTable.addColumn(overviewColumn, "");
        if (showRaceDetails) {
            SafeHtmlCell racesCell = new SafeHtmlCell();
            Column<StrippedLeaderboardDTO, SafeHtml> racesColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(
                    racesCell) {
                @Override
                public SafeHtml getValue(StrippedLeaderboardDTO leaderboard) {
                    return leaderboardRacesToHtml(leaderboard);
                }
            };
            leaderboardsTable.addColumn(racesColumn, stringMessages.races());
        }
        
        if(leaderboardGroup.displayLeaderboardsInReverseOrder) {
            leaderboardsTable.setRowData(leaderboardGroup.getLeaderboardsInReverseOrder());
        } else {
            leaderboardsTable.setRowData(leaderboardGroup.getLeaderboards());
        }
        flexTable.setWidget(1, 0,leaderboardsTable);
        flexTable.getFlexCellFormatter().setColSpan(1, 0,2);
        
    }

    private HorizontalPanel createLegendPanel() {
        HorizontalPanel legendPanel = new HorizontalPanel();
        legendPanel.setStyleName(STYLE_LEGEND);
        legendPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        legendPanel.setSpacing(5);
        
        Label legendLabel = new Label(stringMessages.legend() + ":");
        legendLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        legendPanel.add(legendLabel);
        
        Label inactiveRace = new Label(stringMessages.untracked());
        inactiveRace.setStyleName(STYLE_INACTIVE_RACE);
        legendPanel.add(inactiveRace);

        Label activeRace = new Label(stringMessages.tracked());
        activeRace.setStyleName(STYLE_LEGEND_TRACKED);
        legendPanel.add(activeRace);
        
        Label liveRace = new Label(stringMessages.live());
        liveRace.setStyleName(STYLE_LEGEND_LIVE);
        legendPanel.add(liveRace);
        return legendPanel;
    }

    private SafeHtml leaderboardRacesToHtml(StrippedLeaderboardDTO leaderboard) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        if (leaderboard.isRegattaLeaderboard && leaderboard.regattaName != null) {
            RegattaDTO regatta = regattasByName.get(leaderboard.regattaName);
            boolean renderSeriesName = Util.size(regatta.series) > 1;
            for (SeriesDTO series : regatta.series) {
                b.appendHtmlConstant("<div>");
                renderSeriesToHtml(leaderboard, series, renderSeriesName, b);
                b.appendHtmlConstant("<div style=\"clear:both;\"></div>");
                b.appendHtmlConstant("</div>");
            }
        } else {
            List<RaceColumnDTO> raceColumns = leaderboard.getRaceList();
            renderRacesToHTml(leaderboard.name, raceColumns, new FleetDTO(LeaderboardNameConstants.DEFAULT_FLEET_NAME, /* series name */ null, 0, null), b); 
        }
        return b.toSafeHtml();
    }

    private void renderSeriesToHtml(StrippedLeaderboardDTO leaderboard, SeriesDTO series, boolean renderSeriesName, SafeHtmlBuilder b) {
        boolean hasMultipleFleets = series.getFleets().size() > 1;
        b.appendHtmlConstant("<div style=\"float:left;\">");
        if(renderSeriesName) {
            b.append(TEXTTEMPLATE.textWithClass(series.name, 50, STYLE_TABLE_TEXT));
        }
        b.appendHtmlConstant("</div>");
        b.appendHtmlConstant("<div style=\"float:left;\">");
        for(FleetDTO fleet: series.getFleets()) {
            Color color = fleet.getColor();
            // show the "fleet" and the color only if there are more than one fleet in this fleet group and a color has been set
            b.appendHtmlConstant("<div style=\"\">");

            if (hasMultipleFleets) {
                if(color != null) {
                    b.append(COLORBOXTEMPLATE.colorBox(color.getAsHtml(), STYLE_COLORBOX));
                }
                b.append(TEXTTEMPLATE.textWithClass(fleet.name, 50, STYLE_TABLE_TEXT));
            } else {
                String displayName = fleet.name;
                if (! "Default".equals(fleet.name)) {
                    b.append(TEXTTEMPLATE.textWithClass(displayName, 50, STYLE_TABLE_TEXT));
                } 
            }
            
            List<RaceColumnDTO> raceColumnsOfSeries = getRacesOfFleet(leaderboard, series, fleet);
            renderRacesToHTml(leaderboard.name, raceColumnsOfSeries, fleet, b);
            
            b.appendHtmlConstant("</div>");
        }
        b.appendHtmlConstant("</div>");
    }
    
    private void renderRacesToHTml(String leaderboardName, List<RaceColumnDTO> raceColumns, FleetDTO fleet, SafeHtmlBuilder b) {
        for (RaceColumnDTO raceColumn : raceColumns) {
            String raceColumnName = raceColumn.getRaceColumnName();
            RaceDTO race = raceColumn.getRace(fleet);
            renderRaceLink(leaderboardName, race, raceColumn.isLive(fleet), raceColumnName, b);
        }
    }

    private List<RaceColumnDTO> getRacesOfFleet(StrippedLeaderboardDTO leaderboard, SeriesDTO series, FleetDTO fleet) {
        List<RaceColumnDTO> racesColumnsOfFleet = new ArrayList<RaceColumnDTO>();
        
        for (RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
            for (FleetDTO fleetOfRaceColumn : raceColumn.getFleets()) {
                if(fleet.equals(fleetOfRaceColumn)) {
                    racesColumnsOfFleet.add(raceColumn);
                }
            }
        }
        return racesColumnsOfFleet;
    }
    
    private void renderRaceLink(String leaderboardName, RaceDTO race, boolean isLive, String raceColumnName, SafeHtmlBuilder b) {
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        if (race != null) {
            RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier();
            String link = URLEncoder.encode("/gwt/RaceBoard.html?leaderboardName=" + leaderboardName
                    + "&raceName=" + raceIdentifier.getRaceName() + "&root=" + root + raceIdentifier.getRaceName()
                    + "&regattaName=" + raceIdentifier.getRegattaName() + "&leaderboardGroupName=" + leaderboardGroup.name);
            if (debugParam != null && !debugParam.isEmpty()) {
                link += "&gwt.codesvr=" + debugParam;
            }
            if (viewMode != null && !viewMode.isEmpty()) {
                link += "&viewMode=" + viewMode;
            }
            if (isLive) {
                b.append(getAnchor(link, raceColumnName, STYLE_LIVE_RACE));
            } else if (race.trackedRace.hasGPSData && race.trackedRace.hasWindData) {
                b.append(getAnchor(link, raceColumnName, STYLE_ACTIVE_RACE));
            } else {
                b.append(TEXTTEMPLATE.textWithClass(raceColumnName, STYLE_INACTIVE_RACE));
            }
            
        } else {
            b.append(TEXTTEMPLATE.textWithClass(raceColumnName, STYLE_INACTIVE_RACE));
        }
    }
    
    private SafeHtml getAnchor(String link, String linkText, String style) {
        if (isEmbedded) {
            return ANCHORTEMPLATE.anchorWithTarget(link, linkText, style, "_blank");
        } else {
            return ANCHORTEMPLATE.anchor(link, linkText, style);
        }
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
