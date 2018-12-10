package com.sap.sailing.selenium.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.sap.sailing.selenium.core.Managed;
import com.sap.sailing.selenium.core.SeleniumRunner;
import com.sap.sailing.selenium.core.TestEnvironment;
import com.sap.sailing.selenium.core.WindowManager;
import com.sap.sailing.selenium.pages.PageObject;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * <p>Abstract base class for unit tests with Selenium. This class is already annotated as required to get executed
 *   with the Selenium runner. Furthermore there is a rule, which automatically captures a screenshot if a test fails,
 *   since it is helpful to see how a page looked in the case a test failed.</p>
 * 
 * @author
 *   D049941
 */
@RunWith(SeleniumRunner.class)
public abstract class AbstractSeleniumTest {
    private static final Logger logger = Logger.getLogger(AbstractSeleniumTest.class.getName());
    
    /**
     * <p>File extension for screenshots captured with a Selenium web driver.</p>
     */
    private static final String SCREENSHOT_FILE_EXTENSION = ".png"; //$NON-NLS-1$
    
    private static final String NOT_SUPPORTED_IMAGE = "/com/sap/sailing/selenium/resources/not-supported.png"; //$NON-NLS-1$
    
    private static final String ATTACHMENT_FORMAT = "[[ATTACHMENT|%s]]"; //$NON-NLS-1$
    
    private static final String CLEAR_STATE_URL = "sailingserver/test-support/clearState"; //$NON-NLS-1$
    
    private static final String LOGIN_URL = "security/api/restsecurity/login";
    
    private static final int CLEAR_STATE_SUCCESFUL_STATUS_CODE = 204;

    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    
    /**
     * <p></p>
     * 
     * @param contextRoot
     * 
     * @return
     *   <code>true</code> if the state was reseted successfully and <code>false</code> otherwise.
     */
    protected void clearState(String contextRoot) {
        logger.info("clearing server state");
        try {
            URL url = new URL(contextRoot + CLEAR_STATE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.connect();
            if (connection.getResponseCode() != CLEAR_STATE_SUCCESFUL_STATUS_CODE) {
                throw new RuntimeException(connection.getResponseMessage());
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        // To be able to access LocalStorage we need to load a page having the target origin
        getWebDriver().get(contextRoot);
        
        final String notificationTimeoutKey = "sse.notification.customTimeOutInSeconds";
        final String notificationTimeoutValue = Integer.toString(PageObject.DEFAULT_WAIT_TIMEOUT_SECONDS);
        if (getWebDriver() instanceof WebStorage) {
            // clear local storage
            final WebStorage webStorage = (WebStorage)getWebDriver();
            webStorage.getLocalStorage().clear();
            
            // extending the timeout of notifications to 100s to prevent timing failures
            webStorage.getLocalStorage().setItem(notificationTimeoutKey,
                    notificationTimeoutValue);
        } else {
            // Fallback solution for IE
            ((JavascriptExecutor)getWebDriver()).executeScript("window.localStorage.clear();");
            ((JavascriptExecutor) getWebDriver()).executeScript("window.localStorage.setItem(\""
                    + notificationTimeoutKey + "\", \"" + notificationTimeoutValue + "\");");
        }
        
        try {
            // In IE 11 we sometimes see the problem that IE somehow automatically changes the zoom level to 75%.
            // Selenium tests with InternetExplorerDriver fail if the zoom level is not set to 100% due to the fact that coordinates determined aren't correct.
            // With this we enforce a zoom level of 100% before running a test.
            // To make this work correctly you also need to set InternetExplorerDriver.IGNORE_ZOOM_SETTING to true (this should be pre-configured in local-test-environment.xml when activating IE driver)
            getWebDriver().findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.CONTROL, "0"));
        } catch (Exception e) {
        }
    }
    
    protected void setUpAuthenticatedSession(WebDriver webDriver) {
        // To be able to set a cookie we need to load a page having the target origin
        webDriver.get(getContextRoot());
        
        logger.info("Authenticating session...");
        Cookie sessionCookie = authenticate(getContextRoot());
        webDriver.get(getContextRoot() + "index.html"); // initialize web driver so setting a cookie for the local domain is possible
        final Cookie cookieWithoutDomain = new Cookie(sessionCookie.getName(), sessionCookie.getValue(), null, sessionCookie.getPath(), sessionCookie.getExpiry(), sessionCookie.isSecure(), sessionCookie.isHttpOnly());
        webDriver.manage().addCookie(cookieWithoutDomain);
        logger.info("...obtained session cookie "+sessionCookie);
    }

    /**
     * If subclasses want to clear the state using {@link #clearState(String)}, they must re-define this method and
     * first invoke {@link #clearState(String)} before calling this implementation using <code>super.setUp()</code>.
     * This is important because {@link #clearState(String)} will also clear all session state that has been constructed
     * by {@link #setUpAuthenticatedSession()}.
     */
    @Before
    public void setUp() {
        setUpAuthenticatedSession(getWebDriver());
    }
    
    /**
     * Obtains a session cookie for a session authenticated using the default admin user.
     * 
     * @return the cookie that represents the authenticated session or <code>null</code> if the session
     * couldn't successfully be authenticated
     */
    protected Cookie authenticate(String contextRoot) {
        try {
            Cookie result = null;
            URL url = new URL(contextRoot + LOGIN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.connect();
            connection.getOutputStream().write("username=admin&password=admin".getBytes());
            if (connection.getResponseCode() / 100 != 3) { // expecting something like "302 Found" which redirects to the success page
                throw new RuntimeException("" + connection.getResponseCode() + " "+connection.getResponseMessage());
            }
            List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
            if (cookies != null) {
                for (String cookie : cookies) {
                    if (cookie.startsWith(SESSION_COOKIE_NAME + "=")) {
                        String cookieValue = cookie.substring(cookie.indexOf('=')+1, cookie.indexOf(';'));
                        result = new Cookie(SESSION_COOKIE_NAME, cookieValue, /* domain */ "localhost", /* path */ "/", MillisecondsTimePoint.now().plus(Duration.ONE_WEEK).asDate());
                    }
                }
            }
            return result;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    private class ScreenShotRule extends TestWatcher {
        @Override
        protected void failed(Throwable error, Description description) {
            captureScreenshots();
        }

        @Override
        protected void finished(Description description) {
            try {
                environment.getWindowManager().closeAllWindows();
            } finally {
                super.finished(description);
            }
        }
    }
    
    /**
     * <p>Rule for capturing of a screenshot if a test fails.</p>
     */
    @Rule
    public final ScreenShotRule takeScreenshotAndCloseWindows = new ScreenShotRule();

    /**
     * <p>The test environment used for the execution of the the tests.</p>
     */
    @Managed
    protected TestEnvironment environment;
    
    /**
     * <p>Returns the context root (base URL) against the tests are executed. The context root identifies a web
     *   application and usually consists of a protocol definition, the host and a path.</p>
     * 
     * @return
     *   The context root against the tests are executed.
     */
    protected String getContextRoot() {
        return this.environment.getContextRoot();
    }
    
    /**
     * <p>Returns the web driver to use for the execution of the tests.</p>
     * 
     * @return
     *   The web driver to use for the execution of the tests
     */
    protected WebDriver getWebDriver() {
        return this.environment.getWebDriver();
    }
    
    /**
     * <p>Returns the window manager for the used web driver, which can be used to open new windows and switching
     *   between multiple windows.</p>
     * 
     * @return
     *   The window manager for the used web driver.
     */
    protected WindowManager getWindowManager() {
        return this.environment.getWindowManager();
    }
        
    /**
     * <p>Captures a screen shot and saves the picture as an PNG file under the given file name. The complete path to
     *   the stored picture consists of the screenshot folder, as defined in the test environment, and the given
     *   filename with "png" as file extension.</p>
     * 
     * <p>If the used web driver does not support the capturing of screenshots an alternative picture is used instead
     *   of the screenshot.</p>
     * 
     * @param filename
     *   The file name under which the screenshot should be saved.
     * @throws IOException
     *   if an I/O error occurs.
     */
    protected void captureScreenshots() {
        File screenshotFolder = this.environment.getScreenshotFolder();
        if (screenshotFolder != null) {
            this.environment.getWindowManager().forEachOpenedWindow(window -> {
                final String filename = UUID.randomUUID().toString();
                
                WebDriver driver = window.getWebDriver();
                if (RemoteWebDriver.class.equals(driver.getClass())) {
                    driver = new Augmenter().augment(driver);
                }
                InputStream source = getScreenshotNotSupportedImage();
                if (driver instanceof TakesScreenshot) {
                    source = new ByteArrayInputStream(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
                }
                try {
                    File destinationDir = new File(screenshotFolder, getClass().getName());
                    destinationDir.mkdirs();
                    File destination = new File(destinationDir, filename + SCREENSHOT_FILE_EXTENSION); //$NON-NLS-1$
                    Path path = destination.toPath();
                    Files.copy(source, path, StandardCopyOption.REPLACE_EXISTING);
                    // ATTENTION: Do not remove this line because it is needed for the JUnit Attachment Plugin!
                    System.out.println(String.format(ATTACHMENT_FORMAT, destination.getCanonicalFile().toURI()));
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
    }
    
    private InputStream getScreenshotNotSupportedImage() {
        return AbstractSeleniumTest.class.getResourceAsStream(NOT_SUPPORTED_IMAGE);
    }
}
