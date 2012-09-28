package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TextfieldEntryDialog;

public class FregResultImportUrlPanel extends FlowPanel {
    private final ListBox urlListBox;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    
    public FregResultImportUrlPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        urlListBox = new ListBox(/* multiple select */ true);
        Button addButton = new Button(stringMessages.add());
        Button removeButton = new Button(stringMessages.remove());
        Button refreshButton = new Button(stringMessages.refresh());
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addUrl();
            }
        });
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeSelectedUrls();
            }
        });
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshUrlList();
            }
        });
        VerticalPanel vp = new VerticalPanel();
        vp.add(urlListBox);
        HorizontalPanel buttonPanel = new HorizontalPanel();
        vp.add(buttonPanel);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(refreshButton);
        add(vp);
        refreshUrlList();
    }

    private void refreshUrlList() {
        sailingService.getFregResultUrls(new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorRefreshingFregUrlList(caught.getMessage()));
            }

            @Override
            public void onSuccess(List<String> result) {
                urlListBox.clear();
                for (String s : result) {
                    urlListBox.addItem(s);
                }
            }
        });
    }

    private void removeSelectedUrls() {
        Set<String> toRemove = new HashSet<String>();
        for (int i=urlListBox.getItemCount()-1; i>=0; i--) {
            if (urlListBox.isItemSelected(i)) {
                toRemove.add(urlListBox.getItemText(i));
                urlListBox.removeItem(i);
            }
        }
        sailingService.removeFregURLs(toRemove, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorRemovingFregUrls(caught.getMessage()));
                refreshUrlList();
            }

            @Override
            public void onSuccess(Void result) {
                Window.setStatus(stringMessages.successfullyUpdatedFregUrls());
            }
        });
    }

    private void addUrl() {
        final DataEntryDialog<String> dialog = new TextfieldEntryDialog(stringMessages.addFragUrl(),
                stringMessages.addFragUrl(), stringMessages.add(), stringMessages.cancel(), "http://",
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
            public void ok(String result) {
                urlListBox.addItem(result);
                sailingService.addFragUrl(result, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorAddingFragUrl(caught.getMessage()));
                        refreshUrlList(); // take back our pre-mature addition
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Window.setStatus(stringMessages.successfullyUpdatedFregUrls());
                    }
                });
            }
        });
        dialog.show();
    }

}
