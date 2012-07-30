package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;

/**
 * A panel that has a race selection (inherited from {@link AbstractRaceManagementPanel}) and which adds a table
 * for a selected race showing the race's waypoints together with the number of mark passings already received for that
 * waypoint. Also, the control can be used to send course updates into the tracked race, mostly to simulate these types
 * of events. Conceivably, this may in the future also become a way to set up and edit courses for a tracked race.
 * 
 * @author Frank Mittag (C5163874), Axel Uhl (D043530)
 *
 */
public class RaceCourseManagementPanel extends AbstractRaceManagementPanel {
    private final CellTable<WaypointDTO> courseWaypointsTable;
    private final Label courseDataRequestTimeLabel;
    
    private final ListDataProvider<WaypointDTO> waypointDataProvider;
    
    private final HorizontalPanel courseActionsPanel;

    private final MultiSelectionModel<WaypointDTO> wayPointSelectionModel; 
    
    public RaceCourseManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, final StringMessages stringMessages) {
        super(sailingService, errorReporter, regattaRefresher, stringMessages);

        courseDataRequestTimeLabel = new Label();
        this.selectedRaceContentPanel.add(courseDataRequestTimeLabel);
        
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        courseWaypointsTable = new CellTable<WaypointDTO>(/* pageSize */10000, tableRes);
        this.selectedRaceContentPanel.add(courseWaypointsTable);
        wayPointSelectionModel = new MultiSelectionModel<WaypointDTO>();
        courseWaypointsTable.setSelectionModel(wayPointSelectionModel);
        
        TextColumn<WaypointDTO> nameColumn = new TextColumn<WaypointDTO>() {
            @Override
            public String getValue(WaypointDTO waypointDTO) {
                return waypointDTO.name;
            }
        }; 
        courseWaypointsTable.addColumn(nameColumn, stringMessages.waypoint());

        final SafeHtmlCell buoysCell = new SafeHtmlCell();
        Column<WaypointDTO, SafeHtml> buoysColumn = new Column<WaypointDTO, SafeHtml>(buoysCell) {
            @Override
            public SafeHtml getValue(WaypointDTO waypoint) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                int buoysCount = waypoint.buoys.size();
                int i = 1;
                for (MarkDTO mark : waypoint.buoys) {
                    builder.appendEscaped(mark.name + ", " + stringMessages.position() + ": " + mark.position.toFormattedString());
                    if (i < buoysCount) {
                        builder.appendHtmlConstant("<br>");
                    }
                    i++;
                }
                return builder.toSafeHtml();
            }
        };
        courseWaypointsTable.addColumn(buoysColumn, stringMessages.buoys());
        TextColumn<WaypointDTO> markPassingsCountColumn = new TextColumn<WaypointDTO>() {
            @Override
            public String getValue(WaypointDTO waypointDTO) {
                return "" + waypointDTO.markPassingsCount;
            }
        }; 
        courseWaypointsTable.addColumn(markPassingsCountColumn, stringMessages.markPassing());
        waypointDataProvider = new ListDataProvider<WaypointDTO>();
        waypointDataProvider.addDataDisplay(courseWaypointsTable);
        courseActionsPanel = new HorizontalPanel();
        courseActionsPanel.setSpacing(10);
        Button insertRoundBtn = new Button("Insert round (2 waypoints)");
        insertRoundBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                insertRound(sailingService);
            }

        });
        courseActionsPanel.add(insertRoundBtn);
        Button removeRoundBtn = new Button("Remove Round (2 waypoints)");
        removeRoundBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeRound(sailingService);
            }
        });
        courseActionsPanel.add(removeRoundBtn);
        Button refreshBtn = new Button(stringMessages.refresh());
        refreshBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshSelectedRaceData();
            }
        });
        courseActionsPanel.add(refreshBtn);
        
        courseActionsPanel.setVisible(false);
        this.selectedRaceContentPanel.add(courseActionsPanel);
    }

    private void insertRound(final SailingServiceAsync sailingService) {
        if(wayPointSelectionModel.getSelectedSet().size() != 2) {
            Window.alert("To insert (copy) a round you have to select two waypoints.");
            return;
        } else {
            Map<Integer, String> controlPointsToInsert = new TreeMap<Integer, String>();
            for (WaypointDTO selectedWaypoint: wayPointSelectionModel.getSelectedSet()) {
                controlPointsToInsert.put(selectedWaypoint.courseIndex, selectedWaypoint.name);
            }
            int insertPosition = waypointDataProvider.getList().size()-2;
            sailingService.addWaypointsToRaceCourse(singleSelectedRace,
                    new ArrayList<String>(controlPointsToInsert.values()), insertPosition, new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void arg0) {
                    refreshSelectedRaceData();
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    RaceCourseManagementPanel.this.errorReporter.reportError("Error trying to add a round to race "+
                            singleSelectedRace+": " + caught.getMessage());
                }
            });
        }
    }

    @Override
    void refreshSelectedRaceData() {
        if (singleSelectedRace != null && selectedRaceDTO != null) {
            courseActionsPanel.setVisible(true);
            sailingService.getRaceCourse(singleSelectedRace, new Date(),  new AsyncCallback<RaceCourseDTO>() {
                @Override
                public void onSuccess(RaceCourseDTO result) {
                    updateCourseInfo(result);
                }

                @Override
                public void onFailure(Throwable caught) {
                    RaceCourseManagementPanel.this.errorReporter.reportError("Error trying to obtain the course of the race: " + caught.getMessage());
                }
            });
        } else {
            courseActionsPanel.setVisible(false);
        }
    }

    private void updateCourseInfo(RaceCourseDTO courseDTO) {
        waypointDataProvider.setList(courseDTO.waypoints);
        courseDataRequestTimeLabel.setText(courseDTO.requestTime.toString());
    }

    private void removeRound(final SailingServiceAsync sailingService) {
        if(wayPointSelectionModel.getSelectedSet().size() != 2) {
            Window.alert("To delete a round you have to select two waypoints.");
            return;
        } else {
            Map<Integer, WaypointDTO> controlPointsToDelete = new TreeMap<Integer, WaypointDTO>();
            for(WaypointDTO selectedWaypoint: wayPointSelectionModel.getSelectedSet()) {
                controlPointsToDelete.put(selectedWaypoint.courseIndex, selectedWaypoint);
            }
            sailingService.removeWaypointsFromRaceCourse(singleSelectedRace,
                    new ArrayList<WaypointDTO>(controlPointsToDelete.values()), new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void arg0) {
                    refreshSelectedRaceData();
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    RaceCourseManagementPanel.this.errorReporter.reportError("Error trying to remove a round from race "+
                            singleSelectedRace+": " + caught.getMessage());
                }
            });
        }
    }
}
