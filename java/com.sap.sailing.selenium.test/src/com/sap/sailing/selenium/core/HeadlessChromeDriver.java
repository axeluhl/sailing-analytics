package com.sap.sailing.selenium.core;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 *Specific {@link ChromeDriver} that is configured to start Chrome in headless mode. In theory, you do not need a
 * specific subclass of {@link ChromeDriver} but we currently can't apply specific command line options using XML based
 * configuration using {@link TestEnvironmentConfiguration}. So this is currently just a simple workaround but not a
 * long-term solution.
 */
public class HeadlessChromeDriver extends ChromeDriver {

    public HeadlessChromeDriver(Capabilities capabilities) {
        super(ChromeDriverService.createDefaultService(), constructChromeOptions(capabilities));
    }

    private static ChromeOptions constructChromeOptions(Capabilities capabilities) {
        final ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.merge(capabilities);
        chromeOptions.addArguments("--headless", "--window-size=1440,900");
        return chromeOptions;
    }
}
