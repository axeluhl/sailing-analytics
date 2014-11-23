package com.sap.sailing.domain.racelog.tracking.analyzing.test;

import org.junit.Before;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventRestoreFactory;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class AbstractRaceLogTrackingTest {

    protected RaceLog log;
    protected final AbstractLogEventAuthor author = new AbstractLogEventAuthorImpl("author0", 0);
    protected final AbstractLogEventAuthor author1 = new AbstractLogEventAuthorImpl("author1", 1);
    
    protected final TimePoint now = MillisecondsTimePoint.now();
    protected final RaceLogEventRestoreFactory factory = RaceLogEventRestoreFactory.INSTANCE;

    public AbstractRaceLogTrackingTest() {
        super();
    }

    @Before
    public void setup() {
        log = new RaceLogImpl("log");
    }

}