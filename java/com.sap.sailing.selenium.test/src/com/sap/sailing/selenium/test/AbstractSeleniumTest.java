package com.sap.sailing.selenium.test;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.runner.RunWith;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.sap.sailing.selenium.test.core.Managed;
import com.sap.sailing.selenium.test.core.Selenium;
import com.sap.sailing.selenium.test.core.TestEnvironment;

@RunWith(Selenium.class)
public abstract class AbstractSeleniumTest {
    protected static final String GWT_DEBUG_PREFIX = "gwt-debug-";
    
    @Managed
    protected TestEnvironment environment;
    
    protected String getContextRoot() {
        return this.environment.getContextRoot();
    }
    
    protected WebDriver getWebDriver() {
        return this.environment.getWebDriver();
    }
    
    protected void captureScreenshot() throws IOException {
        WebDriver driver = getWebDriver();
        
        if(driver instanceof RemoteWebDriver) {
            driver = new Augmenter().augment(driver);
        }
        
        File source = getScreenshotNotSupportedImage();
        
        if(driver instanceof TakesScreenshot)
            source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        
        // TODO: Use the right path
        String path = "./target/screenshots/" + source.getName();
        
        Files.copy(source.toPath(), new File(path).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    
    private File getScreenshotNotSupportedImage() {
        try {
            URL pictureURL = AbstractSeleniumTest.class.getResource("not-supported.png");
            URI pictureURI = pictureURL.toURI();
            
            return new File(pictureURI);
        } catch(URISyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }
}
