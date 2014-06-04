package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class EventBaseDTO extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = 818666323178097939L;
    public VenueDTO venue;
    public Date startDate;
    public Date endDate;
    public boolean isPublic;
    public UUID id;

    private List<? extends LeaderboardGroupBaseDTO> leaderboardGroups;
    private List<String> imageURLs = new ArrayList<>();
    private List<String> videoURLs = new ArrayList<>();
    
    /**
     * The base URL for the server instance on which the data for this event can be reached. Could be something like
     * <code>http://sapsailing.com</code> for archived events that will forever remain in the archive, or
     * <code>http://danishleague2014.sapsailing.com</code> for other events that may not yet be archived or may change
     * servers at any time in the future, therefore requiring a dedicated stable URL that the Apache server can
     * resolve to the correct host IP and Java server instance.
     */
    private String baseURL;

    EventBaseDTO() {} // for serialization only
    
    public EventBaseDTO(List<? extends LeaderboardGroupBaseDTO> leaderboardGroups) {
        this.leaderboardGroups = leaderboardGroups;
    }
    
    public EventBaseDTO(String name, List<? extends LeaderboardGroupBaseDTO> leaderboardGroups) {
        super(name);
        this.leaderboardGroups = leaderboardGroups;
    }

    /**
     * If not {@link #setBaseURL(String) set}, defaults to <code>http://sapsailing.com</code>. Meant to describe the
     * base URL that maps to the server instance at which this event can be found, such as
     * <code>http://505worlds2013.sapsailing.com</code>.
     */
    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public void addImageURL(String imageURL) {
        imageURLs.add(imageURL);
    }
    
    public void addVideoURL(String videoURL) {
        videoURLs.add(videoURL);
    }

    public Iterable<String> getImageURLs() {
        return imageURLs;
    }

    public Iterable<String> getVideoURLs() {
        return videoURLs;
    }
    
    public Iterable<? extends LeaderboardGroupBaseDTO> getLeaderboardGroups() {
        return leaderboardGroups;
    }

}
