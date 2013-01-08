package com.sap.sailing.selenium.test;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Rule;

import org.junit.rules.TestWatchman;

import org.junit.runner.RunWith;

import org.junit.runners.model.FrameworkMethod;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.sap.sailing.selenium.core.Managed;
import com.sap.sailing.selenium.core.Selenium;
import com.sap.sailing.selenium.core.TestEnvironment;

@RunWith(Selenium.class)
public abstract class AbstractSeleniumTest {
    private static final String NOT_SUPPORTED_IMAGE = "/com/sap/sailing/selenium/resources/not-supported.png"; //$NON-NLS-1$
    
    private static final String ATTACHMENT_FORMAT = "[[ATTACHMENT|%s]]"; //$NON-NLS-1$
    
    private class ScreenShotRule extends TestWatchman {
        @Override
        public void failed(Throwable cause, FrameworkMethod method) {
            try {
                captureScreenshot(method.getName());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
    
    //@Rule
    //public ScreenShotRule takeScreenshoot = new ScreenShotRule();

    @Managed
    protected TestEnvironment environment;
    
    protected String getContextRoot() {
        return this.environment.getContextRoot();
    }
    
    protected WebDriver getWebDriver() {
        return this.environment.getWebDriver();
    }
        
    protected void captureScreenshot(String filename) throws URISyntaxException, IOException {
        URL screenshotFolder = this.environment.getScreenshotFolder();
        
        if(screenshotFolder == null)
            return;
        
        WebDriver driver = getWebDriver();
        
        if(RemoteWebDriver.class.equals(driver.getClass())) {
            driver = new Augmenter().augment(driver);
        }
        
        File source = getScreenshotNotSupportedImage();
        
        if(driver instanceof TakesScreenshot)
            source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        
        URL destination = new URL(screenshotFolder, filename + ".png"); //$NON-NLS-1$
        
        Files.copy(source.toPath(), new File(destination.toURI()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        // ATTENTION: Do not remove this line because it is needed for the JUnit Attachment Plugin!
        System.out.println(String.format(ATTACHMENT_FORMAT, destination));
    }
    
    private File getScreenshotNotSupportedImage() {
        try {
            //BundleContext context = SeleniumTestsActivator.getContext();
            //Bundle bundle = context.getBundle();
            //URL pictureURL = bundle.getResource("/com/sap/sailing/selenium/resources/not-supported.png");
            URL pictureURL = AbstractSeleniumTest.class.getResource(NOT_SUPPORTED_IMAGE);
            URI pictureURI = pictureURL.toURI();
            
            return new File(pictureURI);
        } catch(URISyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }
}
