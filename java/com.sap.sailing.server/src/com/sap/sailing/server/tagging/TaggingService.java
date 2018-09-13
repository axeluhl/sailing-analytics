package com.sap.sailing.server.tagging;

import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sse.common.TimePoint;

public interface TaggingService {

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
     * @return list of {@link TagDTO tags}
     */
    public List<TagDTO> getPublicTags(String leaderboardName, String raceColumnName, String fleetName);

    /**
     * Returns all public tags since the given <code>latestReceivedTagTime</code> for the specified race.
     * 
     * @param raceIdentifier
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @return list of {@link TagDTO tags}
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
     * @return list of {@link TagDTO tags}
     */
    public List<TagDTO> getPrivateTags(String leaderboardName, String raceColumnName, String fleetName);

    /**
     * Returns the last error code as string. Need to be converted into error message to display this message to the
     * user. Codes are basically string messages maintained in
     * com.sap.sailing.gwt.ui/src/main/resources/stringmessages_*
     * 
     * @return last error code which occured
     */
    public String getLastErrorCode();

}
