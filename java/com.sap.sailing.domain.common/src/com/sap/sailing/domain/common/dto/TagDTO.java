package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.List;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Used to send tags over network. Allows to create tags with all possible combinations of states (private/public and
 * valid/revoked).
 */
public class TagDTO implements Serializable {

    /**
     * Serializes and deserializes {@link TagDTO}.
     * <p>
     * <b>Notice:</b> There is no way to provide one single JSON-DeSerializer for server and client as GWT does not
     * support org.json.simple package and server does not support the gwt json serializer. Therefor there is one
     * DeSerializer for the server and one DeSerializer for the client side. <b>Changes in any of these DeSerializers
     * require these changes also at the other DeSerializers!</b>
     * <p>
     * All DeSerializers, which need to be adapted at changes, are:
     * <ul>
     * <li>com.sap.sailing.gwt.ui.raceboard.tagging.TagDTODeSerializer</li>
     * <li>com.sap.sailing.server.gateway.jaxrs.api.TagsResource.TagDTODeSerializer</li>
     * </ul>
     */
    // TODO: Find a better way of serilaizing TagDTOs which is avvailable at client- & server-side!
    public static abstract class TagDeSerializer {

        public static final String FIELD_TAG = "tag";
        public static final String FIELD_COMMENT = "comment";
        public static final String FIELD_IMAGE_URL = "image";
        public static final String FIELD_USERNAME = "username";
        public static final String FIELD_VISIBLE_FOR_PUBLIC = "public";
        public static final String FIELD_RACE_TIMEPOINT = "raceTimepoint";
        public static final String FIELD_CREATED_AT = "createdAt";
        public static final String FIELD_REVOKED_AT = "revokedAt";

        /**
         * Serializes single {@link TagDTO tag} to json object.
         * 
         * @param tag
         *            tag to be seriaized
         * @return json object
         */
        public abstract String serializeTag(TagDTO tag);

        /**
         * Serializes list of {@link TagDTO tags} to json array.
         * 
         * @param tags
         *            tags to be seriaized
         * @return json array
         */
        public abstract String serializeTags(List<TagDTO> tags);

        /**
         * Deserializes json object to {@link TagDTO tag}.
         * 
         * @param jsonObject
         *            json object to be deseriaized
         * @return {@link TagDTO tag}
         */
        public abstract TagDTO deserializeTag(String jsonObject);

        /**
         * Deserializes json array to list of {@link TagDTO tags}.
         * 
         * @param jsonArray
         *            json array to be deseriaized
         * @return list of {@link TagDTO tags}
         */
        public abstract List<TagDTO> deserializeTags(String jsonArray);

        /**
         * Serializes given {@link TimePoint}.
         * 
         * @param timepoint
         *            {@link TimePoint} to be serialized
         * @return serialized timepoint as long, <code>0</code> if <code>timepoint</code> is <code>null</code>
         */
        public long serializeTimePoint(TimePoint timepoint) {
            return timepoint == null ? 0 : timepoint.asMillis();
        }

        /**
         * Deserializes long to {@link MillisecondsTimePoint}.
         * 
         * @param timepoint
         *            timepoint to be deserialized
         * @return {@link TimePoint}
         */
        public TimePoint deserilizeTimePoint(long timepoint) {
            return new MillisecondsTimePoint(timepoint);
        }

        /**
         * Combines <code>leaderboardName</code>, <code>raceColumnName</code> and <code>fleetName</code> to a unique
         * key. Used to store private tags in {@link com.sap.sse.security.UserStore UserStore}.
         * 
         * @param leaderboardName
         *            leaderboard name
         * @param raceColumnName
         *            race column name
         * @param fleetName
         *            fleet name
         * @return unique key for given race
         */
        public String generateUniqueKey(String leaderboardName, String raceColumnName, String fleetName) {
            return "Tags:" + escape(leaderboardName) + "+" + escape(raceColumnName) + "+" + escape(fleetName);
        }

        /**
         * Escapes given string by replacing every occurence of '/' by '//' and '+' by '/p'.
         * 
         * @param string
         *            string to be escaped
         * @return escaped string
         */
        private String escape(String string) {
            // '+' needs to be escaped as method replaceAll() expects the first parameter to be a regular expression and
            // not a simple string. As '+' has a different meaning in context of a regex it needs to be escaped by '\+'
            // which again needs to be escaped by '\\+'.
            return string.replaceAll("/", "//").replaceAll("\\+", "/p");
        }
    }

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
