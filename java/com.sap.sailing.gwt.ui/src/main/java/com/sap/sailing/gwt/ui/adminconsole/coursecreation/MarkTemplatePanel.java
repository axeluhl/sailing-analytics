package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.CHANGE_OWNERSHIP;
import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_CHANGE_OWNERSHIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkTemplateDTO;
import com.sap.sse.common.Util;
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

public class MarkTemplatePanel extends FlowPanel {
    private static AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);

    private final SailingServiceAsync sailingService;
    private final LabeledAbstractFilterablePanel<MarkTemplateDTO> filterableMarkTemplates;
    private List<MarkTemplateDTO> allMarkTemplates;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private CellTable<MarkTemplateDTO> markTemplateTable;
    private ListDataProvider<MarkTemplateDTO> markTemplateListDataProvider = new ListDataProvider<>();
    private RefreshableMultiSelectionModel<MarkTemplateDTO> refreshableSelectionModel;

    public MarkTemplatePanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, final UserService userService) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        AccessControlledButtonPanel buttonAndFilterPanel = new AccessControlledButtonPanel(userService,
                SecuredDomainType.MARK_TEMPLATE);
        add(buttonAndFilterPanel);
        allMarkTemplates = new ArrayList<>();
        buttonAndFilterPanel.addUnsecuredAction(stringMessages.refresh(), new Command() {

            @Override
            public void execute() {
                loadMarkTemplates();
            }
        });
        buttonAndFilterPanel.addCreateAction(stringMessages.add(), new Command() {
            @Override
            public void execute() {
                openEditMarkTemplateDialog(new MarkTemplateDTO());
                // TODO add action
            }
        });

        Label lblFilterRaces = new Label(stringMessages.filterMarkTemplateByName() + ":");
        lblFilterRaces.setWordWrap(false);
        buttonAndFilterPanel.addUnsecuredWidget(lblFilterRaces);

        this.filterableMarkTemplates = new LabeledAbstractFilterablePanel<MarkTemplateDTO>(lblFilterRaces,
                allMarkTemplates, markTemplateListDataProvider, stringMessages) {
            @Override
            public List<String> getSearchableStrings(MarkTemplateDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.getName());
                strings.add(t.getShortName());
                strings.add(t.getUuid().toString());
                return strings;
            }

            @Override
            public AbstractCellTable<MarkTemplateDTO> getCellTable() {
                return markTemplateTable;
            }
        };
        createMarkTemplatesTable(userService);
        filterableMarkTemplates.getTextBox().ensureDebugId("MarkTemplatesFilterTextBox");
        buttonAndFilterPanel.addUnsecuredWidget(filterableMarkTemplates);
        filterableMarkTemplates
                .setCheckboxEnabledFilter(event -> userService.hasPermission(event, DefaultActions.UPDATE));
    }

    public void loadMarkTemplates() {
        markTemplateListDataProvider.getList().clear();
        sailingService.getMarkTemplates(new AsyncCallback<Iterable<MarkTemplateDTO>>() {

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.toString());
            }

            @Override
            public void onSuccess(Iterable<MarkTemplateDTO> result) {
                markTemplateListDataProvider.getList().clear();
                Util.addAll(result, markTemplateListDataProvider.getList());
                filterableMarkTemplates.updateAll(markTemplateListDataProvider.getList());
                markTemplateListDataProvider.refresh();
            }
        });
    }

    private void createMarkTemplatesTable(final UserService userService) {
        // Create a CellTable.

        // Set a key provider that provides a unique key for each contact. If key is
        // used to identify contacts when fields (such as the name and address)
        // change.
        markTemplateTable = new BaseCelltable<>(1000, tableResources);
        markTemplateTable.setWidth("100%");

        // Attach a column sort handler to the ListDataProvider to sort the list.
        ListHandler<MarkTemplateDTO> sortHandler = new ListHandler<>(markTemplateListDataProvider.getList());
        markTemplateTable.addColumnSortHandler(sortHandler);

        // Add a selection model so we can select cells.
        refreshableSelectionModel = new RefreshableMultiSelectionModel<>(
                new EntityIdentityComparator<MarkTemplateDTO>() {
                    @Override
                    public boolean representSameEntity(MarkTemplateDTO dto1, MarkTemplateDTO dto2) {
                        return dto1.getUuid().equals(dto2.getUuid());
                    }

                    @Override
                    public int hashCode(MarkTemplateDTO t) {
                        return t.getUuid().hashCode();
                    }
                }, filterableMarkTemplates.getAllListDataProvider());
        markTemplateTable.setSelectionModel(refreshableSelectionModel, DefaultSelectionEventManager
                .createCustomManager(new DefaultSelectionEventManager.CheckboxEventTranslator<MarkTemplateDTO>() {
                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<MarkTemplateDTO> event) {
                        return !isCheckboxColumn(event.getColumn());
                    }

                    @Override
                    public SelectAction translateSelectionEvent(CellPreviewEvent<MarkTemplateDTO> event) {
                        NativeEvent nativeEvent = event.getNativeEvent();
                        if (BrowserEvents.CLICK.equals(nativeEvent.getType())) {
                            if (nativeEvent.getCtrlKey()) {
                                MarkTemplateDTO value = event.getValue();
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

        markTemplateListDataProvider.addDataDisplay(markTemplateTable);
        add(markTemplateTable);
        allMarkTemplates.clear();
        allMarkTemplates.addAll(markTemplateListDataProvider.getList());
    }

    /**
     * Add the columns to the table.
     */
    private void initTableColumns(final ListHandler<MarkTemplateDTO> sortHandler, final UserService userService) {
        Column<MarkTemplateDTO, Boolean> checkColumn = new Column<MarkTemplateDTO, Boolean>(
                new BetterCheckboxCell(tableResources.cellTableStyle().cellTableCheckboxSelected(),
                        tableResources.cellTableStyle().cellTableCheckboxDeselected())) {
            @Override
            public Boolean getValue(MarkTemplateDTO object) {
                // Get the value from the selection model.
                return refreshableSelectionModel.isSelected(object);
            }
        };
        markTemplateTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
        markTemplateTable.setColumnWidth(checkColumn, 40, Unit.PX);

        // id
        Column<MarkTemplateDTO, String> idColumn = new Column<MarkTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkTemplateDTO markTemplate) {
                return markTemplate.getUuid().toString();
            }
        };
        // name
        Column<MarkTemplateDTO, String> nameColumn = new Column<MarkTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkTemplateDTO markTemplate) {
                return markTemplate.getName();
            }
        };
        // short name
        Column<MarkTemplateDTO, String> shortNameColumn = new Column<MarkTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkTemplateDTO markTemplate) {
                return markTemplate.getShortName();
            }
        };
        // color
        Column<MarkTemplateDTO, String> colorColumn = new Column<MarkTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkTemplateDTO markTemplate) {
                return markTemplate.getColor() != null ? markTemplate.getColor().getAsHtml() : "";
            }
        };
        // shape
        Column<MarkTemplateDTO, String> shapeColumn = new Column<MarkTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkTemplateDTO markTemplate) {
                return markTemplate.getShape();
            }
        };
        // pattern
        Column<MarkTemplateDTO, String> patternColumn = new Column<MarkTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkTemplateDTO markTemplate) {
                return markTemplate.getPattern();
            }
        };
        // mark type
        Column<MarkTemplateDTO, String> typeColumn = new Column<MarkTemplateDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkTemplateDTO markTemplate) {
                return markTemplate.getType() != null ? markTemplate.getType().name() : "";
            }
        };

        nameColumn.setSortable(true);
        sortHandler.setComparator(nameColumn, new Comparator<MarkTemplateDTO>() {
            public int compare(MarkTemplateDTO markTemplate1, MarkTemplateDTO markTemplate2) {
                return markTemplate1.getName().compareTo(markTemplate2.getName());
            }
        });

        markTemplateTable.addColumn(nameColumn, stringMessages.name());
        markTemplateTable.addColumn(shortNameColumn, stringMessages.shortName());
        markTemplateTable.addColumn(colorColumn, stringMessages.color());
        markTemplateTable.addColumn(shapeColumn, stringMessages.shape());
        markTemplateTable.addColumn(patternColumn, stringMessages.pattern());
        markTemplateTable.addColumn(typeColumn, stringMessages.type());

        SecuredDTOOwnerColumn.configureOwnerColumns(markTemplateTable, sortHandler, stringMessages);

        final HasPermissions type = SecuredDomainType.MARK_TEMPLATE;

        final AccessControlledActionsColumn<MarkTemplateDTO, DefaultActionsImagesBarCell> actionsColumn = create(
                new DefaultActionsImagesBarCell(stringMessages), userService);
        final EditOwnershipDialog.DialogConfig<MarkTemplateDTO> configOwnership = EditOwnershipDialog
                .create(userService.getUserManagementService(), type, markTemplate -> {
                    /* no refresh action */}, stringMessages);

        final EditACLDialog.DialogConfig<MarkTemplateDTO> configACL = EditACLDialog.create(
                userService.getUserManagementService(), type, markTemplate -> markTemplate.getAccessControlList(),
                stringMessages);
        actionsColumn.addAction(ACTION_CHANGE_OWNERSHIP, CHANGE_OWNERSHIP, configOwnership::openDialog);
        actionsColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                markTemplate -> configACL.openDialog(markTemplate));
        markTemplateTable.addColumn(idColumn, stringMessages.id());
        markTemplateTable.addColumn(actionsColumn, stringMessages.actions());

    }

    public void refreshMarkTemplates() {
        loadMarkTemplates();
    }

    void openEditMarkTemplateDialog(final MarkTemplateDTO originalMarkTemplate) {
        final MarkTemplateEditDialog dialog = new MarkTemplateEditDialog(stringMessages, originalMarkTemplate,
                new DialogCallback<MarkTemplateDTO>() {
                    @Override
                    public void ok(MarkTemplateDTO markTemplate) {
                        sailingService.addOrUpdateMarkTemplate(markTemplate, new AsyncCallback<MarkTemplateDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter
                                        .reportError("Error trying to update mark template: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(MarkTemplateDTO updatedMarkTemplate) {
                                int editedMarkTemplateIndex = filterableMarkTemplates.indexOf(originalMarkTemplate);
                                filterableMarkTemplates.remove(originalMarkTemplate);
                                if (editedMarkTemplateIndex >= 0) {
                                    filterableMarkTemplates.add(editedMarkTemplateIndex, updatedMarkTemplate);
                                } else {
                                    filterableMarkTemplates.add(updatedMarkTemplate);
                                }
                                markTemplateListDataProvider.refresh();
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                    }
                });
        dialog.ensureDebugId("MarkTemplateEditDialog");
        dialog.show();
    }

}
