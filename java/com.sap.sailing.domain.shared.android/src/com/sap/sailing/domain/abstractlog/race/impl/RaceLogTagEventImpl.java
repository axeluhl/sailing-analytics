package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogTagEventImpl extends RaceLogEventImpl implements RaceLogTagEvent{


    private static final long serialVersionUID = 7213518902555323432L;
    
    private String tag, comment, imageURL, userName;
    

    public RaceLogTagEventImpl(String pTag, String pUserName, String pComment, String pImageURL,TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId) {
        super(createdAt, logicalTimePoint, author, pId, pPassId);
        tag = pTag;
        comment = pComment;
        imageURL = pImageURL;
        userName = pUserName;
    }
    
    public RaceLogTagEventImpl(String pTag, String pUserName, String pComment, String pImageURL, TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId) {
        this(pTag, pUserName, pComment, pImageURL, now(), logicalTimePoint, author, randId(), pPassId);
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
    public String getImageURL() {
        return imageURL;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getUserName() {
        return userName;
    }
}