package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.common.ImageSize;
import com.sap.sailing.domain.common.impl.ImageSizeImpl;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class EventImpl extends EventBaseImpl implements Event {
    private static final long serialVersionUID = 855135446595485715L;
    
    private final Set<Regatta> regattas;
    
    private ConcurrentLinkedQueue<LeaderboardGroup> leaderboardGroups;
    
    private transient ConcurrentHashMap<URL, Future<ImageSize>> imageSizeFetchers;
    private transient ExecutorService executor;
    
    public EventImpl(String name, TimePoint startDate, TimePoint endDate, String venueName, boolean isPublic, UUID id) {
        this(name, startDate, endDate, new VenueImpl(venueName), isPublic, id);
    }
    
    /**
     * @param venue must not be <code>null</code>
     */
    public EventImpl(String name, TimePoint startDate, TimePoint endDate, Venue venue, boolean isPublic, UUID id) {
        super(name, startDate, endDate, venue, isPublic, id);
        this.regattas = new HashSet<Regatta>();
        this.leaderboardGroups = new ConcurrentLinkedQueue<>();
        this.imageSizeFetchers = new ConcurrentHashMap<>();
        this.executor = Executors.newCachedThreadPool();
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        if (leaderboardGroups == null) {
            leaderboardGroups = new ConcurrentLinkedQueue<>();
        }
        this.imageSizeFetchers = new ConcurrentHashMap<>();
        this.executor = Executors.newCachedThreadPool();
    }
    
    @Override
    public Iterable<Regatta> getRegattas() {
        return Collections.unmodifiableSet(regattas);
    }

    @Override
    public void addRegatta(Regatta regatta) {
        regattas.add(regatta);
    }

    @Override
    public void removeRegatta(Regatta regatta) {
        regattas.remove(regatta);
    }
    
    public String toString() {
        return getId() + " " + getName() + " " + getVenue().getName() + " " + isPublic();
    }
    
    @Override
    public Iterable<LeaderboardGroup> getLeaderboardGroups() {
        return Collections.unmodifiableCollection(leaderboardGroups);
    }
    
    @Override
    public void setLeaderboardGroups(Iterable<LeaderboardGroup> leaderboardGroups) {
        this.leaderboardGroups.clear();
        Util.addAll(leaderboardGroups, this.leaderboardGroups);
    }

    @Override
    public void addLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        leaderboardGroups.add(leaderboardGroup);
    }

    @Override
    public boolean removeLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        return leaderboardGroups.remove(leaderboardGroup);
    }
    
    private Future<ImageSize> getOrCreateImageSizeCalculator(final URL imageURL) {
        Future<ImageSize> imageSizeFetcher = imageSizeFetchers.get(imageURL);
        if (imageSizeFetcher == null) {
            imageSizeFetcher = executor.submit(new Callable<ImageSize>() {
                @Override
                public ImageSize call() throws IOException {
                    ImageSize result = null;
                    ImageInputStream in = null;
                    try {
                        URLConnection conn = imageURL.openConnection();
                        in = ImageIO.createImageInputStream(conn.getInputStream());
                        final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
                        if (readers.hasNext()) {
                            ImageReader reader = readers.next();
                            try {
                                reader.setInput(in);
                                result = new ImageSizeImpl(reader.getWidth(0), reader.getHeight(0));
                            } finally {
                                reader.dispose();
                            }
                        }
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                    return result;
                }
            });
            imageSizeFetchers.put(imageURL, imageSizeFetcher);
        }
        return imageSizeFetcher;
    }
    
    @Override
    public ImageSize getImageSize(URL imageURL) throws InterruptedException, ExecutionException {
        Future<ImageSize> imageSizeCalculator = getOrCreateImageSizeCalculator(imageURL);
        return imageSizeCalculator.get();
    }

    @Override
    public void addImageURL(URL imageURL) {
        super.addImageURL(imageURL);
        refreshImageSizeFetcher(imageURL);
    }

    private void refreshImageSizeFetcher(URL imageURL) {
        if (imageURL != null) {
            removeImageSizeFetcher(imageURL);
            getOrCreateImageSizeCalculator(imageURL);
        }
    }

    @Override
    public void removeImageURL(URL imageURL) {
        super.removeImageURL(imageURL);
        removeImageSizeFetcher(imageURL);
    }

    private void removeImageSizeFetcher(URL imageURL) {
        if (imageURL != null) {
            imageSizeFetchers.remove(imageURL);
        }
    }

    @Override
    public void setImageURLs(Iterable<URL> imageURLs) {
        super.setImageURLs(imageURLs);
        if (imageURLs != null) {
            for (URL imageURL : imageURLs) {
                refreshImageSizeFetcher(imageURL);
            }
        }
    }

    @Override
    public void addSponsorImageURL(URL sponsorImageURL) {
        super.addSponsorImageURL(sponsorImageURL);
        refreshImageSizeFetcher(sponsorImageURL);
    }

    @Override
    public void removeSponsorImageURL(URL sponsorImageURL) {
        super.removeSponsorImageURL(sponsorImageURL);
        removeImageSizeFetcher(sponsorImageURL);
    }

    @Override
    public void setSponsorImageURLs(Iterable<URL> sponsorImageURLs) {
        super.setSponsorImageURLs(sponsorImageURLs);
        if (sponsorImageURLs != null) {
            for (URL imageURL : sponsorImageURLs) {
                refreshImageSizeFetcher(imageURL);
            }
        }
    }

    @Override
    public void setLogoImageURL(URL logoImageURL) {
        super.setLogoImageURL(logoImageURL);
        refreshImageSizeFetcher(logoImageURL);
    }
}
