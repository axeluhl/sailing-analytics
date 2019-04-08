package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TracTracConfigurationDTO implements IsSerializable {
    public String name;
    public String jsonURL;
    public String liveDataURI;
    public String storedDataURI;
    public String courseDesignUpdateURI;
    public String tractracUsername;
    public String tractracPassword;
    
    public TracTracConfigurationDTO() {}

    public TracTracConfigurationDTO(String name, String jsonURL, String liveDataURI, String storedDataURI, String courseDesignUpdateUrl, 
            String tractracUsername, String tractracPassword) {
        super();
        this.name = name;
        this.jsonURL = jsonURL;
        this.liveDataURI = liveDataURI;
        this.storedDataURI = storedDataURI;
        this.courseDesignUpdateURI = courseDesignUpdateUrl;
        this.tractracUsername = tractracUsername;
        this.tractracPassword = tractracPassword;
    }
    
}
