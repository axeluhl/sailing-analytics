package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.courseCreation.CourseTemplateDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkRoleDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkTemplateDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.WaypointTemplateDTO;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;

public class CourseTemplateEditDialog extends DataEntryDialog<CourseTemplateDTO> {
    private static AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);

    private final TextBox nameTextBox;
    private final TextBox urlTextBox;
    private final TextBox numberOfLapsTextBox;
    private final StringMessages stringMessages;
    private final Map<UUID, MarkRoleDTO> markRolesMap;
    private final List<MarkTemplateDTO> allMarkTemplates;
    private final List<String> allMarkTemplatesSelectionList;

    private CellTable<MarkTemplateWithAssociatedRoleDTO> markTemplatesTable;
    private List<MarkTemplateWithAssociatedRoleDTO> markTemplates = new ArrayList<>();
    private final Button buttonAddMarkTemplate;
    private CellTable<WaypointTemplateDTO> waypointTemplatesTable;
    private Collection<WaypointTemplateDTO> waypointTemplates = new ArrayList<>();
    private final Button buttonAddWaypointTemplate;

    private final UUID currentUuid;

    private final StringListEditorComposite tagsEditor;

    private static final RegExp urlRegExp = RegExp
            .compile("^((ftp|http|https)://[\\w@.\\-\\_]+(:\\d{1,5})?(/[\\w#!:.?+=&%@!\\_\\-/]+)*){1}$");

    public CourseTemplateEditDialog(final SailingServiceAsync sailingService, final UserService userService,
            final StringMessages stringMessages, CourseTemplateDTO courseTemplateToEdit,
            Map<UUID, MarkRoleDTO> markRolesMap, List<MarkTemplateDTO> allMarkTemplates,
            DialogCallback<CourseTemplateDTO> callback, final boolean isNew) {
        super(stringMessages.edit() + " " + stringMessages.courseTemplates(), null, stringMessages.ok(),
                stringMessages.cancel(), new Validator<CourseTemplateDTO>() {
                    @Override
                    public String getErrorMessage(CourseTemplateDTO valueToValidate) {
                        final StringBuilder sb = new StringBuilder();
                        boolean invalidName = valueToValidate.getName() == null || valueToValidate.getName().isEmpty();
                        if (invalidName) {
                            sb.append(stringMessages.pleaseEnterAName()).append(". ");
                        }
                        final Set<UUID> distinctUUIDs = new HashSet<>();
                        if (valueToValidate.getAssociatedRoles().values().stream().map(u -> distinctUUIDs.add(u))
                                .filter(b -> b == false).count() > 0) {
                            sb.append(stringMessages.markRoleUsedTwice()).append(". ");
                        }
                        AtomicBoolean unAssignedMarkTemplateUsed = new AtomicBoolean(false);
                        valueToValidate.getWaypointTemplates().forEach(wt -> {
                            if (wt.getPassingInstruction() == null) {
                                sb.append(stringMessages.wayPointRequiresPassingInstruction()).append(". ");
                            } else {
                                wt.getMarkRolesForControlPoint()
                                        .forEach(wtmt -> unAssignedMarkTemplateUsed.set(unAssignedMarkTemplateUsed.get()
                                                || valueToValidate.getMarkTemplates().stream()
                                                        .noneMatch(mt -> mt.getUuid().equals(wtmt.getUuid()))));
                                if (hasTwoMarks(wt)) {
                                    if (wt.getShortName() == null) {
                                        sb.append(stringMessages.wayPointRequiresShortName()).append(". ");
                                    }
                                    if (wt.getName() == null) {
                                        sb.append(stringMessages.wayPointRequiresName()).append(". ");
                                    }
                                    if (wt.getMarkRolesForControlPoint().size() == 2
                                            && wt.getMarkRolesForControlPoint().get(0)
                                                    .equals(wt.getMarkRolesForControlPoint().get(1))) {
                                        sb.append(stringMessages.wayPointMarkTemplatesAreTheSame());
                                    }
                                }
                            }
                        });
                        if (unAssignedMarkTemplateUsed.get()) {
                            sb.append(stringMessages.wayPointMarkInSequenceMissing()).append(". ");
                        }
                        if (valueToValidate.getOptionalImageUrl().isPresent()) {
                            if (urlRegExp.exec(valueToValidate.getOptionalImageUrl().get()) == null) {
                                sb.append(stringMessages.invalidImageURL()).append(". ");
                            }
                        }
                        return sb.toString();
                    }
                }, /* animationEnabled */true, callback);
        this.currentUuid = courseTemplateToEdit.getUuid();
        this.ensureDebugId("CourseTemplateToEditEditDialog");
        this.stringMessages = stringMessages;

        this.nameTextBox = createTextBox(courseTemplateToEdit.getName());
        this.urlTextBox = createTextBox(courseTemplateToEdit.getOptionalImageUrl().orElse(""));
        this.numberOfLapsTextBox = createTextBox(courseTemplateToEdit.getDefaultNumberOfLaps() != null
                ? courseTemplateToEdit.getDefaultNumberOfLaps().toString()
                : null);

        this.markRolesMap = markRolesMap;
        this.allMarkTemplates = allMarkTemplates;
        this.allMarkTemplatesSelectionList = allMarkTemplates.stream().map(MarkTemplateDTO::getName)
                .collect(Collectors.toList());

        createMarkTemplateTable(/* readOnly */ !isNew, userService);
        buttonAddMarkTemplate = new Button(stringMessages.add());
        buttonAddMarkTemplate.addClickHandler(c -> {
            markTemplates.add(new MarkTemplateWithAssociatedRoleDTO(allMarkTemplates.stream().findFirst().orElse(null),
                    markRolesMap.values().stream().findFirst().orElse(null)));
            refreshMarkTemplateTable();
            validateAndUpdate();
        });
        buttonAddMarkTemplate.setEnabled(isNew);
        markTemplates.addAll(courseTemplateToEdit.getAssociatedRoles().entrySet().stream()
                .map(e -> new MarkTemplateWithAssociatedRoleDTO(e.getKey(), markRolesMap.get(e.getValue())))
                .collect(Collectors.toList()));
        refreshMarkTemplateTable();

        createWaypointTemplateTable(/* readOnly */ !isNew);
        waypointTemplates.addAll(courseTemplateToEdit.getWaypointTemplates());
        refreshWaypointsTable();
        buttonAddWaypointTemplate = new Button(stringMessages.add());
        buttonAddWaypointTemplate.addClickHandler(c -> {
            final MarkTemplateDTO firstMarkTemplate = allMarkTemplates.stream().findFirst().orElse(null);
            waypointTemplates
                    .add(new WaypointTemplateDTO(null, null, Arrays.asList(firstMarkTemplate, firstMarkTemplate),
                            Arrays.stream(PassingInstruction.relevantValues()).findFirst().orElse(null)));
            refreshWaypointsTable();
            validateAndUpdate();
        });
        buttonAddWaypointTemplate.setEnabled(isNew);

        nameTextBox.addKeyUpHandler(e -> validateAndUpdate());
        urlTextBox.addKeyUpHandler(e -> validateAndUpdate());

        tagsEditor = new StringListEditorComposite(courseTemplateToEdit.getTags(), stringMessages,
                stringMessages.edit(stringMessages.tags()), IconResources.INSTANCE.removeIcon(),
                Collections.emptyList(), stringMessages.tag());
    }

    private void refreshMarkTemplateTable() {
        markTemplatesTable.setRowCount(markTemplates.size());
        markTemplatesTable.setRowData(0, markTemplates);
    }

    private void createMarkTemplateTable(final boolean readOnly, final UserService userService) {
        markTemplatesTable = new BaseCelltable<>(1000, tableResources);
        markTemplatesTable.setWidth("100%");
        final SelectionCell markTemplateSelectionCell = new SelectionCell(allMarkTemplatesSelectionList);
        final HideableAndEditableCell<WaypointTemplateDTO, String, SelectionCell> hideableMarkTemplateSelectionCell = new HideableAndEditableCell<WaypointTemplateDTO, String, SelectionCell>(
                markTemplateSelectionCell, /* hidden */ null, /* editable */ wt -> !readOnly);
        Column<MarkTemplateWithAssociatedRoleDTO, String> markTemplateColumn = new Column<MarkTemplateWithAssociatedRoleDTO, String>(
                hideableMarkTemplateSelectionCell) {
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
                validateAndUpdate();
            }
        });
        final SelectionCell associatedRoleSelectionCell = new SelectionCell(
                markRolesMap.values().stream().map(MarkRoleDTO::getName).collect(Collectors.toList()));
        final HideableAndEditableCell<WaypointTemplateDTO, String, SelectionCell> hideableAssociatedRoleSelectionCell = new HideableAndEditableCell<WaypointTemplateDTO, String, SelectionCell>(
                associatedRoleSelectionCell, /* hidden */ null, /* editable */ wt -> !readOnly);
        Column<MarkTemplateWithAssociatedRoleDTO, String> associatedRoleColumn = new Column<MarkTemplateWithAssociatedRoleDTO, String>(
                hideableAssociatedRoleSelectionCell) {

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
                validateAndUpdate();

            }

        });
        DefaultActionsImagesBarCell imagesBarCell = new DefaultActionsImagesBarCell(stringMessages) {

            @Override
            protected Iterable<ImageSpec> getImageSpecs() {
                return readOnly ? Collections.emptyList() : Arrays.asList(getDeleteImageSpec());
            }

        };
        ImagesBarColumn<MarkTemplateWithAssociatedRoleDTO, DefaultActionsImagesBarCell> actionsColumn = new ImagesBarColumn<>(
                imagesBarCell);
        actionsColumn.setFieldUpdater(new FieldUpdater<MarkTemplateWithAssociatedRoleDTO, String>() {
            @Override
            public void update(int index, MarkTemplateWithAssociatedRoleDTO markTemplate, String value) {
                markTemplates.remove(index);
                validateAndUpdate();
                refreshMarkTemplateTable();
            }
        });

        markTemplatesTable.addColumn(markTemplateColumn, stringMessages.markTemplate());
        markTemplatesTable.addColumn(associatedRoleColumn, stringMessages.markRoles());
        markTemplatesTable.addColumn(actionsColumn, stringMessages.actions());
    }

    private void refreshWaypointsTable() {
        waypointTemplatesTable.setRowCount(waypointTemplates.size());
        waypointTemplatesTable.setRowData(0, (List<? extends WaypointTemplateDTO>) waypointTemplates);
    }

    private void createWaypointTemplateTable(final boolean readOnly) {
        waypointTemplatesTable = new BaseCelltable<>(1000, tableResources);
        waypointTemplatesTable.setWidth("100%");

        final HideableAndEditableCell<WaypointTemplateDTO, String, TextInputCell> hideableShortNameCell = new HideableAndEditableCell<>(
                new TextInputCell(), /* hidden */ wt -> !hasTwoMarks(wt), /* editable */ wt -> !readOnly);
        Column<WaypointTemplateDTO, String> shortNameColumn = new Column<WaypointTemplateDTO, String>(
                hideableShortNameCell) {
            @Override
            public String getValue(WaypointTemplateDTO waypointTemplate) {
                return waypointTemplate.getShortName();
            }
        };
        shortNameColumn.setFieldUpdater(new FieldUpdater<WaypointTemplateDTO, String>() {
            @Override
            public void update(int index, WaypointTemplateDTO waypointTemplate, String value) {
                waypointTemplate.setShortName(value);
                validateAndUpdate();
            }
        });
        final HideableAndEditableCell<WaypointTemplateDTO, String, TextInputCell> hideableNameCell = new HideableAndEditableCell<>(
                new TextInputCell(), /* hidden */ wt -> !hasTwoMarks(wt), /* editable */ wt -> !readOnly);
        Column<WaypointTemplateDTO, String> nameColumn = new Column<WaypointTemplateDTO, String>(hideableNameCell) {
            @Override
            public String getValue(WaypointTemplateDTO waypointTemplate) {
                return waypointTemplate.getName();
            }
        };
        nameColumn.setFieldUpdater(new FieldUpdater<WaypointTemplateDTO, String>() {
            @Override
            public void update(int index, WaypointTemplateDTO waypointTemplate, String value) {
                waypointTemplate.setName(value);
                validateAndUpdate();
            }
        });
        final SelectionCell markTemplate1SelectionCell = new SelectionCell(allMarkTemplatesSelectionList);
        final HideableAndEditableCell<WaypointTemplateDTO, String, SelectionCell> hideablemarkTemplate1Cell = new HideableAndEditableCell<WaypointTemplateDTO, String, SelectionCell>(
                markTemplate1SelectionCell, /* hidden */ null, /* editable */ wt -> !readOnly);
        Column<WaypointTemplateDTO, String> markTemplateColumn1 = new Column<WaypointTemplateDTO, String>(
                hideablemarkTemplate1Cell) {
            @Override
            public String getValue(WaypointTemplateDTO waypointTemplate) {
                return waypointTemplate.getMarkRolesForControlPoint() != null
                        && waypointTemplate.getMarkRolesForControlPoint().size() >= 1
                                ? waypointTemplate.getMarkRolesForControlPoint().get(0).getName()
                                : "";
            }
        };
        markTemplateColumn1.setFieldUpdater(new FieldUpdater<WaypointTemplateDTO, String>() {
            @Override
            public void update(int index, WaypointTemplateDTO waypointTemplate, String value) {
                MarkTemplateDTO selectedMarkTemplate = allMarkTemplates.stream()
                        .filter(mt -> mt.getName().equals(value)).findFirst().get();
                if (waypointTemplate.getMarkRolesForControlPoint().size() == 0) {
                    waypointTemplate.getMarkRolesForControlPoint().add(selectedMarkTemplate);
                } else {
                    waypointTemplate.getMarkRolesForControlPoint().set(0, selectedMarkTemplate);
                }
                validateAndUpdate();
            }
        });
        final SelectionCell markTemplate2SelectionCell = new SelectionCell(allMarkTemplatesSelectionList);
        final HideableAndEditableCell<WaypointTemplateDTO, String, SelectionCell> hideablemarkTemplate2Cell = new HideableAndEditableCell<WaypointTemplateDTO, String, SelectionCell>(
                markTemplate2SelectionCell, /* hidden */ wt -> !hasTwoMarks(wt), /* editable */ wt -> !readOnly);
        Column<WaypointTemplateDTO, String> markTemplateColumn2 = new Column<WaypointTemplateDTO, String>(
                hideablemarkTemplate2Cell) {
            @Override
            public String getValue(WaypointTemplateDTO waypointTemplate) {
                return waypointTemplate.getMarkRolesForControlPoint() != null
                        && waypointTemplate.getMarkRolesForControlPoint().size() == 2
                                ? waypointTemplate.getMarkRolesForControlPoint().get(1).getName()
                                : null;
            }
        };
        markTemplateColumn2.setFieldUpdater(new FieldUpdater<WaypointTemplateDTO, String>() {
            @Override
            public void update(int index, WaypointTemplateDTO waypointTemplate, String value) {
                MarkTemplateDTO selectedMarkTemplate = allMarkTemplates.stream()
                        .filter(mt -> mt.getName().equals(value)).findFirst().get();
                if (waypointTemplate.getMarkRolesForControlPoint().size() == 1) {
                    waypointTemplate.getMarkRolesForControlPoint().add(selectedMarkTemplate);
                } else {
                    waypointTemplate.getMarkRolesForControlPoint().set(1, selectedMarkTemplate);
                }
                validateAndUpdate();
            }
        });
        final HideableAndEditableCell<WaypointTemplateDTO, String, SelectionCell> hideablePassingInstructionCell = new HideableAndEditableCell<WaypointTemplateDTO, String, SelectionCell>(
                new SelectionCell(Arrays.stream(PassingInstruction.relevantValues()).map(PassingInstruction::name)
                        .collect(Collectors.toList())),
                /* hidden */ null, /* editable */ wt -> !readOnly);
        Column<WaypointTemplateDTO, String> passingInstructionColumn = new Column<WaypointTemplateDTO, String>(
                hideablePassingInstructionCell) {
            @Override
            public String getValue(WaypointTemplateDTO waypointTemplate) {
                return waypointTemplate.getPassingInstruction().name();
            }
        };
        passingInstructionColumn.setFieldUpdater(new FieldUpdater<WaypointTemplateDTO, String>() {
            @Override
            public void update(int index, WaypointTemplateDTO waypointTemplate, String value) {
                waypointTemplate.setPassingInstruction(Arrays.stream(PassingInstruction.relevantValues())
                        .filter(pi -> pi.name().equals(value)).findFirst().get().name());
                validateAndUpdate();
                refreshWaypointsTable();
            }
        });
        DefaultActionsImagesBarCell imagesBarCell = new DefaultActionsImagesBarCell(stringMessages) {
            @Override
            protected Iterable<ImageSpec> getImageSpecs() {
                return readOnly ? Collections.emptyList() : Arrays.asList(getDeleteImageSpec());
            }
        };
        ImagesBarColumn<WaypointTemplateDTO, DefaultActionsImagesBarCell> actionsColumn = new ImagesBarColumn<>(
                imagesBarCell);
        actionsColumn.setFieldUpdater(new FieldUpdater<WaypointTemplateDTO, String>() {
            @Override
            public void update(int index, WaypointTemplateDTO waypointTemplate, String value) {
                waypointTemplates.removeIf(r -> r == waypointTemplate);
                validateAndUpdate();
                refreshWaypointsTable();
            }
        });

        waypointTemplatesTable.addColumn(passingInstructionColumn, stringMessages.passingInstructions());
        waypointTemplatesTable.addColumn(shortNameColumn, stringMessages.shortName());
        waypointTemplatesTable.addColumn(nameColumn, stringMessages.name());
        waypointTemplatesTable.addColumn(markTemplateColumn1, stringMessages.markTemplate1());
        waypointTemplatesTable.addColumn(markTemplateColumn2, stringMessages.markTemplate2());
        waypointTemplatesTable.addColumn(actionsColumn, stringMessages.actions());
    }

    private static boolean hasTwoMarks(final WaypointTemplateDTO wt) {
        boolean hasTwoMarks = wt != null && wt.getPassingInstruction() != null
                && Arrays.stream(wt.getPassingInstruction().applicability).anyMatch(a -> a == 2);
        return hasTwoMarks;

    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return nameTextBox;
    }

    @Override
    protected CourseTemplateDTO getResult() {
        final UUID id = currentUuid == null ? UUID.randomUUID() : currentUuid;
        final ArrayList<MarkTemplateDTO> markTemplates = new ArrayList<MarkTemplateDTO>();
        final Map<MarkTemplateDTO, UUID> associatedRoles = new HashMap<>();
        final String optionalUrl = urlTextBox.getText() != null && !urlTextBox.getText().isEmpty()
                ? urlTextBox.getText()
                : null;

        this.markTemplates.stream().forEach(mt -> {
            markTemplates.add(mt.markTemplate);
            if (mt.getAssociatedRole() != null) {
                associatedRoles.put(mt.getMarkTemplate(), mt.getAssociatedRole().getUuid());
            }
        });
        // TODO: repeatable part
        Integer defaultNumberOfLaps = null;
        try {
            defaultNumberOfLaps = numberOfLapsTextBox.getValue() != null && numberOfLapsTextBox.getValue().length() > 0
                    ? Integer.parseInt(numberOfLapsTextBox.getValue())
                    : null;
        } catch (NumberFormatException nfe) {
            defaultNumberOfLaps = null;
        }
        waypointTemplates.forEach(wt -> {
            if (!hasTwoMarks(wt) && wt.getMarkRolesForControlPoint() != null
                    && wt.getMarkRolesForControlPoint().size() == 2) {
                wt.getMarkRolesForControlPoint().remove(1);
            }
        });
        return new CourseTemplateDTO(id, nameTextBox.getValue(), markTemplates, waypointTemplates, associatedRoles,
                optionalUrl, tagsEditor.getValue(), null, defaultNumberOfLaps);
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(6, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, nameTextBox);
        result.setWidget(1, 0, new Label(stringMessages.imageURL()));
        result.setWidget(1, 1, urlTextBox);
        result.setWidget(2, 0, buttonAddMarkTemplate);
        result.setWidget(2, 1, markTemplatesTable);
        result.setWidget(3, 0, buttonAddWaypointTemplate);
        result.setWidget(3, 1, waypointTemplatesTable);
        result.setWidget(4, 0, new Label(stringMessages.tags()));
        result.setWidget(4, 1, tagsEditor);
        result.setWidget(5, 0, new Label(stringMessages.defaultNumberOfLaps()));
        result.setWidget(5, 1, numberOfLapsTextBox);
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

    public static class HideableAndEditableCell<DTO, V, T extends Cell<V>> implements Cell<V> {

        private final T concreteCell;
        private final Predicate<DTO> hiddenPredicate;
        private final Predicate<DTO> editablePredicate;

        public HideableAndEditableCell(final T concreteCell, final Predicate<DTO> hiddenPredicate,
                final Predicate<DTO> editablePredicate) {
            this.concreteCell = concreteCell;
            this.hiddenPredicate = hiddenPredicate;
            this.editablePredicate = editablePredicate;
        }

        @Override
        public void render(final Context context, final V value, final SafeHtmlBuilder sb) {
            @SuppressWarnings("unchecked")
            DTO object = (DTO) context.getKey();
            if (hiddenPredicate == null || !hiddenPredicate.test(object)) {
                if (editablePredicate != null && !editablePredicate.test(object)) {
                    sb.appendHtmlConstant("<div contentEditable='false' unselectable='false' >" + value + "</div>");
                } else {
                    concreteCell.render(context, value, sb);
                }
            }
        }

        @Override
        public boolean dependsOnSelection() {
            return concreteCell.dependsOnSelection();
        }

        @Override
        public Set<String> getConsumedEvents() {
            return concreteCell.getConsumedEvents();
        }

        @Override
        public boolean handlesSelection() {
            return concreteCell.handlesSelection();
        }

        @Override
        public boolean isEditing(Context context, Element parent, V value) {
            return concreteCell.isEditing(context, parent, value);
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, V value, NativeEvent event,
                ValueUpdater<V> valueUpdater) {
            concreteCell.onBrowserEvent(context, parent, value, event, valueUpdater);
        }

        @Override
        public boolean resetFocus(Context context, Element parent, V value) {
            return concreteCell.resetFocus(context, parent, value);
        }

        @Override
        public void setValue(Context context, Element parent, V value) {
            concreteCell.setValue(context, parent, value);
        }

    }

}
