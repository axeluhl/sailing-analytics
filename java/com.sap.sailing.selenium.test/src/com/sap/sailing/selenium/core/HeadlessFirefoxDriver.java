package com.sap.sailing.selenium.core;

import java.util.Objects;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Specific {@link FirefoxDriver} that is configured to start Firefox in headless mode. In theory, you do not need a
 * specific subclass of {@link FirefoxDriver} but we currently can't apply specific command line options using XML based
 * configuration using {@link TestEnvironmentConfiguration}. So this is currently just a simple workaround but not a
 * long-term solution.
 */
public class HeadlessFirefoxDriver extends FirefoxDriver {

    public HeadlessFirefoxDriver(Capabilities desiredCapabilities) {
        super(constructFirefoxOptions(desiredCapabilities));
        
    }
    
    private static FirefoxOptions constructFirefoxOptions(Capabilities desiredCapabilities) {
        final FirefoxOptions firefoxOptions = new FirefoxOptions(Objects.requireNonNull(desiredCapabilities, "No capabilities seen"));
        FirefoxBinary firefoxBinary = new FirefoxBinary();
        firefoxBinary.addCommandLineOptions("-headless");
        firefoxOptions.setBinary(firefoxBinary);
        return firefoxOptions;
    }
}
