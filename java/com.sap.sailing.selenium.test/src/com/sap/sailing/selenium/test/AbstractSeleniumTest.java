package com.sap.sailing.selenium.test;

import java.io.File;
import java.io.IOException;

import org.junit.runner.RunWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;

import com.google.common.io.Files;
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
    
    public void captureScreenshot() throws IOException {
        Augmenter augmenter = new Augmenter();
        WebDriver augmentedDriver = augmenter.augment(getWebDriver());
        File source = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
        System.out.println("Path for file is: " +  "'./target/screenshots/" + source.getName() + "'");
        String path = "./target/screenshots/" + source.getName();
        
        Files.copy(source, new File(path));
    }
}
