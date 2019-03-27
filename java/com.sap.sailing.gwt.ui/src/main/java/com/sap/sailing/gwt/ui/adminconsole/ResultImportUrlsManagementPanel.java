package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TextfieldEntryDialog;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;

public class ResultImportUrlsManagementPanel extends FlowPanel {
    private final ListBox urlProviderSelectionListBox;
    private final ListBox urlListBox;
    
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final Button addButton;
    private final Button removeButton;
    private final Button refreshButton;

    public ResultImportUrlsManagementPanel(SailingServiceAsync sailingService, UserService userService, ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        final Label sampleURLLabel = new Label();
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        urlListBox = new ListBox();
        urlListBox.setMultipleSelect(true);
        urlProviderSelectionListBox = new ListBox();
        urlListBox.setMultipleSelect(false);
        urlProviderSelectionListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = urlProviderSelectionListBox.getSelectedIndex();
                boolean buttonsEnabled = selectedIndex > 0;  
                addButton.setEnabled(buttonsEnabled);
                removeButton.setEnabled(buttonsEnabled);
                refreshButton.setEnabled(buttonsEnabled);
                final String value = urlProviderSelectionListBox.getValue(selectedIndex);
                // believe it or not: adding an item with (someString, null) results in an item
                // whose getValue() returns "null" and not null...
                sampleURLLabel.setText(value == null || value.equals("null") ? "" : stringMessages.sampleURL(value));
                refreshUrlList();
            }
        });
        
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, SecuredDomainType.RESULT_IMPORT_URL);
        addButton = buttonPanel.addCreateAction(stringMessages.add(), this::addUrl);
        removeButton = buttonPanel.addRemoveAction(stringMessages.remove(), this::removeSelectedUrls);
        refreshButton = buttonPanel.addUnsecuredAction(stringMessages.refresh(), this::refreshUrlList);
        
        VerticalPanel vp = new VerticalPanel();
        HorizontalPanel providerSelectionPanel = new HorizontalPanel();
        providerSelectionPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        vp.add(providerSelectionPanel);
        providerSelectionPanel.add(new Label(stringMessages.urlProviders()));
        providerSelectionPanel.add(urlProviderSelectionListBox);
        sailingService.getUrlResultProviderNamesAndOptionalSampleURL(new AsyncCallback<List<Pair<String, String>>>() {
            @Override
            public void onSuccess(List<Pair<String, String>> urlProviderNamesAndOptionalSampleURL) {
                urlProviderSelectionListBox.clear();
                urlProviderSelectionListBox.addItem(stringMessages.pleaseSelectAURLProvider());
                for (Pair<String, String> urlProviderNameAndSampleURL : urlProviderNamesAndOptionalSampleURL) {
                    urlProviderSelectionListBox.addItem(urlProviderNameAndSampleURL.getA(), urlProviderNameAndSampleURL.getB());
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        });
        vp.add(urlListBox);
        vp.add(sampleURLLabel);
        
        vp.add(buttonPanel);
        add(vp);
        refreshUrlList();
    }
   
    private String getSelectedProviderName() {
        String result = null;
        int selectedIndex = urlProviderSelectionListBox.getSelectedIndex();
        if(selectedIndex > 0) {
            result = urlProviderSelectionListBox.getItemText(selectedIndex); 
        }
        return result;
    }
    
    private void refreshUrlList() {
        String selectedProviderName = getSelectedProviderName();
        if (selectedProviderName != null) {
            sailingService.getResultImportUrls(selectedProviderName, new AsyncCallback<List<String>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(stringMessages.errorRefreshingResultImportUrlList(caught.getMessage()));
                }

                @Override
                public void onSuccess(List<String> result) {
                    urlListBox.clear();
                    for (String s : result) {
                        urlListBox.addItem(s);
                    }
                }
            });
        } else {
            urlListBox.clear();
        }
    }

    private void removeSelectedUrls() {
        Set<String> toRemove = new HashSet<String>();
        for (int i=urlListBox.getItemCount()-1; i>=0; i--) {
            if (urlListBox.isItemSelected(i)) {
                toRemove.add(urlListBox.getItemText(i));
                urlListBox.removeItem(i);
            }
        }
        String selectedProviderName = getSelectedProviderName();
        sailingService.removeResultImportURLs(selectedProviderName, toRemove, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorRemovingResultImportUrls(caught.getMessage()));
                refreshUrlList();
            }

            @Override
            public void onSuccess(Void result) {
                Notification.notify(stringMessages.successfullyUpdatedResultImportUrls(), NotificationType.INFO);
            }
        });
    }

    private void addUrl() {
        final TextfieldEntryDialog dialog = new TextfieldEntryDialog(stringMessages.addResultImportUrl(),
                stringMessages.addResultImportUrl(), stringMessages.add(), stringMessages.cancel(), "http://",
                new DataEntryDialog.Validator<String>() {
                    @Override
                    public String getErrorMessage(String valueToValidate) {
                        String result = null;
                        if (valueToValidate == null || valueToValidate.length() == 0) {
                            result = stringMessages.pleaseEnterNonEmptyUrl();
                        }
                        return result;
                    }
        }, new DialogCallback<String>() {
            @Override
            public void cancel() {
                // user cancelled; just don't add
            }

            @Override
            public void ok(final String url) {
                String selectedProviderName = getSelectedProviderName();
                sailingService.addResultImportUrl(selectedProviderName, url, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorAddingResultImportUrl(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        urlListBox.addItem(url);
                        Notification.notify(stringMessages.successfullyUpdatedResultImportUrls(), NotificationType.INFO);
                    }
                });
            }
        });
        dialog.getEntryField().setVisibleLength(100);
        dialog.show();
    }
}
