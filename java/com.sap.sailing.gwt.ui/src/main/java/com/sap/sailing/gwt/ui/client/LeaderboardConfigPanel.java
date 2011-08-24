package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.shared.EventDAO;

public class LeaderboardConfigPanel extends FormPanel implements EventDisplayer {

    private RaceTreeView raceTree;
    
    private final StringConstants stringConstants;
    
    private final SailingServiceAsync sailingService;
    
    private final List<String> leaderboardNames;
    
    private final ErrorReporter errorReporter;

    private final ListBox leaderboardsListBox;

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
        Grid grid = new Grid(6, 2);
        setWidget(grid);
        grid.setSize("100%", "100%");
        
        Label lblLeaderboards = new Label("Leaderboards");
        grid.setWidget(0, 0, lblLeaderboards);
        
        leaderboardsListBox = new ListBox(/* isMultipleSelect */ false);
        grid.setWidget(1, 0, leaderboardsListBox);
        leaderboardsListBox.setVisibleItemCount(10);
        
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
        Button btnEditScores = new Button(stringConstants.editScores());
        verticalPanel.add(btnEditScores);
        btnEditScores.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
        
        Button btnRemove = new Button(stringConstants.remove());
        verticalPanel.add(btnRemove);
        btnRemove.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeSelectedLeaderboard();
            }
        });
        
        Label lblRaceNamesIn = new Label(stringConstants.columnNamesInSelectedLeaderboard());
        grid.setWidget(2, 0, lblRaceNamesIn);
        
        ListBox listBox_1 = new ListBox();
        grid.setWidget(3, 0, listBox_1);
        listBox_1.setVisibleItemCount(5);
        
        VerticalPanel verticalPanel_1 = new VerticalPanel();
        grid.setWidget(3, 1, verticalPanel_1);
        
        Button btnAdd = new Button(stringConstants.addDotDotDot());
        verticalPanel_1.add(btnAdd);
        
        Button btnRename = new Button(stringConstants.renameDotDotDot());
        verticalPanel_1.add(btnRename);
        
        Button btnRemove_1 = new Button(stringConstants.remove());
        verticalPanel_1.add(btnRemove_1);
        
        Label lblTrackedRaceConnected = new Label(stringConstants.trackedRaceConnectedToSelectedRaceName());
        grid.setWidget(4, 0, lblTrackedRaceConnected);
        
        raceTree = new RaceTreeView(stringConstants, /* multiselection */ false);
        grid.setWidget(5, 0, raceTree);
        
        VerticalPanel verticalPanel_2 = new VerticalPanel();
        grid.setWidget(5, 1, verticalPanel_2);
        
        Button btnLinkToColumn = new Button(stringConstants.linkToColumn());
        verticalPanel_2.add(btnLinkToColumn);
        
        Button btnUnlink = new Button(stringConstants.unlink());
        verticalPanel_2.add(btnUnlink);
    }

    private void updateLeaderboardNamesListBox() {
        leaderboardsListBox.clear();
        for (String leaderboardName : leaderboardNames) {
            leaderboardsListBox.addItem(leaderboardName);
        }
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        raceTree.fillEvents(result);
    }

    private void addNewLeaderboard() {
        final DialogBox myErrorDialogBox = new DialogBox();
        myErrorDialogBox.setText(stringConstants.createNewLeaderboard());
        myErrorDialogBox.setAnimationEnabled(true);
        final Button okButton = new Button("OK");
        okButton.setEnabled(false);
        VerticalPanel dialogVPanel = new VerticalPanel();
        final Label statusLabel = new Label(stringConstants.pleaseEnterNonEmptyName());
        dialogVPanel.add(statusLabel);
        final TextBox[] discardThresholdBoxes = new TextBox[4];
        dialogVPanel.add(new Label(stringConstants.leaderboardName()));
        final TextBox leaderboardNameField = new TextBox();
        AbstractEntryPoint.addFocusUponKeyUpToggler(leaderboardNameField);
        leaderboardNameField.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                enableOkButtonIfValid(okButton, leaderboardNameField, discardThresholdBoxes, statusLabel);
            }
        });
        dialogVPanel.add(leaderboardNameField);
        dialogVPanel.add(new Label(stringConstants.discardRacesFromHowManyStartedRacesOn()));
        HorizontalPanel hp = new HorizontalPanel();
        for (int i=0; i<discardThresholdBoxes.length; i++) {
            hp.add(new Label(""+(i+1)+"."));
            TextBox tb = new TextBox();
            tb.setVisibleLength(2);
            AbstractEntryPoint.addFocusUponKeyUpToggler(tb);
            tb.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    enableOkButtonIfValid(okButton, leaderboardNameField, discardThresholdBoxes, statusLabel);
                }
            });
            discardThresholdBoxes[i] = tb;
            hp.add(tb);
        }
        dialogVPanel.add(hp);
        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        HorizontalPanel buttonPanel = new HorizontalPanel();
        dialogVPanel.add(buttonPanel);
        buttonPanel.add(okButton);
        Button cancelButton = new Button(stringConstants.cancel());
        buttonPanel.add(cancelButton);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                myErrorDialogBox.hide();
            }
        });
        myErrorDialogBox.setWidget(dialogVPanel);
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                myErrorDialogBox.hide();
                List<Integer> discardThresholds = new ArrayList<Integer>();
                for (int i=0; i<discardThresholdBoxes.length; i++) {
                    if (discardThresholdBoxes[i].getValue() != null && discardThresholdBoxes[i].getValue().length() > 0) {
                        discardThresholds.add(Integer.valueOf(discardThresholdBoxes[i].getValue()));
                    }
                }
                int[] discanrdThresholdsAsIntArray = new int[discardThresholds.size()];
                int i=0;
                for (Integer integer : discardThresholds) {
                    discanrdThresholdsAsIntArray[i++] = integer;
                }
                createNewLeaderboard(leaderboardNameField.getText(), discanrdThresholdsAsIntArray);
            }
        });
        AbstractEntryPoint.linkEnterToButton(okButton, leaderboardNameField);
        AbstractEntryPoint.linkEnterToButton(okButton, discardThresholdBoxes);
        AbstractEntryPoint.linkEscapeToButton(cancelButton, leaderboardNameField);
        AbstractEntryPoint.linkEscapeToButton(cancelButton, discardThresholdBoxes);
        myErrorDialogBox.center();
        leaderboardNameField.setFocus(true);
    }

    private void enableOkButtonIfValid(final Button okButton, final TextBox leaderboardNameField, TextBox[] discardThresholdBoxes, Label statusLabel) {
        boolean nonEmpty = leaderboardNameField.getValue() != null && leaderboardNameField.getValue().length() > 0;
        boolean unique = !leaderboardNames.contains(leaderboardNameField.getValue());
        boolean discardThresholdsAscending = true;
        boolean discardThresholdsAreNumeric = discardThresholdBoxes[0].getValue() == null ||
                discardThresholdBoxes[0].getValue().matches("[0-9]*");
        for (int i=1; i<discardThresholdBoxes.length; i++) {
            if (discardThresholdBoxes[i].getValue() != null && discardThresholdBoxes[i].getValue().length() > 0) {
                try {
                    discardThresholdsAscending = discardThresholdsAscending &&
                            discardThresholdBoxes[i-1].getValue() != null && discardThresholdBoxes[i-1].getValue().length() > 0 &&
                            Integer.valueOf(discardThresholdBoxes[i-1].getValue()) < Integer.valueOf(discardThresholdBoxes[i].getValue());
                } catch (NumberFormatException e) {
                    discardThresholdsAreNumeric = false;
                }
            }
        }
        if (!nonEmpty) {
            statusLabel.setText(stringConstants.pleaseEnterNonEmptyName());
        } else if (!unique) {
            statusLabel.setText(stringConstants.leaderboardWithThisNameAlreadyExists());
        } else if (!discardThresholdsAreNumeric) {
            statusLabel.setText(stringConstants.discardThresholdsMustBeNumeric());
        } else if (!discardThresholdsAscending) {
            statusLabel.setText(stringConstants.discardThresholdsMustBeAscending());
        } else {
            statusLabel.setText(" ");
        }
        okButton.setEnabled(nonEmpty && unique && discardThresholdsAreNumeric && discardThresholdsAscending);
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
            }
        });
    }
}