package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.ImagesBarCell;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public class IgtimiAccountsPanel extends FlowPanel {

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final CellTable<String> allAccounts;
    private final LabeledAbstractFilterablePanel<String> filterAccountsPanel;

    public static class AccountImagesBarCell extends ImagesBarCell {
        public static final String ACTION_REMOVE = "ACTION_REMOVE";
        private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
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
                    new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(), makeImagePrototype(resources.removeIcon())));
        }
    }
    
    public IgtimiAccountsPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        allAccounts = new CellTable<String>(/* pageSize */10000, tableRes);
        final SingleSelectionModel<String> accountsSelectionModel = new SingleSelectionModel<String>();
        allAccounts.setSelectionModel(accountsSelectionModel);
        final ListDataProvider<String> filteredAccounts = new ListDataProvider<String>();
        ListHandler<String> accountColumnListHandler = new ListHandler<String>(filteredAccounts.getList());
        filteredAccounts.addDataDisplay(allAccounts);
        final List<String> emptyList = Collections.emptyList();
        filterAccountsPanel = new LabeledAbstractFilterablePanel<String>(new Label(stringMessages.igtimiAccounts()), emptyList, allAccounts, filteredAccounts) {
            @Override
            public Iterable<String> getSearchableStrings(String t) {
                Set<String> strings = Collections.singleton(t);
                return strings;
            }
        };
        final Panel controlsPanel = new HorizontalPanel();
        final Button removeAccountButton = new Button(stringMessages.remove());
        removeAccountButton.setEnabled(false);
        removeAccountButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (accountsSelectionModel.getSelectedObject() != null) {
                    if (Window.confirm("Do you really want to remove the leaderboards?")) {
                        removeAccount(accountsSelectionModel.getSelectedObject(), filteredAccounts);
                    }
                }
            }
        });
        accountsSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                removeAccountButton.setEnabled(accountsSelectionModel.getSelectedObject() != null);
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
        updateAllAccounts(sailingService, filterAccountsPanel, stringMessages, errorReporter);
        Button addAccountButton = new Button(stringMessages.addIgtimiAccount());
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
                updateAllAccounts(sailingService, filterAccountsPanel, stringMessages, errorReporter);
            }
        });
        add(refreshButton);
        this.sailingService.getIgtimiAuthorizationUrl(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(IgtimiAccountsPanel.this.stringMessages.errorGettingIgtimiAuthorizationUrl(caught.getMessage()),
                        /* silentMode */ true);
            }

            @Override
            public void onSuccess(String result) {
                Anchor addIgtimiUserLink = new Anchor(stringMessages.addIgtimiUser(), result); 
                controlsPanel.add(addIgtimiUserLink);
            }
        });
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
                                        Window.alert(stringMessages.successfullyAuthorizedAccessToIgtimiUser(editedObject.geteMail()));
                                        updateAllAccounts(sailingService, filterAccountsPanel, stringMessages, errorReporter);
                                    } else {
                                        Window.alert(stringMessages.couldNotAuthorizedAccessToIgtimiUser(editedObject.geteMail()));
                                    }
                                }
                            });
                        }

                        @Override
                        public void cancel() {
                        }

                    });
        }

        @Override
        protected Widget getAdditionalWidget() {
            Grid grid = new Grid(2, 2);
            grid.setWidget(0, 0, new Label(stringMessages.emailAddress()));
            eMail = createTextBox("");
            grid.setWidget(0, 1, eMail);
            grid.setWidget(1, 0, new Label(stringMessages.password()));
            password = createPasswordTextBox("");
            grid.setWidget(1, 1, password);
            return grid;
        }
        
        @Override
        public void show() {
            super.show();
            eMail.setFocus(true);
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
                Window.alert(stringMessages.successfullyRemoveIgtimiAccount(eMailOfAccountToRemove));
                removeFrom.getList().remove(eMailOfAccountToRemove);
            }
        });
    }
}
