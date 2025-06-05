package com.sap.sailing.selenium.core;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.sap.sailing.selenium.core.TestEnvironmentConfiguration.DriverDefinition;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

/**
 * Can resolve a parameter of type {@link TestEnvironment} to an environment that uses the given
 * {@link DriverDefinition} to create a Selenium WebDriver instance. It can be returned as a additional extension by the
 * {@link SeleniumTestInvocationProvider}, one per web driver discovered in the {@link TestEnvironmentConfiguration}.
 * <p>
 * 
 * Furthermore, being a {@link BeforeEachCallback}, it will create the environment, and if the test being executed is an
 * instance of {@link AbstractSeleniumTest}, sets the environment on the test instance using its
 * {@link AbstractSeleniumTest#setEnvironment(TestEnvironment)} method.
 * 
 * @author Axel Uhl (d043530)
 *
 */
class SeleniumParameterResolver implements ParameterResolver, BeforeEachCallback {
    private final DriverDefinition driverDef;
    private TestEnvironment environment;

    SeleniumParameterResolver(DriverDefinition driverDef) {
        this.driverDef = driverDef;
    }

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return pc.getParameter().getType() == TestEnvironment.class;
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        return environment;
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        environment = TestEnvironmentFactory.create(driverDef);
        if (testInstance instanceof AbstractSeleniumTest) {
            ((AbstractSeleniumTest) testInstance).setEnvironment(environment);
        }
    }
}
