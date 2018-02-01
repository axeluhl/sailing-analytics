package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RaceLogCourseManagementWidget extends CourseManagementWidget {
    private final String leaderboardName;
    private final String raceColumnName;
    private final String fleetName;
    private final Button removeMark; 

    public RaceLogCourseManagementWidget(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final String leaderboardName, final String raceColumnName,
            final String fleetName) {
        super(sailingService, errorReporter, stringMessages);

        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;

        Button addMark = new Button(stringMessages.addMarkToRegatta());
        addMark.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new MarkEditDialog(stringMessages, new MarkDTO(null, null), true,
                        new DataEntryDialog.DialogCallback<MarkDTO>() {
                            @Override
                            public void ok(MarkDTO mark) {
                                sailingService.addMarkToRegattaLog(leaderboardName, mark, new AsyncCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        refreshMarks();
                                    }

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError("Could not add mark: " + caught.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void cancel() {
                            }
                        }).show();
            }
        });
        marksBtnsPanel.add(addMark);

        removeMark = new Button(stringMessages.remove(stringMessages.mark()));
        removeMark.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Set<MarkDTO> marksToRemove = marks.getSelectionModel().getSelectedSet();

                for (final MarkDTO markToRemove : marksToRemove) {
                    sailingService.revokeMarkDefinitionEventInRegattaLog(leaderboardName, markToRemove,
                            new AsyncCallback<Void>() {

                                @Override
                                public void onSuccess(Void result) {
                                    refreshMarks();
                                }

                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Removing mark failed: "+caught.getMessage());
                                }
                            });
                }
            }
        });

        removeMark.setEnabled(false);
        marksBtnsPanel.add(removeMark);

        ImagesBarColumn<MarkDTO, RaceLogTrackingCourseDefinitionDialogMarksImagesBarCell> actionColumn = new ImagesBarColumn<MarkDTO, RaceLogTrackingCourseDefinitionDialogMarksImagesBarCell>(
                new RaceLogTrackingCourseDefinitionDialogMarksImagesBarCell(stringMessages));
        actionColumn.setFieldUpdater(new FieldUpdater<MarkDTO, String>() {
            @Override
            public void update(int index, final MarkDTO markDTO, String value) {
                if (RaceLogTrackingCourseDefinitionDialogMarksImagesBarCell.ACTION_PING.equals(value)) {
                    new PositionEntryDialog(stringMessages.pingPosition(stringMessages.mark()), stringMessages,
                            new DataEntryDialog.DialogCallback<Pair<Position, TimePoint>>() {
                                @Override
                                public void ok(Pair<Position, TimePoint> positionAndTimePoint) {
                                    sailingService.pingMark(leaderboardName, markDTO,
                                            positionAndTimePoint.getB(), positionAndTimePoint.getA(), new AsyncCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void result) {
                                                    refresh();
                                                }

                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    errorReporter.reportError("Could not ping mark: "
                                                            + caught.getMessage());
                                                }
                                            });
                                }

                                @Override
                                public void cancel() {
                                }
                            }).show();
                }
            }
        });
        marks.getTable().addColumn(actionColumn);
    }
    
    @Override
    protected void markSelectionChanged() {
        Set<MarkDTO> marksToRemove = marks.getSelectionModel().getSelectedSet();
        sailingService.checkIfMarksAreUsedInOtherRaceLogs(leaderboardName, raceColumnName, fleetName, marksToRemove,
                new AsyncCallback<Pair<Boolean, String>>() {
                    @Override
                    public void onSuccess(Pair<Boolean, String> result) {
                        if (result.getA()) {
                            removeMark.setEnabled(false);
                            removeMark.setTitle(stringMessages.removalOfMarkDisabledMayBeUsedInRaces(result.getB()));
                        } else {
                            removeMark.setEnabled(true);
                            removeMark.setTitle("");
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Could not load course: " + caught.getMessage(), /* silent */ true);
                    }
                });
    }

    @Override
    public void refresh() {
        sailingService.getLastCourseDefinitionInRaceLog(leaderboardName, raceColumnName, fleetName,
                new AsyncCallback<RaceCourseDTO>() {
                    @Override
                    public void onSuccess(RaceCourseDTO result) {
                        updateWaypointsAndControlPoints(result);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Could not load course: " + caught.getMessage());
                    }
                });
        refreshMarks();
    }

    /**
     * Loads the marks data from the server using {@link SailingServiceAsync#getMarksInRegattaLog(String, AsyncCallback)} and updates
     * the {@link #marks} table with the results.
     */
    protected void refreshMarks() {
        sailingService.getMarksInRegattaLog(leaderboardName, new AsyncCallback<Iterable<MarkDTO>>() {
            @Override
            public void onSuccess(Iterable<MarkDTO> result) {
                marks.refresh(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load marks: " + caught.getMessage());
            }
        });
    }
}