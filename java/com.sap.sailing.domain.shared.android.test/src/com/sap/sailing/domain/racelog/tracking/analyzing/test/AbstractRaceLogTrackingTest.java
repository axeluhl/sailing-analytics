package com.sap.sailing.domain.racelog.tracking.analyzing.test;

import org.junit.Before;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventRestoreFactory;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class AbstractRaceLogTrackingTest {

    protected RaceLog log;
    protected final RaceLogEventAuthor author = new RaceLogEventAuthorImpl("author0", 0);
    protected final RaceLogEventAuthor author1 = new RaceLogEventAuthorImpl("author1", 1);
    
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