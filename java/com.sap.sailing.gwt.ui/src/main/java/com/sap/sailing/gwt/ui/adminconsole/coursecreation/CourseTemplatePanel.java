package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.CHANGE_OWNERSHIP;
import static com.sap.sse.security.shared.HasPermissions.DefaultActions.DELETE;
import static com.sap.sse.security.shared.HasPermissions.DefaultActions.UPDATE;
import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_CHANGE_OWNERSHIP;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_DELETE;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_UPDATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.courseCreation.CourseTemplateDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkRoleDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkTemplateDTO;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.controls.BetterCheckboxCell;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledActionsColumn;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog;
import com.sap.sse.security.ui.client.component.SecuredDTOOwnerColumn;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;

public class CourseTemplatePanel extends FlowPanel {
    private static AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);

    private final SailingServiceAsync sailingService;
    private final LabeledAbstractFilterablePanel<CourseTemplateDTO> filterableCourseTemplatePanel;
    private List<CourseTemplateDTO> allCourseTemplates;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private CellTable<CourseTemplateDTO> courseTemplateTable;
    private ListDataProvider<CourseTemplateDTO> courseTemplateListDataProvider = new ListDataProvider<>();
    private RefreshableMultiSelectionModel<CourseTemplateDTO> refreshableSelectionModel;
    private List<MarkRoleDTO> allMarkRoles;
    private List<MarkTemplateDTO> allMarkTemplates;

    public CourseTemplatePanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, final UserService userService) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        AccessControlledButtonPanel buttonAndFilterPanel = new AccessControlledButtonPanel(userService,
                SecuredDomainType.MARK_TEMPLATE);
        add(buttonAndFilterPanel);
        allCourseTemplates = new ArrayList<>();
        Label lblFilterRaces = new Label(stringMessages.filterCourseTemplateByName() + ":");
        lblFilterRaces.setWordWrap(false);

        this.filterableCourseTemplatePanel = new LabeledAbstractFilterablePanel<CourseTemplateDTO>(lblFilterRaces,
                allCourseTemplates, courseTemplateListDataProvider, stringMessages) {
            @Override
            public List<String> getSearchableStrings(CourseTemplateDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.getName());
                strings.add(t.getUuid().toString());
                return strings;
            }

            @Override
            public AbstractCellTable<CourseTemplateDTO> getCellTable() {
                return courseTemplateTable;
            }
        };
        filterableCourseTemplatePanel.getTextBox().ensureDebugId("CourseTemplateFilterTextBox");
        createCourseTemplateTable(userService);
        buttonAndFilterPanel.addUnsecuredAction(stringMessages.refresh(), new Command() {

            @Override
            public void execute() {
                loadCourseTemplates();
            }
        });
        buttonAndFilterPanel.addCreateAction(stringMessages.add(), new Command() {
            @Override
            public void execute() {
                openEditCourseTemplateDialog(new CourseTemplateDTO(), userService, true);
            }
        });

        final Button removeButton = buttonAndFilterPanel.addRemoveAction(refreshableSelectionModel,
                stringMessages.remove(), new Command() {

                    @Override
                    public void execute() {
                        if (askUserForConfirmation()) {
                            removeCourseTemplates(
                                    refreshableSelectionModel.getSelectedSet().stream().map(courseTemplateDTO -> {
                                        return courseTemplateDTO.getUuid();
                                    }).collect(Collectors.toList()));
                        }
                    }

                    private void removeCourseTemplates(Collection<UUID> courseTemplatesUuids) {
                        if (!courseTemplatesUuids.isEmpty()) {
                            sailingService.removeCourseTemplates(courseTemplatesUuids, new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError(
                                            "Error trying to remove course teamplates:" + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    refreshCourseTemplates();
                                }
                            });
                        }
                    }

                    private boolean askUserForConfirmation() {
                        if (refreshableSelectionModel
                                .itemIsSelectedButNotVisible(courseTemplateTable.getVisibleItems())) {
                            final String markRolesNames = refreshableSelectionModel.getSelectedSet().stream()
                                    .map(CourseTemplateDTO::getName).collect(Collectors.joining("\n"));
                            return Window.confirm(
                                    stringMessages.doYouReallyWantToRemoveNonVisibleCourseTemplates(markRolesNames));
                        }
                        return Window.confirm(stringMessages.doYouReallyWantToRemoveCourseTemplates());
                    }
                });

        removeButton.setEnabled(false);
        buttonAndFilterPanel.addUnsecuredWidget(lblFilterRaces);
        buttonAndFilterPanel.addUnsecuredWidget(filterableCourseTemplatePanel);
        filterableCourseTemplatePanel
                .setUpdatePermissionFilterForCheckbox(event -> userService.hasPermission(event, DefaultActions.UPDATE));

    }

    public void loadCourseTemplates() {
        courseTemplateListDataProvider.getList().clear();
        sailingService.getCourseTemplates(new AsyncCallback<List<CourseTemplateDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.toString());
            }

            @Override
            public void onSuccess(List<CourseTemplateDTO> result) {
                courseTemplateListDataProvider.getList().clear();
                courseTemplateListDataProvider.getList().addAll(result);
                filterableCourseTemplatePanel.updateAll(courseTemplateListDataProvider.getList());
                courseTemplateListDataProvider.refresh();
            }
        });
    }

    private void loadMarkRoles() {
        sailingService.getMarkRoles(new AsyncCallback<List<MarkRoleDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.toString());
            }

            @Override
            public void onSuccess(List<MarkRoleDTO> markRoles) {
                allMarkRoles = markRoles;
            }
        });
    }

    private void loadMarkTemplates() {
        sailingService.getMarkTemplates(new AsyncCallback<List<MarkTemplateDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.toString());
            }

            @Override
            public void onSuccess(List<MarkTemplateDTO> markTemplateDTOs) {
                allMarkTemplates = markTemplateDTOs;
            }
        });
    }

    private void createCourseTemplateTable(final UserService userService) {
        // Create a CellTable.

        // Set a key provider that provides a unique key for each contact. If key is
        // used to identify contacts when fields (such as the name and address)
        // change.
        courseTemplateTable = new BaseCelltable<>(1000, tableResources);
        courseTemplateTable.setWidth("100%");

        // Attach a column sort handler to the ListDataProvider to sort the list.
        ListHandler<CourseTemplateDTO> sortHandler = new ListHandler<>(courseTemplateListDataProvider.getList());
        courseTemplateTable.addColumnSortHandler(sortHandler);

        // Add a selection model so we can select cells.
        refreshableSelectionModel = new RefreshableMultiSelectionModel<>(
                new EntityIdentityComparator<CourseTemplateDTO>() {
                    @Override
                    public boolean representSameEntity(CourseTemplateDTO dto1, CourseTemplateDTO dto2) {
                        return dto1.getUuid().equals(dto2.getUuid());
                    }

                    @Override
                    public int hashCode(CourseTemplateDTO t) {
                        return t.getUuid().hashCode();
                    }
                }, filterableCourseTemplatePanel.getAllListDataProvider());
        courseTemplateTable.setSelectionModel(refreshableSelectionModel, DefaultSelectionEventManager
                .createCustomManager(new DefaultSelectionEventManager.CheckboxEventTranslator<CourseTemplateDTO>() {
                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<CourseTemplateDTO> event) {
                        return !isCheckboxColumn(event.getColumn());
                    }

                    @Override
                    public SelectAction translateSelectionEvent(CellPreviewEvent<CourseTemplateDTO> event) {
                        NativeEvent nativeEvent = event.getNativeEvent();
                        if (BrowserEvents.CLICK.equals(nativeEvent.getType())) {
                            if (nativeEvent.getCtrlKey()) {
                                CourseTemplateDTO value = event.getValue();
                                refreshableSelectionModel.setSelected(value,
                                        !refreshableSelectionModel.isSelected(value));
                                return SelectAction.IGNORE;
                            }
                            if (!refreshableSelectionModel.getSelectedSet().isEmpty()
                                    && !isCheckboxColumn(event.getColumn())) {
                                return SelectAction.DEFAULT;
                            }
                        }
                        return SelectAction.TOGGLE;
                    }

                    private boolean isCheckboxColumn(int columnIndex) {
                        return columnIndex == 0;
                    }
                }));

        // Initialize the columns.
        initTableColumns(sortHandler, userService);

        courseTemplateListDataProvider.addDataDisplay(courseTemplateTable);
        add(courseTemplateTable);
        allCourseTemplates.clear();
        allCourseTemplates.addAll(courseTemplateListDataProvider.getList());
    }

    /**
     * Add the columns to the table.
     */
    private void initTableColumns(final ListHandler<CourseTemplateDTO> sortHandler, final UserService userService) {
        Column<CourseTemplateDTO, Boolean> checkColumn = new Column<CourseTemplateDTO, Boolean>(
                new BetterCheckboxCell(tableResources.cellTableStyle().cellTableCheckboxSelected(),
                        tableResources.cellTableStyle().cellTableCheckboxDeselected())) {
            @Override
            public Boolean getValue(CourseTemplateDTO object) {
                // Get the value from the selection model.
                return refreshableSelectionModel.isSelected(object);
            }
        };
        courseTemplateTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
        courseTemplateTable.setColumnWidth(checkColumn, 40, Unit.PX);

        // id
        Column<CourseTemplateDTO, String> idColumn = new Column<CourseTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(CourseTemplateDTO courseTemplate) {
                return courseTemplate.getUuid().toString();
            }
        };
        // name
        Column<CourseTemplateDTO, String> nameColumn = new Column<CourseTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(CourseTemplateDTO courseTemplate) {
                return courseTemplate.getName();
            }
        };
        // url
        Column<CourseTemplateDTO, String> urlColumn = new Column<CourseTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(CourseTemplateDTO courseTemplate) {
                return courseTemplate.getOptionalImageUrl().orElse("");
            }
        };
        // tags
        Column<CourseTemplateDTO, String> tagsColumn = new Column<CourseTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(CourseTemplateDTO courseTemplate) {
                return String.join(", ", courseTemplate.getTags());
            }
        };
        // # Waypoint Templates
        Column<CourseTemplateDTO, String> waypointTemplateCountColumn = new Column<CourseTemplateDTO, String>(
                new TextCell()) {
            @Override
            public String getValue(CourseTemplateDTO courseTemplate) {
                return Integer.toString(courseTemplate.getWaypointTemplates().size());
            }
        };

        nameColumn.setSortable(true);
        sortHandler.setComparator(nameColumn, new Comparator<CourseTemplateDTO>() {
            public int compare(CourseTemplateDTO courseTemplate1, CourseTemplateDTO courseTemplate2) {
                return courseTemplate1.getName().compareTo(courseTemplate2.getName());
            }
        });

        courseTemplateTable.addColumn(nameColumn, stringMessages.name());
        courseTemplateTable.addColumn(urlColumn, stringMessages.url());
        courseTemplateTable.addColumn(tagsColumn, stringMessages.tags());
        courseTemplateTable.addColumn(waypointTemplateCountColumn, stringMessages.waypoints());

        SecuredDTOOwnerColumn.configureOwnerColumns(courseTemplateTable, sortHandler, stringMessages);

        final HasPermissions type = SecuredDomainType.COURSE_TEMPLATE;

        final AccessControlledActionsColumn<CourseTemplateDTO, DefaultActionsImagesBarCell> actionsColumn = create(
                new DefaultActionsImagesBarCell(stringMessages), userService);
        final EditOwnershipDialog.DialogConfig<CourseTemplateDTO> configOwnership = EditOwnershipDialog
                .create(userService.getUserManagementService(), type, courseTemplate -> {
                    /* no refresh action */}, stringMessages);

        final EditACLDialog.DialogConfig<CourseTemplateDTO> configACL = EditACLDialog.create(
                userService.getUserManagementService(), type, courseTemplate -> courseTemplate.getAccessControlList(),
                stringMessages);
        actionsColumn.addAction(ACTION_DELETE, DELETE, e -> {
            if (Window.confirm(stringMessages.doYouReallyWantToRemoveCourseTemplate(e.getName()))) {
                sailingService.removeCourseTemplate(e.getUuid(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.couldNotRemoveCourseTemplate(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        refreshCourseTemplates();
                    }
                });
            }
        });
        actionsColumn.addAction(ACTION_UPDATE, UPDATE, e -> openEditCourseTemplateDialog(e, userService, false));
        actionsColumn.addAction(ACTION_CHANGE_OWNERSHIP, CHANGE_OWNERSHIP, configOwnership::openDialog);
        actionsColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                courseTemplate -> configACL.openDialog(courseTemplate));
        courseTemplateTable.addColumn(idColumn, stringMessages.id());
        courseTemplateTable.addColumn(actionsColumn, stringMessages.actions());
    }

    public void refreshCourseTemplates() {
        loadCourseTemplates();
        loadMarkRoles();
        loadMarkTemplates();
    }

    void openEditCourseTemplateDialog(final CourseTemplateDTO originalCourseTemplate, final UserService userService,
            final boolean isNew) {
        final CourseTemplateEditDialog dialog = new CourseTemplateEditDialog(sailingService, userService,
                stringMessages, originalCourseTemplate, allMarkRoles, allMarkTemplates,
                new DialogCallback<CourseTemplateDTO>() {
                    @Override
                    public void ok(CourseTemplateDTO courseTemplate) {
                        sailingService.createOrUpdateCourseTemplate(courseTemplate,
                                new AsyncCallback<CourseTemplateDTO>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError(
                                                "Error trying to store course template: " + caught.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(CourseTemplateDTO updatedCourseTemplate) {
                                        int editedCourseTemplateIndex = filterableCourseTemplatePanel
                                                .indexOf(originalCourseTemplate);
                                        filterableCourseTemplatePanel.remove(originalCourseTemplate);
                                        if (editedCourseTemplateIndex >= 0) {
                                            filterableCourseTemplatePanel.add(editedCourseTemplateIndex,
                                                    updatedCourseTemplate);
                                        } else {
                                            filterableCourseTemplatePanel.add(updatedCourseTemplate);
                                        }
                                        courseTemplateListDataProvider.refresh();
                                    }
                                });
                    }

                    @Override
                    public void cancel() {
                    }
                }, isNew);
        dialog.ensureDebugId("CourseTemplateEditDialog");
        dialog.show();
    }

}
