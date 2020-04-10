package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.CHANGE_OWNERSHIP;
import static com.sap.sse.security.shared.HasPermissions.DefaultActions.DELETE;
import static com.sap.sse.security.shared.HasPermissions.DefaultActions.UPDATE;
import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_CHANGE_OWNERSHIP;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_DELETE;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_UPDATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleResources;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkPropertiesDTO;
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

public class MarkPropertiesPanel extends FlowPanel {
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    private static AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);
    private static final AbstractImagePrototype positionImagePrototype = AbstractImagePrototype
            .create(resources.ping());
    private static final AbstractImagePrototype setDeviceIdentifierImagePrototype = AbstractImagePrototype
            .create(resources.mapDevices());

    private final SailingServiceAsync sailingService;
    private final LabeledAbstractFilterablePanel<MarkPropertiesDTO> filterableMarkProperties;
    private List<MarkPropertiesDTO> allMarkProperties;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private CellTable<MarkPropertiesDTO> markPropertiesTable;
    private ListDataProvider<MarkPropertiesDTO> markPropertiesListDataProvider = new ListDataProvider<>();
    private RefreshableMultiSelectionModel<MarkPropertiesDTO> refreshableSelectionModel;

    public MarkPropertiesPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, final UserService userService) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        AccessControlledButtonPanel buttonAndFilterPanel = new AccessControlledButtonPanel(userService,
                SecuredDomainType.MARK_TEMPLATE);
        add(buttonAndFilterPanel);
        allMarkProperties = new ArrayList<>();
        Label lblFilterRaces = new Label(stringMessages.filterMarkPropertiesByName() + ":");
        lblFilterRaces.setWordWrap(false);
        this.filterableMarkProperties = new LabeledAbstractFilterablePanel<MarkPropertiesDTO>(lblFilterRaces,
                allMarkProperties, markPropertiesListDataProvider, stringMessages) {
            @Override
            public List<String> getSearchableStrings(MarkPropertiesDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.getName());
                strings.add(t.getCommonMarkProperties().getShortName());
                strings.add(t.getUuid().toString());
                Util.addAll(t.getTags(), strings);
                return strings;
            }

            @Override
            public AbstractCellTable<MarkPropertiesDTO> getCellTable() {
                return markPropertiesTable;
            }
        };
        createMarkPropertiesTable(userService);
        buttonAndFilterPanel.addUnsecuredAction(stringMessages.refresh(), new Command() {
            @Override
            public void execute() {
                loadMarkProperties();
            }
        });
        buttonAndFilterPanel.addCreateAction(stringMessages.add(), new Command() {
            @Override
            public void execute() {
                openEditMarkPropertiesDialog(new MarkPropertiesDTO());
            }
        });
        buttonAndFilterPanel.addRemoveAction(refreshableSelectionModel, stringMessages.remove(), new Command() {

            @Override
            public void execute() {
                if (askUserForConfirmation()) {
                    removeMarkProperties(refreshableSelectionModel.getSelectedSet().stream().map(markPropertiesDTO -> {
                        return markPropertiesDTO.getUuid();
                    }).collect(Collectors.toList()));
                }
            }

            private void removeMarkProperties(Collection<UUID> markPropertiesUuids) {
                if (!markPropertiesUuids.isEmpty()) {
                    sailingService.removeMarkProperties(markPropertiesUuids, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error trying to remove mark properties:" + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Void result) {
                            refreshMarkProperties();
                        }
                    });
                }
            }

            private boolean askUserForConfirmation() {
                if (refreshableSelectionModel.itemIsSelectedButNotVisible(markPropertiesTable.getVisibleItems())) {
                    final String markPropertiesNames = refreshableSelectionModel.getSelectedSet().stream()
                            .map(MarkPropertiesDTO::getName).collect(Collectors.joining("\n"));
                    return Window.confirm(
                            stringMessages.doYouReallyWantToRemoveNonVisibleMarkProperties(markPropertiesNames));
                }
                return Window.confirm(stringMessages.doYouReallyWantToRemoveSeveralMarkProperties());
            }
        });
        buttonAndFilterPanel.addUnsecuredWidget(lblFilterRaces);
        filterableMarkProperties.getTextBox().ensureDebugId("MarkPropertiesFilterTextBox");
        buttonAndFilterPanel.addUnsecuredWidget(filterableMarkProperties);
        filterableMarkProperties
                .setUpdatePermissionFilterForCheckbox(event -> userService.hasPermission(event, DefaultActions.UPDATE));
    }

    public void loadMarkProperties() {
        markPropertiesListDataProvider.getList().clear();
        sailingService.getMarkProperties(new AsyncCallback<List<MarkPropertiesDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.toString());
            }

            @Override
            public void onSuccess(List<MarkPropertiesDTO> result) {
                markPropertiesListDataProvider.getList().clear();
                Util.addAll(result, markPropertiesListDataProvider.getList());
                filterableMarkProperties.updateAll(markPropertiesListDataProvider.getList());
                markPropertiesListDataProvider.refresh();
            }
        });
    }

    private void createMarkPropertiesTable(final UserService userService) {
        // Create a CellTable.
        // Set a key provider that provides a unique key for each contact. If key is
        // used to identify contacts when fields (such as the name and address)
        // change.
        markPropertiesTable = new BaseCelltable<>(1000, tableResources);
        markPropertiesTable.setWidth("100%");
        // Attach a column sort handler to the ListDataProvider to sort the list.
        ListHandler<MarkPropertiesDTO> sortHandler = new ListHandler<>(markPropertiesListDataProvider.getList());
        markPropertiesTable.addColumnSortHandler(sortHandler);
        // Add a selection model so we can select cells.
        refreshableSelectionModel = new RefreshableMultiSelectionModel<>(
                new EntityIdentityComparator<MarkPropertiesDTO>() {
                    @Override
                    public boolean representSameEntity(MarkPropertiesDTO dto1, MarkPropertiesDTO dto2) {
                        return dto1.getUuid().equals(dto2.getUuid());
                    }

                    @Override
                    public int hashCode(MarkPropertiesDTO t) {
                        return t.getUuid().hashCode();
                    }
                }, filterableMarkProperties.getAllListDataProvider());
        markPropertiesTable.setSelectionModel(refreshableSelectionModel, DefaultSelectionEventManager
                .createCustomManager(new DefaultSelectionEventManager.CheckboxEventTranslator<MarkPropertiesDTO>() {
                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<MarkPropertiesDTO> event) {
                        return !isCheckboxColumn(event.getColumn());
                    }

                    @Override
                    public SelectAction translateSelectionEvent(CellPreviewEvent<MarkPropertiesDTO> event) {
                        NativeEvent nativeEvent = event.getNativeEvent();
                        if (BrowserEvents.CLICK.equals(nativeEvent.getType())) {
                            if (nativeEvent.getCtrlKey()) {
                                MarkPropertiesDTO value = event.getValue();
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
        markPropertiesListDataProvider.addDataDisplay(markPropertiesTable);
        add(markPropertiesTable);
        allMarkProperties.clear();
        allMarkProperties.addAll(markPropertiesListDataProvider.getList());
    }

    /**
     * Add the columns to the table.
     */
    private void initTableColumns(final ListHandler<MarkPropertiesDTO> sortHandler, final UserService userService) {
        Column<MarkPropertiesDTO, Boolean> checkColumn = new Column<MarkPropertiesDTO, Boolean>(
                new BetterCheckboxCell(tableResources.cellTableStyle().cellTableCheckboxSelected(),
                        tableResources.cellTableStyle().cellTableCheckboxDeselected())) {
            @Override
            public Boolean getValue(MarkPropertiesDTO object) {
                // Get the value from the selection model.
                return refreshableSelectionModel.isSelected(object);
            }
        };
        markPropertiesTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
        markPropertiesTable.setColumnWidth(checkColumn, 40, Unit.PX);

        // id
        Column<MarkPropertiesDTO, String> idColumn = new Column<MarkPropertiesDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkPropertiesDTO markProperties) {
                return markProperties.getUuid().toString();
            }
        };
        // name
        Column<MarkPropertiesDTO, String> nameColumn = new Column<MarkPropertiesDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkPropertiesDTO markProperties) {
                return markProperties.getName();
            }
        };
        // short name
        Column<MarkPropertiesDTO, String> shortNameColumn = new Column<MarkPropertiesDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkPropertiesDTO markProperties) {
                return markProperties.getCommonMarkProperties().getShortName();
            }
        };
        // color
        Column<MarkPropertiesDTO, String> colorColumn = new Column<MarkPropertiesDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkPropertiesDTO markProperties) {
                return markProperties.getCommonMarkProperties().getColor() != null
                        ? markProperties.getCommonMarkProperties().getColor().getAsHtml()
                        : "";
            }
        };
        // shape
        Column<MarkPropertiesDTO, String> shapeColumn = new Column<MarkPropertiesDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkPropertiesDTO markProperties) {
                return markProperties.getCommonMarkProperties().getShape();
            }
        };
        // pattern
        Column<MarkPropertiesDTO, String> patternColumn = new Column<MarkPropertiesDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkPropertiesDTO markProperties) {
                return markProperties.getCommonMarkProperties().getPattern();
            }
        };
        // mark type
        Column<MarkPropertiesDTO, String> typeColumn = new Column<MarkPropertiesDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkPropertiesDTO markProperties) {
                return markProperties.getCommonMarkProperties().getType() != null
                        ? markProperties.getCommonMarkProperties().getType().name()
                        : "";
            }
        };
        // tags
        Column<MarkPropertiesDTO, String> tagsColumn = new Column<MarkPropertiesDTO, String>(new TextCell()) {
            @Override
            public String getValue(MarkPropertiesDTO markProperties) {
                return String.join(", ", markProperties.getTags());
            }
        };

        Column<MarkPropertiesDTO, AbstractImagePrototype> positioningColumn = new Column<MarkPropertiesDTO, AbstractImagePrototype>(
                new AbstractCell<AbstractImagePrototype>() {

                    @Override
                    public void render(Context context, AbstractImagePrototype image, SafeHtmlBuilder sb) {
                        if (image != null) sb.append(image.getSafeHtml());
                    }
                }) {
            @Override
            public AbstractImagePrototype getValue(MarkPropertiesDTO markProperties) {
                switch (markProperties.getPositioningType()) {
                case "FIXED_POSITION":
                    return positionImagePrototype;
                case "DEVICE":
                    return setDeviceIdentifierImagePrototype;
                default:
                    return null;
                }
            }
        };

        nameColumn.setSortable(true);
        sortHandler.setComparator(nameColumn, new Comparator<MarkPropertiesDTO>() {
            public int compare(MarkPropertiesDTO markProperties1, MarkPropertiesDTO markProperties2) {
                return markProperties1.getName().compareTo(markProperties2.getName());
            }
        });

        markPropertiesTable.addColumn(nameColumn, stringMessages.name());
        markPropertiesTable.addColumn(shortNameColumn, stringMessages.shortName());
        markPropertiesTable.addColumn(colorColumn, stringMessages.color());
        markPropertiesTable.addColumn(shapeColumn, stringMessages.shape());
        markPropertiesTable.addColumn(patternColumn, stringMessages.pattern());
        markPropertiesTable.addColumn(typeColumn, stringMessages.type());
        markPropertiesTable.addColumn(positioningColumn, stringMessages.position());
        markPropertiesTable.addColumn(tagsColumn, stringMessages.tags());

        SecuredDTOOwnerColumn.configureOwnerColumns(markPropertiesTable, sortHandler, stringMessages);

        final HasPermissions type = SecuredDomainType.MARK_PROPERTIES;

        final AccessControlledActionsColumn<MarkPropertiesDTO, MarkPropertiesImagesbarCell> actionsColumn = create(
                new MarkPropertiesImagesbarCell(stringMessages), userService);
        final EditOwnershipDialog.DialogConfig<MarkPropertiesDTO> configOwnership = EditOwnershipDialog
                .create(userService.getUserManagementService(), type, markProperties -> {
                    /* no refresh action */}, stringMessages);

        final EditACLDialog.DialogConfig<MarkPropertiesDTO> configACL = EditACLDialog.create(
                userService.getUserManagementService(), type, markProperties -> markProperties.getAccessControlList(),
                stringMessages);
        actionsColumn.addAction(ACTION_DELETE, DELETE, e -> {
            if (Window.confirm(stringMessages.doYouReallyWantToRemoveMarkProperties(e.getName()))) {
                sailingService.removeMarkProperties(e.getUuid(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.couldNotRemoveMarkProperties(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        refreshMarkProperties();
                    }
                });
            }
        });
        actionsColumn.addAction(ACTION_UPDATE, UPDATE, this::openEditMarkPropertiesDialog);
        actionsColumn.addAction(MarkPropertiesImagesbarCell.ACTION_SET_DEVICE_IDENTIFIER,
                this::openEditMarkPropertiesDeviceIdentifierDialog);
        actionsColumn.addAction(MarkPropertiesImagesbarCell.ACTION_SET_POSITION,
                this::openEditMarkPropertiesPositionDialog);
        actionsColumn.addAction(MarkPropertiesImagesbarCell.ACTION_UNSET_POSITION,
                this::unsetPosition);
        actionsColumn.addAction(ACTION_CHANGE_OWNERSHIP, CHANGE_OWNERSHIP, configOwnership::openDialog);
        actionsColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                markProperties -> configACL.openDialog(markProperties));
        markPropertiesTable.addColumn(idColumn, stringMessages.id());
        markPropertiesTable.addColumn(actionsColumn, stringMessages.actions());
    }

    public void refreshMarkProperties() {
        loadMarkProperties();
    }

    void openEditMarkPropertiesDialog(final MarkPropertiesDTO originalMarkProperties) {
        final MarkPropertiesEditDialog dialog = new MarkPropertiesEditDialog(stringMessages, originalMarkProperties,
                new DialogCallback<MarkPropertiesDTO>() {
                    @Override
                    public void ok(MarkPropertiesDTO markProperties) {
                        sailingService.addOrUpdateMarkProperties(markProperties,
                                new AsyncCallback<MarkPropertiesDTO>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError(
                                                "Error trying to update mark properties: " + caught.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(MarkPropertiesDTO updatedMarkProperties) {
                                        int editedMarkPropertiesIndex = filterableMarkProperties
                                                .indexOf(originalMarkProperties);
                                        filterableMarkProperties.remove(originalMarkProperties);
                                        if (editedMarkPropertiesIndex >= 0) {
                                            filterableMarkProperties.add(editedMarkPropertiesIndex,
                                                    updatedMarkProperties);
                                        } else {
                                            filterableMarkProperties.add(updatedMarkProperties);
                                        }
                                        markPropertiesListDataProvider.refresh();
                                    }
                                });
                    }

                    @Override
                    public void cancel() {
                    }
                });
        dialog.ensureDebugId("MarkPropertiesEditDialog");
        dialog.show();
    }

    void openEditMarkPropertiesDeviceIdentifierDialog(final MarkPropertiesDTO originalMarkProperties) {
        final MarkPropertiesDeviceIdentifierEditDialog dialog = new MarkPropertiesDeviceIdentifierEditDialog(
                stringMessages, null, new DialogCallback<DeviceIdentifierDTO>() {
                    @Override
                    public void ok(DeviceIdentifierDTO deviceIdentifier) {
                        sailingService.updateMarkPropertiesPositioning(originalMarkProperties.getUuid(),
                                deviceIdentifier, null, new AsyncCallback<MarkPropertiesDTO>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError(stringMessages.errorTryingToUpdateMarkProperties(caught.getMessage()));
                                    }

                                    @Override
                                    public void onSuccess(MarkPropertiesDTO updatedMarkProperties) {
                                        int editedMarkPropertiesIndex = filterableMarkProperties
                                                .indexOf(originalMarkProperties);
                                        filterableMarkProperties.remove(originalMarkProperties);
                                        if (editedMarkPropertiesIndex >= 0) {
                                            filterableMarkProperties.add(editedMarkPropertiesIndex,
                                                    updatedMarkProperties);
                                        } else {
                                            filterableMarkProperties.add(updatedMarkProperties);
                                        }
                                        markPropertiesListDataProvider.refresh();
                                    }
                                });
                    }

                    @Override
                    public void cancel() {
                    }
                });
        dialog.ensureDebugId("MarkPropertiesDeviceIdentifierEditDialog");
        dialog.show();
    }

    private void unsetPosition(final MarkPropertiesDTO originalMarkProperties) {
        if (Window.confirm(stringMessages.confirmUnsettingPositionForMarkProperties(originalMarkProperties.getName()))) {
            sailingService.updateMarkPropertiesPositioning(originalMarkProperties.getUuid(), /* no tracking device */ null,
                    /* and no fixed position either */ null, new AsyncCallback<MarkPropertiesDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(stringMessages.errorTryingToUpdateMarkProperties(caught.getMessage()));
                        }

                        @Override
                        public void onSuccess(MarkPropertiesDTO updatedMarkProperties) {
                            int editedMarkPropertiesIndex = filterableMarkProperties
                                    .indexOf(originalMarkProperties);
                            filterableMarkProperties.remove(originalMarkProperties);
                            if (editedMarkPropertiesIndex >= 0) {
                                filterableMarkProperties.add(editedMarkPropertiesIndex,
                                        updatedMarkProperties);
                            } else {
                                filterableMarkProperties.add(updatedMarkProperties);
                            }
                            markPropertiesListDataProvider.refresh();
                        }
                    });
        }
    }
    
    void openEditMarkPropertiesPositionDialog(final MarkPropertiesDTO originalMarkProperties) {
        final MarkPropertiesPositionEditDialog dialog = new MarkPropertiesPositionEditDialog(stringMessages, null,
                new DialogCallback<Position>() {
                    @Override
                    public void ok(Position fixedPosition) {
                        sailingService.updateMarkPropertiesPositioning(originalMarkProperties.getUuid(), null,
                                fixedPosition, new AsyncCallback<MarkPropertiesDTO>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError(stringMessages.errorTryingToUpdateMarkProperties(caught.getMessage()));
                                    }

                                    @Override
                                    public void onSuccess(MarkPropertiesDTO updatedMarkProperties) {
                                        int editedMarkPropertiesIndex = filterableMarkProperties
                                                .indexOf(originalMarkProperties);
                                        filterableMarkProperties.remove(originalMarkProperties);
                                        if (editedMarkPropertiesIndex >= 0) {
                                            filterableMarkProperties.add(editedMarkPropertiesIndex,
                                                    updatedMarkProperties);
                                        } else {
                                            filterableMarkProperties.add(updatedMarkProperties);
                                        }
                                        markPropertiesListDataProvider.refresh();
                                    }
                                });
                    }

                    @Override
                    public void cancel() {
                    }
                });
        dialog.ensureDebugId("MarkPropertiesPositionEditDialog");
        dialog.show();
    }

    private static class MarkPropertiesImagesbarCell extends DefaultActionsImagesBarCell {
        public static final String ACTION_SET_DEVICE_IDENTIFIER = "ACTION_SET_DEVICE_IDENTIFIER";
        public static final String ACTION_SET_POSITION = "ACTION_SET_POSITION";
        public static final String ACTION_UNSET_POSITION = "ACTION_UNSET_POSITION";
        private final StringMessages stringMessages;

        public MarkPropertiesImagesbarCell(StringMessages stringMessages) {
            super(stringMessages);
            this.stringMessages = stringMessages;
        }

        @Override
        protected Iterable<ImageSpec> getImageSpecs() {
            return Arrays.asList(getUpdateImageSpec(),
                    new ImageSpec(ACTION_SET_DEVICE_IDENTIFIER, stringMessages.setDeviceIdentifier(),
                            resources.mapDevices()),
                    new ImageSpec(ACTION_SET_POSITION, stringMessages.setPosition(), resources.ping()),
                    new ImageSpec(ACTION_UNSET_POSITION, stringMessages.unsetPosition(), resources.removePing()),
                    getDeleteImageSpec(), getChangeOwnershipImageSpec(), getChangeACLImageSpec());
        }
    }
}
