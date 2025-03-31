package com.sap.sailing.yachtscoring.resultimport.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.impl.ResultDocumentDescriptorImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class AbstractCharlstonRaceWeek2015Test extends AbstractYachtScoringEventResourceTest {
    protected static final String CHARLSTONRACEWEEK2015_TESTFILE_XRR = "event1220_CharlstonRaceWeek2015_xrr.xml";
    protected static final String CHARLSTONRACEWEEK2015_EVENT_NAME = "2015 Sperry Charleston Race Week, North Charleston, SC, USA";
    
    protected static final String BOAT_CLASS_J111 = "J 111";
    protected static final String BOAT_CLASS_MELGES24 = "Melges 24";
    

    protected ResultDocumentProvider getTestDocumentProvider() {
        return new ResultDocumentProvider() {
            @Override
            public Iterable<ResultDocumentDescriptor> getResultDocumentDescriptors() throws IOException {
                try {
                    List<ResultDocumentDescriptor> result = new ArrayList<ResultDocumentDescriptor>();
                    Date _J111Date = DatatypeConverter.parseDateTime("2016-01-19T12:55:08.000Z").getTime();
                    Date _Melges24Date = DatatypeConverter.parseDateTime("2016-01-19T12:55:08.000Z").getTime();
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(CHARLSTONRACEWEEK2015_TESTFILE_XRR),
                            null, new MillisecondsTimePoint(_J111Date), CHARLSTONRACEWEEK2015_EVENT_NAME , BOAT_CLASS_J111, BOAT_CLASS_J111));
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(CHARLSTONRACEWEEK2015_TESTFILE_XRR),
                            null, new MillisecondsTimePoint(_Melges24Date), CHARLSTONRACEWEEK2015_EVENT_NAME , BOAT_CLASS_MELGES24, BOAT_CLASS_MELGES24));
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
