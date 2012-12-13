package com.sap.sailing.domain.racecommittee.impl;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;
import com.sap.sailing.domain.tracking.impl.TrackImpl;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

public class RaceCommitteeEventTrackImpl extends TrackImpl<RaceCommitteeEvent> implements RaceCommitteeEventTrack {

	private static final long serialVersionUID = -176745401321893502L;

	public RaceCommitteeEventTrackImpl(String nameForReadWriteLock) {
		super(new ArrayListNavigableSet<Timed>(RaceCommitteeEventComparator.INSTANCE), nameForReadWriteLock);
	}

	@Override
	public void add(RaceCommitteeEvent event) {
		lockForWrite();
        try {
            getInternalRawFixes().add(event);
        } finally {
            unlockAfterWrite();
        }
        //TODO: Notify potential listeners
	}

}
