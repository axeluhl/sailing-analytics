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
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TextfieldEntryDialog;

public class ResultImportUrlsManagementPanel extends FlowPanel {
    private final ListBox urlListBox;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private String selectedProviderName = FREG_PROVIDER;
    
    private final static String FREG_PROVIDER = "FREG HTML Score Importer"; 
    private final static String WINREGATTA_PROVIDER = "'WinRegatta Plus' XLS Score Importer"; 
    
    public ResultImportUrlsManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
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
        HorizontalPanel providerSelectionPanel = new HorizontalPanel();
        vp.add(providerSelectionPanel);
        
        RadioButton fregProviderRB = new RadioButton("providerSelection", FREG_PROVIDER);
        fregProviderRB.setValue(true);
        fregProviderRB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectedProviderName = FREG_PROVIDER;
				refreshUrlList();
			}
		});
        providerSelectionPanel.add(fregProviderRB);
        
        RadioButton winRegattaProviderRB = new RadioButton("providerSelection", WINREGATTA_PROVIDER);
        winRegattaProviderRB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectedProviderName = WINREGATTA_PROVIDER;
				refreshUrlList();
			}
		});
		providerSelectionPanel.add(winRegattaProviderRB);

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
    }

    private void removeSelectedUrls() {
        Set<String> toRemove = new HashSet<String>();
        for (int i=urlListBox.getItemCount()-1; i>=0; i--) {
            if (urlListBox.isItemSelected(i)) {
                toRemove.add(urlListBox.getItemText(i));
                urlListBox.removeItem(i);
            }
        }
        sailingService.removeResultImportURLs(selectedProviderName, toRemove, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorRemovingResultImportUrls(caught.getMessage()));
                refreshUrlList();
            }

            @Override
            public void onSuccess(Void result) {
                Window.setStatus(stringMessages.successfullyUpdatedResultImportUrls());
            }
        });
    }

    private void addUrl() {
        final DataEntryDialog<String> dialog = new TextfieldEntryDialog(stringMessages.addResultImportUrl(),
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
        }, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                // user cancelled; just don't add
            }

            @Override
            public void onSuccess(String url) {
                urlListBox.addItem(url);
                sailingService.addResultImportUrl(selectedProviderName, url, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorAddingResultImportUrl(caught.getMessage()));
                        refreshUrlList(); // take back our pre-mature addition
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Window.setStatus(stringMessages.successfullyUpdatedResultImportUrls());
                    }
                });
            }
        });
        dialog.show();
    }

}
