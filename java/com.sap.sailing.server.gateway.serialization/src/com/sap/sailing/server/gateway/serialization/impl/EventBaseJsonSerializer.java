package com.sap.sailing.server.gateway.serialization.impl;

import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.common.ImageSize;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class EventBaseJsonSerializer implements JsonSerializer<EventBase> {
    private static final Logger logger = Logger.getLogger(EventBaseJsonSerializer.class.getName());
    
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_START_DATE = "startDate";
    public static final String FIELD_END_DATE = "endDate";
    public static final String FIELD_VENUE = "venue";
    public static final String FIELD_IMAGE_URLS = "imageURLs";
    public static final String FIELD_VIDEO_URLS = "videoURLs";
    public static final String FIELD_SPONSOR_IMAGE_URLS = "sponsorImageURLs";
    public static final String FIELD_LOGO_IMAGE_URL = "logoImageURL";
    public static final String FIELD_IMAGE_SIZES = "imageSizes";
    public static final String FIELD_IMAGE_URL = "imageURL";
    public static final String FIELD_IMAGE_WIDTH = "imageWidth";
    public static final String FIELD_IMAGE_HEIGHT = "imageHeight";
    public static final String FIELD_OFFICIAL_WEBSITE_URL = "officialWebsiteURL";
    public static final String FIELDS_LEADERBOARD_GROUPS = "leaderboardGroups";

    private final JsonSerializer<Venue> venueSerializer;
    private final JsonSerializer<? super LeaderboardGroupBase> leaderboardGroupBaseSerializer;

    public EventBaseJsonSerializer(JsonSerializer<Venue> venueSerializer, JsonSerializer<? super LeaderboardGroupBase> leaderboardGroupBaseSerializer) {
        this.leaderboardGroupBaseSerializer = leaderboardGroupBaseSerializer;
        this.venueSerializer = venueSerializer;
    }

    public JSONObject serialize(EventBase event) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, event.getId().toString());
        result.put(FIELD_NAME, event.getName());
        result.put(FIELD_DESCRIPTION, event.getDescription());
        result.put(FIELD_OFFICIAL_WEBSITE_URL, event.getOfficialWebsiteURL() != null ? event.getOfficialWebsiteURL().toString() : null);
        result.put(FIELD_LOGO_IMAGE_URL, event.getLogoImageURL() != null ? event.getLogoImageURL().toString() : null);
        result.put(FIELD_START_DATE, event.getStartDate() != null ? event.getStartDate().asMillis() : null);
        result.put(FIELD_END_DATE, event.getStartDate() != null ? event.getEndDate().asMillis() : null);
        result.put(FIELD_VENUE, venueSerializer.serialize(event.getVenue()));
        result.put(FIELD_IMAGE_URLS, getURLsAsStringArray(event.getImageURLs()));
        result.put(FIELD_VIDEO_URLS, getURLsAsStringArray(event.getVideoURLs()));
        result.put(FIELD_SPONSOR_IMAGE_URLS, getURLsAsStringArray(event.getSponsorImageURLs()));
        JSONArray leaderboardGroups = new JSONArray();
        result.put(FIELDS_LEADERBOARD_GROUPS, leaderboardGroups);
        for (LeaderboardGroupBase lg : event.getLeaderboardGroups()) {
            leaderboardGroups.add(leaderboardGroupBaseSerializer.serialize(lg));
        }
        JSONArray imageSizes = new JSONArray();
        result.put(FIELD_IMAGE_SIZES, imageSizes);
        for (URL imageURL : event.getImageURLs()) {
            addImageSize(imageURL, imageSizes, event);
        }
        if (event.getLogoImageURL() != null) {
            addImageSize(event.getLogoImageURL(), imageSizes, event);
        }
        for (URL sponsorImageURL : event.getSponsorImageURLs()) {
            addImageSize(sponsorImageURL, imageSizes, event);
        }
        return result;
    }

    /**
     * If <code>eventBase</code> knows the size of the image with URL <code>imageURL</code>, that size is serialized as
     * JSON object to <code>imageSizes</code>
     */
    private void addImageSize(URL imageURL, JSONArray imageSizes, EventBase eventBase) {
        ImageSize imageSize;
        try {
            imageSize = eventBase.getImageSize(imageURL);
            if (imageSize != null) {
                JSONObject imageSizeJson = new JSONObject();
                imageSizes.add(imageSizeJson);
                imageSizeJson.put(FIELD_IMAGE_URL, imageURL.toString());
                imageSizeJson.put(FIELD_IMAGE_WIDTH, imageSize.getWidth());
                imageSizeJson.put(FIELD_IMAGE_HEIGHT, imageSize.getHeight());
            }
        } catch (InterruptedException e) {
            logger.log(Level.FINE, "Couldn't retrieve image size for URL "+imageURL, e);
        } catch (ExecutionException e) {
            logger.log(Level.FINE, "Couldn't retrieve image size for URL "+imageURL, e);
        }
    }

    private JSONArray getURLsAsStringArray(Iterable<URL> urls) {
        JSONArray jsonImageURLs = new JSONArray();
        for (URL url : urls) {
            jsonImageURLs.add(url.toString());
        }
        return jsonImageURLs;
    }
}
