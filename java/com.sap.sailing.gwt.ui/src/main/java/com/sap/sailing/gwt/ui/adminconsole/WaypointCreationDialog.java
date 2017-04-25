package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class WaypointCreationDialog extends DataEntryDialog<WaypointDTO> {    
    private final ControlPointTableWrapper<RefreshableSingleSelectionModel<ControlPointDTO>> controlPointsWrapper;
    private final ListBox passingInstructions;
    private final StringMessages stringMessages;
    
    public static interface DefaultPassingInstructionProvider {
        PassingInstruction getDefaultPassingInstruction(int numberOfMarksInControlPoint, String controlPointIdAsString);
    }
    
    public WaypointCreationDialog(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages, AdminConsoleTableResources tableRes,
            List<ControlPointDTO> controlPoints, final DefaultPassingInstructionProvider defaultPassingInstructionProvider,
            DialogCallback<WaypointDTO> callback) {
        super(stringMessages.waypoint(), stringMessages.waypoint(),
                stringMessages.ok(), stringMessages.cancel(), new DataEntryDialog.Validator<WaypointDTO>() {
                    @Override
                    public String getErrorMessage(WaypointDTO valueToValidate) {
                        if (valueToValidate.controlPoint == null) {
                            return stringMessages.pleaseSelectAControlPoint();
                        }
                        return null;
                    }
                }, /* animationEnabled */ false, callback);
        this.stringMessages = stringMessages;
        controlPointsWrapper = new ControlPointTableWrapper<RefreshableSingleSelectionModel<ControlPointDTO>>(
                /* multiSelection */ false, sailingService, stringMessages, errorReporter);
        controlPointsWrapper.getDataProvider().getList().addAll(controlPoints);
        passingInstructions = createListBox(false);
        updatePassingInstructions(defaultPassingInstructionProvider);
        controlPointsWrapper.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                updatePassingInstructions(defaultPassingInstructionProvider);
                validateAndUpdate();
            }
        });
    }
    
    private void updatePassingInstructions(DefaultPassingInstructionProvider defaultPassingInstructionProvider) {
        passingInstructions.clear();
        final ControlPointDTO controlPoint = controlPointsWrapper.getSelectionModel().getSelectedObject();
        final int numMarks = controlPoint != null ? Util.size(controlPoint.getMarks()) : 0;
        PassingInstruction defaultPassingInstruction = defaultPassingInstructionProvider.getDefaultPassingInstruction(numMarks,
                controlPoint == null ? null : controlPoint.getIdAsString());
        int i = 0;
        passingInstructions.insertItem(PassingInstruction.None.name(), i++);
        for (PassingInstruction pi : PassingInstruction.relevantValues()) {
            for (int numApplicableMarks : pi.applicability) {
                if (numApplicableMarks == numMarks) {
                    passingInstructions.insertItem(pi.name(), i);
                    passingInstructions.setItemSelected(i, pi == defaultPassingInstruction);
                    i++;
                }
            }
        }
    }

    @Override
    protected WaypointDTO getResult() {
        PassingInstruction pi = passingInstructions.getSelectedIndex() > 0 ?
                PassingInstruction.valueOf(passingInstructions.getItemText(passingInstructions.getSelectedIndex()))
                : PassingInstruction.None;
        ControlPointDTO controlPoint = controlPointsWrapper.getSelectionModel().getSelectedObject();
        String name = null;
        if (controlPoint != null) {
            name = controlPoint.getName();
        }
        return new WaypointDTO(name, controlPoint, pi);
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(2,1);
        grid.setWidget(0, 0, controlPointsWrapper);
        HorizontalPanel passingInstructionsRow = new HorizontalPanel();
        grid.setWidget(1, 0, passingInstructionsRow);
        passingInstructionsRow.add(new Label(stringMessages.passingInstructions() + ":"));
        passingInstructionsRow.add(passingInstructions);
        return grid;
    }
}