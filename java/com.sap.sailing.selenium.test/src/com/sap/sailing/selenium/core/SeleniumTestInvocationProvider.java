package com.sap.sailing.selenium.core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import com.sap.sailing.selenium.core.TestEnvironmentConfiguration.DriverDefinition;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

/**
 * Used to extend the {@link SeleniumTestTemplate} annotation which in turn is used to extend the
 * base class of all Selenium tests, {@link AbstractSeleniumTest}. This provider produces
 * text invocation contexts, one for each {@link TestEnvironmentConfiguration#getDriverDefinitions() driver definition}
 * found in the test environment configuration.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SeleniumTestInvocationProvider implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        TestEnvironmentConfiguration config = TestEnvironmentConfiguration.getInstance();
        return config.getDriverDefinitions().stream().map(driverDef -> {
            return new SeleniumTestContext(driverDef);
        });
    }

    static class SeleniumTestContext implements TestTemplateInvocationContext {
        private final DriverDefinition driverDefinition;

        SeleniumTestContext(DriverDefinition driverDefinition) {
            this.driverDefinition = driverDefinition;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return "Selenium Test - " + driverDefinition.getDriver();
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return Arrays.asList(new SeleniumParameterResolver(driverDefinition));
        }
    }
}