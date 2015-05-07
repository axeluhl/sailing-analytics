package com.sap.sailing.android.buoy.positioning.app.valueobjects;

public class MarkInfo {

    private String markId;
    private String name;
    private String type;
    private String className;
    private String checkinDigest;

    public String getId() {
        return markId;
    }

    public void setId(String id) {
        this.markId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCheckinDigest() {
        return checkinDigest;
    }

    public void setCheckinDigest(String checkinDigest) {
        this.checkinDigest = checkinDigest;
    }

}
