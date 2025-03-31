package com.sap.sailing.server.gateway.impl;

import com.sap.sailing.server.gateway.JsonServiceDescriptor;

public class JsonServiceDescriptorImpl implements JsonServiceDescriptor {
    private String name;
    private String contextPath;
    private String[] mandatoryParameterNames;
    private String[] optionalParameterNames;
    private String description;
    private String version;

    public JsonServiceDescriptorImpl(String name, String contextPath, String version) {
        this.name = name;
        this.contextPath = contextPath;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String[] getMandatoryParameterNames() {
        return mandatoryParameterNames;
    }

    public void setMandatoryParameterNames(String[] mandatoryParameterNames) {
        this.mandatoryParameterNames = mandatoryParameterNames;
    }

    public String[] getOptionalParameterNames() {
        return optionalParameterNames;
    }

    public void setOptionalParameterNames(String[] optionalParameterNames) {
        this.optionalParameterNames = optionalParameterNames;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
