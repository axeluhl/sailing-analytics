package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.impl.TimeRangeImpl;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

class MediaLibrary {

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
    private final ConcurrentMap<TimeRange, Set<MediaTrack>> cacheByInterval = new ConcurrentHashMap<TimeRange, Set<MediaTrack>>();

    private final NamedReentrantReadWriteLock lock = new NamedReentrantReadWriteLock(MediaLibrary.class.getName(), /* fair */ false);

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
     * http://en.wikipedia.org/wiki/Interval_tree.
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
    Set<MediaTrack> findMediaTracksInTimeRange(TimePoint startTime, TimePoint endTime) {

        if (startTime != null || (startTime != null && endTime != null && startTime.before(endTime))) {

            TimeRange interval = new TimeRangeImpl(startTime, endTime);
            LockUtil.lockForRead(lock);
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
                LockUtil.unlockAfterRead(lock);
            }
        }
        // else
        return Collections.emptySet();
    }

    public Collection<MediaTrack> findLiveMediaTracksForRace(String regattaName, String raceName) {
        Set<MediaTrack> result = new HashSet<MediaTrack>();
        for (MediaTrack mediaTrack : mediaTracksByDbId.values()) {
            if (mediaTrack.duration == null) {
                result.add(mediaTrack);
            }
        }
        return result;
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
        LockUtil.lockForWrite(lock);
        try {
            for (MediaTrack mediaTrack : mediaTracks) {
                this.mediaTracksByDbId.put(mediaTrack, mediaTrack);
                updateCache_Add(mediaTrack);
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    void deleteMediaTrack(MediaTrack mediaTrack) {
        LockUtil.lockForWrite(lock);
        try {
            MediaTrack deletedMediaTrack = mediaTracksByDbId.remove(mediaTrack);
            updateCache_Remove(deletedMediaTrack);
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    void titleChanged(MediaTrack changedMediaTrack) {
        LockUtil.lockForWrite(lock);
        try {
            MediaTrack mediaTrack = mediaTracksByDbId.get(changedMediaTrack);
            if (mediaTrack != null) {
                mediaTrack.title = changedMediaTrack.title;
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    void urlChanged(MediaTrack changedMediaTrack) {
        LockUtil.lockForWrite(lock);
        try {
            MediaTrack mediaTrack = mediaTracksByDbId.get(changedMediaTrack);
            if (mediaTrack != null) {
                mediaTrack.url = changedMediaTrack.url;
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    void mimeTypeChanged(MediaTrack changedMediaTrack) {
        LockUtil.lockForWrite(lock);
        try {
            MediaTrack mediaTrack = mediaTracksByDbId.get(changedMediaTrack);
            if (mediaTrack != null) {
                mediaTrack.mimeType = changedMediaTrack.mimeType;
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    void startTimeChanged(MediaTrack changedMediaTrack) {
        LockUtil.lockForWrite(lock);
        try {
            MediaTrack mediaTrack = mediaTracksByDbId.get(changedMediaTrack);
            if (mediaTrack != null) {
                mediaTrack.startTime = changedMediaTrack.startTime;
                updateCache_Change(mediaTrack);
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    void durationChanged(MediaTrack changedMediaTrack) {
        LockUtil.lockForWrite(lock);
        try {
            MediaTrack mediaTrack = mediaTracksByDbId.get(changedMediaTrack);
            if (mediaTrack != null) {
                mediaTrack.duration = changedMediaTrack.duration;
                updateCache_Change(mediaTrack);
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    /**
     * To be called only under write lock!
     */
    private void updateCache_Add(MediaTrack mediaTrack) {
        for (Entry<TimeRange, Set<MediaTrack>> cacheEntry : cacheByInterval.entrySet()) {
            TimeRange interval = cacheEntry.getKey();
            if (mediaTrack.overlapsWith(interval.from(), interval.to())) {
                cacheEntry.getValue().add(mediaTrack);
            }
        }
    }

    /**
     * To be called only under write lock!
     */
    private void updateCache_Change(MediaTrack mediaTrack) {
        for (Entry<TimeRange, Set<MediaTrack>> cacheEntry : cacheByInterval.entrySet()) {
            cacheEntry.getValue().remove(mediaTrack);
            TimeRange interval = cacheEntry.getKey();
            if (mediaTrack.overlapsWith(interval.from(), interval.to())) {
                cacheEntry.getValue().add(mediaTrack);
            }
        }
    }

    /**
     * To be called only under write lock!
     */
    private void updateCache_Remove(MediaTrack mediaTrack) {
        for (Entry<TimeRange, Set<MediaTrack>> cacheEntry : cacheByInterval.entrySet()) {
            cacheEntry.getValue().remove(mediaTrack);
        }
    }

    /**
     * Returns a non-live copy of the media tracks
     */
    Collection<MediaTrack> allTracks() {
        LockUtil.lockForRead(lock);
        try {
            return new ArrayList<MediaTrack>(mediaTracksByDbId.values());
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    void serialize(ObjectOutputStream stream) throws IOException {
        LockUtil.lockForRead(lock);
        try {
            stream.writeObject(new ArrayList<MediaTrack>(mediaTracksByDbId.values()));
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    @SuppressWarnings("unchecked")
    void deserialize(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        addMediaTracks((Collection<MediaTrack>) stream.readObject());
    }

    public void clear() {
        LockUtil.lockForWrite(lock);
        try {
            mediaTracksByDbId.clear();
            cacheByInterval.clear();
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    public MediaTrack lookupMediaTrack(MediaTrack mediaTrack) {
        return mediaTracksByDbId.get(mediaTrack);
    }

}
