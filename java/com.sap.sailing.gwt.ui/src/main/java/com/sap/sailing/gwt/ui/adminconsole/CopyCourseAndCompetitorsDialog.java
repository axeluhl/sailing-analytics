package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.adminconsole.AbstractLeaderboardConfigPanel.RaceColumnDTOAndFleetDTOWithNameBasedEquality;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Distance;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CopyCourseAndCompetitorsDialog extends DataEntryDialog<CourseAndCompetitorCopyOperation> {
    private final ListBox leaderboardDropDown;
    private final RaceTableWrapper<RefreshableMultiSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>> racesTable;
    private final CheckBox courseCheckBox;
    private final CheckBox competitorCheckBox;
    private final CheckBox copyMarkDeviceMappingsCheckBox;
    private final IntegerBox priorityBox;
    private final StringMessages stringMessages;
    private SailingServiceWriteAsync sailingServiceWrite;
    private ErrorReporter errorReporter;
    
    public CopyCourseAndCompetitorsDialog(SailingServiceWriteAsync sailingServiceWrite, ErrorReporter errorReporter, final StringMessages stringMessages,
            Collection<RaceColumnDTOAndFleetDTOWithNameBasedEquality> races,
            RaceColumnDTOAndFleetDTOWithNameBasedEquality raceToExclude, List<StrippedLeaderboardDTO> availableLeaderboardList,
            String leaderboardName, Distance buoyZoneRadius, DialogCallback<CourseAndCompetitorCopyOperation> dialogCallback) {
        super(stringMessages.selectRaces(), stringMessages.selectRaces(), stringMessages.ok(), stringMessages.cancel(),
                new Validator<CourseAndCompetitorCopyOperation>() {
                    @Override
                    public String getErrorMessage(CourseAndCompetitorCopyOperation valueToValidate) {
                        if (valueToValidate.getRaceLogsToCopyTo().isEmpty()) {
                            return stringMessages.selectAtLeastOne();
                        }
                        if (valueToValidate.getPriority() == null || valueToValidate.getPriority() < 0) {
                            return stringMessages.priorityMustBeANonNegativeNumber();
                        }
                        return null;
                    }
        }, true, dialogCallback);
        this.stringMessages = stringMessages;
        leaderboardDropDown = createListBox(/* multi-select */ false);
        copyMarkDeviceMappingsCheckBox = createCheckbox(stringMessages.copyMarkDeviceMappings());
        final List<StrippedLeaderboardDTO> availableLeaderboardsSortedByName = availableLeaderboardList.stream()
                .sorted((lb1, lb2) -> new NaturalComparator().compare(lb1.getName(), lb2.getName()))
                .collect(Collectors.toList());
        fillLeaderboardDropDownAndSelect(availableLeaderboardsSortedByName, leaderboardName);
        racesTable = new RaceTableWrapper<RefreshableMultiSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>>(
                sailingServiceWrite, stringMessages, errorReporter, /* multiSelection */ true);
        racesTable.setSelectedLeaderboardName(leaderboardName);
        racesTable.getDataProvider().getList().addAll(races);
        racesTable.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                validateAndUpdate();
            }
        });
        leaderboardDropDown.addChangeHandler(e->updateRacesTable(leaderboardDropDown.getSelectedValue(), availableLeaderboardList, raceToExclude, leaderboardName));
        courseCheckBox = createCheckbox(stringMessages.copyCourse());
        courseCheckBox.setValue(true);
        competitorCheckBox = createCheckbox(stringMessages.copyCompetitors());
        competitorCheckBox.setValue(false); // competitors are usually registered on the regatta
        priorityBox = createIntegerBox(/* default priority */ 1, /* visibleLength */ 1);
        this.sailingServiceWrite = sailingServiceWrite;
        this.errorReporter = errorReporter;
    }
    
    private void updateRacesTable(String nameOfSelectedLeaderboard,
        List<StrippedLeaderboardDTO> availableLeaderboardList,
        RaceColumnDTOAndFleetDTOWithNameBasedEquality raceToExclude, String fromLeaderboardName) {
        racesTable.getDataProvider().getList().clear();
        final List<RaceColumnDTOAndFleetDTOWithNameBasedEquality> newRaces = new ArrayList<>();
        for (final StrippedLeaderboardDTO leaderboard : availableLeaderboardList) {
            if (leaderboard.getName().equals(nameOfSelectedLeaderboard)) {
                for (final RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
                    for (final FleetDTO fleet : raceColumn.getFleets()) {
                        if (!raceColumn.getName().equals(raceToExclude.getA().getName()) ||
                                !fleet.getName().equals(raceToExclude.getB().getName())) {
                            newRaces.add(new RaceColumnDTOAndFleetDTOWithNameBasedEquality(raceColumn, fleet, leaderboard));
                        }
                    }
                }
            }
        }
        racesTable.refresh(newRaces);
        // make "copy mark device mappings" checkbox visible if the target leaderboard differs from the source leaderboard
        copyMarkDeviceMappingsCheckBox.setVisible(!nameOfSelectedLeaderboard.equals(fromLeaderboardName));
    }

    private void fillLeaderboardDropDownAndSelect(List<StrippedLeaderboardDTO> availableLeaderboardList, String leaderboardNameToSelect) {
        int i=0;
        for (final StrippedLeaderboardDTO leaderboard : availableLeaderboardList) {
            leaderboardDropDown.addItem(leaderboard.getName(), leaderboard.getName());
            if (leaderboard.getName().equals(leaderboardNameToSelect)) {
                leaderboardDropDown.setSelectedIndex(i);
            }
            i++;
        }
    }

    @Override
    protected Widget getAdditionalWidget() {
        final FlowPanel mainPanel = new FlowPanel();
        HorizontalPanel checkBoxPanel = new HorizontalPanel();
        checkBoxPanel.add(courseCheckBox);
        checkBoxPanel.add(competitorCheckBox);
        checkBoxPanel.add(copyMarkDeviceMappingsCheckBox);
        copyMarkDeviceMappingsCheckBox.setVisible(false); // initially the leaderboard drop-down has selected the "from" leaderboard
        mainPanel.add(checkBoxPanel);
        mainPanel.add(leaderboardDropDown);
        mainPanel.add(racesTable);
        final HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(3);
        mainPanel.add(hp);
        hp.add(new Label(stringMessages.authorPriority()));
        hp.add(priorityBox);
        return mainPanel;
    }

    @Override
    protected CourseAndCompetitorCopyOperation getResult() {
        Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality> racesToCopyTo = racesTable.getSelectionModel().getSelectedSet();
        return new CourseAndCompetitorCopyOperation(racesToCopyTo, courseCheckBox.getValue(), competitorCheckBox.getValue(),
                copyMarkDeviceMappingsCheckBox.getValue(), priorityBox.getValue(), sailingServiceWrite, errorReporter);
    }

}
