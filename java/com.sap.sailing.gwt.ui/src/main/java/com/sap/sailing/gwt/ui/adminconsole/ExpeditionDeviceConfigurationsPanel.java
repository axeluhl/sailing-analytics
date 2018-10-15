package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
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
import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public class ExpeditionDeviceConfigurationsPanel extends FlowPanel {
    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final CellTable<ExpeditionDeviceConfiguration> allDeviceConfigurations;
    private final LabeledAbstractFilterablePanel<ExpeditionDeviceConfiguration> filterDeviceConfigurationsPanel;
    private final RefreshableSingleSelectionModel<ExpeditionDeviceConfiguration> refreshableDeviceConfigurationsSelectionModel;

    public static class DeviceConfigurationImagesBarCell extends ImagesBarCell {
        public static final String ACTION_REMOVE = "ACTION_REMOVE";
        public static final String ACTION_EDIT = "ACTION_EDIT";
        private final StringMessages stringMessages;
        
        public DeviceConfigurationImagesBarCell(StringMessages stringMessages) {
            super();
            this.stringMessages = stringMessages;
        }

        public DeviceConfigurationImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringConstants) {
            super();
            this.stringMessages = stringConstants;
        }

        @Override
        protected Iterable<ImageSpec> getImageSpecs() {
            return Arrays.asList(
                    new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(IconResources.INSTANCE.editIcon())),
                    new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon())));
        }
    }
    
    public ExpeditionDeviceConfigurationsPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        allDeviceConfigurations = new BaseCelltable<>(/* pageSize */10000, tableRes);
        final ListDataProvider<ExpeditionDeviceConfiguration> filteredDeviceConfigurations = new ListDataProvider<>();
        ListHandler<ExpeditionDeviceConfiguration> deviceConfigurationColumnListHandler = new ListHandler<>(filteredDeviceConfigurations.getList());
        filteredDeviceConfigurations.addDataDisplay(allDeviceConfigurations);
        final List<ExpeditionDeviceConfiguration> emptyList = Collections.emptyList();
        filterDeviceConfigurationsPanel = new LabeledAbstractFilterablePanel<ExpeditionDeviceConfiguration>(new Label(stringMessages.expeditionDeviceConfigurations()),
                emptyList, filteredDeviceConfigurations) {
            @Override
            public Iterable<String> getSearchableStrings(ExpeditionDeviceConfiguration t) {
                Set<String> strings = new HashSet<>();
                strings.add(t.getName());
                strings.add(t.getDeviceUuid().toString());
                strings.add(""+t.getExpeditionBoatId());
                return strings;
            }

            @Override
            public AbstractCellTable<ExpeditionDeviceConfiguration> getCellTable() {
                return allDeviceConfigurations;
            }
        };
        refreshableDeviceConfigurationsSelectionModel = new RefreshableSingleSelectionModel<>(new EntityIdentityComparator<ExpeditionDeviceConfiguration>() {
            @Override
            public boolean representSameEntity(ExpeditionDeviceConfiguration dto1, ExpeditionDeviceConfiguration dto2) {
                return dto1.getDeviceUuid().equals(dto2.getDeviceUuid());
            }

            @Override
            public int hashCode(ExpeditionDeviceConfiguration t) {
                return t.getDeviceUuid().hashCode();
            }
        }, filterDeviceConfigurationsPanel.getAllListDataProvider());
        allDeviceConfigurations.setSelectionModel(refreshableDeviceConfigurationsSelectionModel);
        final Panel controlsPanel = new HorizontalPanel();
        controlsPanel.add(filterDeviceConfigurationsPanel);
        add(controlsPanel);
        add(allDeviceConfigurations);
        Column<ExpeditionDeviceConfiguration, String> deviceConfigurationNameColumn = new TextColumn<ExpeditionDeviceConfiguration>() {
            @Override
            public String getValue(ExpeditionDeviceConfiguration object) {
                return object.getName();
            }
        };
        deviceConfigurationNameColumn.setSortable(true);
        deviceConfigurationColumnListHandler.setComparator(deviceConfigurationNameColumn, (c1, c2)->new NaturalComparator(/* caseSensitive */ false).compare(c1.getName(), c2.getName()));
        allDeviceConfigurations.addColumn(deviceConfigurationNameColumn, stringMessages.name());
        Column<ExpeditionDeviceConfiguration, String> deviceConfigurationUuidColumn = new TextColumn<ExpeditionDeviceConfiguration>() {
            @Override
            public String getValue(ExpeditionDeviceConfiguration object) {
                return object.getDeviceUuid().toString();
            }
        };
        deviceConfigurationUuidColumn.setSortable(true);
        deviceConfigurationColumnListHandler.setComparator(deviceConfigurationUuidColumn,
                (c1, c2)->new NaturalComparator(/* caseSensitive */ false).compare(c1.getDeviceUuid().toString(), c2.getDeviceUuid().toString()));
        allDeviceConfigurations.addColumn(deviceConfigurationUuidColumn, stringMessages.id());
        Column<ExpeditionDeviceConfiguration, String> deviceConfigurationBoatIdColumn = new TextColumn<ExpeditionDeviceConfiguration>() {
            @Override
            public String getValue(ExpeditionDeviceConfiguration object) {
                return object.getExpeditionBoatId() == null ? "" : object.getExpeditionBoatId().toString();
            }
        };
        deviceConfigurationBoatIdColumn.setSortable(true);
        deviceConfigurationColumnListHandler.setComparator(deviceConfigurationBoatIdColumn,
                Comparator.comparing(ExpeditionDeviceConfiguration::getExpeditionBoatId, Comparator.nullsLast(Comparator.naturalOrder())));
        allDeviceConfigurations.addColumn(deviceConfigurationBoatIdColumn, stringMessages.expeditionBoatId());
        ImagesBarColumn<ExpeditionDeviceConfiguration, DeviceConfigurationImagesBarCell> deviceConfigurationActionColumn = new ImagesBarColumn<ExpeditionDeviceConfiguration, DeviceConfigurationImagesBarCell>(
                new DeviceConfigurationImagesBarCell(stringMessages));
        deviceConfigurationActionColumn.setFieldUpdater(new FieldUpdater<ExpeditionDeviceConfiguration, String>() {
            @Override
            public void update(int index, ExpeditionDeviceConfiguration deviceConfiguration, String value) {
                if (DeviceConfigurationImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveExpeditionDeviceConfiguration(deviceConfiguration.getName()))) {
                        removeDeviceConfiguration(deviceConfiguration, filterDeviceConfigurationsPanel);
                    }
                } else if (DeviceConfigurationImagesBarCell.ACTION_EDIT.equals(value)) {
                    new EditDeviceConfigurationDialog(filterDeviceConfigurationsPanel, sailingService, stringMessages, errorReporter, deviceConfiguration).show();
                }
            }
        });
        allDeviceConfigurations.addColumn(deviceConfigurationActionColumn, stringMessages.actions());
        allDeviceConfigurations.addColumnSortHandler(deviceConfigurationColumnListHandler);
        updateAllAccounts(sailingService, filterDeviceConfigurationsPanel, stringMessages, errorReporter);
        Button addAccountButton = new Button(stringMessages.add());
        addAccountButton.ensureDebugId("addExpeditionDeviceConfiguration");
        addAccountButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addAccount();
            }
        });
        add(addAccountButton);
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refresh();
            }
        });
        add(refreshButton);
    }
    
    public void refresh() {
        updateAllAccounts(sailingService, filterDeviceConfigurationsPanel, stringMessages, errorReporter);
    }

    private abstract class AbstractDeviceConfigurationDialog extends DataEntryDialog<ExpeditionDeviceConfiguration> {
        protected TextBox boatName;
        protected com.sap.sse.gwt.client.controls.IntegerBox boatId;
        
        public AbstractDeviceConfigurationDialog(final LabeledAbstractFilterablePanel<ExpeditionDeviceConfiguration> filterAccountsPanel,
                final SailingServiceAsync sailingService, final StringMessages stringMessages, final ErrorReporter errorReporter, String title,
                DialogCallback<ExpeditionDeviceConfiguration> callback) {
            super(title, title, stringMessages.ok(), stringMessages.cancel(),
                    new Validator<ExpeditionDeviceConfiguration>() {
                        @Override
                        public String getErrorMessage(ExpeditionDeviceConfiguration valueToValidate) {
                            final String errorMessage;
                            if (valueToValidate.getName() == null || valueToValidate.getName().isEmpty()) {
                                errorMessage = stringMessages.boatNameMustNotBeEmpty();
                            } else {
                                errorMessage = null;
                            }
                            return errorMessage;
                        }
                    }, /* animationEnabled */ true, callback);
        }

        @Override
        protected Widget getAdditionalWidget() {
            Grid grid = new Grid(2, 2);
            grid.setWidget(0, 0, new Label(stringMessages.name()));
            boatName = createTextBox("");
            boatName.ensureDebugId("expeditionBoatName");
            grid.setWidget(0, 1, boatName);
            grid.setWidget(1, 0, new Label(stringMessages.expeditionBoatId()));
            boatId = createIntegerBox(/* initial value */ 0, /* visibleLength */ 2);
            boatId.ensureDebugId("expeditionBoatId");
            grid.setWidget(1, 1, boatId);
            return grid;
        }
        
        @Override
        protected FocusWidget getInitialFocusWidget() {
            return boatName;
        }
    }

    private class AddDeviceConfigurationDialog extends AbstractDeviceConfigurationDialog {
        public AddDeviceConfigurationDialog(final LabeledAbstractFilterablePanel<ExpeditionDeviceConfiguration> filterAccountsPanel,
                final SailingServiceAsync sailingService, final StringMessages stringMessages, final ErrorReporter errorReporter) {
            super(filterAccountsPanel, sailingService, stringMessages, errorReporter, stringMessages.addExpeditionDeviceConfiguration(),
                    new DialogCallback<ExpeditionDeviceConfiguration>() {
                @Override
                public void ok(final ExpeditionDeviceConfiguration editedObject) {
                    sailingService.addOrReplaceExpeditionDeviceConfiguration(editedObject, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(stringMessages.errorUpdatingExpeditionDeviceConfiguration(editedObject.getName(),
                                    caught.getMessage()));
                        }

                        @Override
                        public void onSuccess(Void result) {
                            filterAccountsPanel.add(editedObject);
                        }
                    });
                }

                @Override
                public void cancel() {
                }
            });
            ensureDebugId("AddExpeditionDeviceConfigurationDialog");
        }
        
        @Override
        protected ExpeditionDeviceConfiguration getResult() {
            return new ExpeditionDeviceConfiguration(boatName.getText(), UUID.randomUUID(), boatId.getValue());
        }
    }

    private class EditDeviceConfigurationDialog extends AbstractDeviceConfigurationDialog {
        private final ExpeditionDeviceConfiguration valueToEdit;
        
        public EditDeviceConfigurationDialog(final LabeledAbstractFilterablePanel<ExpeditionDeviceConfiguration> filterAccountsPanel,
                final SailingServiceAsync sailingService, final StringMessages stringMessages, final ErrorReporter errorReporter,
                final ExpeditionDeviceConfiguration valueToEdit) {
            super(filterAccountsPanel, sailingService, stringMessages, errorReporter, stringMessages.editExpeditionDeviceConfiguration(),
                    new DialogCallback<ExpeditionDeviceConfiguration>() {
                @Override
                public void ok(final ExpeditionDeviceConfiguration editedObject) {
                    sailingService.addOrReplaceExpeditionDeviceConfiguration(editedObject, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(stringMessages.errorUpdatingExpeditionDeviceConfiguration(editedObject.getName(),
                                    caught.getMessage()));
                        }

                        @Override
                        public void onSuccess(Void result) {
                            filterAccountsPanel.remove(valueToEdit);
                            filterAccountsPanel.add(editedObject);
                        }
                    });
                }

                @Override
                public void cancel() {
                }
            });
            this.valueToEdit = valueToEdit;
            ensureDebugId("EditExpeditionDeviceConfigurationDialog");
        }

        @Override
        protected Widget getAdditionalWidget() {
            final Widget result = super.getAdditionalWidget();
            boatName.setValue(valueToEdit.getName());
            boatId.setValue(valueToEdit.getExpeditionBoatId());
            return result;
        }

        @Override
        protected ExpeditionDeviceConfiguration getResult() {
            return new ExpeditionDeviceConfiguration(this.boatName.getValue(), valueToEdit.getDeviceUuid(), this.boatId.getValue());
        }
    }

    private static void updateAllAccounts(SailingServiceAsync sailingService, final LabeledAbstractFilterablePanel<ExpeditionDeviceConfiguration> filterAccountsPanel,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        sailingService.getExpeditionDeviceConfigurations(new AsyncCallback<List<ExpeditionDeviceConfiguration>>() {
            @Override
            public void onSuccess(List<ExpeditionDeviceConfiguration> result) {
                filterAccountsPanel.updateAll(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorFetchingExpeditionDeviceConfigurations(caught.getMessage()));
            }
        });
    }

    private void addAccount() {
        new AddDeviceConfigurationDialog(filterDeviceConfigurationsPanel, sailingService, stringMessages, errorReporter).show();
    }
    
    private void removeDeviceConfiguration(final ExpeditionDeviceConfiguration expeditionDeviceConfiguration,
            final LabeledAbstractFilterablePanel<ExpeditionDeviceConfiguration> filterDeviceConfigurationsPanel) {
        sailingService.removeExpeditionDeviceConfiguration(expeditionDeviceConfiguration, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorTryingToRemoveExpeditionDeviceConfiguration(expeditionDeviceConfiguration.getName(), caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
                filterDeviceConfigurationsPanel.remove(expeditionDeviceConfiguration);
            }
        });
    }
}
