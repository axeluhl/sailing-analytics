package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogTagEventImpl extends RaceLogEventImpl implements RaceLogTagEvent {

    private static final long serialVersionUID = 7213518902555323432L;

    private final String tag, comment, imageURL, username;
    private TimePoint revokedAt;

    public RaceLogTagEventImpl(String tag, String comment, String imageURL, TimePoint createdAt,
            TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable id, int passId) {
        super(createdAt, logicalTimePoint, author, id, passId);
        this.tag = tag;
        this.comment = comment;
        this.imageURL = imageURL;
        username = author.getName();
        revokedAt = null;
    }

    public RaceLogTagEventImpl(String tag, String comment, String imageURL, TimePoint createdAt,
            TimePoint logicalTimePoint, AbstractLogEventAuthor author, int passId) {
        this(tag, comment, imageURL, createdAt, logicalTimePoint, author, randId(), passId);
    }

    public RaceLogTagEventImpl(String tag, String comment, String imageURL, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int passId) {
        this(tag, comment, imageURL, now(), logicalTimePoint, author, randId(), passId);
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
    public String getUsername() {
        return username;
    }

    /**
     * Only sets revokedAt if tag is not already revoked and given TimePoint is not null.
     */
    @Override
    public void markAsRevoked(TimePoint revokedAt) {
        if (this.revokedAt == null && revokedAt != null) {
            this.revokedAt = revokedAt;
        }
    }

    @Override
    public TimePoint getRevokedAt() {
        return revokedAt;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getShortInfo() {
        return "tag=" + tag + ", comment=" + comment;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RaceLogTagEventImpl other = (RaceLogTagEventImpl) obj;
        if (comment == null) {
            if (other.comment != null)
                return false;
        } else if (!comment.equals(other.comment))
            return false;
        if (imageURL == null) {
            if (other.imageURL != null)
                return false;
        } else if (!imageURL.equals(other.imageURL))
            return false;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        if (getLogicalTimePoint() == null) {
            if (other.getLogicalTimePoint() != null)
                return false;
        } else if (!getLogicalTimePoint().equals(other.getLogicalTimePoint()))
            return false;
        if (getCreatedAt() == null) {
            if (other.getCreatedAt() != null)
                return false;
        } else if (!getCreatedAt().equals(other.getCreatedAt()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RaceLogTagEvent [tag=" + tag + ", comment=" + comment + ", imageURL=" + imageURL + ", username="
                + username +  ", revokedAt=" + revokedAt + "]";
    }
}