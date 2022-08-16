package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class GateCreationDialog extends DataEntryDialog<GateDTO> {    
    private final MarkTableWrapper<RefreshableMultiSelectionModel<MarkDTO>> marksWrapper;
    private final TextBox name;
    private final TextBox shortName;
    private final StringMessages stringMessages;
    
    public GateCreationDialog(SailingServiceWriteAsync sailingServiceWrite, ErrorReporter errorReporter,
            final StringMessages stringMessages, AdminConsoleTableResources tableRes,
            List<MarkDTO> marks, DialogCallback<GateDTO> callback) {
        super(stringMessages.gate(), stringMessages.gate(),
                stringMessages.ok(), stringMessages.cancel(), new DataEntryDialog.Validator<GateDTO>() {
                    @Override
                    public String getErrorMessage(GateDTO valueToValidate) {
                        if (valueToValidate == null) {
                            return stringMessages.selectTwoMarksForGate();
                        } else if (valueToValidate.getName() == null || valueToValidate.getName().isEmpty()) {
                            return stringMessages.pleaseEnterAName();
                        }
                        return null;
                    }
                }, /* animationEnabled */ false, callback);
        this.stringMessages = stringMessages;
        name = createTextBox("");
        shortName = createTextBox("");
        marksWrapper = new MarkTableWrapper<RefreshableMultiSelectionModel<MarkDTO>>(
                /* multiSelection */ true, sailingServiceWrite, stringMessages, errorReporter);
        marksWrapper.getDataProvider().getList().addAll(marks);
        marksWrapper.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                validateAndUpdate();
            }
        });
    }

    @Override
    protected GateDTO getResult() {
        GateDTO controlPoint = null;
        Set<MarkDTO> selection = marksWrapper.getSelectionModel().getSelectedSet();
        if (selection.size() == 2) {
            name.setEnabled(true);
            Iterator<MarkDTO> i = marksWrapper.getSelectionModel().getSelectedSet().iterator();
            MarkDTO first = i.next();
            MarkDTO second = i.next();
            controlPoint = new GateDTO(UUID.randomUUID().toString(), name.getText(), first, second,
                    shortName.getText());
        }
        return controlPoint;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(3, 1);
        HorizontalPanel nameRow = new HorizontalPanel();
        nameRow.add(new Label(stringMessages.name()));
        nameRow.add(name);

        HorizontalPanel shortnameRow = new HorizontalPanel();
        shortnameRow.add(new Label(stringMessages.shortName()));
        shortnameRow.add(shortName);
        grid.setWidget(0, 0, nameRow);
        grid.setWidget(1, 0, shortnameRow);
        grid.setWidget(2, 0, marksWrapper);
        return grid;
    }
}