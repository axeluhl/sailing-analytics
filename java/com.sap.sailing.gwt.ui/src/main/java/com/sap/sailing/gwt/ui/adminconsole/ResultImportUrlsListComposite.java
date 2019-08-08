package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TextfieldEntryDialog;
import com.sap.sailing.gwt.ui.shared.UrlDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;

/**
 * A composite for showing and managing a list of all result import URLs.
 * 
 * @author Tim Hessenmüller (D062243)
 */
public class ResultImportUrlsListComposite extends Composite {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final ListBox urlProviderListBox;

    private final ResultImportUrlsTableWrapper<RefreshableMultiSelectionModel<UrlDTO>> table;

    public ResultImportUrlsListComposite(SailingServiceAsync sailingServiceAsync, UserService userService,
            ErrorReporter errorReporter, StringMessages stringMessages) {
        this.sailingService = sailingServiceAsync;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        final CaptionPanel captionPanel = new CaptionPanel(stringMessages.resultImportUrls());
        final VerticalPanel panel = new VerticalPanel();
        // panel.setSpacing(10); //TODO css
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService,
                SecuredDomainType.RESULT_IMPORT_URL);

        final Button add = buttonPanel.addCreateAction(stringMessages.add(), this::addUrl);
        add.setEnabled(false);

        final Button remove = buttonPanel.addRemoveAction(stringMessages.remove(), new Command() {
            @Override
            public void execute() {
                removeUrls(table.getSelectionModel().getSelectedSet()); // TODO Ask user to confirm
            }
        });
        remove.setEnabled(false);

        final Button refresh = buttonPanel.addUnsecuredAction(stringMessages.refresh(), this::updateTable);
        refresh.setEnabled(false);

        Grid urlSample = new Grid(1, 2);
        urlSample.setWidget(0, 0, new Label(stringMessages.sampleURL("")));
        Label urlSampleLabel = new Label();
        urlSample.setWidget(0, 1, urlSampleLabel);

        urlProviderListBox = new ListBox();
        urlProviderListBox.setMultipleSelect(false);
        urlProviderListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int index = urlProviderListBox.getSelectedIndex();
                boolean buttonsEnabled = index > 0;
                add.setEnabled(buttonsEnabled);
                refresh.setEnabled(buttonsEnabled);
                table.getSelectionModel().clear();
                table.update(getSelectedProviderName());
                String provider = urlProviderListBox.getSelectedValue();
                urlSampleLabel.setText(provider == null || provider.equals("null") ? "" : provider);
            }
        });
        sailingService.getUrlResultProviderNamesAndOptionalSampleURL(new AsyncCallback<List<Pair<String, String>>>() {
            @Override
            public void onSuccess(List<Pair<String, String>> urlProviderNamesAndOptionalSampleURL) {
                urlProviderListBox.clear();
                urlProviderListBox.addItem(stringMessages.pleaseSelectAURLProvider());
                for (Pair<String, String> urlProviderNameAndSampleURL : urlProviderNamesAndOptionalSampleURL) {
                    urlProviderListBox.addItem(urlProviderNameAndSampleURL.getA(), urlProviderNameAndSampleURL.getB());
                }
            }

            @Override
            public void onFailure(Throwable caught) {
            }
        });

        table = new ResultImportUrlsTableWrapper<>(sailingServiceAsync, userService, stringMessages, errorReporter);
        table.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                remove.setEnabled(!table.getSelectionModel().getSelectedSet().isEmpty());
            }
        });

        Grid providerGrid = new Grid(1, 2);
        providerGrid.setWidget(0, 0, new Label(stringMessages.urlProviders()));
        providerGrid.setWidget(0, 1, urlProviderListBox);

        panel.add(providerGrid);
        panel.add(buttonPanel);
        panel.add(urlSample);
        panel.add(table);
        captionPanel.setContentWidget(panel);
        initWidget(captionPanel);
    }

    private void addUrl() {
        TextfieldEntryDialog dialog = new TextfieldEntryDialog(stringMessages.addResultImportUrl(),
                stringMessages.addResultImportUrl(), stringMessages.add(), stringMessages.cancel(), "http://",
                new DataEntryDialog.Validator<String>() {
                    @Override
                    public String getErrorMessage(String valueToValidate) {
                        if (valueToValidate == null || valueToValidate.length() == 0) {
                            return stringMessages.pleaseEnterNonEmptyUrl();
                        } // TODO Validate
                        return null;
                    }
                }, new DataEntryDialog.DialogCallback<String>() {
                    @Override
                    public void ok(String url) {
                        String providerName = getSelectedProviderName();
                        UrlDTO urlDTO = new UrlDTO(url); // TODO Security data

                        sailingService.addResultImportUrl(providerName, urlDTO, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter
                                        .reportError(stringMessages.errorAddingResultImportUrl(caught.getMessage()));
                            }

                            @Override
                            public void onSuccess(Void result) {
                                Notification.notify(stringMessages.successfullyUpdatedResultImportUrls(),
                                        NotificationType.INFO);
                                updateTable();
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                        // Cancelled by user do nothing.
                    }
                });
        dialog.getEntryField().setVisibleLength(120);
        dialog.show();
    }

    private void removeUrls(Set<UrlDTO> set) {
        if (set != null && set.size() > 0) {
            String providerName = getSelectedProviderName();
            sailingService.removeResultImportURLs(providerName, set, new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Notification.notify(stringMessages.successfullyUpdatedResultImportUrls(), NotificationType.INFO);
                    updateTable();
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(stringMessages.errorRemovingResultImportUrls(caught.getMessage()));
                }
            });
        }
    }

    private void updateTable() {
        table.update(getSelectedProviderName());
    }

    private String getSelectedProviderName() {
        String result = null;
        int selectedIndex = urlProviderListBox.getSelectedIndex();
        if (selectedIndex > 0) {
            result = urlProviderListBox.getItemText(selectedIndex);
        }
        return result;
    }
}
