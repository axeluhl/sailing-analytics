package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.net.URL;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogTagEventImpl extends RaceLogEventImpl implements RaceLogTagEvent{


    private static final long serialVersionUID = 7213518902555323432L;
    
    private String tag, comment;
    private URL imageURL;
    

    public RaceLogTagEventImpl(String pTag, String pComment, URL pImageURL,TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId) {
        super(createdAt, logicalTimePoint, author, pId, pPassId);
        tag = pTag;
        comment = pComment;
        imageURL = pImageURL;
    }
    
    public RaceLogTagEventImpl(String pTag, String pComment, URL pImageURL, TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId) {
        this(pTag, pComment, pImageURL, now(), logicalTimePoint, author, randId(), pPassId);
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public TimePoint getTimePoint() {
        return getLogicalTimePoint();
    }

    @Override
    public URL getImageURL() {
        return imageURL;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}