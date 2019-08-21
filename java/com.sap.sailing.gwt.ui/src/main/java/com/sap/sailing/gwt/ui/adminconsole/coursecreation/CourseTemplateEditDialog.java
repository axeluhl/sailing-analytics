package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.adminconsole.coursecreation.ControlPointEditDialog.ControlPointEditDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.courseCreation.CourseTemplateDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkTemplateDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.WaypointTemplateDTO;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CourseTemplateEditDialog extends DataEntryDialog<CourseTemplateDTO> {
    private static AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);

    private final TextBox nameTextBox;
    private final StringMessages stringMessages;
    private final Button addWaypointTemplate;

    private CellTable<WaypointTemplateDTO> waypointTemplatesTable;
    private Collection<WaypointTemplateDTO> waypointTemplates = new ArrayList<>();

    public CourseTemplateEditDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            CourseTemplateDTO courseTemplateToEdit, DialogCallback<CourseTemplateDTO> callback) {
        super(stringMessages.edit() + " " + stringMessages.courseTemplates(), null, stringMessages.ok(),
                stringMessages.cancel(), new Validator<CourseTemplateDTO>() {
                    @Override
                    public String getErrorMessage(CourseTemplateDTO valueToValidate) {
                        String result = null;
                        boolean invalidName = valueToValidate.getName() == null || valueToValidate.getName().isEmpty();
                        if (invalidName) {
                            result = stringMessages.pleaseEnterAName();
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.ensureDebugId("CourseTemplateToEditEditDialog");
        this.stringMessages = stringMessages;

        this.nameTextBox = createTextBox(courseTemplateToEdit.getName());

        addWaypointTemplate = new Button(stringMessages.add());
        addWaypointTemplate.addClickHandler(c -> {
            ControlPointEditDialog dialog = new ControlPointEditDialog(sailingService, stringMessages,
                    new ControlPointEditDTO(), new DialogCallback<ControlPointEditDTO>() {
                        @Override
                        public void ok(ControlPointEditDTO editedObject) {
                            // create list with one or two elements based on passing instruction
                            final Collection<MarkTemplateDTO> markTemplates = editedObject
                                    .getPassingInstruction().applicability[0] == 2
                                            ? Arrays.asList(editedObject.getMarkTemplate1(),
                                                    editedObject.getMarkTemplate2().get())
                                            : Collections.singletonList(editedObject.getMarkTemplate1());
                            waypointTemplates
                                    .add(new WaypointTemplateDTO(markTemplates, editedObject.getPassingInstruction()));
                            refreshWaypointsTable();
                        }

                        @Override
                        public void cancel() {
                            // nothing to do
                        }
                    });

            dialog.show();
        });
        createWaypointTemplateTable();
        waypointTemplates.addAll(courseTemplateToEdit.getWaypointTemplates());
        refreshWaypointsTable();
        nameTextBox.addKeyUpHandler(e -> validateAndUpdate());
    }

    private void refreshWaypointsTable() {
        waypointTemplatesTable.setRowCount(waypointTemplates.size());
        waypointTemplatesTable.setRowData(0, (List<? extends WaypointTemplateDTO>) waypointTemplates);
    }

    private void createWaypointTemplateTable() {
        waypointTemplatesTable = new BaseCelltable<>(1000, tableResources);
        waypointTemplatesTable.setWidth("100%");

        Column<WaypointTemplateDTO, String> waypointTemplateColumn = new Column<WaypointTemplateDTO, String>(
                new TextCell()) {
            @Override
            public String getValue(WaypointTemplateDTO waypointTemplate) {
                return waypointTemplate.getMarkTemplatesForControlPoint().stream().map(m -> m.getName())
                        .collect(Collectors.joining("-"));
            }
        };
        waypointTemplatesTable.addColumn(waypointTemplateColumn, stringMessages.name());

    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return nameTextBox;
    }

    @Override
    protected CourseTemplateDTO getResult() {
        // TODO: implement
        return new CourseTemplateDTO();
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(6, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, nameTextBox);
        result.setWidget(1, 0, addWaypointTemplate);
        result.setWidget(1, 1, waypointTemplatesTable);
        return result;
    }

}
