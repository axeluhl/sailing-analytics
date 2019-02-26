package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Set;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.gwt.ui.adminconsole.AbstractLeaderboardConfigPanel.RaceColumnDTOAndFleetDTOWithNameBasedEquality;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Distance;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CopyCourseAndCompetitorsDialog extends DataEntryDialog<CourseAndCompetitorCopyOperation> {
    private final RaceTableWrapper<RefreshableMultiSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>>
        racesTable;
    private final CheckBox courseCheckBox;
    private final CheckBox competitorCheckBox;
    private final IntegerBox priorityBox;
    private final StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    
    public CopyCourseAndCompetitorsDialog(SailingServiceAsync sailingService, ErrorReporter errorReporter, final StringMessages stringMessages,
            Collection<RaceColumnDTOAndFleetDTOWithNameBasedEquality> races,
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
        racesTable = new RaceTableWrapper<RefreshableMultiSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>>(
                sailingService, stringMessages, errorReporter, /* multiSelection */ true);
        racesTable.setSelectedLeaderboardName(leaderboardName);
        racesTable.getDataProvider().getList().addAll(races);
        racesTable.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                validateAndUpdate();
            }
        });
        courseCheckBox = createCheckbox(stringMessages.copyCourse());
        courseCheckBox.setValue(true);
        competitorCheckBox = createCheckbox(stringMessages.copyCompetitors());
        competitorCheckBox.setValue(false); // competitors are usually registered on the regatta
        priorityBox = createIntegerBox(/* default priority */ 1, /* visibleLength */ 1);
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final FlowPanel mainPanel = new FlowPanel();
        HorizontalPanel checkBoxPanel = new HorizontalPanel();
        checkBoxPanel.add(courseCheckBox);
        checkBoxPanel.add(competitorCheckBox);
        mainPanel.add(checkBoxPanel);
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
                priorityBox.getValue(), sailingService, errorReporter);
    }

}
