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
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TextfieldEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class SailingServerInstancesManagementPanel extends FlowPanel {
    private final ListBox urlListBox;
    
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final Button addButton;
    private final Button removeButton;
    private final Button refreshButton;

    public SailingServerInstancesManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        urlListBox = new ListBox(/* multiple select */ true);

        addButton = new Button(stringMessages.add());
        removeButton = new Button(stringMessages.remove());
        refreshButton = new Button(stringMessages.refresh());
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
        providerSelectionPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        vp.add(providerSelectionPanel);
        
        providerSelectionPanel.add(new Label("SailingServer Instance URL's"));
                
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
        sailingService.getSailingServerInstanceUrls(new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorRefreshingSailingServerInstanceUrlList(caught.getMessage()));
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
        
        sailingService.removeSailingServerInstanceURLs(toRemove, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorRemovingSailingServerInstanceUrls(caught.getMessage()));
                refreshUrlList();
            }

            @Override
            public void onSuccess(Void result) {
                Window.setStatus(stringMessages.successfullyUpdatedSailingServerInstanceUrls());
            }
        });
    }

    private void addUrl() {
        final TextfieldEntryDialog dialog = new TextfieldEntryDialog(stringMessages.addSailingServerInstanceUrl(),
                stringMessages.addSailingServerInstanceUrl(), stringMessages.add(), stringMessages.cancel(), "http://",
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
                sailingService.addSailingServerInstanceUrl(url, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorAddingSailingServerInstanceUrl(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        urlListBox.addItem(url);
                        Window.setStatus(stringMessages.successfullyUpdatedSailingServerInstanceUrls());
                    }
                });
            }
        });
        dialog.getEntryField().setVisibleLength(100);
        dialog.show();
    }
}
