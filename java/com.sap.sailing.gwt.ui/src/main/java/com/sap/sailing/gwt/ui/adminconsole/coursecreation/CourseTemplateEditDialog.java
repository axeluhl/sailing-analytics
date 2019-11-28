package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
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
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkRoleDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkTemplateDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.WaypointTemplateDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;

public class CourseTemplateEditDialog extends DataEntryDialog<CourseTemplateDTO> {
    private static AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);

    private final TextBox nameTextBox;
    private final TextBox urlTextBox;
    private final StringMessages stringMessages;
    private final Button addWaypointTemplate;

    private CellTable<MarkTemplateWithAssociatedRoleDTO> markTemplatesTable;
    private List<MarkTemplateWithAssociatedRoleDTO> markTemplates = new ArrayList<>();
    private final Map<UUID, MarkRoleDTO> markRolesMap;
    private final List<MarkTemplateDTO> allMarkTemplates;
    private final Button buttonAddMarkTemplate;

    private CellTable<WaypointTemplateDTO> waypointTemplatesTable;
    private Collection<WaypointTemplateDTO> waypointTemplates = new ArrayList<>();

    private final UUID currentUuid;

    private final StringListEditorComposite tagsEditor;

    public CourseTemplateEditDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            CourseTemplateDTO courseTemplateToEdit, Map<UUID, MarkRoleDTO> markRolesMap,
            List<MarkTemplateDTO> allMarkTemplates, DialogCallback<CourseTemplateDTO> callback, final boolean isNew) {
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
        this.currentUuid = courseTemplateToEdit.getUuid();
        this.ensureDebugId("CourseTemplateToEditEditDialog");
        this.stringMessages = stringMessages;

        this.nameTextBox = createTextBox(courseTemplateToEdit.getName());
        this.urlTextBox = createTextBox(courseTemplateToEdit.getOptionalImageUrl().orElse(""));

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
                            waypointTemplates.add(new WaypointTemplateDTO(editedObject.getName().orElse(null),
                                    editedObject.getShortName(), markTemplates, editedObject.getPassingInstruction()));
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
        this.markRolesMap = markRolesMap;
        this.allMarkTemplates = allMarkTemplates;
        createMarkTemplateTable();
        buttonAddMarkTemplate = new Button(stringMessages.add());
        buttonAddMarkTemplate.addClickHandler(c -> {
            markTemplates.add(new MarkTemplateWithAssociatedRoleDTO());
            refreshMarkTemplateTable();
        });
        markTemplates.addAll(courseTemplateToEdit.getAssociatedRoles().entrySet().stream()
                .map(e -> new MarkTemplateWithAssociatedRoleDTO(e.getKey(), markRolesMap.get(e.getValue())))
                .collect(Collectors.toList()));
        refreshMarkTemplateTable();

        nameTextBox.addKeyUpHandler(e -> validateAndUpdate());
        urlTextBox.addKeyUpHandler(e -> validateAndUpdate());

        tagsEditor = new StringListEditorComposite(courseTemplateToEdit.getTags(), stringMessages,
                stringMessages.edit(stringMessages.tags()), IconResources.INSTANCE.removeIcon(),
                Collections.emptyList(), stringMessages.tag());

        this.addWaypointTemplate.setEnabled(isNew);
    }

    private void refreshMarkTemplateTable() {
        markTemplatesTable.setRowCount(markTemplates.size());
        markTemplatesTable.setRowData(0, markTemplates);
    }

    private void createMarkTemplateTable() {
        markTemplatesTable = new BaseCelltable<>(1000, tableResources);
        markTemplatesTable.setWidth("100%");
        Column<MarkTemplateWithAssociatedRoleDTO, String> markTemplateColumn = new Column<MarkTemplateWithAssociatedRoleDTO, String>(
                new SelectionCell(
                        allMarkTemplates.stream().map(MarkTemplateDTO::getName).collect(Collectors.toList()))) {
            @Override
            public String getValue(MarkTemplateWithAssociatedRoleDTO markTemplate) {
                return markTemplate.getMarkTemplate() != null ? markTemplate.getMarkTemplate().getName() : "";
            }
        };
        markTemplateColumn.setFieldUpdater(new FieldUpdater<MarkTemplateWithAssociatedRoleDTO, String>() {
            @Override
            public void update(int index, MarkTemplateWithAssociatedRoleDTO markTemplate, String value) {
                markTemplate.markTemplate = allMarkTemplates.stream().filter(mt -> mt.getName().equals(value))
                        .findFirst().get();
            }
        });
        Column<MarkTemplateWithAssociatedRoleDTO, String> associatedRoleColumn = new Column<MarkTemplateWithAssociatedRoleDTO, String>(
                new SelectionCell(
                        markRolesMap.values().stream().map(MarkRoleDTO::getName).collect(Collectors.toList())) {
                }) {
            @Override
            public String getValue(MarkTemplateWithAssociatedRoleDTO markTemplate) {
                return markTemplate.getAssociatedRole() != null ? markTemplate.getAssociatedRole().getName() : "";
            }
        };
        associatedRoleColumn.setFieldUpdater(new FieldUpdater<MarkTemplateWithAssociatedRoleDTO, String>() {
            @Override
            public void update(int index, MarkTemplateWithAssociatedRoleDTO markTemplate, String value) {
                markTemplate.setAssociatedRole(
                        markRolesMap.values().stream().filter(mr -> mr.getName().equals(value)).findFirst().get());

            }

        });
        DefaultActionsImagesBarCell imagesBarCell = new DefaultActionsImagesBarCell(stringMessages) {

            @Override
            protected Iterable<ImageSpec> getImageSpecs() {
                return Arrays.asList(getDeleteImageSpec());
            }

        };
        ImagesBarColumn<MarkTemplateWithAssociatedRoleDTO, DefaultActionsImagesBarCell> actionsColumn = new ImagesBarColumn<>(
                imagesBarCell);

        markTemplatesTable.addColumn(markTemplateColumn, stringMessages.markTemplate());
        markTemplatesTable.addColumn(associatedRoleColumn, stringMessages.markRoles());
        markTemplatesTable.addColumn(actionsColumn, stringMessages.actions());
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
        final UUID id = currentUuid == null ? UUID.randomUUID() : currentUuid;
        final ArrayList<MarkTemplateDTO> markTemplates = new ArrayList<MarkTemplateDTO>();
        final Map<MarkTemplateDTO, UUID> associatedRoles = new HashMap<>();
        final String optionalUrl = urlTextBox.getText() != null && !urlTextBox.getText().isEmpty()
                ? urlTextBox.getText()
                : null;

        waypointTemplates.stream().forEach(wt -> {
            markTemplates.addAll(Util.asList(wt.getMarkTemplatesForControlPoint()));
        });
        this.markTemplates.forEach(mt -> associatedRoles.put(mt.getMarkTemplate(),
                mt.getAssociatedRole() != null ? mt.getAssociatedRole().getUuid() : null));
        // TODO: repeatable part
        // TODO: default number of laps
        return new CourseTemplateDTO(id, nameTextBox.getValue(), markTemplates, waypointTemplates, associatedRoles,
                optionalUrl, tagsEditor.getValue(), null, null);
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(5, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, nameTextBox);
        result.setWidget(1, 0, new Label(stringMessages.url()));
        result.setWidget(1, 1, urlTextBox);
        result.setWidget(2, 0, buttonAddMarkTemplate);
        result.setWidget(2, 1, markTemplatesTable);
        result.setWidget(3, 0, addWaypointTemplate);
        result.setWidget(3, 1, waypointTemplatesTable);
        result.setWidget(4, 0, new Label(stringMessages.tags()));
        result.setWidget(4, 1, tagsEditor);
        return result;
    }

    public static class MarkTemplateWithAssociatedRoleDTO {
        private MarkTemplateDTO markTemplate;
        private MarkRoleDTO associatedRole;

        public MarkTemplateWithAssociatedRoleDTO() {
        }

        public MarkTemplateWithAssociatedRoleDTO(final MarkTemplateDTO markTemplate, final MarkRoleDTO markRole) {
            this.markTemplate = markTemplate;
            this.associatedRole = markRole;
        }

        public MarkTemplateDTO getMarkTemplate() {
            return markTemplate;
        }

        public MarkRoleDTO getAssociatedRole() {
            return associatedRole;
        }

        public void setAssociatedRole(final MarkRoleDTO markRole) {
            associatedRole = markRole;
        }

    }
}
