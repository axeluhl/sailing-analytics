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

In addition we need all the text fields and the checkbox of the dialog for creating a new event, which are instantiated by the class `EventCreateDialog`, as well as for the dialog itself (Listing 2).

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
                    public void handleFailure(Throwable t) {
                        errorReporter.reportError("Remote Procedure Call getEvents() - Failure: " + t.getMessage());
                    }
                    
                    public void handleSuccess(List<EventDTO> result) {
                        allEvents.clear();
                        allEvents.addAll(result);
                        filterTextbox.updateAll(allEvents);
                    }
                });
            }
        }

## Writing the Page Objects

After finishing the preparation of the UI you can start to write the page objects, which represent complex HTML structures like a table, a form or even the whole page. Page objects hiding this complexity and provide service methods in terms of how the user interacts with the part of the page they represent. They also keep necessary changes in one place, for the case the UI changes. Our framework contains the two base classes `HostPage` and `PageArea`, which provide some common functionality like waiting until all asynchrony requests have completed. For our test we need a page object representing the event management tab as well as a page object for the dialog. We start with the dialog here, since it is need for the event management later.

To simulate the user interactions like clicking at a button or filling out a text field, you need a reference to the `WebElement`. Selenium uses locating mechanisms, which are define by the different `By` implementations. These are passed to the `findElement` and `findElements` methods of the `WebDriver` or another `WebElement`. While the `WebDriver` searches for the elements in the whole page a `WebElement` only looks up in its children. To support the Page-Object pattern, our framework provides a factory that creates dynamic proxies which are used to initialize annotated instance variables of the type `WebElement` or `List<WebElement>` automatically. The actual element is looked up when you access the variable.

For the dialog you should define the variables for the 3 different text fields as well as the checkbox and annotated them accordingly with a `FindBy` (Listing 5). The `how` element is used to define the implementation of the `By` and the `using` element the value to use. Since you assigned a debug identifier to all elements, you have to use the implementation `BySeleniumId`. Sometimes it may be necessary that you need another locating mechanism (e.g. `ByXpath`) but for the dialog it is not the case. If you know that an element is static and always there, you can also annotate the field with `CacheLookup`. In this case the element is never looked up again once it has been used the first time. Without this annotation the element is looked up every time it is used.

    public class EventCreateDialogPO extends DataEntryDialogPO {
        @FindBy(how = BySeleniumId.class, using = "NameTextBox")
        private WebElement nameTextBox;
        
        @FindBy(how = BySeleniumId.class, using = "VenueTextBox")
        private WebElement venueTextBox;
        
        @FindBy(how = BySeleniumId.class, using = "PublicationUrlTextBox")
        private WebElement publicationUrlTextBox;
        
        @FindBy(how = BySeleniumId.class, using = "IsPublicCheckBox")
        private WebElement isPublicCheckBox;
        
        public EventCreateDialogPO(WebDriver driver, WebElement element) {
            super(driver, element);
        }
    }

Now that you have access to the web elements, you can add your service methods to the page object. Usually the service methods are coarse granular, like `login(String user, String password)`, but since the dialog has an error state for the case of invalid input, which you may test later, you should add methods to interact with the single widgets (Listing 6). The methods for clicking the “Ok” or the “Cancel” button are already implemented in the base class `DataEntryDialogPO`. After finishing the dialog with “Ok”, our base class also waits for all pending asynchrony requests and returns immediately if there is none.

    public class EventCreateDialogPO extends DataEntryDialogPO {
        ...
        
        public void setName(String name) {
            this.nameTextBox.clear();
            this.nameTextBox.sendKeys(name);
        }
        
        public void setVenue(String venue) {
            this.venueTextBox.clear();
            this.venueTextBox.sendKeys(venue);
        }
        
        public void setPublicationUrl(String url) {
            this.publicationUrlTextBox.clear();
            this.publicationUrlTextBox.sendKeys(url);
        }
        
        public void setPubic(boolean isPublic) {
            CheckBoxPO checkbox = new CheckBoxPO(this.driver, this.isPublicCheckBox);
            checkbox.setSelected(isPublic);
        }
    }

The page object for the event management panel is written in a similar fashion (Listing 7). The method `startCreatingEvent` performs a click on the button, looks up the `WebElement` for the dialog programmatically and returns the page object for the dialog. Since service methods should be coarse granular usually, you should also add the method `createEvent` which just takes the data for the new event and hides the interaction with the dialog completely. The last two methods return page objects which represent a GWT CellTable and its entries that already exist in our framework.

    public class SailingEventManagementPanelPO extends PageArea {
        @FindBy(how = BySeleniumId.class, using = "AddEventButton")
        private WebElement createEventButton;
        
        @FindBy(how = BySeleniumId.class, using = "EventsCellTable")
        private WebElement eventsTable;
        
        public SailingEventManagementPanelPO(WebDriver driver, WebElement element) {
            super(driver, element);
        }
        
        public EventCreateDialogPO startCreatingEvent() {
            this.createEventButton.click();
            
            WebElement dialog = findElementBySeleniumId(this.driver, "EventCreateDialog");
            
            return new EventCreateDialogPO(this.driver, dialog);
        }
        
        public void createEvent(String name, String venue, String url, boolean isPublic) {
            EventCreateDialogPO dialog = startCreatingEvent();
            dialog.setName(name);
            dialog.setVenue(venue);
            dialog.setPublicationUrl(url);
            dialog.setPubic(isPublic);
            dialog.pressOk();
        }
        
        public CellTablePO<DataEntryPO> getEventsTable() {
            return new GenericCellTablePO<DataEntryPO>(this.driver, this.eventsTable, DataEntryPO.class);
        }
        
        public List<DataEntryPO> getEvents() {
            CellTablePO<DataEntryPO> table = getEventsTable();
            
            return table.getEntries();
        }
    }

In the last step, you have to plug your new page object for the event management in the page object for the administration console, which contains a method to switch to a tab. You should add a new method that calls the existing one with the right parameters and return your page object for the event management (Listing 8).

    public class AdminConsolePage extends HostPage {
        ...
        
        private static final String EVENT_MANAGEMENT_TAB_LABEL = "Events"; //$NON-NLS-1$
        private static final String EVENT_MANAGEMENT_TAB_IDENTIFIER = "SailingEventManagementPanel"; //$NON-NLS-1$
        
        ...

        public SailingEventManagementPanelPO goToRegattaStructure() {
            return new SailingEventManagementPanelPO(this.driver, goToTab(EVENT_MANAGEMENT_TAB_LABEL,
                EVENT_MANAGEMENT_TAB_IDENTIFIER));
        }
        
        ...
    }

## Writing the Test

Now that you have all necessary page objects, you can start to write the test. Our framework uses a specialized JUnit-Runner for the execution of a test with selenium and injects a `TestEnvironment` into a field of the test class that is annotated with `Managed`. The injected `TestEnvironment` gives you access to the `WebDriver` and the base URL to the application under test. However, you can simply extend the base class `AbstractSeleniumTest` which has all required annotations applied already.

Writing the test is straight forward and there are no big differences to other test cases. You use the page objects to simulate the user interactions and to retrieve the data for your assertions (Listing 9). Since our application persists most of the object, you should clear the state of the `RacingEventService` before each test. For the test itself you use the factory method in the page object for the administration console to navigate to it. There you have to switch to the tab for the event management, where you can create your test event and retrieve the existing events. Since you cleared the state of the application before the test, there should only be one which makes the assertion of the test.

    public class TestEventCreation extends AbstractSeleniumTest {
        @Before
        public void clearDatabase() {
            clearState(getContextRoot());
        }
        
        @Test
        public void testCreateEvent() {
            AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
            SailingEventManagementPanelPO eventManagement = adminConsole.goToEventManagement();
            
            eventManagement.createEvent("Test Event", "Test Venue", "", false);
        
            List<DataEntry> events = eventManagement.getEvents();
            assertTrue(events.size() == 1);
        }
    }

## Executing the Test


## Advanced Topics

TODO by Riccardo

* Extend DataEntryPO
* Query Framework
* WindowManager