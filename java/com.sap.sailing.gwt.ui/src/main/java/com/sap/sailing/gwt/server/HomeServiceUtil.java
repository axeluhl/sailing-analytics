package com.sap.sailing.gwt.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.ui.shared.eventview.HasRegattaMetadata.RegattaState;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.gwt.ui.shared.media.MediaConstants;
import com.sap.sse.common.Util;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.ImageSize;
import com.sap.sse.common.media.MediaDescriptor;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.common.media.VideoDescriptor;

public final class HomeServiceUtil {
    private HomeServiceUtil() {
    }

    private static final int MINIMUM_IMAGE_HEIGHT_FOR_SAILING_PHOTOGRAPHY_IN_PIXELS = 500;
    private static final String STAGE_IMAGE_URL_SUBSTRING_INDICATOR_CASE_INSENSITIVE = "stage";
    private static final String THUMBNAIL_IMAGE_URL_SUBSTRING_INDICATOR_CASE_INSENSITIVE = "eventteaser";
    
    public static String findEventThumbnailImageUrlAsString(EventBase event) {
        URL url = findEventThumbnailImageUrl(event);
        return url == null ? null : url.toString();
    }
    
    public static boolean isFakeSeries(EventBase event) {
        Iterator<? extends LeaderboardGroupBase> lgIter = event.getLeaderboardGroups().iterator();
        if(!lgIter.hasNext()) {
            return false;
        }
        LeaderboardGroupBase lg = lgIter.next();
        if(lgIter.hasNext()) {
            return false;
        }
        return lg.hasOverallLeaderboard();
    }
    
    public static boolean isSingleRegatta(Event event) {
        boolean first = true;
        for(LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for(@SuppressWarnings("unused") Leaderboard lb: lg.getLeaderboards()) {
                if(!first) {
                    return false;
                }
                first = false;
            }
        }
        return true;
    }
    
    public static RegattaState calculateRegattaState(RegattaMetadataDTO regatta) {
        Date now = new Date();
        Date startDate = regatta.getStartDate();
        Date endDate = regatta.getEndDate();
        if(startDate != null && now.compareTo(startDate) < 0) {
            return RegattaState.UPCOMING;
        }
        if(endDate != null && now.compareTo(endDate) > 0) {
            return RegattaState.FINISHED;
        }
        if(startDate != null && now.compareTo(startDate) >= 0 && endDate != null && now.compareTo(endDate) <= 0) {
            return RegattaState.RUNNING;
        }
        return RegattaState.UNKNOWN;
    }
    
    public static EventState calculateEventState(EventBase event) {
        return calculateEventState(event.isPublic(), event.getStartDate().asDate(), event.getEndDate().asDate());
    }
    
    public static EventState calculateEventState(boolean isPublic, Date startDate, Date endDate) {
        Date now = new Date();
        if(now.compareTo(startDate) < 0) {
            if(isPublic) {
                return EventState.UPCOMING;
            }
            return EventState.PLANNED;
        }
        if(now.compareTo(endDate) > 0) {
            return EventState.FINISHED;
        }
        return EventState.RUNNING;
    }
    
    private static URL findEventThumbnailImageUrl(EventBase event) {

        final class ImageHolder {

            final int PERFECT_HEIGHT = 240;
            final int PERFECT_WIDTH = 370;
            final double PERFECT_RATIO = PERFECT_HEIGHT / (double) PERFECT_WIDTH;
            final URL url;
            final int height;
            final int width;
            final int size;
            final double ratio;

            ImageHolder() {
                this(null, null);
            }

            ImageHolder(URL url, ImageSize imageSize) {
                this(url, imageSize == null ? -1 : imageSize.getWidth(), imageSize == null ? -1 : imageSize.getHeight());
            }
            
            ImageHolder(URL url, int width, int height) {
                this.url = url;
                this.height = height;
                this.width = width;
                if(width == -1 || height == -1)  {
                    this.size = -1;
                    this.ratio = -1;
                } else {
                    this.size = height * width;
                    this.ratio = height / (double) width;
                }
            }

            boolean isBetterWorstcaseThan(ImageHolder otherImageHolder) {

                if (isNull()) {
                    return false;
                }

                if (this.url != null && url.toString().contains(STAGE_IMAGE_URL_SUBSTRING_INDICATOR_CASE_INSENSITIVE)) {
                    return false;
                }

                if (otherImageHolder.isNull()) {
                    return true;
                }

                if (!isBigEnough() && otherImageHolder.isBigEnough()) {
                    return true;
                }
                if (this.fitsRatio() && otherImageHolder.fitsRatio() && otherImageHolder.isBigEnough()
                        && otherImageHolder.isSmallerThan(this)) {
                    return true;
                }
                return false;
            }

            boolean isSmallerThan(ImageHolder otherImageHolder) {
                return size < otherImageHolder.size;
            }

            boolean isBigEnough() {
                return height >= PERFECT_HEIGHT && width >= PERFECT_WIDTH;
            }

            boolean fitsRatio() {
                return ratio == PERFECT_RATIO;
            }

            boolean isPerfectFit() {
                return (height == PERFECT_HEIGHT && width == PERFECT_WIDTH);
            }

            boolean isNull() {
                return size == -1;
            }
        }

        // search for name pattern
        for (URL imageUrl : event.getImageURLs()) {
            if (imageUrl.toString().toLowerCase().contains(THUMBNAIL_IMAGE_URL_SUBSTRING_INDICATOR_CASE_INSENSITIVE)) {
                return imageUrl;
            }
        }

        ImageHolder actualWorstcase = new ImageHolder();
        ImageHolder bestFit = new ImageHolder();

        for (URL candidateImageUrl : getPhotoGalleryImageURLs(event)) {
            ImageHolder candidate;
            try {
                candidate = new ImageHolder(candidateImageUrl, event.getImageSize(candidateImageUrl));
            } catch (Exception e) {
                candidate = new ImageHolder(candidateImageUrl, -1, -1);
            }

            if (candidate.isPerfectFit()) {
                return candidate.url;
            }

            if (candidate.fitsRatio() && candidate.isBigEnough()) {
                if (candidate.isSmallerThan(bestFit)) {
                    bestFit = candidate;
                }
            }

            if (candidate.isBetterWorstcaseThan(actualWorstcase)) {
                actualWorstcase = candidate;
            }

        }

        if (!bestFit.isNull()) {
            return bestFit.url;
        }
        if (!actualWorstcase.isNull()) {
            return actualWorstcase.url;
        }
        return null;
    }
    
    /**
     * The stage image is determined from the {@link #images} collection by a series of heuristics and fall-back
     * rules:
     * <ol>
     * <li>If one or more image has {@link MediaConstants#STAGE} tag, only they are considered candidates.</li>
     * <li>If no image has {@link MediaConstants#STAGE} tag, all images are considered
     * candidates.</li>
     * <li>From all candidates, the one with the biggest known size (determined by the product of width and height) is
     * chosen.</li>
     * <li>If the size isn't known for any candidate, the first candidate in {@link #imageURLs} is picked.</li>
     * </ol>
     */
    public static String getStageImageURLAsString(final EventBase event) {
        ImageDescriptor image = getStageImage(event);
        return image == null ? null : image.getURL().toString();
    }
    
    /**
     * The stage image is determined from the {@link #images} collection by a series of heuristics and fall-back
     * rules:
     * <ol>
     * <li>If one or more image has {@link MediaConstants#STAGE} tag, only they are considered candidates.</li>
     * <li>If no image has {@link MediaConstants#STAGE} tag, all images are considered
     * candidates.</li>
     * <li>From all candidates, the one with the biggest known size (determined by the product of width and height) is
     * chosen.</li>
     * <li>If the size isn't known for any candidate, the first candidate in {@link #imageURLs} is picked.</li>
     * </ol>
     */
    public static ImageDescriptor getStageImage(final EventBase event) {
        ImageDescriptor bestMatch = null;
        
        for(ImageDescriptor candidate : event.getImages()) {
            if(bestMatch == null || isBetterStageImage(candidate, bestMatch)) {
                bestMatch = candidate;
                continue;
            }
        }
        
        return bestMatch;
    }
    
    private static boolean isBetterStageImage(ImageDescriptor candidate, ImageDescriptor reference) {
        boolean stage = hasTag(candidate, MediaConstants.STAGE);
        boolean stageRef = hasTag(reference, MediaConstants.STAGE);
        if(stage != stageRef) {
            return stage;
        }
        boolean teaser = hasTag(candidate, MediaConstants.TEASER);
        boolean teaserRef = hasTag(reference, MediaConstants.TEASER);
        if(teaser != teaserRef) {
            return !teaser;
        }
        
        int size = imageSize(candidate);
        int sizeRef = imageSize(reference);
        if(size != sizeRef) {
            return size > sizeRef;
        }
        return candidate.getCreatedAtDate().compareTo(reference.getCreatedAtDate()) > 0;
    }

    private static int imageSize(ImageDescriptor candidate) {
        int width = candidate.getWidthInPx() == null ? 0 : candidate.getWidthInPx();
        int height = candidate.getHeightInPx() == null ? 0 : candidate.getHeightInPx();
        // TODO fallback to event.getImageSize() if size is 0/null?
        return width * height;
    }

    public static List<String> getPhotoGalleryImageURLsAsString(EventBase event) {
        List<URL> urls = getPhotoGalleryImageURLs(event);
        List<String> result = new ArrayList<String>(urls.size());
        for (URL url : urls) {
            result.add(url.toString());
        }
        return result;
    }

    public static List<URL> getPhotoGalleryImageURLs(EventBase event) {
        ImageDescriptor stageImage = getStageImage(event); // if set, exclude stage image from photo gallery
        URL stageImageURL = stageImage == null ? null : stageImage.getURL();
        List<URL> result = new ArrayList<>();
        Iterable<URL> imageURLs = event.getImageURLs();
        
        boolean first = true;
        for (Iterator<URL> iter = imageURLs.iterator(); iter.hasNext();  ) {
            URL imageUrl = iter.next();
            if(imageUrl.toString().toLowerCase().contains(THUMBNAIL_IMAGE_URL_SUBSTRING_INDICATOR_CASE_INSENSITIVE)) {
                continue;
            }
            if ((first && !iter.hasNext()) || !Util.equalsWithNull(imageUrl, stageImageURL)) {
                result.add(imageUrl);
            }
            first = false;
        }
        return result;
    }
    
    public static List<URL> getSailingLovesPhotographyImages(EventBase event) {
        final List<URL> acceptedImages = new LinkedList<>();
        for (URL candidateImageUrl : event.getImageURLs()) {
            try {
                ImageSize imageSize = event.getImageSize(candidateImageUrl);
                if (imageSize != null && imageSize.getHeight() > MINIMUM_IMAGE_HEIGHT_FOR_SAILING_PHOTOGRAPHY_IN_PIXELS) {
                    acceptedImages.add(candidateImageUrl);
                }
            } catch (Exception e) {
                // TODO what should we do here?
            }
        }
        return acceptedImages;
    }

    public static int calculateCompetitorsCount(Leaderboard sl) {
        return Util.size(sl.getCompetitors());
    }
    
    public static int calculateRaceCount(Leaderboard sl) {
        int count=0;
        for (RaceColumn column : sl.getRaceColumns()) {
            count += Util.size(column.getFleets());
        }
        return count;
    }
    
    public static int calculateTrackedRaceCount(Leaderboard sl) {
        int count=0;
        for (RaceColumn column : sl.getRaceColumns()) {
            for (Fleet fleet : column.getFleets()) {
                TrackedRace trackedRace = column.getTrackedRace(fleet);
                if(trackedRace != null && trackedRace.hasGPSData() && trackedRace.hasWindData()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public static String calculateBoatClass(Leaderboard leaderboard) {
        String boatClass = null;
        String boatClassDisplayName = null;
        for (Competitor competitor : leaderboard.getCompetitors()) {
            if(competitor.getBoat() != null && competitor.getBoat().getBoatClass() != null) {
                if(boatClass == null) {
                    boatClass = competitor.getBoat().getBoatClass().getName();
                    boatClassDisplayName = competitor.getBoat().getBoatClass().getDisplayName();
                } else if(competitor.getBoat().getBoatClass().getName() != null && !boatClass.equals(competitor.getBoat().getBoatClass().getName())) {
                    // more than one boatClass
                    return null;
                }
                
            }
        }
        return boatClassDisplayName != null ? boatClassDisplayName : boatClass;
    }

    public static boolean hasMedia(Event event) {
        return hasVideos(event) || hasPhotos(event);
    }
    
    public static boolean hasPhotos(Event event) {
        for(ImageDescriptor image : event.getImages()) {
            if(!hasTag(image, MediaConstants.TEASER)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasVideos(Event event) {
        return !Util.isEmpty(event.getVideoURLs());
    }

    public static boolean isPartOfEvent(Event event, Regatta regattaEntity) {
        for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
            if(courseArea.equals(regattaEntity.getDefaultCourseArea())) {
                return true;
            }
        }
        return false;
    }
    
    public static URL getRandomURL(Iterable<URL> urls) {
        if(Util.isEmpty(urls)) {
            return null;
        }
        int size = Util.size(urls);
        return Util.get(urls, new Random(size).nextInt(size));
    }
    
    public static VideoDescriptor getStageVideo(Event event, String localeName, Set<MimeType> acceptedTypes, Collection<String> rankedTags, boolean acceptOtherTags) {
        VideoDescriptor bestMatch = null;
        
        for (VideoDescriptor videoCandidate : event.getVideos()) {
            if(!acceptedTypes.contains(videoCandidate.getMimeType())) {
                continue;
            }
            
            if(!acceptOtherTags && !hasOneTag(videoCandidate, rankedTags)) {
                continue;
            }
            
            LocaleMatch localeMatch = matchLocale(videoCandidate, localeName);
            if(localeMatch == LocaleMatch.NO_MATCH) {
                continue;
            }
            
            if(bestMatch == null) {
                bestMatch = videoCandidate;
                continue;
            }
            
            int compareByTag = compareByTag(videoCandidate, bestMatch, rankedTags);
            if(compareByTag > 0 || (compareByTag == 0 && isBetter(videoCandidate, bestMatch, localeName))) {
                bestMatch = videoCandidate;
                continue;
            }
        }
        return bestMatch;
    }
    
    private static int compareByTag(VideoDescriptor videoCandidate, VideoDescriptor bestMatch,
            Collection<String> rankedTags) {
        for(String rankedTag : rankedTags) {
            boolean hasTag = hasTag(videoCandidate, rankedTag);
            boolean hasTagBestMatch = hasTag(bestMatch, rankedTag);
            if(hasTag != hasTagBestMatch) {
                return hasTag ? 1 : -1;
            }
        }
        return 0;
    }

    private static boolean isBetter(VideoDescriptor candidate, VideoDescriptor reference, String localeName) {
        LocaleMatch localeMatch = matchLocale(candidate, localeName);
        LocaleMatch localeMatchRef = matchLocale(reference, localeName);
        if(localeMatch != localeMatchRef) {
            return localeMatch.compareTo(localeMatchRef) < 0 ? true : false;
        }
        
        // TODO filter by length
        
        return candidate.getCreatedAtDate().compareTo(reference.getCreatedAtDate()) > 0;
    }
    
    private static boolean hasTag(MediaDescriptor videoCandidate, String tag) {
        return Util.contains(videoCandidate.getTags(), tag);
    }

    private static boolean hasOneTag(MediaDescriptor videoCandidate, Collection<String> acceptedTags) {
        for(String tag : videoCandidate.getTags()) {
            if(acceptedTags.contains(tag)) {
                return true;
            }
        }
        return false;
    }
    
    private enum LocaleMatch {
        PERFECT, NOT_TAGGED, EN_FALLBACK, NO_MATCH
    }

    private static LocaleMatch matchLocale(VideoDescriptor videoCandidate, String localeName) {
        boolean hasLocaleTag = false;
        boolean hasEn = false;
        for(String tag : videoCandidate.getTags()) {
            if(tag.equals(MediaConstants.LOCALE_PREFIX + localeName)) {
                return LocaleMatch.PERFECT;
            }
            hasLocaleTag |= tag.startsWith(MediaConstants.LOCALE_PREFIX);
            hasEn |= tag.equals(MediaConstants.LOCALE_EN);
        }
        if(!hasLocaleTag) {
            return LocaleMatch.NOT_TAGGED;
        }
        if(hasEn) {
            return LocaleMatch.EN_FALLBACK;
        }
        return LocaleMatch.NO_MATCH;
    }
}
