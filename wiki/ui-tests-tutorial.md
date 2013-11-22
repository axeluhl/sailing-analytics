In this tutorial, we want to give a practical introduction in how to write UI-Tests with our framework and Selenium. You will learn to prepare your UI for testing as well as to write page objects and tests. For this reason, we write a small test which interacts with the administration console and the goal of the test is to verify the correct creation of a new event.

When you start to write UI tests, the first thing you have to do is, to ensure that the UI is testable. This means, that you have to be able to easily find all widgets in the final HTML-Document the user interacts with. The simplest approach here is to use debug identifiers which are provided by GWT and to assign an identifier to all important widgets, like buttons and text fields. Our framework contains a corresponding mechanism to lookup elements by the debug identifier, but more on this later.

    public class SailingEventManagementPanel extends SimplePanel implements EventRefresher {
        public SailingEventManagementPanel(final SailingServiceAsync sailingService,
                final ErrorReporter errorReporter, final StringMessages stringMessages) {
            ...
            
            Button createEventBtn = new Button(stringMessages.actionAddEvent());
            createEventBtn.enusreDebugId(“AddEventButton”);
            ...
            
            eventTable = new CellTable<EventDTO>(10000, tableRes);
            eventTable.enusreDebugId(“EventsCellTable”);
            ...
        }
    }

: Listing 1