package com.sap.sailing.selenium.core;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.stream.StreamSource;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestEnvironmentConfiguration {
    protected static class DriverDefinition {
        private String driver;
        private Map<String, String> capabilities;
        
        public DriverDefinition(String driver, Map<String, String> capabilities) {
            this.driver = driver;
            this.capabilities = capabilities;
        }
        
        public String getDriver() {
            return this.driver;
        }
        
        public Map<String, String> getCapabilities() {
            return this.capabilities;
        }
    }
    
    public static final String TEST_ENVIRONMENT_CONFIGURATION = "selenium.test.environment.configuration";
    
    private static final String TEST_ENVIRONMENT_SCHEMA = "test-environment-configuration.xsd";
    
    private static final String NAMESPACE_URI = "http://www.sapsailing.com/test-environment";
    
    private static final String CONTEXT_ROOT = "context-root";
    
    private static final String SYSTEM_PROPERTIES = "system-properties";
    
    private static final String SYSTEM_PROPERTY = "system-property";
    
    private static final String DRIVER_DEFINITION = "driver-definition";
    
    private static final String DRIVER_CLASS = "class";
    
    private static final String DRIVER_CAPABILITIES = "capabilities";
    
    private static final String DRIVER_CAPABILITY = "capability";
    
    private static final String PARAMETER_NAME = "name";
    
    private static final String PARAMETER_VALUE = "value";
    
    private static TestEnvironmentConfiguration instance;
    
    public static TestEnvironmentConfiguration getInstance() {
        if(instance == null) {
            try {
                instance = createTestEnvironmentConfiguration();
            } catch(Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        
        return instance;
    }
    
    private static synchronized TestEnvironmentConfiguration createTestEnvironmentConfiguration() throws
            ParserConfigurationException, SAXException, IOException {
        if(instance != null)
            return instance;
        
        Document document = readTestConfiguration();
        
        Element testEnvironmentNode = document.getDocumentElement();
        testEnvironmentNode.normalize();
                
        String contextRoot = XMLHelper.getContentTextNS(testEnvironmentNode, CONTEXT_ROOT, NAMESPACE_URI);
        Map<String, String> systemProperties = createSystemProperties(testEnvironmentNode);
        List<DriverDefinition> driverDefenitions = createDriverDefenitions(testEnvironmentNode);
        
        return new TestEnvironmentConfiguration(contextRoot, systemProperties, driverDefenitions);
    }
    
    private static synchronized Document readTestConfiguration() throws ParserConfigurationException,
            SAXException, IOException {
        String path = System.getProperty(TEST_ENVIRONMENT_CONFIGURATION);
        
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new StreamSource(TEST_ENVIRONMENT_SCHEMA));
        
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setSchema(schema);
        
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        InputSource source = new InputSource(new FileInputStream(path));
        
        return builder.parse(source);
    }
    
    private static Map<String, String> createSystemProperties(Element testEnvironmentNode) {
        Element systemPropertiesNode = XMLHelper.getElementNS(testEnvironmentNode, SYSTEM_PROPERTIES, NAMESPACE_URI);
        
        if(systemPropertiesNode == null)
            return Collections.EMPTY_MAP;
        
        Map<String, String> properties = new HashMap<>();
        
        List<Element> systemPropertyNodes = XMLHelper.getElementsNS(systemPropertiesNode, SYSTEM_PROPERTY,
                NAMESPACE_URI);
        
        for(Element systemPropertyNode : systemPropertyNodes) {
            String propertyName = XMLHelper.getContentTextNS(systemPropertyNode, PARAMETER_NAME, NAMESPACE_URI);
            String propertyValue = XMLHelper.getContentTextNS(systemPropertyNode, PARAMETER_VALUE, NAMESPACE_URI);
            
            properties.put(propertyName, propertyValue);
        }
        
        return properties;
    }
    
    private static List<DriverDefinition> createDriverDefenitions(Element testEnvironmentNode) {
        List<DriverDefinition> defenitions = new LinkedList<>();
        
        List<Element> driverDefinitionNodes = XMLHelper.getElementsNS(testEnvironmentNode, DRIVER_DEFINITION,
                NAMESPACE_URI);
        
        for(Element driverDefinitionNode : driverDefinitionNodes) {
            defenitions.add(createDriverDefinition(driverDefinitionNode));
        }
                
        return defenitions;
    }
    
    private static DriverDefinition createDriverDefinition(Element driverDefinitionNode) {
        String driverClass = XMLHelper.getAttributeValueNS(driverDefinitionNode, DRIVER_CLASS, null);;
        
        Element capabilitiesNode = XMLHelper.getElementNS(driverDefinitionNode, DRIVER_CAPABILITIES, NAMESPACE_URI);
        
        if(capabilitiesNode == null)
            return new DriverDefinition(driverClass, Collections.EMPTY_MAP);
        
        Map<String, String> capabilities = new HashMap<>();
        
        for(Element capabilityNode : XMLHelper.getElementsNS(capabilitiesNode, DRIVER_CAPABILITY, NAMESPACE_URI)) {
            String capabilityName = XMLHelper.getContentTextNS(capabilityNode, PARAMETER_NAME, NAMESPACE_URI);
            String capabilityValue = XMLHelper.getContentTextNS(capabilityNode, PARAMETER_VALUE, NAMESPACE_URI);
            
            capabilities.put(capabilityName, capabilityValue);
        }
        
        return new DriverDefinition(driverClass, capabilities);
    }
    
    private String root;
    
    private Map<String, String> properties;
    
    private List<DriverDefinition> definitions;
    
    private TestEnvironmentConfiguration(String root, Map<String, String> properties, List<DriverDefinition> defenitions) {
        this.root = root;
        this.properties = properties;
        this.definitions = defenitions;
    }
    
    public String getContextRoot() {
        return this.root;
    }
    
    public Map<String, String> getSystemProperties() {
        return this.properties;
    }
    
    public List<DriverDefinition> getDriverDefinitions() {
        return this.definitions;
    }
}
