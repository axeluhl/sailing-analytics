package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.ImageSize;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class EventBaseDTO extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = 818666323178097939L;

    public VenueDTO venue;
    public Date startDate;
    public Date endDate;
    public boolean isPublic;
    public UUID id;

    private String description;
    private List<? extends LeaderboardGroupBaseDTO> leaderboardGroups;
    private List<String> imageURLs = new ArrayList<>();
    private List<String> videoURLs = new ArrayList<>();
    private List<String> sponsorImageURLs = new ArrayList<>();
    private String logoImageURL;
    private String officialWebsiteURL;
    /** placeholder for social media URL's -> attributes will be implemented later on */
    private String facebookURL;
    private String twitterURL;

    /**
     * For the image URL keys holds the sizes of these images if known. An image size is "known" by this object if it
     * was provided to the {@link #setImageSize} method.
     */
    private Map<String, ImageSize> imageSizes;

    /**
     * The base URL for the server instance on which the data for this event can be reached. Could be something like
     * <code>http://sapsailing.com</code> for archived events that will forever remain in the archive, or
     * <code>http://danishleague2014.sapsailing.com</code> for other events that may not yet be archived or may change
     * servers at any time in the future, therefore requiring a dedicated stable URL that the Apache server can resolve
     * to the correct host IP and Java server instance.
     */
    private String baseURL;

    /**
     * Indicates whether the event is hosted on a remote server or not
     */
    private boolean isOnRemoteServer;

    EventBaseDTO() {
    } // for serialization only

    public EventBaseDTO(List<? extends LeaderboardGroupBaseDTO> leaderboardGroups) {
        this.leaderboardGroups = leaderboardGroups;
        this.imageSizes = new HashMap<String, ImageSize>();
    }

    public EventBaseDTO(String name, List<? extends LeaderboardGroupBaseDTO> leaderboardGroups) {
        super(name);
        this.leaderboardGroups = leaderboardGroups;
        this.imageSizes = new HashMap<String, ImageSize>();
    }

    public boolean isRunning() {
        Date now = new Date();
        if (startDate != null && endDate != null && (now.after(startDate) && now.before(endDate))) {
            return true;
        }
        return false;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoImageURL() {
        return logoImageURL;
    }

    public void setLogoImageURL(String logoImageURL) {
        this.logoImageURL = logoImageURL;
    }

    public String getOfficialWebsiteURL() {
        return officialWebsiteURL;
    }

    public void setOfficialWebsiteURL(String officialWebsiteURL) {
        this.officialWebsiteURL = officialWebsiteURL;
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

    public void addSponsorImageURL(String sponsorImageURL) {
        sponsorImageURLs.add(sponsorImageURL);
    }

    public List<String> getImageURLs() {
        return imageURLs;
    }

    public List<String> getVideoURLs() {
        return videoURLs;
    }

    public List<String> getSponsorImageURLs() {
        return sponsorImageURLs;
    }

    public Iterable<? extends LeaderboardGroupBaseDTO> getLeaderboardGroups() {
        return leaderboardGroups;
    }

    public boolean isOnRemoteServer() {
        return isOnRemoteServer;
    }

    public void setIsOnRemoteServer(boolean isOnRemoteServer) {
        this.isOnRemoteServer = isOnRemoteServer;
    }

    public String getFacebookURL() {
        return facebookURL;
    }

    public void setFacebookURL(String facebookURL) {
        this.facebookURL = facebookURL;
    }

    public String getTwitterURL() {
        return twitterURL;
    }

    public void setTwitterURL(String twitterURL) {
        this.twitterURL = twitterURL;
    }

    public void setImageSize(String imageURL, ImageSize imageSize) {
        if (imageSize == null) {
            imageSizes.remove(imageURL);
        } else {
            imageSizes.put(imageURL, imageSize);
        }
    }

    /**
     * @return the size of the image referenced by <code>imageURL</code> or <code>null</code> if that size is not known
     *         of the image URL is none of those known to this event, in particular neither of {@link #getImageURLs()}
     *         or {@link #getSponsorImageURLs()}.
     */
    public ImageSize getImageSize(String imageURL) {
        return imageSizes.get(imageURL);
    }
}
