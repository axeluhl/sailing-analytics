package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;

public class TagDTO implements IsSerializable {

    private String tag, comment, imageURL, username;
    private boolean visibleForPublic;
    private TimePoint raceTimepoint, createdAt, revokedAt;
    
    public static final int MAX_TAG_LENGTH = 100;
    public static final int MAX_IMAGE_URL_LENGTH = 200;
    public static final int MAX_COMMENT_LENGTH = 400;

    // for GWT
    public TagDTO() {}

    public TagDTO(String tag, String comment, String imageURL, String username, boolean visibleForPublic, TimePoint raceTimepoint, TimePoint createdAt) {
        this(tag, comment, imageURL, username, visibleForPublic, raceTimepoint, createdAt, null);
    }
    
    public TagDTO(String tag, String comment, String imageURL, String username, boolean visibleForPublic, TimePoint raceTimepoint,
            TimePoint createdAt, TimePoint revokedAt) {
        this.tag = tag;
        this.comment = comment;
        this.imageURL = imageURL;
        this.username = username;
        this.raceTimepoint = raceTimepoint;
        this.createdAt = createdAt;
        this.visibleForPublic = visibleForPublic;
        this.revokedAt = revokedAt;
    }

    public String getTag() {
        return tag;
    }

    public String getComment() {
        return comment;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getUsername() {
        return username;
    }
    
    public boolean isVisibleForPublic() {
        return visibleForPublic;
    }

    public TimePoint getRaceTimepoint() {
        return raceTimepoint;
    }

    public TimePoint getCreatedAt() {
        return createdAt;
    }

    public TimePoint getRevokedAt() {
        return revokedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TagDTO other = (TagDTO) obj;
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
        if (raceTimepoint == null) {
            if (other.raceTimepoint != null)
                return false;
        } else if (!raceTimepoint.equals(other.raceTimepoint))
            return false;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username)) {
            return false;
        }
        if (isVisibleForPublic() != other.isVisibleForPublic()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TagDTO [tag=" + tag
                + ", comment=" + comment
                + ", imageURL=" + imageURL
                + ", username=" + username
                + ", raceTimepoint=" + raceTimepoint
                + ", createdAt=" + createdAt
                + ", isPublic=" + visibleForPublic
                + ", revokedAt=" + revokedAt + "]";
    }
}