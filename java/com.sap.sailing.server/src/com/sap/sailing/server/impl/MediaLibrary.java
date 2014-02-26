package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaUtil;

class MediaLibrary {

    static class Interval {

        public final Date begin;
        public final Date end;

        public Interval(Date begin, Date end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof Interval) {
                Interval interval = (Interval) obj;
                return MediaUtil.equalsDatesAllowingNull(this.begin, interval.begin)
                        && MediaUtil.equalsDatesAllowingNull(this.end, interval.end);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new Date[] { begin, end });
        }

    }

    /**
     * The set of MediaTracks kept by this library. Due to complex write operations which require explicit write-locking
     * anyway there's no need to realize this collection with built-in concurrency support.
     */
    private final Map<MediaTrack, MediaTrack> mediaTracksByDbId = new HashMap<MediaTrack, MediaTrack>();

    /**
     * The cache used during lookups by overlapping interval. Realized as ConcurrentMap to prevent from need for
     * write-locking during lookup operation. Cached values are realized as Set to allow for quicker removal from cache
     * result in case a MediaTrack is being removed from the library or changes values such that it needs to be removed
     * from the cache.
     */
    private final ConcurrentMap<Interval, Set<MediaTrack>> cacheByInterval = new ConcurrentHashMap<Interval, Set<MediaTrack>>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

//    /**
//     * Sort in reverse order of start time! For equal start times compare dbId to distinguish different instances.
//     */
//    private static final Comparator<MediaTrack> COMPARATOR_BY_REVERSE_STARTTIME = new Comparator<MediaTrack>() {
//
//        @Override
//        public int compare(MediaTrack mediaTrack1, MediaTrack mediaTrack2) {
//            int result = compareDatesAllowingNull(mediaTrack2.startTime, mediaTrack1.startTime);
//            if (result == 0) {
//                return mediaTrack1.dbId.compareTo(mediaTrack2.dbId);
//            } else {
//                return result;
//            }
//        }
//
//    };

    /**
     * NOTE: The implementation of this lookup using simple linear search is a trade off between development effort and
     * performance gain.
     * 
     * Actually, efficient lookup of overlapping intervals is supposed to be performed using an interval tree, e.g.
     * http://thekevindolan.com/2010/02/interval-tree/
     * 
     * However, considering the expected low number of media entries and the expected high rate of cache hits doesn't
     * justify providing a dedicated interval tree implementation (given that there's none readily available).
     * 
     * TODO: A slight performance gain might be achieved assuming that more recent media tracks are requested more frequently
     * than older ones. Thus, sorting the list of media tracks by start time and starting linear search from the more
     * recent end might reduce loop cycles during linear search. E.g. use a SortedMap with COMPARATOR_BY_REVERSE_STARTTIME commented out above.
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    Set<MediaTrack> findMediaTracksInTimeRange(Date startTime, Date endTime) {

        if (startTime != null) {

            Interval interval = new Interval(startTime, endTime);
            readLock.lock();
            try {
                Set<MediaTrack> cachedMediaTracks = cacheByInterval.get(interval);
                if (cachedMediaTracks == null) {

                    Set<MediaTrack> result = new HashSet<MediaTrack>();
                    for (MediaTrack mediaTrack : mediaTracksByDbId.values()) {
                        if (mediaTrack.overlapsWith(startTime, endTime)) {
                            result.add(mediaTrack);
                        }
                    }
                    cachedMediaTracks = cacheByInterval.putIfAbsent(interval, result);
                    if (cachedMediaTracks != null) {
                        return cachedMediaTracks;
                    } else {
                        return result;
                    }
                } else {
                    return cachedMediaTracks;
                }

            } finally {
                readLock.unlock();
            }
        }
        // else
        return Collections.emptySet();
    }

    /**
     * Adding media tracks is expected to happen so rarely that we don't optimize for it and simply re-use the add-many
     * method.
     * 
     * @param mediaTrack
     */
    void addMediaTrack(MediaTrack mediaTrack) {
        addMediaTracks(Collections.singleton(mediaTrack));
    }

    void addMediaTracks(Collection<MediaTrack> mediaTracks) {
        writeLock.lock();
        try {
            for (MediaTrack mediaTrack : mediaTracks) {
                this.mediaTracksByDbId.put(mediaTrack, mediaTrack);
                updateCache_Add(mediaTrack);
            }
        } finally {
            writeLock.unlock();
        }
    }

    void deleteMediaTrack(MediaTrack mediaTrack) {
        writeLock.lock();
        try {
            MediaTrack deletedMediaTrack = mediaTracksByDbId.remove(mediaTrack);
            updateCache_Remove(deletedMediaTrack);
        } finally {
            writeLock.unlock();
        }
    }

    void titleChanged(MediaTrack changedMediaTrack) {
        writeLock.lock();
        try {
            MediaTrack mediaTrack = mediaTracksByDbId.get(changedMediaTrack);
            if (mediaTrack != null) {
                mediaTrack.title = changedMediaTrack.title;
            }
        } finally {
            writeLock.unlock();
        }
    }

    void urlChanged(MediaTrack changedMediaTrack) {
        writeLock.lock();
        try {
            MediaTrack mediaTrack = mediaTracksByDbId.get(changedMediaTrack);
            if (mediaTrack != null) {
                mediaTrack.url = changedMediaTrack.url;
            }
        } finally {
            writeLock.unlock();
        }
    }

    void mimeTypeChanged(MediaTrack changedMediaTrack) {
        writeLock.lock();
        try {
            MediaTrack mediaTrack = mediaTracksByDbId.get(changedMediaTrack);
            if (mediaTrack != null) {
                mediaTrack.mimeType = changedMediaTrack.mimeType;
            }
        } finally {
            writeLock.unlock();
        }
    }

    void startTimeChanged(MediaTrack changedMediaTrack) {
        writeLock.lock();
        try {
            MediaTrack mediaTrack = mediaTracksByDbId.get(changedMediaTrack);
            if (mediaTrack != null) {
                mediaTrack.startTime = changedMediaTrack.startTime;
                updateCache_Change(mediaTrack);
            }
        } finally {
            writeLock.unlock();
        }
    }

    void durationChanged(MediaTrack changedMediaTrack) {
        writeLock.lock();
        try {
            MediaTrack mediaTrack = mediaTracksByDbId.get(changedMediaTrack);
            if (mediaTrack != null) {
                mediaTrack.durationInMillis = changedMediaTrack.durationInMillis;
                updateCache_Change(mediaTrack);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * To be called only under write lock!
     */
    private void updateCache_Add(MediaTrack mediaTrack) {
        for (Entry<Interval, Set<MediaTrack>> cacheEntry : cacheByInterval.entrySet()) {
            Interval interval = cacheEntry.getKey();
            if (mediaTrack.overlapsWith(interval.begin, interval.end)) {
                cacheEntry.getValue().add(mediaTrack);
            }
        }
    }

    /**
     * To be called only under write lock!
     */
    private void updateCache_Change(MediaTrack mediaTrack) {
        for (Entry<Interval, Set<MediaTrack>> cacheEntry : cacheByInterval.entrySet()) {
            cacheEntry.getValue().remove(mediaTrack);
            Interval interval = cacheEntry.getKey();
            if (mediaTrack.overlapsWith(interval.begin, interval.end)) {
                cacheEntry.getValue().add(mediaTrack);
            }
        }
    }

    /**
     * To be called only under write lock!
     */
    private void updateCache_Remove(MediaTrack mediaTrack) {
        for (Entry<Interval, Set<MediaTrack>> cacheEntry : cacheByInterval.entrySet()) {
            cacheEntry.getValue().remove(mediaTrack);
        }
    }

    Collection<MediaTrack> allTracks() {
        readLock.lock();
        try {
            return new ArrayList<MediaTrack>(mediaTracksByDbId.values());
        } finally {
            readLock.unlock();
        }
    }

    void serialize(ObjectOutputStream stream) throws IOException {
        readLock.lock();
        try {
            stream.writeObject(new ArrayList<MediaTrack>(mediaTracksByDbId.values()));
        } finally {
            readLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    void deserialize(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        addMediaTracks((Collection<MediaTrack>) stream.readObject());
    }

    public void clear() {
        writeLock.lock();
        try {
            mediaTracksByDbId.clear();
            cacheByInterval.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public MediaTrack lookupMediaTrack(MediaTrack mediaTrack) {
        return mediaTracksByDbId.get(mediaTrack);
    }

}
