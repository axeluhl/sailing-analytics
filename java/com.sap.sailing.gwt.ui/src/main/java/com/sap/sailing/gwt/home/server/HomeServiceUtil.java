package com.sap.sailing.gwt.home.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.ImageSize;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;

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
     * The stage image is determined from the {@link #imageURLs} collection by a series of heuristics and fall-back
     * rules:
     * <ol>
     * <li>If one or more image URLs has "stage" (ignoring case) in its name, only they are considered candidates.</li>
     * <li>If no image URL has "stage" (ignoring case) in its name, all images from {@link #imageURLs} are considered
     * candidates.</li>
     * <li>From all candidates, the one with the biggest known size (determined by the product of width and height) is
     * chosen.</li>
     * <li>If the size isn't known for any candidate, the first candidate in {@link #imageURLs} is picked.</li>
     * </ol>
     */
    public static String getStageImageURLAsString(final EventBase event) {
        URL url = getStageImageURL(event);
        return url == null ? null : url.toString();
    }
    
    /**
     * The stage image is determined from the {@link #imageURLs} collection by a series of heuristics and fall-back
     * rules:
     * <ol>
     * <li>If one or more image URLs has "stage" (ignoring case) in its name, only they are considered candidates.</li>
     * <li>If no image URL has "stage" (ignoring case) in its name, all images from {@link #imageURLs} are considered
     * candidates.</li>
     * <li>From all candidates, the one with the biggest known size (determined by the product of width and height) is
     * chosen.</li>
     * <li>If the size isn't known for any candidate, the first candidate in {@link #imageURLs} is picked.</li>
     * </ol>
     */
    public static URL getStageImageURL(final EventBase event) {
        final URL result;
        if (!event.getImageURLs().iterator().hasNext()) {
            result = null;
        } else {
            Comparator<URL> stageImageComparator = new Comparator<URL>() {
                @Override
                public int compare(URL url1, URL url2) {
                    String o1 = url1.toString();
                    String o2 = url2.toString();
                    int result;
                    int o1NameHack = o1.toLowerCase().contains(STAGE_IMAGE_URL_SUBSTRING_INDICATOR_CASE_INSENSITIVE) ? 1 :
                           o1.toLowerCase().contains(THUMBNAIL_IMAGE_URL_SUBSTRING_INDICATOR_CASE_INSENSITIVE) ? -1 : 0;
                    int o2NameHack = o2.toLowerCase().contains(STAGE_IMAGE_URL_SUBSTRING_INDICATOR_CASE_INSENSITIVE) ? 1 :
                        o2.toLowerCase().contains(THUMBNAIL_IMAGE_URL_SUBSTRING_INDICATOR_CASE_INSENSITIVE) ? -1 : 0;
                    final int preResultBasedOnNameHack = o1NameHack - o2NameHack;
                    if (preResultBasedOnNameHack == 0) {
                        try {
                            result = compareBySize(url1, url2);
                        } catch (Exception e) {
                            result = 0;
                        }
                    } else {
                        result = preResultBasedOnNameHack;
                    }
                    return result;
                }

                private int compareBySize(URL o1, URL o2) throws InterruptedException, ExecutionException {
                    final int result;
                    final ImageSize o1Size = event.getImageSize(o1);
                    final ImageSize o2Size = event.getImageSize(o2);
                    result = (o1Size == null ? 0 : (o1Size.getWidth() * o1Size.getHeight()))
                            - (o2Size == null ? 0 : (o2Size.getWidth() * o2Size.getHeight()));
                    return result;
                }
            };
            List<URL> sortedImageURLs = new ArrayList<>();
            for (URL url : event.getImageURLs()) {
                sortedImageURLs.add(url);
            }
            Collections.sort(sortedImageURLs, stageImageComparator);
            result = sortedImageURLs.get(sortedImageURLs.size() - 1);
        }
        return result;
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
        URL stageImageURL = getStageImageURL(event); // if set, exclude stage image from photo gallery
        List<URL> result = new ArrayList<>();
        Iterable<URL> imageURLs = event.getImageURLs();
        
        boolean first = true;
        for (Iterator<URL> iter = imageURLs.iterator(); iter.hasNext();  ) {
            URL imageUrl = iter.next();
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
        int count=0;
        for (Iterator<Competitor> iterator = sl.getCompetitors().iterator(); iterator.hasNext();) {
            iterator.next();
            count++;
        }
        return count;
    }
    
    public static int calculateRaceCount(Leaderboard sl) {
        int count=0;
        for (RaceColumn column : sl.getRaceColumns()) {
            for (Iterator<? extends Fleet> iterator = column.getFleets().iterator(); iterator.hasNext();) {
                iterator.next();
                count++;
            }
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
       if(event.getVideoURLs().iterator().hasNext()) {
           return true;
       }
        Iterator<URL> iterator = event.getImageURLs().iterator();
        if(!iterator.hasNext()) {
            return false;
        }
        iterator.next();
        return iterator.hasNext();
    }

    public static boolean isPartOfEvent(Event event, Regatta regattaEntity) {
        for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
            if(courseArea.equals(regattaEntity.getDefaultCourseArea())) {
                return true;
            }
        }
        return false;
    }
    
    public static URL getRandomURL(Collection<URL> urls) {
        List<URL> videoURLs = new ArrayList<URL>((Collection<URL>)urls);
        if (videoURLs.isEmpty()) {
            return null;
        }
        return videoURLs.get(new Random(videoURLs.size()).nextInt(videoURLs.size()));
    }
}
