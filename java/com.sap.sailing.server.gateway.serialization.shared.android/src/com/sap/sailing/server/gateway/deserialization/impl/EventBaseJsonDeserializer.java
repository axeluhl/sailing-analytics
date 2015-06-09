package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.StrippedEventImpl;
import com.sap.sailing.domain.common.impl.ImageSizeImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.EventBaseJsonSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.ImageDescriptorImpl;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.common.media.VideoDescriptor;
import com.sap.sse.common.media.VideoDescriptorImpl;

public class EventBaseJsonDeserializer implements JsonDeserializer<EventBase> {
    private final JsonDeserializer<Venue> venueDeserializer;
    private final JsonDeserializer<LeaderboardGroupBase> leaderboardGroupDeserializer;

    public EventBaseJsonDeserializer(JsonDeserializer<Venue> venueDeserializer, JsonDeserializer<LeaderboardGroupBase> leaderboardGroupDeserializer) {
        this.venueDeserializer = venueDeserializer;
        this.leaderboardGroupDeserializer = leaderboardGroupDeserializer;
    }

    public EventBase deserialize(JSONObject eventJson) throws JsonDeserializationException {
        UUID id = UUID.fromString((String) eventJson.get(EventBaseJsonSerializer.FIELD_ID));
        String name = (String) eventJson.get(EventBaseJsonSerializer.FIELD_NAME);
        String description = (String) eventJson.get(EventBaseJsonSerializer.FIELD_DESCRIPTION);
        String officialWebsiteURLAsString = (String) eventJson.get(EventBaseJsonSerializer.FIELD_OFFICIAL_WEBSITE_URL);
        String logoImageURLAsString = (String) eventJson.get(EventBaseJsonSerializer.FIELD_LOGO_IMAGE_URL);
        Number startDate = (Number) eventJson.get(EventBaseJsonSerializer.FIELD_START_DATE);
        Number endDate = (Number) eventJson.get(EventBaseJsonSerializer.FIELD_END_DATE);
        final Venue venue;
        if (eventJson.get(EventBaseJsonSerializer.FIELD_VENUE) != null) {
            JSONObject venueObject = Helpers.getNestedObjectSafe(eventJson, EventBaseJsonSerializer.FIELD_VENUE);
            venue = venueDeserializer.deserialize(venueObject);
        } else {
            venue = null;
        }
        JSONArray leaderboardGroupsJson = (JSONArray) eventJson.get(EventBaseJsonSerializer.FIELDS_LEADERBOARD_GROUPS);
        List<LeaderboardGroupBase> leaderboardGroups = new ArrayList<LeaderboardGroupBase>();
        if (leaderboardGroupsJson != null) {
            for (Object lgJson : leaderboardGroupsJson) {
                leaderboardGroups.add(leaderboardGroupDeserializer.deserialize((JSONObject) lgJson));
            }
        }
        StrippedEventImpl result = new StrippedEventImpl(name, startDate == null ? null : new MillisecondsTimePoint(startDate.longValue()),
                endDate == null ? null : new MillisecondsTimePoint(endDate.longValue()), venue, /* is public */ true, id, leaderboardGroups);
        result.setDescription(description);
        if (officialWebsiteURLAsString != null) {
            try {
                result.setOfficialWebsiteURL(new URL(officialWebsiteURLAsString));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing official website URL for event "+name, e);
            }
        }
        if (logoImageURLAsString != null) {
            try {
                result.setLogoImageURL(new URL(logoImageURLAsString));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing logo image URL for event "+name, e);
            }
        }
        if (eventJson.get(EventBaseJsonSerializer.FIELD_IMAGE_URLS) != null) {
            try {
                result.setImageURLs(getURLsFromStrings(Helpers.getNestedArraySafe(eventJson, EventBaseJsonSerializer.FIELD_IMAGE_URLS)));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing image URLs for event "+name, e);
            }
        }
        if (eventJson.get(EventBaseJsonSerializer.FIELD_VIDEO_URLS) != null) {
            try {
                result.setVideoURLs(getURLsFromStrings(Helpers.getNestedArraySafe(eventJson, EventBaseJsonSerializer.FIELD_VIDEO_URLS)));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing video URLs for event "+name, e);
            }
        }
        if (eventJson.get(EventBaseJsonSerializer.FIELD_SPONSOR_IMAGE_URLS) != null) {
            try {
                result.setSponsorImageURLs(getURLsFromStrings(Helpers.getNestedArraySafe(eventJson, EventBaseJsonSerializer.FIELD_SPONSOR_IMAGE_URLS)));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing sponsor image URLs for event "+name, e);
            }
        }
        JSONArray imageSizes = (JSONArray) eventJson.get(EventBaseJsonSerializer.FIELD_IMAGE_SIZES);
        if (imageSizes != null) {
            for (Object imageURLAndSizeObject : imageSizes) {
                JSONObject imageURLAndSizeJson = (JSONObject) imageURLAndSizeObject;
                try {
                    result.setImageSize(
                            new URL((String) imageURLAndSizeJson.get(EventBaseJsonSerializer.FIELD_IMAGE_URL)),
                            new ImageSizeImpl(
                                    ((Number) imageURLAndSizeJson.get(EventBaseJsonSerializer.FIELD_IMAGE_WIDTH)).intValue(),
                                    ((Number) imageURLAndSizeJson.get(EventBaseJsonSerializer.FIELD_IMAGE_HEIGHT)).intValue()));
                } catch (MalformedURLException e) {
                    throw new JsonDeserializationException(e);
                }
            }
        }
        JSONArray imagesJson = (JSONArray) eventJson.get(EventBaseJsonSerializer.FIELD_IMAGES);
        if (imagesJson != null) {
            for (Object imageJson : imagesJson) {
                ImageDescriptor imgaeDescriptor = loadImage((JSONObject) imageJson);
                if (imgaeDescriptor != null) {
                   result.addImage(imgaeDescriptor);
                }
            }            
        }
        JSONArray videosJson = (JSONArray) eventJson.get(EventBaseJsonSerializer.FIELD_VIDEOS);
        if (videosJson != null) {
            for (Object videoJson : videosJson) {
                VideoDescriptor videoDescriptor = loadVideo((JSONObject) videoJson);
                if (videoDescriptor != null) {
                   result.addVideo(videoDescriptor);
                }
            }            
        }

        return result;
    }

    private ImageDescriptor loadImage(JSONObject imageJson) {
        ImageDescriptor image = null;
        URL imageURL = Helpers.getURLField(imageJson, EventBaseJsonSerializer.FIELD_SOURCE_URL);
        if (imageURL != null) {
            String title = (String) imageJson.get(EventBaseJsonSerializer.FIELD_TITLE);
            String subtitle = (String) imageJson.get(EventBaseJsonSerializer.FIELD_SUBTITLE);
            String copyright = (String) imageJson.get(EventBaseJsonSerializer.FIELD_COPYRIGHT);
            Integer imageWidth = (Integer) imageJson.get(EventBaseJsonSerializer.FIELD_IMAGE_WIDTH_IN_PX);
            Integer imageHeight = (Integer) imageJson.get(EventBaseJsonSerializer.FIELD_IMAGE_HEIGHT_IN_PX);
            Long createdAtDateInMs = (Long) imageJson.get(EventBaseJsonSerializer.FIELD_CREATEDATDATE);
            TimePoint createdAtDate = createdAtDateInMs != null ? new MillisecondsTimePoint(createdAtDateInMs) : null;
            JSONArray tags = (JSONArray) imageJson.get(EventBaseJsonSerializer.FIELD_TAGS);
            List<String> imageTags = new ArrayList<String>();
            if (tags != null) {
                for (Object tagObject : tags) {
                    imageTags.add((String) tagObject);
                }
            }
            image = new ImageDescriptorImpl(imageURL, createdAtDate);
            image.setCopyright(copyright);
            image.setTitle(title);
            image.setSubtitle(subtitle);
            image.setTags(imageTags);
            if (imageWidth != null && imageHeight != null) {
                image.setSize(imageWidth, imageHeight);
            }
        }
        return image;
    }
    
    private VideoDescriptor loadVideo(JSONObject videoJson) {
        VideoDescriptor video = null;
        URL videoURL = Helpers.getURLField(videoJson, EventBaseJsonSerializer.FIELD_SOURCE_URL);
        if(videoURL != null) {
            String title = (String) videoJson.get(EventBaseJsonSerializer.FIELD_TITLE);
            String subtitle = (String) videoJson.get(EventBaseJsonSerializer.FIELD_SUBTITLE);
            String copyright = (String) videoJson.get(EventBaseJsonSerializer.FIELD_COPYRIGHT);
            Object mimeTypeRaw = videoJson.get(EventBaseJsonSerializer.FIELD_MIMETYPE);
            MimeType mimeType = mimeTypeRaw == null ? null : MimeType.valueOf((String) mimeTypeRaw);
            Long createdAtDateInMs = (Long) videoJson.get(EventBaseJsonSerializer.FIELD_CREATEDATDATE);
            TimePoint createdAtDate = createdAtDateInMs != null ? new MillisecondsTimePoint(createdAtDateInMs) : null;
            JSONArray tags = (JSONArray) videoJson.get(EventBaseJsonSerializer.FIELD_TAGS);
            Integer lengthInSeconds = (Integer) videoJson.get(EventBaseJsonSerializer.FIELD_VIDEO_LENGTH_IN_SECONDS);
            URL thumbnailURL = Helpers.getURLField(videoJson, EventBaseJsonSerializer.FIELD_VIDEO_THUMBNAIL_URL);
            List<String> videoTags = new ArrayList<String>();
            if (tags != null) {
                for (Object tagObject : tags) {
                    videoTags.add((String) tagObject);
                }
            }
            video = new VideoDescriptorImpl(videoURL, mimeType, createdAtDate);
            video.setCopyright(copyright);
            video.setTitle(title);
            video.setSubtitle(subtitle);
            video.setTags(videoTags);
            video.setLengthInSeconds(lengthInSeconds);
            video.setThumbnailURL(thumbnailURL);
        }
        return video;
    }

    private Iterable<URL> getURLsFromStrings(JSONArray strings) throws MalformedURLException {
        List<URL> result = new ArrayList<URL>();
        if (strings != null) {
            for (Object string : strings) {
                result.add(new URL(string.toString()));
            }
        }
        return result;
    }
}
