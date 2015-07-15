package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.ImageDescriptorImpl;
import com.sap.sse.common.media.ImageSize;
import com.sap.sse.common.media.MediaDescriptor;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MediaUtils;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.common.media.VideoDescriptor;
import com.sap.sse.common.media.VideoDescriptorImpl;

public abstract class EventBaseImpl implements EventBase {
    private static final long serialVersionUID = -5749964088848611074L;

    private String name;
    private String description;
    private final Venue venue;
    private boolean isPublic;
    private final UUID id;
    private TimePoint startDate;
    private TimePoint endDate;
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
        this.images = new ConcurrentLinkedQueue<ImageDescriptor>();
        this.videos = new ConcurrentLinkedQueue<VideoDescriptor>();
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
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
        List<URL> result = new ArrayList<URL>();
        for (ImageDescriptor image : images) {
            if (!image.hasTag(MediaTagConstants.SPONSOR) && !image.hasTag(MediaTagConstants.LOGO)) {
                result.add(image.getURL());
            }
        }
        return result;
    }
    
    @Override
    public Iterable<URL> getVideoURLs() {
        List<URL> result = new ArrayList<URL>();
        for(VideoDescriptor video: videos) {
            result.add(video.getURL());
        }
        return result;
    }

    @Override
    public Iterable<URL> getSponsorImageURLs() {
        List<URL> result = new ArrayList<URL>();
        for(MediaDescriptor media : findMediaWithTag(images, MediaTagConstants.SPONSOR)) {
            result.add(media.getURL());
        }
        return result;
    }
    
    @Override
    public URL getLogoImageURL() {
        URL result = null;
        ImageDescriptor media = findImageWithTag(MediaTagConstants.LOGO);
        if(media != null) {
            result = media.getURL();
        }
        return result;
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
        }
    }

    @Override
    public void removeImage(ImageDescriptor image) {
        images.remove(image);
    }

    @Override
    public void setImages(Iterable<ImageDescriptor> images) {
        this.images.clear();
        if (images != null) {
            Util.addAll(images, this.images);
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
        }
    }

    @Override
    public void removeVideo(VideoDescriptor video) {
        videos.remove(video);
    }

    @Override
    public void setVideos(Iterable<VideoDescriptor> videos) {
        this.videos.clear();
        if (videos != null) {
            Util.addAll(videos, this.videos);
        }
    }

    @Override
    public ImageDescriptor findImageWithTag(String tagName) {
        ImageDescriptor result = null;
        List<ImageDescriptor> mediaWithTag = findMediaWithTag(images, tagName);
        if(mediaWithTag.size() > 0) {
            result = mediaWithTag.get(0);
        } 
        return result;
    }

    @Override
    public VideoDescriptor findVideoWithTag(String tagName) {
        VideoDescriptor result = null;
        List<VideoDescriptor> mediaWithTag = findMediaWithTag(videos, tagName);
        if(mediaWithTag.size() > 0) {
            result = mediaWithTag.get(0);
        } 
        return result;
    }
    
    @Override
    public boolean hasImageWithTag(String tagName) {
        List<ImageDescriptor> mediaWithTag = findMediaWithTag(images, tagName);
        return mediaWithTag.size() > 0;
    }

    @Override
    public List<ImageDescriptor> findImagesWithTag(String tagName) {
        return findMediaWithTag(images, tagName);
    }

    @Override
    public List<VideoDescriptor> findVideosWithTag(String tagName) {
        return findMediaWithTag(videos, tagName);
    }

    private <T extends MediaDescriptor> List<T> findMediaWithTag(Iterable<T> media, String tagName) {
        List<T> result = new ArrayList<>();
        for (T mediaEntry : media) {
            if(mediaEntry.hasTag(tagName)) {
                result.add(mediaEntry);
            }
        }
        return result;
    }
    
    /** 
     * Sets and converts all event images and videos from the old URL based format to the new richer format 
     */ 
    public boolean setMediaURLs(Iterable<URL> imageURLs, Iterable<URL> sponsorImageURLs, Iterable<URL> videoURLs,
            URL logoImageURL, Map<URL, ImageSize> imageSizes) {
        boolean changed = false;
        
        for (URL url : imageURLs) {
            if (!hasMedia(images, url)) {
                ImageDescriptor image = migrateImageURLtoImage(url, getStartDate(), imageSizes.get(url));
                String urlAsString = url.toString();
                if (urlAsString.toLowerCase().indexOf("stage") > 0) {
                    image.addTag(MediaTagConstants.STAGE);
                } else if (urlAsString.toLowerCase().indexOf("eventteaser") > 0) {
                    image.addTag(MediaTagConstants.TEASER);
                } else {
                    image.addTag(MediaTagConstants.GALLERY);
                }
                addImage(image);
                changed = true;
            }
        }
        for (URL url : sponsorImageURLs) {
            if (!hasMedia(images, url)) {
                ImageDescriptor image = migrateImageURLtoImage(url, getStartDate(), imageSizes.get(url));
                image.addTag(MediaTagConstants.SPONSOR);
                addImage(image);
                changed = true;
            }
        }
        
        if (logoImageURL != null && !hasMedia(images, logoImageURL)) {
            ImageDescriptor image = migrateImageURLtoImage(logoImageURL, getStartDate(), imageSizes.get(logoImageURL));
            image.addTag(MediaTagConstants.LOGO);
            addImage(image);
            changed = true;
        }

        for (URL url : videoURLs) {
            if (!hasMedia(videos, url)) {
                MimeType mimeType = MediaUtils.detectMimeTypeFromUrl(url.toString());
                VideoDescriptor video = new VideoDescriptorImpl(url, mimeType, getStartDate());
                addVideo(video);
                changed = true;
            }
        }

        return changed;
    }

    private ImageDescriptor migrateImageURLtoImage(URL url, TimePoint createdAt, ImageSize imageSize) {
        ImageDescriptorImpl image = new ImageDescriptorImpl(url, createdAt);
        if (imageSize != null) {
            image.setSize(imageSize.getWidth(), imageSize.getHeight());
        } else {
            Pair<Integer, Integer> imageDimensions = MediaUtils.getImageDimensions(url);
            if (imageDimensions != null) {
                image.setSize(imageDimensions);
            }
        }
        return image;
    }
    
    private boolean hasMedia(Iterable<? extends MediaDescriptor> media, URL url) {
        for (MediaDescriptor mediaEntry : media) {
            if (url.equals(mediaEntry.getURL())) {
                return true;
            }
        }
        return false;
    }
}
