package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TracTracConfigurationDAO implements IsSerializable {
    public String name;
    public String jsonURL;
    public String liveDataURI;
    public String storedDataURI;
    
    public TracTracConfigurationDAO() {}

    public TracTracConfigurationDAO(String name, String jsonURL, String liveDataURI, String storedDataURI) {
        super();
        this.name = name;
        this.jsonURL = jsonURL;
        this.liveDataURI = liveDataURI;
        this.storedDataURI = storedDataURI;
    }
    
}
