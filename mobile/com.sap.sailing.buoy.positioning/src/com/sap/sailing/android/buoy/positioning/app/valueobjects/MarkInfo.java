package com.sap.sailing.android.buoy.positioning.app.valueobjects;

import java.io.Serializable;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;

public class MarkInfo extends MarkImpl {
    private static final long serialVersionUID = 6139266956501048794L;
    
    private final String className;
    private final String checkinDigest;

    public static MarkInfo create(Mark mark, String classname, String checkinDigest) {
        return new MarkInfo(mark.getId(), mark.getName(), mark.getType(), mark.getColor(), mark.getShape(), mark.getPattern(), classname, checkinDigest);
    }

    public MarkInfo(String name) {
        super(name);
        className = null;
        checkinDigest = null;
    }

    public MarkInfo(Serializable id, String name, String classname, String checkinDigest) {
        super(id, name);
        this.className = classname;
        this.checkinDigest = checkinDigest;
    }

    public MarkInfo(Serializable id, String name, MarkType type, Color color, String shape, String pattern, String classname, String checkinDigest) {
        super(id, name, type, color, shape, pattern);
        this.className = classname;
        this.checkinDigest = checkinDigest;
    }

    public String getClassName() {
        return className;
    }

    public String getCheckinDigest() {
        return checkinDigest;
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
