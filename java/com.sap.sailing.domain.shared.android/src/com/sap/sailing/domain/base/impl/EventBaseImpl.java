package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.MediaDescriptor;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.VideoDescriptor;

public abstract class EventBaseImpl implements EventBase {
    private static final long serialVersionUID = -5749964088848611074L;

    private String name;
    private String description;
    private final Venue venue;
    private boolean isPublic;
    private final UUID id;
    private TimePoint startDate;
    private TimePoint endDate;
    private ConcurrentLinkedQueue<URL> imageURLs;
    private ConcurrentLinkedQueue<URL> videoURLs;
    private ConcurrentLinkedQueue<URL> sponsorImageURLs;
    private URL logoImageURL;
    private URL officialWebsiteURL;
    private ConcurrentLinkedQueue<ImageDescriptor> images;
    private ConcurrentLinkedQueue<VideoDescriptor> videos;

    protected EventBaseImpl(String name, TimePoint startDate, TimePoint endDate, String venueName, boolean isPublic, UUID id) {
        this(name, startDate, endDate, new VenueImpl(venueName), isPublic, id);
    }

    /**
     * @param venue must not be <code>null</code>
     */
    protected EventBaseImpl(String name, TimePoint startDate, TimePoint endDate, Venue venue, boolean isPublic, UUID id) {
        assert venue != null;
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.venue = venue;
        this.isPublic = isPublic;
        this.imageURLs = new ConcurrentLinkedQueue<URL>();
        this.videoURLs = new ConcurrentLinkedQueue<URL>();
        this.sponsorImageURLs = new ConcurrentLinkedQueue<URL>();
        this.images = new ConcurrentLinkedQueue<ImageDescriptor>();
        this.videos = new ConcurrentLinkedQueue<VideoDescriptor>();
        syncImageAndVideoURLsForBackwardCompatibility();
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        if (imageURLs == null) {
            imageURLs = new ConcurrentLinkedQueue<URL>();
        }
        if (videoURLs == null) {
            videoURLs = new ConcurrentLinkedQueue<URL>();
        }
        if (sponsorImageURLs == null) {
            sponsorImageURLs = new ConcurrentLinkedQueue<URL>();
        }
        if (images == null) {
            images = new ConcurrentLinkedQueue<ImageDescriptor>();
        }
        if (videos == null) {
            videos = new ConcurrentLinkedQueue<VideoDescriptor>();
        }
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Venue getVenue() {
        return venue;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @param newName must not be <code>null</code>
     */
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("An event name must not be null");
        }
        this.name = newName;
    }

    @Override
    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public TimePoint getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(TimePoint startDate) {
        this.startDate = startDate;
    }

    @Override
    public TimePoint getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(TimePoint endDate) {
        this.endDate = endDate;
    }

    @Override
    public Iterable<URL> getImageURLs() {
        return Collections.unmodifiableCollection(imageURLs);
    }
    
    protected void setImageURLs(Iterable<URL> imageURLs) {
        this.imageURLs.clear();
        if (imageURLs != null) {
            Util.addAll(imageURLs, this.imageURLs);
        }
    }

    @Override
    public Iterable<URL> getVideoURLs() {
        return Collections.unmodifiableCollection(videoURLs);
    }

    private void setVideoURLs(Iterable<URL> videoURLs) {
        this.videoURLs.clear();
        if (videoURLs != null) {
            Util.addAll(videoURLs, this.videoURLs);
        }
    }

    @Override
    public Iterable<URL> getSponsorImageURLs() {
        return Collections.unmodifiableCollection(sponsorImageURLs);
    }
    
    protected void setSponsorImageURLs(Iterable<URL> sponsorImageURLs) {
        this.sponsorImageURLs.clear();
        if (sponsorImageURLs != null) {
            Util.addAll(sponsorImageURLs, this.sponsorImageURLs);
        }
    }

    @Override
    public URL getLogoImageURL() {
        return logoImageURL;
    }

    @Override
    public void setLogoImageURL(URL logoImageURL) {
        this.logoImageURL = logoImageURL;
    }

    @Override
    public URL getOfficialWebsiteURL() {
        return officialWebsiteURL;
    }

    @Override
    public void setOfficialWebsiteURL(URL officialWebsiteURL) {
        this.officialWebsiteURL = officialWebsiteURL;
    }

    @Override
    public Iterable<ImageDescriptor> getImages() {
        return Collections.unmodifiableCollection(images);
    }
    
    @Override
    public void addImage(ImageDescriptor image) {
        if (!images.contains(image)) {
            images.add(image);
            syncImageURLsFromImagesForBackwardCompatibility();
        }
    }

    @Override
    public void removeImage(ImageDescriptor image) {
        images.remove(image);
        syncImageURLsFromImagesForBackwardCompatibility();
    }

    @Override
    public void setImages(Iterable<ImageDescriptor> images) {
        this.images.clear();
        if (images != null) {
            Util.addAll(images, this.images);
            syncImageURLsFromImagesForBackwardCompatibility();
        }
    }
    
    @Override
    public Iterable<VideoDescriptor> getVideos() {
        return Collections.unmodifiableCollection(videos);
    }
    
    @Override
    public void addVideo(VideoDescriptor video) {
        if (!videos.contains(video)) {
            videos.add(video);
            syncVideoURLsFromVideosForBackwardCompatibility();
        }
    }

    @Override
    public void removeVideo(VideoDescriptor video) {
        videos.remove(video);
        syncVideoURLsFromVideosForBackwardCompatibility();
    }

    @Override
    public void setVideos(Iterable<VideoDescriptor> videos) {
        this.videos.clear();
        if (videos != null) {
            Util.addAll(videos, this.videos);
            syncVideoURLsFromVideosForBackwardCompatibility();
        }
    }

    private void syncImageURLsFromImagesForBackwardCompatibility() {
        List<URL> imageURLs = createURLsFromMedia(images, MediaTagConstants.SPONSOR, null);
        List<URL> sponsorImageURLs = createURLsFromMedia(images, null, MediaTagConstants.SPONSOR);
        setImageURLs(imageURLs);
        setSponsorImageURLs(sponsorImageURLs);
    }
    
    private void syncVideoURLsFromVideosForBackwardCompatibility() {
        List<URL> videoURLs = createURLsFromMedia(videos, null, null);
        setVideoURLs(videoURLs);
    }

    private void syncImageAndVideoURLsForBackwardCompatibility() {
        syncImageURLsFromImagesForBackwardCompatibility();
        syncVideoURLsFromVideosForBackwardCompatibility();
    }

    private List<URL> createURLsFromMedia(Iterable<? extends MediaDescriptor> media, String blacklistTag, String whitelistTag) {
        List<URL> result = new ArrayList<>();
        for (MediaDescriptor mediaEntry : media) {
            if (blacklistTag != null && Util.contains(mediaEntry.getTags(), blacklistTag)) {
                continue;
            }
            if (whitelistTag != null && !Util.contains(mediaEntry.getTags(), whitelistTag)) {
                continue;
            }
            result.add(mediaEntry.getURL());
        }
        return result;
    }
}
