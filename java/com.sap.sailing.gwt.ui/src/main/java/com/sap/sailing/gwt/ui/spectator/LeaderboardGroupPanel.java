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
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.sap.sailing.domain.common.EventNameAndRaceName;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.components.HasWelcomeWidget;

public class LeaderboardGroupPanel extends FormPanel implements HasWelcomeWidget {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml anchor(String url, String displayName);
    }

    private static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    private static final int MAX_COLUMNS_IN_ROW = 10; 
    
    private SailingServiceAsync sailingService;
    private StringMessages stringConstants;
    private ErrorReporter errorReporter;
    private LeaderboardGroupDTO group;
    
    private VerticalPanel mainPanel;
    private Widget welcomeWidget = null;
    
    public LeaderboardGroupPanel(SailingServiceAsync sailingService, StringMessages stringConstants,
            ErrorReporter errorReporter, final String groupName) {
        super();
        this.sailingService = sailingService;
        this.stringConstants = stringConstants;
        this.errorReporter = errorReporter;

        mainPanel = new VerticalPanel();
        mainPanel.setWidth("95%");
        add(mainPanel);
        final Runnable buildGUI = new Runnable() {
            @Override
            public void run() {
                buildGUI();
            }
        };
        loadGroup(groupName, buildGUI);
    }

    private void loadGroup(final String groupName, final Runnable actionsAfterLoading) {
        sailingService.getLeaderboardGroupByName(groupName, new AsyncCallback<LeaderboardGroupDTO>() {
            @Override
            public void onSuccess(LeaderboardGroupDTO group) {
                if (group != null) {
                    LeaderboardGroupPanel.this.group = group;
                    if (actionsAfterLoading != null) {
                        actionsAfterLoading.run();
                    }
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
        //Create group details GUI
        HorizontalPanel groupDetailsPanel = new HorizontalPanel();
        groupDetailsPanel.setSpacing(8);
        mainPanel.add(groupDetailsPanel);
        
        Label groupNameLabel = new Label(group.name + ":");
        groupDetailsPanel.add(groupNameLabel);
        
        //Using HTML to display the line breaks in the description
        HTML groupDescriptionLabel = new HTML(new SafeHtmlBuilder().appendEscapedLines(group.description).toSafeHtml());
        groupDetailsPanel.add(groupDescriptionLabel);
        
        //Create group leaderboards GUI
        CaptionPanel leaderboardsCaptionPanel = new CaptionPanel(stringConstants.leaderboards());
        mainPanel.add(leaderboardsCaptionPanel);
        
        if (group.leaderboards.size() <= MAX_COLUMNS_IN_ROW) {
            CellTable<Integer> leaderboardsTable = new CellTable<Integer>();
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
            leaderboardsCaptionPanel.add(leaderboardsTable);
        } else {
            AnchorCell nameAnchorCell = new AnchorCell();
            Column<LeaderboardDTO, SafeHtml> nameColumn = new Column<LeaderboardDTO, SafeHtml>(nameAnchorCell) {
                @Override
                public SafeHtml getValue(LeaderboardDTO leaderboard) {
                    String debugParam = Window.Location.getParameter("gwt.codesvr");
                    return ANCHORTEMPLATE.anchor("/gwt/Leaderboard.html?name=" + leaderboard.name
                            + "&leaderboardGroupName=" + group.name
                            + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""),
                            leaderboard.name);
                }
            };

            SafeHtmlCell racesCell = new SafeHtmlCell();
            Column<LeaderboardDTO, SafeHtml> racesColumn = new Column<LeaderboardDTO, SafeHtml>(racesCell) {
                @Override
                public SafeHtml getValue(LeaderboardDTO leaderboard) {
                    return leaderboardRacesToHtml(leaderboard);
                }
            };

            CellTable<LeaderboardDTO> leaderboardsTable = new CellTable<LeaderboardDTO>();
            leaderboardsTable.setWidth("100%");
            leaderboardsTable.setSelectionModel(new NoSelectionModel<LeaderboardDTO>());
            
            leaderboardsTable.addColumn(nameColumn, stringConstants.name());
            leaderboardsTable.setColumnWidth(nameColumn, "30%");
            leaderboardsTable.addColumn(racesColumn, stringConstants.races());
            leaderboardsTable.setColumnWidth(racesColumn, "70%");
            
            leaderboardsTable.setRowData(group.leaderboards);
            leaderboardsCaptionPanel.add(leaderboardsTable);
        }
    }
    
    private SafeHtml raceToRaceBoardLink(LeaderboardDTO leaderboard, RaceInLeaderboardDTO race) {
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        String displayName = race.getRaceColumnName();
        if (race.getRaceColumnName().equals(stringConstants.overview())) {
            String link = "/gwt/Leaderboard.html?name=" + leaderboard.name + "&leaderboardGroupName=" + group.name +
                  (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr="+debugParam : "");
            b.append(ANCHORTEMPLATE.anchor(link, displayName));
        } else {
            if (race.getRaceIdentifier() != null) {
                EventNameAndRaceName raceId = (EventNameAndRaceName) race.getRaceIdentifier();
                String link = "/gwt/RaceBoard.html?leaderboardName=" + leaderboard.name + "&raceName=" + raceId.getRaceName()
                        + "&eventName=" + raceId.getEventName() + "&leaderboardGroupName=" + group.name
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : "");
                b.append(ANCHORTEMPLATE.anchor(link, displayName));
            } else {
                b.appendHtmlConstant(race.getRaceColumnName());
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
                String link = "/gwt/RaceBoard.html?leaderboardName=" + leaderboard.name + "&raceName="
                        + raceId.getRaceName() + "&eventName=" + raceId.getEventName() + "&leaderboardGroupName="
                        + group.name + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : "");
                b.append(ANCHORTEMPLATE.anchor(link, race.getRaceColumnName()));
            } else {
                b.appendHtmlConstant(race.getRaceColumnName());
            }
            first = false;
        }
        return b.toSafeHtml();
    }

    @Override
    public void setWelcomeWidgetVisible(boolean isVisible) {
        if (welcomeWidget != null) {
            welcomeWidget.setVisible(isVisible);
        }
    }

    @Override
    public void setWelcomeWidget(Widget welcome) {
        boolean needsToBeInserted = welcomeWidget == null;
        welcomeWidget = welcome;
        welcomeWidget.setWidth("100%");
        if (needsToBeInserted) {
            mainPanel.insert(welcomeWidget, 0);
        }
    }

}
