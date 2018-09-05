package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sse.common.TimePoint;

/**
 * Tags can be added by users to mark an important situation at a race. Tags consist of a title, optional comment and
 * optional image URL.
 */
public interface RaceLogTagEvent extends RaceLogEvent, Revokable {

    /**
     * Returns title of tag. The title should be short and summarize the tag.
     * 
     * @return title of tag
     */
    String getTag();

    /**
     * Returns name of user who created the tag. Equals name of {@link #getAuthor() author} of {@link RaceLogEvent}.
     * 
     * @return username of author
     */
    String getUsername();

    /**
     * Returns optional comment. Comments should add additional information to the title.
     * 
     * @return comment of tag, may be <code>null</code> as comments are optional
     */
    String getComment();

    /**
     * Returns optional image URL.
     * 
     * @return image URL of tag, may be <code>null</code> as images are optional
     */
    String getImageURL();

    /**
     * Marks this {@link RaceLogTagEvent} as revoked. Used to differentiate between revoked and non-revoked
     * {@link RaceLogTagEvent tag events}.
     * 
     * @param revokedAt
     *            {@link RaceLogRevokeEvent#getCreatedAt() timepoint of creation} of {@link RaceLogRevokeEvent}
     */
    void markAsRevoked(TimePoint revokedAt);

    /**
     * Returns timepoint of creation of {@link RaceLogRevokeEvent}.
     * 
     * @return {@link RaceLogRevokeEvent#getCreatedAt() timepoint of creation} of {@link RaceLogRevokeEvent} if
     *         {@link RaceLogTagEvent tag} is {@link #markAsRevoked() revoked}, otherwise <code>null</code>
     */
    TimePoint getRevokedAt();
}
