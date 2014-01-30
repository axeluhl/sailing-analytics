***
# UI-Tests Tutorial

In this tutorial, we want to give a practical introduction in how to write UI-Tests with our framework and Selenium. You will learn to prepare your UI for testing as well as to write page objects and tests. For this reason, we write a small test which interacts with the administration console and the goal of the test is to verify the correct creation of a new event.

When you start to write UI tests, the first thing you have to do is, to ensure that the UI is testable. This means, that you have to be able to easily find all widgets in the final HTML-Document the user interacts with. The simplest approach here is to use debug identifiers which are provided by GWT and to assign an identifier to all important widgets, like buttons and text fields. Our framework contains a corresponding mechanism to lookup elements by the debug identifier, but more on this later.

If we look at the "Events"  tab of the administration console, we can see that we need the button for adding a new event as well as the table for the validation of the creation (Listing 1). In addition we need all the text fields and the checkbox of the dialog for creating a new event, which are instantiated by the class `EventCreateDialog`, as well as for the dialog itself (Listing 2).

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



    public class EventCreateDialog extends EventDialog<EventDTO> {
        public EventCreateDialog(Collection<EventDTO> existingEvents, StringMessages stringConstants,
                DialogCallback<EventDTO> callback) {
            super(new EventParameterValidator(stringConstants, existingEvents), stringConstants, callback);
            
            nameEntryField = createTextBox(null);
            nameEntryField.ensureDebugId("NameTextField");
            nameEntryField.setWidth("200px");
            
            venueEntryField = createTextBox(null);
            venueEntryField.ensureDebugId("VenueTextField");
            venueEntryField.setWidth("200px");
            
            publicationUrlEntryField = createTextBox(null);
            publicationUrlEntryField.ensureDebugId("PublicationUrlTextField");
            publicationUrlEntryField.setWidth("200px");
            
            isPublicCheckBox = createCheckbox("");
            isPublicCheckBox.ensureDebugId("IsPublicCheckbox");
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