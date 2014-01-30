***
# UI-Tests Tutorial

In this tutorial, we want to give a practical introduction in how to write UI-Tests with our framework and Selenium. You will learn to prepare your UI for testing as well as to write page objects and tests. For this reason, we write a small test which interacts with the administration console and the goal of the test is to verify the correct creation of a new event.

## Preparing the UI

When you start to write UI tests, the first thing you have to do is, to ensure that the UI is testable. This means, that you have to be able to easily find all widgets in the final HTML-Document the user interacts with. The simplest approach here is to use debug identifiers which are provided by GWT and to assign an identifier to all important widgets, like buttons and text fields. Our framework contains a corresponding mechanism to lookup elements by the debug identifier, but more on this later.

If we look at the "Events"  tab of the administration console, we can see that we need the button for adding a new event as well as the table for the validation of the creation (Listing 1).

    public class SailingEventManagementPanel extends SimplePanel implements EventRefresher {
        public SailingEventManagementPanel(final SailingServiceAsync sailingService,
                final ErrorReporter errorReporter, final StringMessages stringMessages) {
            ...
            
            Button createEventBtn = new Button(stringMessages.actionAddEvent());
            createEventBtn.enusreDebugId("AddEventButton");
            ...
            
            eventTable = new CellTable<EventDTO>(10000, tableRes);
            eventTable.enusreDebugId("EventsCellTable");
            ...
        }
    }

In addition we need all the text fields and the checkbox of the dialog for creating a new event, which are instantiated by the class ´EventCreateDialog`, as well as for the dialog itself (Listing 2).

    public class EventCreateDialog extends EventDialog<EventDTO> {
        public EventCreateDialog(Collection<EventDTO> existingEvents, StringMessages stringConstants,
                DialogCallback<EventDTO> callback) {
            super(new EventParameterValidator(stringConstants, existingEvents), stringConstants, callback);
            
            nameEntryField = createTextBox(null);
            nameEntryField.ensureDebugId("NameTextBox");
            nameEntryField.setWidth("200px");
            
            venueEntryField = createTextBox(null);
            venueEntryField.ensureDebugId("VenueTextBox");
            venueEntryField.setWidth("200px");
            
            publicationUrlEntryField = createTextBox(null);
            publicationUrlEntryField.ensureDebugId("PublicationUrlTextBox");
            publicationUrlEntryField.setWidth("200px");
            
            isPublicCheckBox = createCheckbox("");
            isPublicCheckBox.ensureDebugId("IsPublicCheckBox");
            isPublicCheckBox.setValue(false);
        }
    }
    
    public class SailingEventManagementPanel extends SimplePanel implements EventRefresher {
        private void openCreateEventDialog() {
            List<EventDTO> existingEvents = new ArrayList<EventDTO>(eventProvider.getList());
            EventCreateDialog dialog = new EventCreateDialog(Collections.unmodifiableCollection(existingEvents),
                    stringMessages, new DialogCallback<EventDTO>() {
                @Override
                public void cancel() {
                }
                
                @Override
                public void ok(EventDTO newEvent) {
                    createNewEvent(newEvent);
                }
            });
            dialog.ensureDebugId("EventCreateDialog");
            dialog.show();
        }
    }

Finally you should assign a debug identifier to the event management panel (Listing 3), since it is acts as a context for the search.

    public class AdminConsoleEntryPoint extends AbstractEntryPoint implements RegattaRefresher {
        protected void doOnModuleLoad() {
            ...
            
            SailingEventManagementPanel sailingEventManagementPanel = new SailingEventManagementPanel(sailingService,
                this, stringMessages);
            sailingEventManagementPanel.ensureDebugId("SailingEventManagementPanel");
            ...
        }
    }

After you assigned an identifier to all widgets, you are almost done with the preparation. But, there is one more thing you should keep an eye on due the nature of GWT, which heavily use AJAX. If you look at the method `createNewEvent(final EventDTO newEvent)` of the class `SailingEventManagementPanel` as well as `fillEvents()`, which is called by the former one, you see that an `AsyncCallback` is passed to the service methods. In the background GWT creates an asynchrony request here and you don’t know when the request completes. Therefor you can’t tell how long you have to wait, before you can proceed with your test.

Our framework addresses this by providing a semaphore that counts pending asynchrony requests and the necessary code is automatically injected into the final HTML-Document by the base class `AbstractEntryPoint`. In the case you develop a new entry point, make sure you extend this one.

To use the semaphore you simply have to replace the `AsyncCallback` with a `MarkedAsyncCallback` and to rename the methods `onFailure` and `onSuccess` to `handleFailure` and `handleSuccess` (Listing 4).

    public class SailingEventManagementPanel extends SimplePanel implements EventRefresher {
        private void createNewEvent(final EventDTO newEvent) {
            ...
            sailingService.createEvent(newEvent.getName(), newEvent.venue.getName(), newEvent.publicationUrl,
                newEvent.isPublic, courseAreaNames, new MarkedAsyncCallback<EventDTO>() {
                    public void handleFailure(Throwable t) {
                        errorReporter.reportError("Error trying to create new event" + newEvent.getName() + ": " +
                            t.getMessage());       
                    }
                    
                    public void handleSuccess(EventDTO newEvent) {
                        fillEvents();
                    }
                });
            }

            public void fillEvents() {
                sailingService.getEvents(new MarkedAsyncCallback<List<EventDTO>>() {
                    public void handleFailure(Throwable caught) {
                        errorReporter.reportError("Remote Procedure Call getEvents() - Failure: " + caught.getMessage());
                    }
                    
                    public void handleSuccess(List<EventDTO> result) {
                        allEvents.clear();
                        allEvents.addAll(result);
                        filterTextbox.updateAll(allEvents);
                    }
                });
            }
        }
