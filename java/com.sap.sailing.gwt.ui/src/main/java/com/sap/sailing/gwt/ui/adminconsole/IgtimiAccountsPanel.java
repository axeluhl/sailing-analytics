package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DialogBox.CaptionImpl;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public class IgtimiAccountsPanel extends FlowPanel {

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final CellTable<String> allAccounts;
    private final LabeledAbstractFilterablePanel<String> filterAccountsPanel;
    private final RefreshableSingleSelectionModel<String> refreshableAccountsSelectionModel;

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
            return Arrays.asList(
                    new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon())));
        }
    }
    
    public IgtimiAccountsPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        allAccounts = new BaseCelltable<>(/* pageSize */10000, tableRes);
        final ListDataProvider<String> filteredAccounts = new ListDataProvider<>();
        ListHandler<String> accountColumnListHandler = new ListHandler<>(filteredAccounts.getList());
        filteredAccounts.addDataDisplay(allAccounts);
        final List<String> emptyList = Collections.emptyList();
        filterAccountsPanel = new LabeledAbstractFilterablePanel<String>(new Label(stringMessages.igtimiAccounts()),
                emptyList, filteredAccounts) {
            @Override
            public Iterable<String> getSearchableStrings(String t) {
                Set<String> strings = Collections.singleton(t);
                return strings;
            }

            @Override
            public AbstractCellTable<String> getCellTable() {
                return allAccounts;
            }
        };
        refreshableAccountsSelectionModel = new RefreshableSingleSelectionModel<>(null,
                filterAccountsPanel.getAllListDataProvider());
        allAccounts.setSelectionModel(refreshableAccountsSelectionModel);
        final Panel controlsPanel = new HorizontalPanel();
        final Button removeAccountButton = new Button(stringMessages.remove());
        removeAccountButton.setEnabled(false);
        removeAccountButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (refreshableAccountsSelectionModel.getSelectedObject() != null) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveTheSelectedIgtimiAccounts())) {
                        removeAccount(refreshableAccountsSelectionModel.getSelectedObject(), filteredAccounts);
                    }
                }
            }
        });
        refreshableAccountsSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                removeAccountButton.setEnabled(refreshableAccountsSelectionModel.getSelectedObject() != null);
            }
        });
        controlsPanel.add(filterAccountsPanel);
        controlsPanel.add(removeAccountButton);
        add(controlsPanel);
        add(allAccounts);
        Column<String, String> accountEmailColumn = new TextColumn<String>() {
            @Override
            public String getValue(String object) {
                return object;
            }
        };
        accountEmailColumn.setSortable(true);
        accountColumnListHandler.setComparator(accountEmailColumn, new NaturalComparator(/* caseSensitive */ false));
        allAccounts.addColumn(accountEmailColumn, stringMessages.emailAddress());
        ImagesBarColumn<String, AccountImagesBarCell> accountActionColumn = new ImagesBarColumn<String, AccountImagesBarCell>(
                new AccountImagesBarCell(stringMessages));
        accountActionColumn.setFieldUpdater(new FieldUpdater<String, String>() {
            @Override
            public void update(int index, String accountEmail, String value) {
                if (AccountImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveIgtimiAccount(accountEmail))) {
                        removeAccount(accountEmail, filteredAccounts);
                    }
                }
            }
        });
        allAccounts.addColumn(accountActionColumn, stringMessages.actions());
        allAccounts.addColumnSortHandler(accountColumnListHandler);
        updateAllAccounts(sailingService, filterAccountsPanel, stringMessages, errorReporter);
        Button addAccountButton = new Button(stringMessages.addIgtimiAccount());
        addAccountButton.ensureDebugId("addIgtimiAccount");
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
                refresh(sailingService, errorReporter, stringMessages);
            }
        });
        add(refreshButton);
        final String protocol = Window.Location.getProtocol().endsWith(":") ?
                    Window.Location.getProtocol().substring(0, Window.Location.getProtocol().length()-1) :
                    Window.Location.getProtocol();
        final String hostname = Window.Location.getHostName();
        final String port = Window.Location.getPort();
        this.sailingService.getIgtimiAuthorizationUrl(protocol, hostname, port, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(IgtimiAccountsPanel.this.stringMessages.errorGettingIgtimiAuthorizationUrl(caught.getMessage()),
                        /* silentMode */ true);
            }

            @Override
            public void onSuccess(String result) {
                final Button addIgtimiUserButton = new Button(stringMessages.addIgtimiUser() + " (OAuth)");
                addIgtimiUserButton.addClickHandler(clickEvent -> {
                    Frame frame = new Frame(UriUtils.fromString(result).asString());
                    frame.addLoadHandler(loadEvent -> refresh(sailingService, errorReporter, stringMessages));
                    frame.setPixelSize(520, 770);
                    final CaptionImpl caption = new CaptionImpl();
                    caption.setText(stringMessages.addIgtimiUser());
                    final DialogBox dialogBox = new DialogBox(/* autoHide */ false, /* modal */ true);
                    Button closeButton = new Button(stringMessages.close());
                    closeButton.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            dialogBox.hide();
                        }
                    });
                    dialogBox.setText(stringMessages.addIgtimiUser());
                    dialogBox.setGlassEnabled(true);
                    final VerticalPanel panel = new VerticalPanel();
                    dialogBox.setWidget(panel);
                    panel.add(frame);
                    panel.add(closeButton);
                    dialogBox.addCloseHandler(event -> {
                        refresh(sailingService, errorReporter, stringMessages);
                    });
                    dialogBox.center();
                });
                controlsPanel.add(addIgtimiUserButton);
            }
        });
    }
    
    private void refresh(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        updateAllAccounts(sailingService, filterAccountsPanel, stringMessages, errorReporter);
    }

    private static class UserData {
        private final String eMail;
        private final String password;
        protected UserData(String eMail, String password) {
            super();
            this.eMail = eMail;
            this.password = password;
        }
        protected String geteMail() {
            return eMail;
        }
        protected String getPassword() {
            return password;
        }
    }

    private class AddAccountDialog extends DataEntryDialog<UserData> {
        private TextBox eMail;
        private PasswordTextBox password;
        
        public AddAccountDialog(final LabeledAbstractFilterablePanel<String> filterAccountsPanel, final SailingServiceAsync sailingService, final StringMessages stringMessages, final ErrorReporter errorReporter) {
            super(stringMessages.addIgtimiUser(), stringMessages.addIgtimiUser(), stringMessages.ok(), stringMessages.cancel(),
                    new Validator<UserData>() {
                        @Override
                        public String getErrorMessage(UserData valueToValidate) {
                            final String errorMessage;
                            if (valueToValidate.geteMail() == null || valueToValidate.geteMail().isEmpty()) {
                                errorMessage = stringMessages.eMailMustNotBeEmpty();
                            } else {
                                errorMessage = null;
                            }
                            return errorMessage;
                        }
                    }, /* animationEnabled */ true, new DialogCallback<UserData>() {
                        @Override
                        public void ok(final UserData editedObject) {
                            sailingService.authorizeAccessToIgtimiUser(editedObject.geteMail(), editedObject.getPassword(), new AsyncCallback<Boolean>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError(stringMessages.errorAuthorizingAccessToIgtimiUser(editedObject.geteMail(),
                                            caught.getMessage()));
                                }

                                @Override
                                public void onSuccess(Boolean result) {
                                    if (result) {
                                        Notification.notify(stringMessages.successfullyAuthorizedAccessToIgtimiUser(editedObject.geteMail()), NotificationType.SUCCESS);
                                        updateAllAccounts(sailingService, filterAccountsPanel, stringMessages, errorReporter);
                                    } else {
                                        Notification.notify(stringMessages.couldNotAuthorizedAccessToIgtimiUser(editedObject.geteMail()), NotificationType.ERROR);
                                    }
                                }
                            });
                        }

                        @Override
                        public void cancel() {
                        }

                    });
            ensureDebugId("AddIgtimiAccountDialog");
        }

        @Override
        protected Widget getAdditionalWidget() {
            Grid grid = new Grid(2, 2);
            grid.setWidget(0, 0, new Label(stringMessages.emailAddress()));
            eMail = createTextBox("");
            eMail.ensureDebugId("igtimiAccountEmail");
            grid.setWidget(0, 1, eMail);
            grid.setWidget(1, 0, new Label(stringMessages.password()));
            password = createPasswordTextBox("");
            password.ensureDebugId("igtimiAccountPassword");
            grid.setWidget(1, 1, password);
            return grid;
        }
        
        @Override
        protected FocusWidget getInitialFocusWidget() {
            return eMail;
        }
        
        @Override
        protected UserData getResult() {
            return new UserData(eMail.getText(), password.getText());
        }
    }

    private static void updateAllAccounts(SailingServiceAsync sailingService, final LabeledAbstractFilterablePanel<String> filterAccountsPanel,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        sailingService.getAllIgtimiAccountEmailAddresses(new AsyncCallback<Iterable<String>>() {
            @Override
            public void onSuccess(Iterable<String> result) {
                filterAccountsPanel.updateAll(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorFetchingIgtimiAccounts(caught.getMessage()));
            }
        });
    }

    private void addAccount() {
        new AddAccountDialog(filterAccountsPanel, sailingService, stringMessages, errorReporter).show();
    }
    
    private void removeAccount(final String eMailOfAccountToRemove, final ListDataProvider<String> removeFrom) {
        sailingService.removeIgtimiAccount(eMailOfAccountToRemove, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorTryingToRemoveIgtimiAccount(eMailOfAccountToRemove));
            }

            @Override
            public void onSuccess(Void result) {
                Notification.notify(stringMessages.successfullyRemoveIgtimiAccount(eMailOfAccountToRemove), NotificationType.INFO);
                removeFrom.getList().remove(eMailOfAccountToRemove);
            }
        });
    }
}
