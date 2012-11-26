package com.sap.sailing.selenium.core;

import java.lang.reflect.Constructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.junit.runner.Description;
import org.junit.runner.Runner;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.sap.sailing.selenium.core.TestEnvironmentConfiguration.DriverDefinition;
import com.sap.sailing.selenium.core.impl.TestEnvironmentImpl;

public class Selenium extends ParentRunner<Runner> {
    private final List<Runner> children;
    
    public Selenium(Class<?> klass) throws InitializationError {
        super(klass);
        
        this.children = new LinkedList<>();
        
        initializeRunners();
    }

    @Override
    protected List<Runner> getChildren() {
        return this.children;
    }

    @Override
    protected Description describeChild(Runner child) {
        // TODO: Provide a better description (including the context root and the driver defenition)
        return child.getDescription();
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier);
    }
    
    @Override
    protected void collectInitializationErrors(java.util.List<java.lang.Throwable> errors) {
        super.collectInitializationErrors(errors);
        
        // QUESTION: Do we need addional checks here?
    }
    
    private void initializeRunners() throws InitializationError {
        try {
            TestEnvironmentConfiguration configuration = TestEnvironmentConfiguration.getInstance();
            String contextRoot = configuration.getContextRoot();
            Map<String, String> systemProperties = configuration.getSystemProperties();
            
            for(Entry<String, String> property : systemProperties.entrySet()) {
                System.setProperty(property.getKey(), property.getValue());
            }
            
            TestClass test = getTestClass();
            
            for(DriverDefinition defenition : configuration.getDriverDefinitions()) {
                this.children.add(new SeleniumJUnit4ClassRunner(test.getJavaClass(), contextRoot, defenition));
            }
        } catch (Exception exception) {
            throw new InitializationError(exception);
        }
    }
    
    private class SeleniumJUnit4ClassRunner extends BlockJUnit4ClassRunner {
        private String root;
        private DriverDefinition definition;
        
        private TestEnvironmentImpl environment;
        
        public SeleniumJUnit4ClassRunner(Class<?> klass, String root, DriverDefinition definition)
                throws InitializationError {
            super(klass);
            
            this.root = root;
            this.definition = definition;
        }

        /**
         * Create the test object and inject Selenium server or web driver into
         * fields that were annotated with {@code annotationType}.
         * 
         * @return The test object.
         * @throws Exception
         *             If there was an error creating the test object.
         */
        @Override
        protected Object createTest() throws Exception {
            final Object test = super.createTest();
            final TestClass testClass = getTestClass();
            
            List<FrameworkField> fields = testClass.getAnnotatedFields(Managed.class);
            
            for (final FrameworkField field : fields) {
                field.getField().setAccessible(true);
                field.getField().set(test, this.environment);
            }
                
            return test;
        }
        
        @Override
        public void run(final RunNotifier notifier) {
            try {
                this.environment = createTestEnvironment();
                
                try {
                    super.run(notifier);
                } finally {
                    this.environment.close();
                }
            } catch (Exception exception) {
                notifier.fireTestFailure(new Failure(getDescription(), exception));
            }
        }
        
        @Override
        protected void collectInitializationErrors(java.util.List<java.lang.Throwable> errors) {
            super.collectInitializationErrors(errors);
            
            // QUESTION: Do we need addional checks here?
        }
        
        private TestEnvironmentImpl createTestEnvironment() throws Exception {
            try {
                String driverClassname = this.definition.getDriver();
                Map<String, String> capabilityDefenitions = this.definition.getCapabilities();
                
                Class<WebDriver> clazz = (Class<WebDriver>) Class.forName(driverClassname);
                Capabilities capabilities = new DesiredCapabilities(capabilityDefenitions);
                
                Constructor<WebDriver> constructor = clazz.getConstructor(Capabilities.class);
                            
                return new TestEnvironmentImpl(constructor.newInstance(capabilities), this.root);
            } catch(Exception exception) {
                throw exception;
            }
        }
    }
}
