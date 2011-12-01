package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.EventNameAndRaceName;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

public class LeaderboardConfigPanel extends FormPanel implements EventDisplayer, RaceSelectionChangeListener {

    private static final int MAX_NUMBER_OF_DISCARDED_RESULTS = 4;

//    private final RaceTreeView raceTree;
    private final TrackedEventsComposite trackedEventsComposite;
  
    private final StringConstants stringConstants;
    
    private final SailingServiceAsync sailingService;
    
    private final List<String> leaderboardNames;
    
    private final ErrorReporter errorReporter;

    private final ListBox leaderboardsListBox;
    
    private final Label[] discardThresholdLabelsForSelectedLeaderboard;

    private final ListBox columnNamesInSelectedLeaderboardListBox;

    private final CheckBox medalRaceCheckBox;
    
    private LeaderboardDAO selectedLeaderboard;

    private final Button renameLeaderboardButton;

    private final Button removeLeaderboardButton;

    private final Button addColumnButton;

    private final Button columnRenameButton;

    private final Button columnRemoveButton;
    
    private final Button columnMoveUpButton;
    
    private final Button columnMoveDownButton;

    private final Button unlinkRaceColumnFromTrackedRaceButton;

    private final Anchor openLeaderboardLink;
    
    private final Anchor editLeaderboardScoresLink;

    public LeaderboardConfigPanel(SailingServiceAsync sailingService, AdminConsole adminConsole,
            ErrorReporter errorReporter, StringConstants stringConstants) {
        this.stringConstants = stringConstants;
        this.sailingService = sailingService;
        leaderboardNames = new ArrayList<String>();
        this.errorReporter = errorReporter;
        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                LeaderboardConfigPanel.this.leaderboardNames.addAll(leaderboardNames);
                updateLeaderboardNamesListBox();
            }
            @Override
            public void onFailure(Throwable t) {
                LeaderboardConfigPanel.this.errorReporter.reportError("Error trying to obtain list of leaderboard names: "+t.getMessage());
            }
        });
        Grid grid = new Grid(5, 2);
        setWidget(grid);
        grid.setSize("100%", "100%");
        Label lblLeaderboards = new Label("Leaderboards");
        grid.setWidget(0, 0, lblLeaderboards);
        
        // ----------- leaderboard creation / removal ----------- 
        HorizontalPanel leaderboardInfoPanel = new HorizontalPanel();
        leaderboardsListBox = new ListBox(/* isMultipleSelect */ false);
        leaderboardsListBox.setVisibleItemCount(10);
        leaderboardsListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent e) {
                leaderboardSelectionChanged();
            }
        });
        leaderboardInfoPanel.add(leaderboardsListBox);
        Grid discardThresholdsGrid = new Grid(5, MAX_NUMBER_OF_DISCARDED_RESULTS+1);
        discardThresholdsGrid.setWidget(0, 0, new Label(stringConstants.discardRacesFromHowManyStartedRacesOn()));
        discardThresholdLabelsForSelectedLeaderboard = new Label[MAX_NUMBER_OF_DISCARDED_RESULTS];
        discardThresholdsGrid.setWidget(1, 0, new Label(stringConstants.discarding()));
        discardThresholdsGrid.setWidget(2, 0, new Label(stringConstants.startingFromNumberOfRaces()));
        for (int i=0; i<MAX_NUMBER_OF_DISCARDED_RESULTS; i++) {
            discardThresholdsGrid.setWidget(1, i+1, new Label(""+(i+1)));
            discardThresholdLabelsForSelectedLeaderboard[i] = new Label();
            discardThresholdsGrid.setWidget(2, i+1, discardThresholdLabelsForSelectedLeaderboard[i]);
        }
        openLeaderboardLink = new Anchor(stringConstants.openSelectedLeaderboard());
        openLeaderboardLink.setEnabled(false);
        discardThresholdsGrid.setWidget(3, 0, openLeaderboardLink);
        editLeaderboardScoresLink = new Anchor(stringConstants.editScores());
        editLeaderboardScoresLink.setEnabled(false);
        discardThresholdsGrid.setWidget(4, 0, editLeaderboardScoresLink);
        leaderboardInfoPanel.add(discardThresholdsGrid);
        grid.setWidget(1, 0, leaderboardInfoPanel);
        VerticalPanel verticalPanel = new VerticalPanel();
        grid.setWidget(1, 1, verticalPanel);
        Button btnNew = new Button(stringConstants.newDotDotDot());
        verticalPanel.add(btnNew);
        btnNew.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                addNewLeaderboard();
            }
        });
        renameLeaderboardButton = new Button(stringConstants.renameDotDotDot());
        verticalPanel.add(renameLeaderboardButton);
        renameLeaderboardButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                renameSelectedLeaderboard();
            }
        });
        removeLeaderboardButton = new Button(stringConstants.remove());
        verticalPanel.add(removeLeaderboardButton);
        removeLeaderboardButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeSelectedLeaderboard();
            }
        });
        Button stressTestButton = new Button(stringConstants.stressTest());
        verticalPanel.add(stressTestButton);
        stressTestButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                performStressTestForSelectedLeaderboard();
            }
        });

        // ------------ specific to selected leaderboard ----------------
        Label lblRaceNamesIn = new Label(stringConstants.columnNamesInSelectedLeaderboard());
        grid.setWidget(2, 0, lblRaceNamesIn);
        
        HorizontalPanel selectedLeaderboardPanel = new HorizontalPanel();
        selectedLeaderboardPanel.setSpacing(10);
        grid.setWidget(3, 0, selectedLeaderboardPanel);
        columnNamesInSelectedLeaderboardListBox = new ListBox();
        selectedLeaderboardPanel.add(columnNamesInSelectedLeaderboardListBox);
        columnNamesInSelectedLeaderboardListBox.setVisibleItemCount(10);
        columnNamesInSelectedLeaderboardListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent e) {
                leaderboardRaceColumnSelectionChanged();
            }
        });
        medalRaceCheckBox = new CheckBox(stringConstants.medalRace());
        medalRaceCheckBox.setEnabled(true); // only displays the immutable medal race property
        selectedLeaderboardPanel.add(medalRaceCheckBox);
        medalRaceCheckBox.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				changeIsMedalRace(((CheckBox) event.getSource()).getValue());
			}
		});

        VerticalPanel labelAndTreePanel = new VerticalPanel();
        Label lblTrackedRaceConnected = new Label(stringConstants.trackedRaceConnectedToSelectedRaceName());
        labelAndTreePanel.add(lblTrackedRaceConnected);

        trackedEventsComposite = new TrackedEventsComposite(sailingService, errorReporter, adminConsole,
                stringConstants, /* multiselection */ false);
 
        labelAndTreePanel.add(trackedEventsComposite);
        trackedEventsComposite.addRaceSelectionChangeListener(this);
        selectedLeaderboardPanel.add(labelAndTreePanel);
        
        VerticalPanel buttonPanelForSelectedLeaderboardDetails = new VerticalPanel();
        grid.setWidget(3, 1, buttonPanelForSelectedLeaderboardDetails);
        
        addColumnButton = new Button(stringConstants.addDotDotDot());
        buttonPanelForSelectedLeaderboardDetails.add(addColumnButton);
        addColumnButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addColumnToSelectedLeaderboard();
            }
        });
        
        columnRenameButton = new Button(stringConstants.renameDotDotDot());
        buttonPanelForSelectedLeaderboardDetails.add(columnRenameButton);
        columnRenameButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                renameSelectedLeaderboardColumn();
            }
        });
        columnRemoveButton = new Button(stringConstants.remove());
        buttonPanelForSelectedLeaderboardDetails.add(columnRemoveButton);
        columnRemoveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeSelectedLeaderboardColumn();
            }
        });
        columnMoveUpButton = new Button(stringConstants.columnMoveUp());
        buttonPanelForSelectedLeaderboardDetails.add(columnMoveUpButton);
        columnMoveUpButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	moveSelectedLeaderboardColumnUp();
            }
        });
        columnMoveDownButton = new Button(stringConstants.columnMoveDown());
        buttonPanelForSelectedLeaderboardDetails.add(columnMoveDownButton);
        columnMoveDownButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	moveSelectedLeaderboardColumnDown();
            }
        });
        
        unlinkRaceColumnFromTrackedRaceButton = new Button(stringConstants.unlink());
        buttonPanelForSelectedLeaderboardDetails.add(unlinkRaceColumnFromTrackedRaceButton);
        unlinkRaceColumnFromTrackedRaceButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                unlinkSelectedRaceColumnFromTrackedRace();
            }
        });

        leaderboardSelectionChanged();
        leaderboardRaceColumnSelectionChanged();
    }

    private void performStressTestForSelectedLeaderboard() {
        final int selectedIndex = leaderboardsListBox.getSelectedIndex();
        if (selectedIndex >= 0) {
            final String leaderboardName = leaderboardsListBox.getItemText(selectedIndex);
            sailingService.stressTestLeaderboardByName(leaderboardName, 100, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error during leaderboard stress test for " + leaderboardName + ": "
                            + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    errorReporter.reportError("Stress test for " + leaderboardName + " finished successfully ");
                }
            });
        }
    }

    private void unlinkSelectedRaceColumnFromTrackedRace() {
        final String selectedRaceColumnName = getSelectedRaceColumnName();
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        sailingService.disconnectLeaderboardColumnFromTrackedRace(selectedLeaderboardName, selectedRaceColumnName, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to unlink tracked race from column "+selectedRaceColumnName+
                        " from leaderboard "+selectedLeaderboardName+": "+t.getMessage());
            }

            @Override
            public void onSuccess(Void arg0) {
                trackedEventsComposite.clearSelection();
            }
        });
    }

    private void removeSelectedLeaderboardColumn() {
        final int selectedIndex = columnNamesInSelectedLeaderboardListBox.getSelectedIndex();
        if (selectedIndex >= 0) {
            final String selectedRaceColumnName = columnNamesInSelectedLeaderboardListBox.getItemText(selectedIndex);
            sailingService.removeLeaderboardColumn(getSelectedLeaderboardName(), selectedRaceColumnName, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable t) {
                    errorReporter
                    .reportError("Error trying to remove leaderboard race column "
                            + selectedRaceColumnName + " in leaderboard "
                            + getSelectedLeaderboardName()+": "+
                            t.getMessage());
                }

                @Override
                public void onSuccess(Void arg0) {
                    columnNamesInSelectedLeaderboardListBox.removeItem(selectedIndex);
                    //selectedLeaderboard.raceNamesAndMedalRaceAndTracked.remove(selectedRaceColumnName);
                    selectedLeaderboard.removeRace(selectedRaceColumnName);
                    selectedLeaderboard.invalidateCompetitorOrdering();
                }
            });
        }
    }

    private void renameSelectedLeaderboardColumn() {
        final int selectedIndex = columnNamesInSelectedLeaderboardListBox.getSelectedIndex();
        if (selectedIndex >= 0) {
            final String selectedRaceColumnName = columnNamesInSelectedLeaderboardListBox.getItemText(selectedIndex);
            TextfieldEntryDialog newNameDialog = new TextfieldEntryDialog(stringConstants.renameRace(),
                    stringConstants.renameRace(), stringConstants.ok(), stringConstants.cancel(),
                    selectedRaceColumnName, new Validator<String>() {
                        @Override
                        public String getErrorMessage(String valueToValidate) {
                            if (valueToValidate == null || valueToValidate.trim().length() == 0) {
                                return stringConstants.pleaseEnterNonEmptyName();
                            } else {
                                return null;
                            }
                        }
                    }, new AsyncCallback<String>() {
                        @Override
                        public void onFailure(Throwable t) {
                        }

                        @Override
                        public void onSuccess(final String newColumnName) {
                            if (!selectedRaceColumnName.equals(newColumnName)) {
                                sailingService.renameLeaderboardColumn(getSelectedLeaderboardName(),
                                        selectedRaceColumnName, newColumnName, new AsyncCallback<Void>() {
                                            @Override
                                            public void onFailure(Throwable t) {
                                                errorReporter
                                                        .reportError("Error trying to rename leaderboard race column "
                                                                + selectedRaceColumnName + " in leaderboard "
                                                                + getSelectedLeaderboardName() + " to " + newColumnName+": "+
                                                                t.getMessage());
                                            }

                                            @Override
                                            public void onSuccess(Void v) {
                                                columnNamesInSelectedLeaderboardListBox.setItemText(selectedIndex,
                                                        newColumnName);
                                                selectedLeaderboard.renameRace(selectedRaceColumnName, newColumnName);
                                            }
                                        });
                            }
                        }
                    });
            newNameDialog.show();
        }
    }
    
    private void moveSelectedLeaderboardColumnUp(){
    	final int selectedIndex = columnNamesInSelectedLeaderboardListBox.getSelectedIndex();
    	if (selectedIndex >= 0){
    		final String selectedRaceColumnName = columnNamesInSelectedLeaderboardListBox.getItemText(selectedIndex);
    		sailingService.moveLeaderboardColumnUp(getSelectedLeaderboardName(), selectedRaceColumnName, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					errorReporter
                    .reportError("Error trying to move leaderboard race column "
                            + selectedRaceColumnName + " in leaderboard "
                            + getSelectedLeaderboardName() + " up: "+
                            caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					String raceName = columnNamesInSelectedLeaderboardListBox.getItemText(selectedIndex);
					int index = selectedIndex;
					index--;
					if (index >= 0){
						columnNamesInSelectedLeaderboardListBox.removeItem(selectedIndex);
						columnNamesInSelectedLeaderboardListBox.insertItem(raceName, index);
						columnNamesInSelectedLeaderboardListBox.setSelectedIndex(index);
						selectedLeaderboard.moveRaceUp(raceName);
					}
				}
			});
    	}
    }
    
    private void moveSelectedLeaderboardColumnDown(){
    	final int selectedIndex = columnNamesInSelectedLeaderboardListBox.getSelectedIndex();
    	if (selectedIndex >= 0){
    		final String selectedRaceColumnName = columnNamesInSelectedLeaderboardListBox.getItemText(selectedIndex);
    		sailingService.moveLeaderboardColumnDown(getSelectedLeaderboardName(), selectedRaceColumnName, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					errorReporter
                    .reportError("Error trying to move leaderboard race column "
                            + selectedRaceColumnName + " in leaderboard "
                            + getSelectedLeaderboardName() + " down: "+
                            caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					String raceName = columnNamesInSelectedLeaderboardListBox.getItemText(selectedIndex);
					int index = selectedIndex;
					index++;
					if (index < columnNamesInSelectedLeaderboardListBox.getItemCount()){
						columnNamesInSelectedLeaderboardListBox.removeItem(selectedIndex);
						columnNamesInSelectedLeaderboardListBox.insertItem(raceName, index);
						columnNamesInSelectedLeaderboardListBox.setSelectedIndex(index);
						selectedLeaderboard.moveRaceDown(raceName);
					}
				}
			});
    	}
    }

    private void leaderboardRaceColumnSelectionChanged() {
        String selectedRaceColumnName = getSelectedRaceColumnName();
        if (selectedRaceColumnName != null) {
            medalRaceCheckBox.setValue(selectedLeaderboard.raceIsMedalRace(selectedRaceColumnName));
            medalRaceCheckBox.setEnabled(true);
            columnRenameButton.setEnabled(true);
            columnRemoveButton.setEnabled(true);
            columnMoveUpButton.setEnabled(true);
            columnMoveDownButton.setEnabled(true);
            unlinkRaceColumnFromTrackedRaceButton.setEnabled(true);
            selectTrackedRaceInRaceTree();
        } else {
        	medalRaceCheckBox.setValue(false);
        	medalRaceCheckBox.setEnabled(false);
            columnRenameButton.setEnabled(false);
            columnRemoveButton.setEnabled(false);
            columnMoveUpButton.setEnabled(false);
            columnMoveDownButton.setEnabled(false);
            trackedEventsComposite.clearSelection();
            unlinkRaceColumnFromTrackedRaceButton.setEnabled(false);
        }
    }
    
    private void selectTrackedRaceInRaceTree() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String selectedRaceColumnName = getSelectedRaceColumnName();
        sailingService.getEventAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(
                selectedLeaderboardName, selectedRaceColumnName, new AsyncCallback<Pair<String,String>>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to determine tracked race linked to race column "+
                                selectedRaceColumnName+" in leaderboard "+selectedLeaderboardName+": "+
                                t.getMessage());
                    }

                    @Override
                    public void onSuccess(Pair<String, String> eventAndRaceName) {
                        if (eventAndRaceName != null) {
                            selectRaceInTree(eventAndRaceName.getA(), eventAndRaceName.getB());
                        } else {
                            trackedEventsComposite.clearSelection();
                        }
                    }
                });
    }

    private void selectRaceInTree(String eventName, String raceName) {
        trackedEventsComposite.selectRaceByName(eventName, raceName);
    }

    private String getSelectedRaceColumnName() {
        int selectedIndex = columnNamesInSelectedLeaderboardListBox.getSelectedIndex();
        return selectedIndex >= 0 ? columnNamesInSelectedLeaderboardListBox.getItemText(selectedIndex) : null;
    }

    private void addColumnToSelectedLeaderboard() {
        final String leaderboardName = getSelectedLeaderboardName();
        final TextfieldEntryDialogWithCheckbox box = new TextfieldEntryDialogWithCheckbox(
                stringConstants.addColumnToLeaderboard(), stringConstants.pleaseEnterNameForNewRaceColumn(),
                stringConstants.ok(), stringConstants.cancel(), stringConstants.medalRace(), /* initialValue */"",
                new Validator<Pair<String, Boolean>>() {
                    @Override
                    public String getErrorMessage(Pair<String, Boolean> valueToValidate) {
                        if (valueToValidate.getA() == null || valueToValidate.getA().trim().length() == 0) {
                            return stringConstants.pleaseEnterNonEmptyName();
                        } else {
                            return null;
                        }
                    }
                }, new AsyncCallback<Pair<String, Boolean>>() {
                    @Override
                    public void onFailure(Throwable t) {}
                    @Override
                    public void onSuccess(final Pair<String, Boolean> columnNameAndMedalRace) {
                        sailingService.addColumnToLeaderboard(columnNameAndMedalRace.getA(), leaderboardName, /* medalRace */ columnNameAndMedalRace.getB(),
                                new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable t) {
                                        errorReporter.reportError("Error trying to add column "
                                                + columnNameAndMedalRace.getA() + " to leaderboard " + leaderboardName
                                                + ": " + t.getMessage());
                                    }

                            @Override
                            public void onSuccess(Void v) {
                                columnNamesInSelectedLeaderboardListBox.addItem(columnNameAndMedalRace.getA());
                                //selectedLeaderboard.raceNamesAndMedalRaceAndTracked.put(columnNameAndMedalRace.getA(),
                                //        new Pair<Boolean, Boolean>(/* medal race */ columnNameAndMedalRace.getB(),
                                //                /* tracked */ false));
                                selectedLeaderboard.addRace(columnNameAndMedalRace.getA(), columnNameAndMedalRace.getB(), false);
                                selectedLeaderboard.invalidateCompetitorOrdering();
                            }
                        });
                    }
                });
        box.show();
    }

    private void renameSelectedLeaderboard() {
        final int selectedIndex = leaderboardsListBox.getSelectedIndex();
        final String leaderboardName = leaderboardsListBox.getItemText(selectedIndex);
        final DialogBox leaderboardRenameDialogBox = new DialogBox();
        leaderboardRenameDialogBox.setText(stringConstants.renameLeaderboard()+" "+leaderboardName);
        leaderboardRenameDialogBox.setAnimationEnabled(true);
        final Button okButton = new Button("OK");
        okButton.setEnabled(leaderboardName.trim().length()>0);
        VerticalPanel dialogVPanel = new VerticalPanel();
        final Label statusLabel = new Label();
        dialogVPanel.add(statusLabel);
        dialogVPanel.add(new Label(stringConstants.leaderboardName()));
        final TextBox leaderboardNameField = new TextBox();
        leaderboardNameField.setText(leaderboardName);
        AbstractEntryPoint.addFocusUponKeyUpToggler(leaderboardNameField);
        leaderboardNameField.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (leaderboardNameField.getValue() != null && leaderboardNameField.getValue().trim().length() > 0) {
                    statusLabel.setText("");
                    okButton.setEnabled(true);
                } else {
                    statusLabel.setText(stringConstants.pleaseEnterNonEmptyName());
                    okButton.setEnabled(false);
                }
            }
        });
        dialogVPanel.add(leaderboardNameField);
        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        HorizontalPanel buttonPanel = new HorizontalPanel();
        dialogVPanel.add(buttonPanel);
        buttonPanel.add(okButton);
        Button cancelButton = new Button(stringConstants.cancel());
        buttonPanel.add(cancelButton);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                leaderboardRenameDialogBox.hide();
            }
        });
        leaderboardRenameDialogBox.setWidget(dialogVPanel);
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                leaderboardRenameDialogBox.hide();
                final String newLeaderboardName = leaderboardNameField.getText().trim();
                sailingService.renameLeaderboard(leaderboardName, newLeaderboardName,
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to rename leaderboard "+leaderboardName+" to "+
                                        newLeaderboardName+": "+caught.getMessage());
                            }
                            @Override
                            public void onSuccess(Void v) {
                                leaderboardsListBox.setItemText(selectedIndex, newLeaderboardName);
                                leaderboardNames.set(selectedIndex, newLeaderboardName);
                                updateLeaderboardDependentLinks(newLeaderboardName);
                            }
                        });
            }
        });
        AbstractEntryPoint.linkEnterToButton(okButton, leaderboardNameField);
        AbstractEntryPoint.linkEscapeToButton(cancelButton, leaderboardNameField);
        leaderboardRenameDialogBox.center();
        leaderboardNameField.setFocus(true);
    }

    private String getSelectedLeaderboardName() {
        int selectedIndex = leaderboardsListBox.getSelectedIndex();
        return selectedIndex >= 0 ? leaderboardsListBox.getItemText(selectedIndex) : null;
    }

    private void updateLeaderboardDependentLinks(final String leaderboardName) {
        openLeaderboardLink.setHref("/Leaderboard.html?name="+leaderboardName);
        editLeaderboardScoresLink.setHref("/LeaderboardEditing.html?name="+leaderboardName);
    }

    private void setLeaderboardDependentLinksEnabled(boolean enabled) {
        openLeaderboardLink.setEnabled(enabled);
        editLeaderboardScoresLink.setEnabled(enabled);
    }

    private void leaderboardSelectionChanged() {
        final String leaderboardName = getSelectedLeaderboardName();
        if (leaderboardName != null) {
            sailingService.getLeaderboardByName(leaderboardName, new Date(),
                    /* namesOfRacesForWhichToLoadLegDetails */ null, new AsyncCallback<LeaderboardDAO>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to fetch leaderboard " + leaderboardName
                            + " from the server: " + caught.getMessage());
                }

                @Override
                public void onSuccess(LeaderboardDAO result) {
                    selectedLeaderboard = result;
                    updateLeaderboardDisplays();
                    addColumnButton.setEnabled(true);
                    renameLeaderboardButton.setEnabled(true);
                    removeLeaderboardButton.setEnabled(true);
                    setLeaderboardDependentLinksEnabled(true);
                    updateLeaderboardDependentLinks(leaderboardName);
                }
            });
        } else {
            selectedLeaderboard = null;
            addColumnButton.setEnabled(false);
            renameLeaderboardButton.setEnabled(false);
            removeLeaderboardButton.setEnabled(false);
            columnNamesInSelectedLeaderboardListBox.clear();
            leaderboardRaceColumnSelectionChanged();
            setLeaderboardDependentLinksEnabled(false);
        }
    }

    private void updateLeaderboardDisplays() {
        int i=0;
        for (int discardThreshold : selectedLeaderboard.discardThresholds) {
            discardThresholdLabelsForSelectedLeaderboard[i++].setText(""+discardThreshold);
        }
        while (i<MAX_NUMBER_OF_DISCARDED_RESULTS) {
            discardThresholdLabelsForSelectedLeaderboard[i++].setText("");
        }
        columnNamesInSelectedLeaderboardListBox.clear();
        for (String columnName : selectedLeaderboard.getRaceColumnNameList()) {
            columnNamesInSelectedLeaderboardListBox.addItem(columnName);
        }
        leaderboardRaceColumnSelectionChanged();
    }

    private void updateLeaderboardNamesListBox() {
        leaderboardsListBox.clear();
        for (String leaderboardName : leaderboardNames) {
            leaderboardsListBox.addItem(leaderboardName);
        }
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        trackedEventsComposite.fillEvents(result);
    }

    private void addNewLeaderboard() {
        LeaderboardCreationDialog dialog = new LeaderboardCreationDialog(Collections.unmodifiableCollection(leaderboardNames),
                stringConstants, errorReporter, new AsyncCallback<Pair<String,String[]>>() {
                    @Override
                    public void onFailure(Throwable arg0) {}
                    @Override
                    public void onSuccess(Pair<String, String[]> result) {
                        List<Integer> discardThresholds = new ArrayList<Integer>();
                        for (int i=0; i<result.getB().length; i++) {
                            if (result.getB()[i] != null && result.getB()[i].trim().length() > 0) {
                                try {
                                    discardThresholds.add(Integer.valueOf(result.getB()[i].trim()));
                                } catch (NumberFormatException e) {
                                    errorReporter.reportError("Internal error; NumberFormatException for "+result.getB()[i]+
                                            " which should have been caught by validation before");
                                }
                            }
                        }
                        int[] discanrdThresholdsAsIntArray = new int[discardThresholds.size()];
                        int i=0;
                        for (Integer integer : discardThresholds) {
                            discanrdThresholdsAsIntArray[i++] = integer;
                        }
                        createNewLeaderboard(result.getA(), discanrdThresholdsAsIntArray);
                    }
                });
        dialog.show();
    }

    private void createNewLeaderboard(final String leaderboardName, int[] discardThresholds) {
        sailingService.createLeaderboard(leaderboardName, discardThresholds, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new leaderboard "+leaderboardName+": "+t.getMessage());
            }
            @Override
            public void onSuccess(Void result) {
                leaderboardNames.add(leaderboardName);
                leaderboardsListBox.addItem(leaderboardName);
                leaderboardsListBox.setSelectedIndex(leaderboardsListBox.getItemCount()-1);
                leaderboardSelectionChanged();
            }
        });
    }


    private void removeSelectedLeaderboard() {
        final int selectedIndex = leaderboardsListBox.getSelectedIndex();
        final String leaderboardName = leaderboardsListBox.getItemText(selectedIndex);    
        sailingService.removeLeaderboard(leaderboardName, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to remove leaderboard "+leaderboardName+": "+caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                leaderboardNames.remove(selectedIndex);
                leaderboardsListBox.removeItem(selectedIndex);
                leaderboardSelectionChanged();
            }
        });
    }

    @Override
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        if (selectedRaces.isEmpty()) {
            unlinkRaceColumnFromTrackedRaceButton.setEnabled(false);
        } else {
            if (getSelectedRaceColumnName() != null) {
                linkTrackedRaceForRaceColumn(selectedRaces.iterator().next());
            } else {
                unlinkRaceColumnFromTrackedRaceButton.setEnabled(false);
            }
        }
    }

    private void linkTrackedRaceForRaceColumn(final Triple<EventDAO, RegattaDAO, RaceDAO> selectedRace) {
        sailingService.connectTrackedRaceToLeaderboardColumn(getSelectedLeaderboardName(), getSelectedRaceColumnName(),
                new EventNameAndRaceName(selectedRace.getA().name, selectedRace.getC().name), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to link tracked race " + selectedRace.getC().name
                                + " of event " + selectedRace.getA().name + " to race column named "
                                + getSelectedRaceColumnName() + " of leaderboard " + getSelectedLeaderboardName()
                                + ": " + t.getMessage());
                        trackedEventsComposite.clearSelection();
                    }

                    @Override
                    public void onSuccess(Void arg0) {
                        unlinkRaceColumnFromTrackedRaceButton.setEnabled(true);
                    }
                });
    }

    private void changeIsMedalRace(final boolean isMedalRace) {
        sailingService.updateIsMedalRace(getSelectedLeaderboardName(), getSelectedRaceColumnName(), isMedalRace,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to change isMedalRace of "
                                + getSelectedRaceColumnName() + ": " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        selectedLeaderboard.setIsMedalRace(getSelectedRaceColumnName(), isMedalRace);
                    }
                });
    }
}