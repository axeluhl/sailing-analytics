package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.common.impl.Util.Pair;

public class EventMasterData {

    private String id;
    private String name;
    private String venueName;
    private String pubUrl;
    private Iterable<Pair<String, String>> courseAreas;
    private boolean isPublic;

    public EventMasterData(String id, String name, String venueName, String pubUrl, Iterable<Pair<String, String>> courseAreas, boolean isPublic) {
        this.id = id;
        this.name = name;
        this.venueName = venueName;
        this.pubUrl = pubUrl;
        this.courseAreas = courseAreas;
        this.isPublic = isPublic;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getPubUrl() {
        return pubUrl;
    }

    public Iterable<Pair<String, String>> getCourseAreas() {
        return courseAreas;
    }

    public boolean isPublic() {
        return isPublic;
    }
    
    

}
