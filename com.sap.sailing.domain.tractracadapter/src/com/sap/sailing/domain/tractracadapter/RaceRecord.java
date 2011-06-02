package com.sap.sailing.domain.tractracadapter;

import java.net.MalformedURLException;
import java.net.URL;

public class RaceRecord {
    private final String name;
    private final String replayURL;
    private final String ID;
    private final URL paramURL;
    
    public RaceRecord(URL jsonURL, String name, String replayURL, String ID) throws MalformedURLException {
        super();
        this.name = name;
        this.replayURL = replayURL;
        this.ID = ID;
        
        String jsonURLAsString = jsonURL.toString();
        int indexOfLastSlash = jsonURLAsString.lastIndexOf('/');
        int indexOfLastButOneSlash = jsonURLAsString.lastIndexOf('/', indexOfLastSlash-1);
        String eventName = jsonURLAsString.substring(indexOfLastButOneSlash+1, indexOfLastSlash);
        paramURL = new URL(jsonURLAsString.substring(0, indexOfLastSlash)+"clientparams.php?event="+
                eventName+"&race="+ID);
    }

    public String getName() {
        return name;
    }

    public String getReplayURL() {
        return replayURL;
    }

    public String getID() {
        return ID;
    }

    public URL getParamURL() {
        return paramURL;
    }
    
}

