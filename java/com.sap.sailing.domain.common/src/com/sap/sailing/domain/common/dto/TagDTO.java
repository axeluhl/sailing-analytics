package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sse.common.TimePoint;

/**
 * Tags are used to link input of users in form of "tags" (extended comments) to a specific race. These tags consists of
 * {@link #tag title}, optional {@link #comment}, optional {@link imageURL image URL} and a {@link #visibleForPublic
 * visibility status}. They can be set to be visible only for the author or visible for everybody. Private tags will be
 * stored in the user store, public tags will be stored as event in the racelog. Not every user has write access to the
 * racelog therefor the default visibility should be private. The {@link com.sap.sailing.server.tagging.TaggingService
 * TaggingService} is able to perform all CRUD operations on tags and also checks for permissions.<br/>
 * <br/>
 * Tags should be unique for the key consisting of: {@link #tag title}, {@link #comment}, {@link imageURL image URL},
 * {@link #visibleForPublic visibility status}, {@link #username} and {@link #raceTimepoint}. <code>NOT</code> part of
 * the key are {@link #createdAt} and {@link #revokedAt}.
 */
public class TagDTO implements Serializable {

    private static final long serialVersionUID = 3907411584518452300L;

    private String tag, comment, imageURL, username;
    private boolean visibleForPublic;
    /**
     * By default every tag should have set the attributes {@link #raceTimepoint} and {@link #createdAt}.<br/>
     * <code>raceTimepoint</code>: absolute timepoint of race when the tag got created<br/>
     * <code>createdAt</code>: actual absolute creation timepoint of tag<br/>
     * <br/>
     * The racelog does not allow to delete events. Instead these events can only be revoked which is implemented as a
     * new revocation event. This applies also for tags so it is not possible to "delete" public tags. To mark a tag as
     * revoked, set the attribute {@link #revokedAt} to a value which does not equal <code>null</code>. So every tag
     * where {@link #revokedAt} equals <code>null</code> can be seen as valid and <code>NOT</code> revoked.
     */
    private TimePoint raceTimepoint, createdAt, revokedAt;

    /**
     * Required by GWT, do <b>NOT</b> use this constructor! Use the other constructors instead to allow information
     * transfer.
     */
    @Deprecated
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
     * @param visibleForPublic
     *            should be <code>true</code> if everbidy should see this tag, otherwise <code>false</code>
     * @param username
     *            name of user who created the tag
     * @param raceTimepoint
     *            timepoint of race where tag ot created
     * @param createdAt
     *            timepoint where <code>username</code> created the tag
     */
    public TagDTO(String tag, String comment, String imageURL, boolean visibleForPublic, String username,
            TimePoint raceTimepoint, TimePoint createdAt) {
        this(tag, comment, imageURL, visibleForPublic, username, raceTimepoint, createdAt, null);
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
     * @param visibleForPublic
     *            should be <code>true</code> if everbidy should see this tag, otherwise <code>false</code>
     * @param username
     *            name of user who created the tag
     * @param raceTimepoint
     *            timepoint of race where tag ot created
     * @param createdAt
     *            timepoint where <code>username</code> created the tag
     * @param revokedAt
     *            timepoint where tag got revoked, may be <code>null</code> if tag is not revoked
     */
    public TagDTO(String tag, String comment, String imageURL, boolean visibleForPublic, String username,
            TimePoint raceTimepoint, TimePoint createdAt, TimePoint revokedAt) {
        this.tag = tag;
        this.comment = comment;
        this.imageURL = imageURL;
        this.visibleForPublic = visibleForPublic;
        this.username = username;
        this.raceTimepoint = raceTimepoint;
        this.createdAt = createdAt;
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
     * Returns visibilty of tag.
     * 
     * @return <code>true</code> if everybody should see the tag, otherwise <code>false</code>
     */
    public boolean isVisibleForPublic() {
        return visibleForPublic;
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

    /**
     * Compares attributes {@link #tag}, {@link #comment}, {@link #imageURL}, {@link #visibleForPublic},
     * {@link #username} and {@link #raceTimepoint}, but <b>NOT</b> attributes {@link #createdAt} and
     * {@link #revokedAt}.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TagDTO other = (TagDTO) obj;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
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
        if (isVisibleForPublic() != other.isVisibleForPublic()) {
            return false;
        }
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username)) {
            return false;
        }
        if (raceTimepoint == null) {
            if (other.raceTimepoint != null)
                return false;
        } else if (!raceTimepoint.equals(other.raceTimepoint))
            return false;
        return true;
    }

    /**
     * Compares {@link TagDTO} with given attributes.
     * 
     * @return <code>true</code> if all parameters match the key attributes of {@link TagDTO}, otherwise
     *         <code>false</code>
     */
    public boolean equals(String tag, String comment, String imageURL, boolean visibleForPublic, String username,
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
        return "TagDTO [tag=" + tag + ", comment=" + comment + ", imageURL=" + imageURL + ", visibleForPublic="
                + visibleForPublic + ", username=" + username + ", raceTimepoint=" + raceTimepoint + ", createdAt="
                + createdAt + ", revokedAt=" + revokedAt + "]";
    }
}
