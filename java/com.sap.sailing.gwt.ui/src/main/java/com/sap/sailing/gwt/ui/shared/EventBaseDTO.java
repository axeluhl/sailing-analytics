package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;
import com.sap.sse.common.WithID;
import com.sap.sse.common.media.ImageSize;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;
import com.sap.sse.security.shared.dto.NamedDTO;

/**
 * Basic event information as a DTO. The inherited {@link NamedDTO#equals(Object)} and {@link NamedDTO#hashCode()}
 * methods that are based on the {@link NamedDTO#getName()} response are overridden here to be based on this event's
 * {@link #getId() ID}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class EventBaseDTO extends NamedDTO implements WithID, IsSerializable {
    private static final long serialVersionUID = 818666323178097939L;

    public VenueDTO venue;
    public Date startDate;
    public Date endDate;
    public boolean isPublic;
    public UUID id;

    private String description;
    private List<? extends LeaderboardGroupBaseDTO> leaderboardGroups;
    private String officialWebsiteURL;
    private Map<String, String> sailorsInfoWebsiteURLs;
    private List<ImageDTO> images = new ArrayList<>();
    private List<VideoDTO> videos = new ArrayList<>();

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
        sailorsInfoWebsiteURLs = new HashMap<>();
    }

    public EventBaseDTO(String name, List<? extends LeaderboardGroupBaseDTO> leaderboardGroups) {
        super(name);
        this.leaderboardGroups = leaderboardGroups;
        this.imageSizes = new HashMap<String, ImageSize>();
        sailorsInfoWebsiteURLs = new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        return Util.equalsWithNull(this.getId(), ((EventBaseDTO) o).getId());
    }
    
    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
    
    public ImageDTO getLogoImage() {
        ImageDTO result = null;
        for (ImageDTO image : images) {
            if (image.hasTag(MediaTagConstants.LOGO.getName())) {
                result = image;
                break;
            }
        }
        return result;
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

    public String getOfficialWebsiteURL() {
        return officialWebsiteURL;
    }

    public void setOfficialWebsiteURL(String officialWebsiteURL) {
        this.officialWebsiteURL = officialWebsiteURL;
    }
    
    public Map<String, String> getSailorsInfoWebsiteURLs() {
        return sailorsInfoWebsiteURLs;
    }
    
    public String getSailorsInfoWebsiteURL(String locale) {
        return sailorsInfoWebsiteURLs.get(locale);
    }
    
    public void setSailorsInfoWebsiteURL(String locale, String url) {
        if(url == null || url.isEmpty()) {
            sailorsInfoWebsiteURLs.remove(locale);
        } else {
            sailorsInfoWebsiteURLs.put(locale, url);
        }
    }

    public void setSailorsInfoWebsiteURLs(Map<String, String> sailorsInfoWebsiteURLs) {
        this.sailorsInfoWebsiteURLs.clear();
        if(sailorsInfoWebsiteURLs != null) {
            this.sailorsInfoWebsiteURLs.putAll(sailorsInfoWebsiteURLs);
        }
    }

    /**
     * If not {@link #setBaseURL(String) set}, defaults to <code>http://sapsailing.com</code>. Meant to describe the
     * base URL that maps to the server instance at which this event can be found, such as
     * <code>http://505worlds2013.sapsailing.com</code>.
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Assign the event's base URL.
     *
     * NOTE: <code>https://</code> will be assumed if no protocol has been provided.
     */
    public void setBaseURL(String baseURL) {
        if (baseURL != null && !baseURL.contains("://")) {
            this.baseURL = "https://" + baseURL;
        } else {
            this.baseURL = baseURL;
        }
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

    public void addImage(ImageDTO image) {
        images.add(image);
    }

    public List<ImageDTO> getImages() {
        return images;
    }

    public void addVideo(VideoDTO video) {
        videos.add(video);
    }

    public List<VideoDTO> getVideos() {
        return videos;
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

    @Override
    public Serializable getId() {
        return id;
    }

}
