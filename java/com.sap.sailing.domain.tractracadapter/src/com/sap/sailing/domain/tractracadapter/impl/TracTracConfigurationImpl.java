package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;

public class TracTracConfigurationImpl implements TracTracConfiguration {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String jsonURL;
    private final String liveDataURI;
    private final String storedDataURI;
    private final String courseDesignUpdateURI;
    private final String tracTracUsername;
    private final String tracTracPassword;
    private final String creatorName;
    private final String eventWebUrl;

    public TracTracConfigurationImpl(String creatorName, String name, String eventWebUrl, String jsonURL, String liveDataURI,
            String storedDataURI, String courseDesignUpdateURI,
            String tracTracUsername, String tracTracPassword) {
        this.creatorName = creatorName;
        this.name = name;
        this.eventWebUrl = eventWebUrl;
        this.jsonURL = jsonURL;
        this.liveDataURI = liveDataURI;
        this.storedDataURI = storedDataURI;
        this.courseDesignUpdateURI = courseDesignUpdateURI;
        this.tracTracUsername = tracTracUsername;
        this.tracTracPassword = tracTracPassword;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJSONURL() {
        return jsonURL;
    }

    @Override
    public String getLiveDataURI() {
        return liveDataURI;
    }

    @Override
    public String getStoredDataURI() {
        return storedDataURI;
    }

    @Override
    public String toString() {
        try {
            return getName()+": "+getJSONURL()+ " ("+getLiveDataURI()+", "+getStoredDataURI()+")";
        } catch (Exception e) {
            return "<Exception during TracTracConfiguration.toString(): "+e.getMessage()+">";
        }
    }

    @Override
    public String getCourseDesignUpdateURI() {
        return courseDesignUpdateURI;
    }

    @Override
    public String getTracTracUsername() {
        return tracTracUsername;
    }

    @Override
    public String getTracTracPassword() {
        return tracTracPassword;
    }

    @Override
    public String getCreatorName() {
        return creatorName;
    }

    @Override
    public String getEventWebUrl() {
        return eventWebUrl;
    }

}
