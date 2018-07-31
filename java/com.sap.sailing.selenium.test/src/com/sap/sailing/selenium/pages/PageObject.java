package com.sap.sailing.selenium.pages;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.sap.sailing.selenium.core.AjaxCallsComplete;
import com.sap.sailing.selenium.core.AjaxCallsExecuted;
import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.ElementSearchConditions;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.core.FindBys;
import com.sap.sailing.selenium.core.SeleniumElementLocatorFactory;
import com.sap.sailing.selenium.core.SeleniumFieldDecorator;

/**
 * <p>Within a web app's UI there are areas that tests interact with. A page object simply models these as objects
 *   within the test code and abstracts from the actual structure (DOM) of a website. This reduces the amount of
 *   duplicated code and means that if the UI changes, the fix need only be applied in one place.</p>
 * 
 * <p>Page objects can be thought of as facing in two directions simultaneously. Facing towards the developer of a test,
 *   they represent the services offered by a particular page. Facing away from the developer, they should be the only
 *   thing that has a deep knowledge of the structure of a page (or part of a page). It's simplest to think of the
 *   methods on a page object as offering the "services" that a page offers rather than exposing the details and
 *   mechanics of the page.</p>
 * 
 * <p>Because the developer of a test should think about the services that they're interacting with rather than the
 *   implementation, page objects should seldom expose the underlying web driver or elements. To facilitate this,
 *   methods on a page object should return other page objects.</p>
 * 
 * <p>To make the development of page objects as simple and easy as possible, we also support a factory for this pattern
 *   which helps to remove some boiler-plate code from the page objects by using annotations. The factory uses the
 *   annotations on fields to lazily locate and wait for an element or an element list to appear, by polling the UI on
 *   a regular basis, and initializes the field with the element or the list of elements.</p>
 * 
 * @author
 *   D049941
 */
public class PageObject {
    private static final Logger logger = Logger.getLogger(PageObject.class.getName());
    
    public static final int DEFAULT_WAIT_TIMEOUT_SECONDS = 120;
    
    public static final int DEFAULT_POLLING_INTERVAL = 5;
    
    private static final MessageFormat TAB_PANEL_EXPRESSION = new MessageFormat(
            ".//div[contains(@class, \"gwt-TabBarItem\")]/div[text()=\"{0}\"]/..");
    
    private static final MessageFormat TAB_LAYOUT_PANEL_EXPRESSION = new MessageFormat(
            ".//div[contains(@class, \"gwt-TabLayoutPanelTabInner\")]/div[text()=\"{0}\"]/../..");
    
    private static final MessageFormat VERTICAL_TAB_LAYOUT_PANEL_EXPRESSION = new MessageFormat(
            ".//div[contains(@class, \"gwt-VerticalTabLayoutPanelTabInner\")]/div[text()=\"{0}\"]/../..");
    
    /**
     * </p>The default timeout of 15 seconds for the lookup of other elements.</p>
     */
    protected static final int DEFAULT_LOOKUP_TIMEOUT = 60;
    
    /**
     * <p>The web driver to use.</p>
     */
    protected final WebDriver driver;
    
    /**
     * <p>The context for the locating of elements.</p>
     */
    protected final SearchContext context;
    
    /**
     * <p>Creates a FluentWait with the given input and the default timeout and polling interval.</p>
     * 
     * @param input
     *   
     * @return
     *   
     */
    public static <T> FluentWait<T> createFluentWait(T input) {
        return createFluentWait(input, Collections.<Class<? extends Throwable>>emptyList());
    }
    
    /**
     * <p></p>
     * 
     * @param input
     *   
     * @param exceptions
     *   
     * @return
     *   
     */
    @SafeVarargs
    public static <T> FluentWait<T> createFluentWait(T input, Class<? extends Throwable>... exceptions) {
        return createFluentWait(input, Arrays.asList(exceptions));
    }
    
    /**
     * <p></p>
     * 
     * @param input
     *   
     * @param exceptions
     *   
     * @return
     *   
     */
    public static <T> FluentWait<T> createFluentWait(T input, Collection<Class<? extends Throwable>> exceptions) {
        return createFluentWait(input, DEFAULT_WAIT_TIMEOUT_SECONDS, DEFAULT_POLLING_INTERVAL, exceptions);
    }
    
    /**
     * <p>Creates a FluentWait with the given input and the specified timeout and polling interval.</p>
     * 
     * @param input
     *   
     * @param timeoutInSeconds
     *   
     * @param pollingEverySoManySeconds
     *   
     * @return
     *   
     */
    public static <T> FluentWait<T> createFluentWait(T input, int timeoutInSeconds, int pollingEverySoManySeconds) {
        return createFluentWait(input, timeoutInSeconds, pollingEverySoManySeconds, Collections.<Class<? extends Throwable>>emptyList());
    }
    
    /**
     * <p></p>
     * 
     * @param input
     *   
     * @param timeout
     *   
     * @param polling
     *   
     * @param exceptions
     *   
     * @return
     *   
     */
    @SafeVarargs
    public static <T> FluentWait<T> createFluentWait(T input, int timeout, int polling,
            Class<? extends Throwable>... exceptions) {
        return createFluentWait(input, timeout, polling, Arrays.asList(exceptions));
    }
    
    /**
     * <p></p>
     * 
     * @param input
     *   
     * @param timeoutInSeconds
     *   
     * @param pollingEverySoManySeconds
     *   
     * @param exceptions
     *   
     * @return
     *   
     */
    public static <T> FluentWait<T> createFluentWait(T input, int timeoutInSeconds, int pollingEverySoManySeconds,
            Collection<Class<? extends Throwable>> exceptions) {
        FluentWait<T> wait = new FluentWait<>(input);
        wait.withTimeout(timeoutInSeconds, TimeUnit.SECONDS);
        wait.pollingEvery(pollingEverySoManySeconds, TimeUnit.SECONDS);
        wait.ignoreAll(exceptions);
        
        return wait;
    }
    
    /**
     * <p>Creates a new page object with the given web driver. This constructor is equivalent to the two argument
     *   constructor whereby the web driver is also used as the context for the page object.</p>
     * 
     * @param driver
     *   The web driver which also acts as the context to use.
     */
    public PageObject(WebDriver driver) {
        this(driver, driver);
    }
    
    /**
     * <p>Creates a new page object with the given web driver and context.</p>
     * 
     * @param driver
     *   The web driver to use.
     * @param context
     *   The context to use.
     */
    public PageObject(WebDriver driver, SearchContext context) {
        this.driver = driver;
        this.context = context;
        
        verify();
        initElements();
    }
    
    /**
     * <p>Verifies the page object. This method can be overwritten to check that the browser is on the correct page or
     *   that the provided context represents the right element. The default implementation does nothing.</p>
     */
    protected void verify() {
    }
    
    /**
     * <p>Initialize the page object. The default implementation use a factory to lazily initialize the elements
     *   (annotated fields) of the page object using the timeout duration returned by {@link #getPageLoadTimeOutInSeconds()}.
     *   To get a field lazily initialized you have to annotate the field either with {@link FindBy} or with
     *   {@link FindBys}. If the element never changes (that is, that the same instance in the DOM can always be used)
     *   it is also possible to use a cache for the lookup by using the annotation {@code CacheLookup} in addition.</p>
     * 
     * <p>A simple example for the implementation of a page object can look as follows:</p>
     * 
     * <pre>
     *   public class MySearchPage extends PageObject {
     *       // The element is looked up using the id attribute and it is
     *       // never look up again once it has been used the first time
     *       &#64;FindBy(how = ById.class, using = "my-searchfield")
     *       &#64;CacheLookup
     *       private WebElement searchField;
     *       
     *       // The element is looked up using the tag name and it is
     *       // look up every time it is used
     *       &#64;FindBy(how = ByTagName.class, using = "input")
     *       private WebElement submitButton;
     *       
     *       public MySearchPage(WebDriver driver) {
     *           super(driver);
     *       }
     *       
     *       public void enterSearchText(String text) {
     *          this.searchField.clear();
     *          this.searchField.sendKeys(text);
     *       }
     *       
     *       public ResultPage performSearch() {
     *          this.submitButton.click();
     *       }
     *   }
     * </pre>
     * 
     * <p>If the implementation is not sufficient for your case, you can overwrite this method with or without calling
     *   the default implementation.</p>
     */
    protected void initElements() {
        SeleniumElementLocatorFactory factory = new SeleniumElementLocatorFactory(this.context, getLookupTimeOut());
        SeleniumFieldDecorator decorator = new SeleniumFieldDecorator(factory);
        
        PageFactory.initElements(decorator, this);
    }
    
    /**
     * <p>Returns the timeout duration for the initialization of the page object.</p>
     * 
     * @return
     *   The timeout duration for the initialization of the page object.
     */
    protected int getLookupTimeOut() {
        return DEFAULT_LOOKUP_TIMEOUT;
    }
    
    /**
     * <p>Finds and returns the first element with the specified selenium id in the search context of the page object.
     *   If multiple elements exists, the element closest to the context is returned.</p>
     * 
     * @param id
     *   The selenium id of the element.
     * @return
     *   The first matching element in the search context of the page object.
     * @see #findElementBySeleniumId(SearchContext context, String id)
     */
    protected WebElement findElementBySeleniumId(String id) {
        return findElementBySeleniumId(this.context, id);
    }
    
    /**
     * <p>Waits for an element with the specified selenium id to appear in the given search context. If multiple
     *   elements exists, the element closest to the context is returned.</p>
     * 
     * @param context
     *   The search context to use for the search.
     * @param id
     *   The selenium id of the element.
     * @return
     *   The first matching element in the given context.
     */
    protected WebElement waitForElementBySeleniumId(SearchContext context, String id, int timeout) {
        FluentWait<SearchContext> wait = createFluentWait(context, timeout, DEFAULT_POLLING_INTERVAL);
        return (WebElement) wait.until(new Function<SearchContext, Object>() {
            @Override
            public Object apply(SearchContext context) {
                try {
                    return context.findElement(new BySeleniumId(id));
                } catch (Exception e) {
                    return null;
                }
            }
        });
    }
    
    /**
     * <p>
     * Finds and returns the first element with the specified selenium id in the given search context. If multiple
     * elements exists, the first found element is returned. If no matching element can be found, {@code null} is
     * returned.
     * </p>
     * 
     * @param context
     *            The search context to use for the search.
     * @param id
     *            The selenium id of the element.
     * @return The first matching element in the given context.
     */
    protected WebElement findElementOrNullBySeleniumId(SearchContext context, String id) {
        final List<WebElement> elements = context.findElements(new BySeleniumId(id));
        WebElement result;
        if (elements.isEmpty()) {
            result = null;
        } else {
            result = elements.get(0);
        }
        return result;
    }
    
    /**
     * <p>
     * Finds and returns the first element with the specified selenium id in the given search context. If multiple
     * elements exists, the first found element is returned. If no matching element can be found, {@code null} is
     * returned.
     * </p>
     * 
     * @param id
     *            The selenium id of the element.
     * @return The first matching element in the given context.
     */
    protected WebElement findElementOrNullBySeleniumId(String id) {
        return findElementOrNullBySeleniumId(this.context, id);
    }
    
    /**
     * <p>Finds and returns the first element with the specified selenium id in the given search context. If multiple
     *   elements exists, the element closest to the context is returned.</p>
     * 
     * @param context
     *   The search context to use for the search.
     * @param id
     *   The selenium id of the element.
     * @return
     *   The first matching element in the given context.
     */
    protected WebElement findElementBySeleniumId(SearchContext context, String id) {
        return context.findElement(new BySeleniumId(id));
    }
    
    /**
     * <p>Finds and returns all elements within the specified selenium id in the search context of the page object.</p>
     * 
     * @param id
     *   The selenium id of the elements.
     * @return
     *   A list of all matching elements in the search context of the page object, or an empty list if nothing matches.
     * @see #findElementsBySeleniumId(SearchContext context, String id)
     */
    protected List<WebElement> findElementsBySeleniumId(String id) {
        return findElementsBySeleniumId(this.context, id);
    }
    
    /**
     * <p>Finds and returns all elements within the specified selenium id in the given search context.</p>
     * 
     * @param context
     *   The search context to use for the search.
     * @param id
     *   The selenium id of the elements.
     * @return
     *   A list of all matching elements in the given context, or an empty list if nothing matches.
     */
    protected List<WebElement> findElementsBySeleniumId(SearchContext context, String id) {
        return context.findElements(new BySeleniumId(id));
    }
    
    /**
     * <p>Waits until all pending Ajax request in the global category have been complete using a timeout of 30 seconds
     *   and a polling interval of 5 seconds. In reality, the interval may be greater as the cost of actually evaluating
     *   the condition is not factored in.</p>
     * 
     * @throws org.openqa.selenium.TimeoutException
     *   if the timeout of 30 seconds expires.
     * @see #waitForAjaxRequests(int timeout, int polling)
     */
    protected void waitForAjaxRequests() {
        waitForAjaxRequests(DEFAULT_WAIT_TIMEOUT_SECONDS, DEFAULT_POLLING_INTERVAL);
    }
    
    /**
     * <p>Waits until all pending Ajax request in the specified category have been complete using a timeout of 30
     *   seconds and a polling interval of 5 seconds. In reality, the interval may be greater as the cost of actually
     *   evaluating the condition is not factored in.</p>
     * 
     * @param category
     *   The category of Ajax requests to wait for.
     * @throws org.openqa.selenium.TimeoutException
     *   if the timeout of 30 seconds expires.
     * @see #waitForAjaxRequests(int timeout, int polling)
     */
    protected void waitForAjaxRequests(String category) {
        waitForAjaxRequests(category, DEFAULT_WAIT_TIMEOUT_SECONDS, DEFAULT_POLLING_INTERVAL);
    }
    
    /**
     * <p>Waits until all pending Ajax request in the global category have been complete using the given timeout
     *   and polling interval. In reality, the interval may be greater as the cost of actually evaluating the condition
     *   is not factored in.</p>
     * 
     * @param timeoutInSeconds
     *   The timeout duration for the waiting in seconds.
     * @param pollingEverySoManySeconds
     *   The interval in seconds in which the check should be performed.
     * @throws org.openqa.selenium.TimeoutException
     *   if the timeout expires.
     */
    protected void waitForAjaxRequests(int timeoutInSeconds, int pollingEverySoManySeconds) {
        waitForAjaxRequests(AjaxCallsComplete.CATEGORY_GLOBAL, timeoutInSeconds, pollingEverySoManySeconds);
    }
    
    /**
     * <p>Waits until all pending Ajax request in the specified category have been complete using the given timeout
     *   and polling interval. In reality, the interval may be greater as the cost of actually evaluating the condition
     *   is not factored in.</p>
     * 
     * @param category
     *   The category of Ajax requests to wait for.
     * @param timeoutInSeconds
     *   The timeout duration for the waiting in seconds.
     * @param pollingEverySoManySeconds
     *   The interval in seconds in which the check should be performed.
     * @throws org.openqa.selenium.TimeoutException
     *   if the timeout expires.
     */
    protected void waitForAjaxRequests(String category, int timeoutInSeconds, int pollingEverySoManySeconds) {
        FluentWait<WebDriver> wait = createFluentWait(this.driver, timeoutInSeconds, pollingEverySoManySeconds);
        
        wait.until(new AjaxCallsComplete(category));
    }
    
    protected void waitForAjaxRequestsExecuted(int numberOfCalls) {
        waitForAjaxRequestsExecuted(numberOfCalls, DEFAULT_WAIT_TIMEOUT_SECONDS, DEFAULT_POLLING_INTERVAL);
    }
    
    protected void waitForAjaxRequestsExecuted(String category, int numberOfCalls) {
        waitForAjaxRequestsExecuted(category, numberOfCalls, DEFAULT_WAIT_TIMEOUT_SECONDS, DEFAULT_POLLING_INTERVAL);
    }
    
    protected void waitForAjaxRequestsExecuted(int numberOfCalls, int timeout, int polling) {
        waitForAjaxRequestsExecuted(AjaxCallsComplete.CATEGORY_GLOBAL, numberOfCalls, timeout, polling);
    }
    
    protected void waitForAjaxRequestsExecuted(String category, int numberOfCalls, int timeout, int polling) {
        FluentWait<WebDriver> wait = createFluentWait(this.driver, timeout, polling);
        wait.until(new AjaxCallsExecuted(category, numberOfCalls));
    }
    
    protected void waitForElement(String seleniumId) {
        WebDriverWait webDriverWait = new WebDriverWait(driver, DEFAULT_LOOKUP_TIMEOUT);
        webDriverWait.until(ExpectedConditions.presenceOfElementLocated(new BySeleniumId(seleniumId)));
    }
    
    protected void waitUntil(Predicate<WebDriver> predicate) {
        WebDriverWait webDriverWait = new WebDriverWait(driver, DEFAULT_LOOKUP_TIMEOUT);
        webDriverWait.until(predicate);
    }
    
    protected void waitUntil(BooleanSupplier supplier) {
        waitUntil((driver) -> supplier.getAsBoolean());
    }
    
    /**
     * Returns a {@link PageArea} instance representing the element with the specified selenium id using the
     * {@link WebDriver} as search context.
     * 
     * @param supplier {@link PageAreaSupplier} used to instantiate the {@link PageArea}
     * @param seleniumId the selenium id of the desired element
     * @return {@link PageArea} representing the first matching element
     * 
     * @see #findElementBySeleniumId(SearchContext, String)
     */
    protected <T extends PageArea> T getPO(PageAreaSupplier<T> supplier, String seleniumId) {
        return supplier.get(driver, findElementBySeleniumId(driver, seleniumId));
    }
    
    /**
     * Waits for the element with the given seleniumId and returns a {@link PageArea} instance representing the element. The 
     * {@link WebDriver} is used as search context.
     * 
     * @param supplier {@link PageAreaSupplier} used to instantiate the {@link PageArea}
     * @param seleniumId the selenium id of the desired element
     * @param timeout the timeout in seconds to wait for the element
     * @return {@link PageArea} representing the first matching element
     * 
     * @see #findElementBySeleniumId(SearchContext, String)
     */
    protected <T extends PageArea> T waitForPO(PageAreaSupplier<T> supplier, String seleniumId, int timeout) {
        return supplier.get(driver, waitForElementBySeleniumId(driver, seleniumId, timeout));
    }
    
    /**
     * Returns a {@link PageArea} instance representing the element with the specified selenium id in the search
     * context of this page area.
     * 
     * @param supplier {@link PageAreaSupplier} used to instantiate the {@link PageArea}
     * @param seleniumId the selenium id of the desired element
     * @return {@link PageArea} representing the first matching element
     * 
     * @see #findElementBySeleniumId(String)
     */
    protected <T extends PageArea> T getChildPO(PageAreaSupplier<T> supplier, String seleniumId) {
        return supplier.get(driver, findElementBySeleniumId(seleniumId));
    }
    
    protected interface PageAreaSupplier<T extends PageArea> {
        T get(WebDriver driver, WebElement element);
    }
    
    protected WebElement goToTab(WebElement tabPanel, String tabName, final String id, TabPanelType tabPanelType) {
        final MessageFormat expressionFormat;
        switch(tabPanelType) {
        case TAB_PANEL:
            expressionFormat = TAB_PANEL_EXPRESSION;
            break;
        case TAB_LAYOUT_PANEL:
            expressionFormat = TAB_LAYOUT_PANEL_EXPRESSION;
            break;
        case VERTICAL_TAB_LAYOUT_PANEL:
            expressionFormat = VERTICAL_TAB_LAYOUT_PANEL_EXPRESSION;
            break;
            default:
                throw new IllegalArgumentException("TabPanelType \"" + tabPanelType + "\" is unsupported");
        }
        final String expression = expressionFormat.format(new Object[] {tabName});
        WebElement tab = tabPanel.findElement(By.xpath(expression));
        WebDriverWait waitForTab = new WebDriverWait(driver, 20); // here, wait time is 20 seconds
        waitForTab.until(ExpectedConditions.visibilityOf(tab)); // this will wait for tab to be visible for 20 seconds
        tab.click();
        // Wait for the tab to become visible due to the used animations.
        FluentWait<WebElement> wait = createFluentWait(tabPanel);
        WebElement content = wait.until(ElementSearchConditions.visibilityOfElementLocated(new BySeleniumId(id)));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "internal error sleeping for 500ms", e);
        } // wait for a bit to make sure the UI had a change to trigger any asynchronous background update/refresh
        waitForAjaxRequests(); // switching tabs can trigger asynchronous updates, replacing UI elements
        return content;
    }
    
    public enum TabPanelType {
        TAB_PANEL, TAB_LAYOUT_PANEL, VERTICAL_TAB_LAYOUT_PANEL
    }
    
    /**
     * Waits for an alert box to appear and accepts the alert. If no alert shows up, an Exception is thrown.
     */
    protected void waitForAlertAndAccept() throws InterruptedException {
       waitForAlertAndAccept(DEFAULT_WAIT_TIMEOUT_SECONDS);
    }
     
    /**
     * Waits for an alert box to appear and accepts the alert. If no alert shows up, an Exception is thrown.
     */
    protected void waitForAlertAndAccept(int timeoutInSeconds) throws InterruptedException {
        int i = 0;
        while (i < timeoutInSeconds) {
            i++;
            try {
                Alert alert = driver.switchTo().alert();
                alert.accept();
                return;
            } catch (NoAlertPresentException e) {
                Thread.sleep(1000);
            }
        }
        throw new NoAlertPresentException();
    }

    /**
     * Waits for an notification to appear and dismisses the notification by clicking on it. If no notification shows up, an Exception is thrown.
     */
    protected void waitForNotificationAndDismiss() throws InterruptedException {
        waitForNotificationAndDismiss(DEFAULT_WAIT_TIMEOUT_SECONDS);
    }

    /**
     * Waits for an notification to appear and dismisses the notification by clicking on it. If no notification shows up, an Exception is thrown.
     */
    protected void waitForNotificationAndDismiss(int timeoutInSeconds) throws InterruptedException {
        int i = 0;
        while (i < timeoutInSeconds) {
            i++;
            try {
                List<WebElement> notifications = driver.findElements(By.id("notificationBar"));
                if (notifications.size() > 0) {
                    notifications.get(0).findElements(By.cssSelector("*"))
                            .forEach(notification -> notification.click());
                    ;
                    return;
                } else {
                    throw new NoAlertPresentException();
                }
            } catch (NoAlertPresentException e) {
                Thread.sleep(1000);
            }
        }
        throw new NoAlertPresentException();
    }
    
    public boolean isElementEntirelyVisible(WebElement element) {
        try {
            if (element.isDisplayed()) {
                final int windowWidth = driver.manage().window().getSize().getWidth();
                if (windowWidth >= element.getLocation().x
                        + element.getSize().width) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            // The element may currently only partially visible which makes some of the calls fail
            return false;
        }
    }
}
