package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.base.ProgressBarBase.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TextfieldEntryDialog;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class StructureImportUrlsManagementPanel extends FlowPanel {
    private final ListBox urlProviderSelectionListBox;
    private final ListBox urlListBox;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final RegattaRefresher regattaRefresher;
    private final EventManagementPanel eventManagementPanel;
    
    EventDTO newEvent = null;

    private final Button addButton;
    private final Button removeButton;
    private final Button refreshButton;
    private StructureImportProgressBar progressBar;
    private Label overallName;
    private int parsed;

    public StructureImportUrlsManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, RegattaRefresher regattaRefresher, EventManagementPanel eventManagementPanel) {
        this.eventManagementPanel = eventManagementPanel;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.regattaRefresher = regattaRefresher;
        urlListBox = new ListBox(/* multiple select */true);
        urlProviderSelectionListBox = new ListBox(false);
        urlProviderSelectionListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = urlProviderSelectionListBox.getSelectedIndex();
                boolean buttonsEnabled = selectedIndex > 0;
                addButton.setEnabled(buttonsEnabled);
                removeButton.setEnabled(buttonsEnabled);
                refreshButton.setEnabled(buttonsEnabled);
                refreshUrlList();
            }
        });

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
        HorizontalPanel buttonPanel = new HorizontalPanel();
        vp.add(buttonPanel);
        buttonPanel.add(addButton);
        add(vp);
        refreshUrlList();

       
    }

    private String getSelectedProviderName() {
        String result = null;
        int selectedIndex = urlProviderSelectionListBox.getSelectedIndex();
        if (selectedIndex > 0) {
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
        for (int i = urlListBox.getItemCount() - 1; i >= 0; i--) {
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
                Window.setStatus(stringMessages.successfullyUpdatedResultImportUrls());
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
                        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to get events");// Ändern
                            }

                            @Override
                            public void onSuccess(List<EventDTO> events) {
                                List<EventDTO> existingEvents = events;
                                EventCreateDialog dialog = new EventCreateDialog(Collections
                                        .unmodifiableCollection(existingEvents), stringMessages,
                                        new DialogCallback<EventDTO>() {
                                            @Override
                                            public void cancel() {
                                            }

                                            @Override
                                            public void ok(final EventDTO newEvent) {
                                            	createEvent(newEvent, url);
                                            }
                                        });
                                dialog.show();
                            }
                        });

                    }
                });
        
        dialog.getEntryField().setVisibleLength(100);
        dialog.show();
    }
    private void createEvent(final EventDTO event, final String url){
    	List<String> courseAreaNames = new ArrayList<String>();
        for (CourseAreaDTO courseAreaDTO : event.venue.getCourseAreas()) {
            courseAreaNames.add(courseAreaDTO.getName());
        }
        sailingService.createEvent(event.getName(), event.startDate, event.endDate, event.venue.getName(),
                event.isPublic, courseAreaNames, event.getImageURLs(), event.getVideoURLs(),
                event.getSponsorImageURLs(), event.getLogoImageURL(), event.getOfficialWebsiteURL(),
                new AsyncCallback<EventDTO>() {

                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter
                                .reportError("Error trying to create new event "
                                        + event.getName()
                                        + ": "
                                        + t.getMessage());
                    }

                    @Override
                    public void onSuccess(EventDTO event) {
                    	newEvent = event;
                    	setDefaultRegatta(url);
                    }
                });
    }
    
    private void createRegattas(String url, RegattaDTO defaultRegatta){
        eventManagementPanel.fillEvents();
        sailingService.addEventImportUrl(url, newEvent, defaultRegatta,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages
                                .errorAddingResultImportUrl(caught
                                        .getMessage()));
                    }

                    @Override
                    public void onSuccess(Void result) {
                    	setProgressBar(43);
                    }
                });
    }
    
    private void createBarAndLabel(int amountOfRegattas){
    	overallName = new Label(stringMessages.overallProgress() + ":");
        add(overallName);
    	progressBar = new StructureImportProgressBar(
    				amountOfRegattas+1, Style.ANIMATED);
    	add(progressBar);
    }
    private void setProgressBar(final int amountOfRegattas){
    	createBarAndLabel(amountOfRegattas);
    	
    	final Timer timer = new Timer() {
            @Override
            public void run() {
                sailingService.getStructureImportOperationProgress(new AsyncCallback<Integer>(){
                	 @Override
                     public void onFailure(Throwable caught) {
                         errorReporter.reportError(stringMessages
                                 .errorAddingResultImportUrl(caught
                                         .getMessage()));
                     }
                	 @Override
                     public void onSuccess(Integer result) {
                		 parsed = result;
                		 progressBar.setPercent(result);
                	 }
                
                });
                if(parsed >= amountOfRegattas){
                    regattaRefresher.fillRegattas();
                    progressBar.removeFromParent();
                    overallName.removeFromParent();
                	this.cancel();
                }
            }
        };
        timer.scheduleRepeating(2000);
    }
    
    private void setDefaultRegatta(final String url){
    	Collection<RegattaDTO> existingRegattas = Collections.emptyList();
    	List<EventDTO> existingEvents = new ArrayList<EventDTO>();
    	existingEvents.add(newEvent);
    	
    	DefaultRegattaCreateDialog dialog = new DefaultRegattaCreateDialog(existingRegattas, existingEvents, stringMessages,
                new DialogCallback<RegattaDTO>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(RegattaDTO newRegatta) {
            	createRegattas(url, newRegatta);
            }
        });
    	dialog.ensureDebugId("DefaultRegattaCreateDialog");
        dialog.show();
    	                 	
    }
}
