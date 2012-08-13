package com.sap.sailing.selenium.test;

import java.io.File;
import java.io.IOException;

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
        
        File source = null;
        
        // TODO: Provide a picture "Not Supported" if the driver is not able to take screenshots
        if(driver instanceof TakesScreenshot)
            source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        else
            source = null;
        
        // TODO: Use the right path
        String path = "./target/screenshots/" + source.getName();
        
        Files.copy(source.toPath(), new File(path).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
