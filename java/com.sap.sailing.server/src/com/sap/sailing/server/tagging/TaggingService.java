package com.sap.sailing.server.tagging;

import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sse.common.TimePoint;

public interface TaggingService {

    /**
     * Enum used to identify issues.
     */
    public enum ErrorCode {
        UNKNOWN_ERROR("Unknown error"),
        USER_NOT_FOUND("User not found"),
        MISSING_PERMISSIONS("Missing permissions"),
        SECURITY_SERIVCE_NOT_FOUND("Security service not found"),
        RACELOG_NOT_FOUND("Racelog not found"),
        TAG_NOT_REVOKABLE("This tag cannot be revoked"),
        TAG_NOT_EMPTY("Tag may not be empty"),
        TIMEPOINT_NOT_EMPTY("Timepoint may not be empty"),
        TAG_TOO_LONG("Tag is too long"),
        COMMENT_TOO_LONG("Comment is too long");

        private final String message;

        ErrorCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Saves given properties as tag for given race. Checks if all parameters are valid and all required parameters are
     * set.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param tag
     *            title of tag, must <b>NOT</b> be <code>null</code>
     * @param comment
     *            optional comment of tag
     * @param imageURL
     *            optional image URL of tag
     * @param visibleForPublic
     *            when set to <code>true</code>, tag will be saved in
     *            {@link com.sap.sailing.domain.abstractlog.race.RaceLog RaceLog} (visible for every user), otherwise
     *            tag will be saved in {@link com.sap.sse.security.UserStore UserStore} (only visible for the creator)
     * @param raceTimepoint
     *            timepoint in race when user created tag, must <b>NOT</b> be <code>null</code>
     * @return <code>true</code> if tag was saved successfully, otherwise <code>false</code>
     */
    public boolean addTag(String leaderboardName, String raceColumnName, String fleetName, String tag, String comment,
            String imageURL, boolean visibleForPublic, TimePoint raceTimepoint);

    /**
     * Removes public {@link TagDTO tag} from {@link com.sap.sailing.domain.abstractlog.race.RaceLog RaceLog} and
     * private {@link TagDTO tag} from {@link com.sap.sse.security.UserStore UserStore}.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param tag
     *            tag to remove
     * @return <code>true</code> if tag was removed successfully, otherwise <code>false</code>
     */
    public boolean removeTag(String leaderboardName, String raceColumnName, String fleetName, TagDTO tag);

    /**
     * Updates given <code>tagToUpdate</code> with the given parameters <code>tag</code>, <code>comment</code>,
     * <code>imageURL</code> and <code>isPublic</code>.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param tagToUpdate
     *            tag to be updated
     * @param tag
     *            new tag title
     * @param comment
     *            new comment
     * @param imageURL
     *            new iamge URL
     * @param visibleForPublic
     *            new privacy status
     * @return <code>true</code> if tag was updated successfully, otherwise <code>false</code>
     */
    public boolean updateTag(String leaderboardName, String raceColumnName, String fleetName, TagDTO tagToUpdate,
            String tag, String comment, String imageURL, boolean visibleForPublic);

    /**
     * Returns all public tags for the specified race.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @return list of {@link TagDTO tags}, empty list in case an error occurs or there are no tags available but
     *         <b>never null</b>!
     */
    public List<TagDTO> getPublicTags(String leaderboardName, String raceColumnName, String fleetName);

    /**
     * Returns all public tags since the given <code>latestReceivedTagTime</code> for the specified race.
     * 
     * @param raceIdentifier
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @return list of {@link TagDTO tags}, empty list in case an error occurs or there are no tags available but
     *         <b>never null</b>!
     */
    public List<TagDTO> getPublicTags(RegattaAndRaceIdentifier raceIdentifier, TimePoint latestReceivedTagTime);

    /**
     * Returns all private tags of current user for the specified race.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @return list of {@link TagDTO tags}, empty list in case an error occurs or there are no tags available but
     *         <b>never null</b>!
     */
    public List<TagDTO> getPrivateTags(String leaderboardName, String raceColumnName, String fleetName);

    /**
     * Returns the last error code of the current user. Needs to be converted into error message to display this message
     * to the user.
     * 
     * @return last {@link ErrorCode error code} which occured if error is known, otherwise
     *         {@link ErrorCode#UNKNOWN_ERROR unknown error}
     */
    public ErrorCode getLastErrorCode();
}
