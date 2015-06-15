package com.sap.sailing.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
import com.sap.sse.common.media.MediaDescriptor;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.VideoDescriptor;

public final class HomeServiceUtil {
    
    private HomeServiceUtil() {
    }

    private static final int MINIMUM_IMAGE_HEIGHT_FOR_SAILING_PHOTOGRAPHY_IN_PIXELS = 500;
    
    public static String findEventThumbnailImageUrlAsString(EventBase event) {
        ImageDescriptor url = findEventThumbnailImage(event);
        return url == null ? null : url.getURL().toString();
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
    
    private static ImageDescriptor findEventThumbnailImage(EventBase event) {

        final class ImageHolder {

            final int PERFECT_HEIGHT = 240;
            final int PERFECT_WIDTH = 370;
            final double PERFECT_RATIO = PERFECT_HEIGHT / (double) PERFECT_WIDTH;
            final int height;
            final int width;
            final int size;
            final double ratio;
            final ImageDescriptor image;
            final boolean stage;

            ImageHolder() {
                this(null);
            }

            ImageHolder(ImageDescriptor image) {
                this.image = image;
                this.stage = image == null ? false : hasTag(image, MediaTagConstants.STAGE);
                this.height = image == null || image.getHeightInPx() == null ? 0 : image.getHeightInPx();
                this.width = image == null || image.getWidthInPx() == null ? 0 : image.getWidthInPx();
                this.size = height * width;
                if(width == 0)  {
                    this.ratio = -1;
                } else {
                    this.ratio = height / (double) width;
                }
            }

            boolean isBetterWorstcaseThan(ImageHolder otherImageHolder) {
                if (isNull()) {
                    return false;
                }

                if (stage) {
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
                return image == null;
            }
        }

        // search for name pattern
        for (ImageDescriptor imageUrl : event.getImages()) {
            if (hasTag(imageUrl, MediaTagConstants.TEASER)) {
                return imageUrl;
            }
        }

        ImageHolder actualWorstcase = new ImageHolder();
        ImageHolder bestFit = new ImageHolder();

        for (ImageDescriptor candidateImage : getPhotoGalleryImages(event)) {
            ImageHolder candidate = new ImageHolder(candidateImage);

            if (candidate.isPerfectFit()) {
                return candidate.image;
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
            return bestFit.image;
        }
        if (!actualWorstcase.isNull()) {
            return actualWorstcase.image;
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
        boolean stage = hasTag(candidate, MediaTagConstants.STAGE);
        boolean stageRef = hasTag(reference, MediaTagConstants.STAGE);
        if(stage != stageRef) {
            return stage;
        }
        boolean teaser = hasTag(candidate, MediaTagConstants.TEASER);
        boolean teaserRef = hasTag(reference, MediaTagConstants.TEASER);
        if(teaser != teaserRef) {
            return !teaser;
        }
        
        int size = candidate.getArea();
        int sizeRef = candidate.getArea();
        if(size != sizeRef) {
            return size > sizeRef;
        }
        return candidate.getCreatedAtDate().compareTo(reference.getCreatedAtDate()) > 0;
    }

    public static List<String> getPhotoGalleryImageURLsAsString(EventBase event) {
        List<ImageDescriptor> urls = getPhotoGalleryImages(event);
        List<String> result = new ArrayList<String>(urls.size());
        for (ImageDescriptor url : urls) {
            result.add(url.getURL().toString());
        }
        return result;
    }

    public static List<ImageDescriptor> getPhotoGalleryImages(EventBase event) {
        ImageDescriptor stageImage = getStageImage(event); // if set, exclude stage image from photo gallery
        List<ImageDescriptor> result = new ArrayList<>();
        Iterable<ImageDescriptor> imageURLs = event.getImages();
        
        boolean first = true;
        for (Iterator<ImageDescriptor> iter = imageURLs.iterator(); iter.hasNext();  ) {
            ImageDescriptor image = iter.next();
            if(hasTag(image, MediaTagConstants.TEASER)) {
                continue;
            }
            if ((first && !iter.hasNext()) || !Util.equalsWithNull(image, stageImage)) {
                result.add(image);
            }
            first = false;
        }
        return result;
    }
    
    public static List<ImageDescriptor> getSailingLovesPhotographyImages(EventBase event) {
        final List<ImageDescriptor> acceptedImages = new LinkedList<>();
        for (ImageDescriptor candidateImageUrl : event.getImages()) {
            if (candidateImageUrl.hasSize() && candidateImageUrl.getHeightInPx() > MINIMUM_IMAGE_HEIGHT_FOR_SAILING_PHOTOGRAPHY_IN_PIXELS) {
                acceptedImages.add(candidateImageUrl);
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
            if(!hasTag(image, MediaTagConstants.TEASER)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasVideos(Event event) {
        return !Util.isEmpty(event.getVideos());
    }

    public static boolean isPartOfEvent(Event event, Regatta regattaEntity) {
        for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
            if(courseArea.equals(regattaEntity.getDefaultCourseArea())) {
                return true;
            }
        }
        return false;
    }
    
    public static VideoDescriptor getRandomVideo(Iterable<VideoDescriptor> urls) {
        if(Util.isEmpty(urls)) {
            return null;
        }
        int size = Util.size(urls);
        return Util.get(urls, new Random(size).nextInt(size));
    }
    
    public static VideoDescriptor getStageVideo(Event event, Locale locale, Collection<String> rankedTags, boolean acceptOtherTags) {
        VideoDescriptor bestMatch = null;
        
        for (VideoDescriptor videoCandidate : event.getVideos()) {
            if(!MediaConstants.SUPPORTED_VIDEO_TYPES.contains(videoCandidate.getMimeType())) {
                continue;
            }
            
            if(!acceptOtherTags && !hasOneTag(videoCandidate, rankedTags)) {
                continue;
            }
            
            LocaleMatch localeMatch = matchLocale(videoCandidate, locale);
            if(localeMatch == LocaleMatch.NO_MATCH) {
                continue;
            }
            
            if(bestMatch == null) {
                bestMatch = videoCandidate;
                continue;
            }
            
            int compareByTag = compareByTag(videoCandidate, bestMatch, rankedTags);
            if(compareByTag > 0 || (compareByTag == 0 && isBetter(videoCandidate, bestMatch, locale))) {
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

    private static boolean isBetter(VideoDescriptor candidate, VideoDescriptor reference, Locale locale) {
        LocaleMatch localeMatch = matchLocale(candidate, locale);
        LocaleMatch localeMatchRef = matchLocale(reference, locale);
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

    private static LocaleMatch matchLocale(VideoDescriptor videoCandidate, Locale locale) {
        Locale localeOfCandidate = videoCandidate.getLocale();
        if(localeOfCandidate == null) {
            return LocaleMatch.NOT_TAGGED;
        }
        if(videoCandidate.getLocale().equals(locale)) {
            return LocaleMatch.PERFECT;
        }
        if(videoCandidate.getLocale().equals(Locale.ENGLISH)) {
            return LocaleMatch.EN_FALLBACK;
        }
        return LocaleMatch.NO_MATCH;
    }
}
