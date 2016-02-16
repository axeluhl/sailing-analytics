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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MarkInfo info = (MarkInfo) o;

        if (markId != null ? !markId.equals(info.markId) : info.markId != null)
            return false;
        if (name != null ? !name.equals(info.name) : info.name != null)
            return false;
        if (type != null ? !type.equals(info.type) : info.type != null)
            return false;
        if (className != null ? !className.equals(info.className) : info.className != null)
            return false;
        return !(checkinDigest != null ? !checkinDigest.equals(info.checkinDigest) : info.checkinDigest != null);

    }

    @Override
    public int hashCode() {
        int result = markId != null ? markId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (className != null ? className.hashCode() : 0);
        result = 31 * result + (checkinDigest != null ? checkinDigest.hashCode() : 0);
        return result;
    }
}
