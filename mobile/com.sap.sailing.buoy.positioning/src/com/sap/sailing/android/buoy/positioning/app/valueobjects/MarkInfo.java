package com.sap.sailing.android.buoy.positioning.app.valueobjects;

import java.io.Serializable;

import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.common.MarkType;

public class MarkInfo extends MarkImpl {
    private static final long serialVersionUID = 6139266956501048794L;
    
    private String className;
    private String checkinDigest;

    public static MarkInfo create(MarkImpl mark) {
        return new MarkInfo(mark.getId(), mark.getName(), mark.getType(), mark.getColor(), mark.getShape(), mark.getPattern());
    }

    public MarkInfo(String name) {
        super(name);
    }

    public MarkInfo(Serializable id, String name) {
        super(id, name);
    }

    public MarkInfo(Serializable id, String name, MarkType type, String color, String shape, String pattern) {
        super(id, name, type, color, shape, pattern);
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

        MarkInfo markInfo = (MarkInfo) o;

        if(!super.equals(markInfo)) {
            return false;
        }

        if (!className.equals(markInfo.className))
            return false;
        return checkinDigest.equals(markInfo.checkinDigest);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + className.hashCode();
        result = 31 * result + checkinDigest.hashCode();
        return result;
    }
}
