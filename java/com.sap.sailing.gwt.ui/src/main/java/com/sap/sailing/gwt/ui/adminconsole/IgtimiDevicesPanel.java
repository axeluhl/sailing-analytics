package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.CHANGE_OWNERSHIP;
import static com.sap.sse.security.shared.HasPermissions.DefaultActions.DELETE;
import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_CHANGE_OWNERSHIP;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_DELETE;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithDateTimeBox;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.IgtimiDataAccessWindowWithSecurityDTO;
import com.sap.sailing.gwt.ui.shared.IgtimiDeviceWithSecurityDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.adminconsole.FilterablePanelProvider;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.AbstractSortableTextColumn;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.FlushableCellTable;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.SelectionCheckboxColumn;
import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledActionsColumn;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog.DialogConfig;
import com.sap.sse.security.ui.client.component.SecuredDTOOwnerColumn;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;

/**
 * TODO bug 6059: implement a second table shown when a single device is selected in the devices table that shows the
 * data access windows for that device; alternatively we could show all data access windows for all devices when nothing
 * is selected. Move the "Add" button from devices to DAWs.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class IgtimiDevicesPanel extends FlowPanel implements FilterablePanelProvider<IgtimiDeviceWithSecurityDTO> {
    private final StringMessages stringMessages;
    private final SailingServiceWriteAsync sailingServiceWrite;
    private final ErrorReporter errorReporter;
    private final LabeledAbstractFilterablePanel<IgtimiDeviceWithSecurityDTO> filterAccountsPanel;
    private final RefreshableMultiSelectionModel<IgtimiDeviceWithSecurityDTO> refreshableDevicesSelectionModel;

    public static class AccountImagesBarCell extends ImagesBarCell {
        public static final String ACTION_REMOVE = "ACTION_REMOVE";
        private final StringMessages stringMessages;

        public AccountImagesBarCell(StringMessages stringMessages) {
            super();
            this.stringMessages = stringMessages;
        }

        public AccountImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringConstants) {
            super();
            this.stringMessages = stringConstants;
        }

        @Override
        protected Iterable<ImageSpec> getImageSpecs() {
            return Arrays.asList(new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(),
                    makeImagePrototype(IconResources.INSTANCE.removeIcon())));
        }
    }

    @SuppressWarnings("unchecked")
    public IgtimiDevicesPanel(final Presenter presenter,
            final StringMessages stringMessages) {
        this.sailingServiceWrite = presenter.getSailingService();
        this.errorReporter = presenter.getErrorReporter();
        this.stringMessages = stringMessages;

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

        // setup table
        final FlushableCellTable<IgtimiDeviceWithSecurityDTO> cellTable = new FlushableCellTable<>(/* pageSize */ 50,
                tableRes);
        final ListDataProvider<IgtimiDeviceWithSecurityDTO> filteredAccounts = new ListDataProvider<>();
        filterAccountsPanel = new LabeledAbstractFilterablePanel<IgtimiDeviceWithSecurityDTO>(
                new Label(stringMessages.igtimiDevices()), Collections.emptyList(), filteredAccounts, stringMessages) {
            @Override
            public Iterable<String> getSearchableStrings(IgtimiDeviceWithSecurityDTO t) {
                final Set<String> strings = new HashSet<>();
                strings.add(""+t.getId());
                if (t.getName() != null) {
                    strings.add(t.getName());
                }
                if (t.getSerialNumber() != null) {
                    strings.add(t.getSerialNumber());
                }
                if (t.getServiceTag() != null) {
                    strings.add(t.getServiceTag());
                }
                return strings;
            }

            @Override
            public AbstractCellTable<IgtimiDeviceWithSecurityDTO> getCellTable() {
                return null;
            }
        };
        createIgtimiDevicesTable(cellTable, tableRes, presenter.getUserService(), filteredAccounts, filterAccountsPanel);
        // refreshableAccountsSelectionModel will be of correct type, see below in createIgtimiAccountsTable
        refreshableDevicesSelectionModel = (RefreshableMultiSelectionModel<IgtimiDeviceWithSecurityDTO>) cellTable
                .getSelectionModel();
        final Panel controlsPanel = new HorizontalPanel();
        filterAccountsPanel
                .setUpdatePermissionFilterForCheckbox(account -> presenter.getUserService().hasPermission(account, DefaultActions.UPDATE));
        controlsPanel.add(filterAccountsPanel);
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(presenter.getUserService(),
                SecuredDomainType.IGTIMI_DEVICE);
        controlsPanel.add(buttonPanel);
        buttonPanel.addUnsecuredAction(stringMessages.refresh(), () -> refresh());
        // setup controls
        final Button removeDeviceButton = buttonPanel.addRemoveAction(stringMessages.remove(), () -> {
            if (refreshableDevicesSelectionModel.getSelectedSet().size() > 0) {
                if (Window.confirm(stringMessages.doYouReallyWantToRemoveTheSelectedIgtimiDevices())) {
                    for (IgtimiDeviceWithSecurityDTO account : refreshableDevicesSelectionModel.getSelectedSet()) {
                        removeDevice(account, filteredAccounts);
                    }
                }
            }
        });
        removeDeviceButton.setEnabled(false);
        refreshableDevicesSelectionModel.addSelectionChangeHandler(
                e -> removeDeviceButton.setEnabled(refreshableDevicesSelectionModel.getSelectedSet().size() > 0));
        add(controlsPanel);
        add(cellTable);
        // add button
        final Button addDeviceButton = buttonPanel.addCreateAction(stringMessages.addIgtimiDevice(), () -> addDataAccessWindow());
        addDeviceButton.ensureDebugId("addIgtimiDevice");
    }

    private FlushableCellTable<IgtimiDeviceWithSecurityDTO> createIgtimiDevicesTable(
            final FlushableCellTable<IgtimiDeviceWithSecurityDTO> table, final CellTableWithCheckboxResources tableResources,
            final UserService userService, final ListDataProvider<IgtimiDeviceWithSecurityDTO> filteredDevices,
            final LabeledAbstractFilterablePanel<IgtimiDeviceWithSecurityDTO> filterDevicesPanel) {
        filteredDevices.addDataDisplay(table);
        final SelectionCheckboxColumn<IgtimiDeviceWithSecurityDTO> devicesSelectionCheckboxColumn = new SelectionCheckboxColumn<IgtimiDeviceWithSecurityDTO>(
                tableResources.cellTableStyle().cellTableCheckboxSelected(),
                tableResources.cellTableStyle().cellTableCheckboxDeselected(),
                tableResources.cellTableStyle().cellTableCheckboxColumnCell(),
                new EntityIdentityComparator<IgtimiDeviceWithSecurityDTO>() {
                    @Override
                    public boolean representSameEntity(IgtimiDeviceWithSecurityDTO a1, IgtimiDeviceWithSecurityDTO a2) {
                        return a1.getId() == a2.getId();
                    }

                    @Override
                    public int hashCode(IgtimiDeviceWithSecurityDTO t) {
                        return 7482 ^ (int) t.getId();
                    }
                }, filterDevicesPanel.getAllListDataProvider(), table);
        final ListHandler<IgtimiDeviceWithSecurityDTO> columnSortHandler = new ListHandler<>(filteredDevices.getList());
        table.addColumnSortHandler(columnSortHandler);
        columnSortHandler.setComparator(devicesSelectionCheckboxColumn, devicesSelectionCheckboxColumn.getComparator());
        final TextColumn<IgtimiDeviceWithSecurityDTO> deviceIdColumn = new AbstractSortableTextColumn<>(
                device -> ""+device.getId(), columnSortHandler);
        final TextColumn<IgtimiDeviceWithSecurityDTO> deviceNameColumn = new AbstractSortableTextColumn<>(
                device -> device.getName(), columnSortHandler);
        final TextColumn<IgtimiDeviceWithSecurityDTO> deviceSerialNumberColumn = new AbstractSortableTextColumn<>(
                device -> device.getSerialNumber(), columnSortHandler);
        final TextColumn<IgtimiDeviceWithSecurityDTO> serviceTagColumn = new AbstractSortableTextColumn<>(
                device -> device.getServiceTag(), columnSortHandler);
        final HasPermissions type = SecuredDomainType.IGTIMI_DEVICE;
        final AccessControlledActionsColumn<IgtimiDeviceWithSecurityDTO, DefaultActionsImagesBarCell> roleActionColumn = create(
                new DefaultActionsImagesBarCell(stringMessages), userService);
        roleActionColumn.addAction(ACTION_DELETE, DELETE, device -> {
            if (Window.confirm(stringMessages.doYouReallyWantToRemoveIgtimiDevice(device.getSerialNumber()))) {
                removeDevice(device, filteredDevices);
            }
        });
        final DialogConfig<IgtimiDeviceWithSecurityDTO> config = EditOwnershipDialog
                .create(userService.getUserManagementWriteService(), type, roleDefinition -> refresh(), stringMessages);
        roleActionColumn.addAction(ACTION_CHANGE_OWNERSHIP, CHANGE_OWNERSHIP, config::openOwnershipDialog);
        final EditACLDialog.DialogConfig<IgtimiDeviceWithSecurityDTO> configACL = EditACLDialog
                .create(userService.getUserManagementWriteService(), type, roleDefinition -> refresh(), stringMessages);
        roleActionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                configACL::openDialog);
        // add columns to table:
        table.addColumn(devicesSelectionCheckboxColumn, devicesSelectionCheckboxColumn.getHeader());
        table.addColumn(deviceIdColumn, stringMessages.id());
        table.addColumn(deviceNameColumn, stringMessages.name());
        table.addColumn(deviceSerialNumberColumn, stringMessages.serialNumber());
        table.addColumn(serviceTagColumn, stringMessages.serviceTag());
        SecuredDTOOwnerColumn.configureOwnerColumns(table, columnSortHandler, stringMessages);
        table.addColumn(roleActionColumn, stringMessages.actions());
        table.setSelectionModel(devicesSelectionCheckboxColumn.getSelectionModel(),
                devicesSelectionCheckboxColumn.getSelectionManager());
        return table;
    }

    public void refresh() {
        sailingServiceWrite.getAllIgtimiDevicesWithSecurity(new AsyncCallback<Iterable<IgtimiDeviceWithSecurityDTO>>() {
            @Override
            public void onSuccess(Iterable<IgtimiDeviceWithSecurityDTO> result) {
                filterAccountsPanel.updateAll(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorFetchingIgtimiDevices(caught.getMessage()));
            }
        });
    }

    private static class DataAccessWindowData {
        private final String deviceSerialNumber;
        private final Date from;
        private final Date to;

        protected DataAccessWindowData(String deviceSerialNumber, Date from, Date to) {
            super();
            this.deviceSerialNumber = deviceSerialNumber;
            this.from = from;
            this.to = to;
        }

        protected String getDeviceSerialNumber() {
            return deviceSerialNumber;
        }

        protected Date getFrom() {
            return from;
        }

        protected Date getTo() {
            return to;
        }
    }

    private class AddDataAccessWindowDialog extends DataEntryDialogWithDateTimeBox<DataAccessWindowData> {
        private TextBox deviceSerialNumber;
        private DateAndTimeInput from;
        private DateAndTimeInput to;

        public AddDataAccessWindowDialog(final Runnable refresher, final SailingServiceWriteAsync sailingServiceWrite,
                final StringMessages stringMessages, final ErrorReporter errorReporter) {
            super(stringMessages.addIgtimiDataAccessWindow(), stringMessages.addIgtimiDataAccessWindow(), stringMessages.ok(),
                    stringMessages.cancel(), new Validator<DataAccessWindowData>() {
                        @Override
                        public String getErrorMessage(DataAccessWindowData valueToValidate) {
                            final String errorMessage;
                            if (Util.hasLength(valueToValidate.getDeviceSerialNumber())) {
                                errorMessage = stringMessages.deviceSerialNumberMustNotBeEmpty();
                            } else {
                                errorMessage = null;
                            }
                            return errorMessage;
                        }
                    }, /* animationEnabled */ true, new DialogCallback<DataAccessWindowData>() {
                        @Override
                        public void ok(final DataAccessWindowData editedObject) {
                            sailingServiceWrite.addIgtimiDataAccessWindow(editedObject.getDeviceSerialNumber(),
                                    editedObject.getFrom(), editedObject.getTo(), new AsyncCallback<IgtimiDataAccessWindowWithSecurityDTO>() {
                                        @Override
                                        public void onFailure(Throwable caught) {
                                            errorReporter.reportError(stringMessages.errorCreatingDataAccessWindow(
                                                    editedObject.getDeviceSerialNumber(), caught.getMessage()));
                                        }

                                        @Override
                                        public void onSuccess(IgtimiDataAccessWindowWithSecurityDTO result) {
                                            if (result != null) {
                                                Notification
                                                        .notify(stringMessages.successfullyCreatedIgtimiDataAccessWindow(
                                                                editedObject.getDeviceSerialNumber()), NotificationType.SUCCESS);
                                                refresher.run();
                                            } else {
                                                Notification.notify(stringMessages.couldNotAuthorizedAccessToIgtimiUser(
                                                        editedObject.getDeviceSerialNumber()), NotificationType.ERROR);
                                            }
                                        }
                                    });
                        }

                        @Override
                        public void cancel() {
                        }

                    });
            ensureDebugId("AddIgtimiDeviceDialog");
        }

        @Override
        protected Widget getAdditionalWidget() {
            Grid grid = new Grid(3, 2);
            grid.setWidget(0, 0, new Label(stringMessages.serialNumber()));
            deviceSerialNumber = createTextBox(""); // TODO bug6059: use an Oracle based on all existing devices
            deviceSerialNumber.ensureDebugId("igtimiDeviceSerialNumber");
            grid.setWidget(0, 1, deviceSerialNumber);
            grid.setWidget(1, 0, new Label(stringMessages.from()));
            from = createDateTimeBox(/* initial value */ null, Accuracy.SECONDS);
            from.ensureDebugId("igtimiDataAccessWindowFrom");
            grid.setWidget(1, 1, from);
            grid.setWidget(2, 0, new Label(stringMessages.to()));
            to = createDateTimeBox(/* initial value */ null, Accuracy.SECONDS);
            to.ensureDebugId("igtimiDataAccessWindowTo");
            grid.setWidget(2, 1, to);
            return grid;
        }

        @Override
        protected FocusWidget getInitialFocusWidget() {
            return deviceSerialNumber;
        }

        @Override
        protected DataAccessWindowData getResult() {
            return new DataAccessWindowData(deviceSerialNumber.getText(), from.getValue(), to.getValue());
        }
    }

    private void addDataAccessWindow() {
        new AddDataAccessWindowDialog(this::refresh, sailingServiceWrite, stringMessages, errorReporter).show();
    }

    private void removeDevice(final IgtimiDeviceWithSecurityDTO device,
            final ListDataProvider<IgtimiDeviceWithSecurityDTO> removeFrom) {
        sailingServiceWrite.removeIgtimiDevice(device.getSerialNumber(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorTryingToRemoveIgtimiDevice(device.getSerialNumber(), caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
                Notification.notify(stringMessages.successfullyRemoveIgtimiDevice(device.getSerialNumber()),
                        NotificationType.INFO);
                removeFrom.getList().remove(device);
            }
        });
    }

    @Override
    public AbstractFilterablePanel<IgtimiDeviceWithSecurityDTO> getFilterablePanel() {
        return filterAccountsPanel;
    }
}
