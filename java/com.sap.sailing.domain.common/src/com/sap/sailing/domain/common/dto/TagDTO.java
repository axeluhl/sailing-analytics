package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sse.common.TimePoint;

/**
 * Used to send tags over network. Allows to create tags with all possible combinations of states (private/public and
 * valid/revoked).
 */
public class TagDTO implements Serializable {

    private static final long serialVersionUID = 3907411584518452300L;

    private String tag, comment, imageURL, username;
    private boolean visibleForPublic;
    private TimePoint raceTimepoint, createdAt, revokedAt;

    public static final int MAX_TAG_LENGTH = 100;
    public static final int MAX_COMMENT_LENGTH = 400;

    /**
     * Required by GWT, do <b>NOT</b> use this constructor! Use the other constructors instead to allow information
     * transfer.
     */
    TagDTO() {
    }

    /**
     * Creates tag which is not revoked and therefor valid.
     * 
     * @param tag
     *            title of tag
     * @param comment
     *            comment of tag, may be <code>null</code> as comment is optional
     * @param imageURL
     *            image URL of tag, may be <code>null</code> as image is optional
     * @param username
     *            name of user who created the tag
     * @param visibleForPublic
     *            should be <code>true</code> if everbidy should see this tag, otherwise <code>false</code>
     * @param raceTimepoint
     *            timepoint of race where tag ot created
     * @param createdAt
     *            timepoint where <code>username</code> created the tag
     */
    public TagDTO(String tag, String comment, String imageURL, String username, boolean visibleForPublic,
            TimePoint raceTimepoint, TimePoint createdAt) {
        this(tag, comment, imageURL, username, visibleForPublic, raceTimepoint, createdAt, null);
    }

    /**
     * Creates tag which may be revoked.
     * 
     * @param tag
     *            title of tag
     * @param comment
     *            comment of tag, may be <code>null</code> as comment is optional
     * @param imageURL
     *            image URL of tag, may be <code>null</code> as image is optional
     * @param username
     *            name of user who created the tag
     * @param visibleForPublic
     *            should be <code>true</code> if everbidy should see this tag, otherwise <code>false</code>
     * @param raceTimepoint
     *            timepoint of race where tag ot created
     * @param createdAt
     *            timepoint where <code>username</code> created the tag
     * @param revokedAt
     *            timepoint where tag got revoked, may be <code>null</code> if tag is not revoked
     */
    public TagDTO(String tag, String comment, String imageURL, String username, boolean visibleForPublic,
            TimePoint raceTimepoint, TimePoint createdAt, TimePoint revokedAt) {
        this.tag = tag;
        this.comment = comment;
        this.imageURL = imageURL;
        this.username = username;
        this.raceTimepoint = raceTimepoint;
        this.createdAt = createdAt;
        this.visibleForPublic = visibleForPublic;
        this.revokedAt = revokedAt;
    }

    /**
     * Returns title of tag.
     * 
     * @return title of tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Returns optional comment of tag.
     * 
     * @return comment of tag, may be <code>null</code>
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns optional image URL of tag.
     * 
     * @return image URL of tag, may be <code>null</code>
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * Returns name of user who created this tag.
     * 
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns visibilty of tag.
     * 
     * @return <code>true</code> if everybody should see the tag, otherwise <code>false</code>
     */
    public boolean isVisibleForPublic() {
        return visibleForPublic;
    }

    /**
     * Returns logical timepoint of race where tag was created.
     * 
     * @return logical timepoint
     */
    public TimePoint getRaceTimepoint() {
        return raceTimepoint;
    }

    /**
     * Returns creation timestamp of tag.
     * 
     * @return creation timestamp
     */
    public TimePoint getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns revokaction timestamp if tag is already revoked.
     * 
     * @return timepoint if tag is already revoked, otherwise <code>null</code>
     */
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

    /**
     * Compares {@link TagDTO} with given attributes.
     * 
     * @return <code>true</code> if all parameters match the attributes of {@link TagDTO}, otherwise <code>false</code>
     */
    public boolean equals(String tag, String comment, String imageURL, String username, boolean visibleForPublic,
            TimePoint raceTimepoint) {
        if (this.comment == null) {
            if (comment != null)
                return false;
        } else if (!this.comment.equals(comment))
            return false;
        if (this.imageURL == null) {
            if (imageURL != null)
                return false;
        } else if (!this.imageURL.equals(imageURL))
            return false;
        if (this.raceTimepoint == null) {
            if (raceTimepoint != null)
                return false;
        } else if (!this.raceTimepoint.equals(raceTimepoint))
            return false;
        if (this.tag == null) {
            if (tag != null)
                return false;
        } else if (!this.tag.equals(tag))
            return false;
        if (this.username == null) {
            if (username != null)
                return false;
        } else if (!this.username.equals(username)) {
            return false;
        }
        if (isVisibleForPublic() != visibleForPublic) {
            return false;
        }
        if (this.raceTimepoint == null) {
            if (raceTimepoint != null)
                return false;
        } else if (!this.raceTimepoint.equals(raceTimepoint)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TagDTO [tag=" + tag + ", comment=" + comment + ", imageURL=" + imageURL + ", username=" + username
                + ", raceTimepoint=" + raceTimepoint + ", createdAt=" + createdAt + ", isPublic=" + visibleForPublic
                + ", revokedAt=" + revokedAt + "]";
    }
}
