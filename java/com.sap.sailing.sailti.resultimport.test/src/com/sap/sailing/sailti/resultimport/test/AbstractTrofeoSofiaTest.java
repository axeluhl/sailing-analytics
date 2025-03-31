package com.sap.sailing.sailti.resultimport.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.impl.ResultDocumentDescriptorImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class AbstractTrofeoSofiaTest extends AbstractSailtiEventResourceTest {
    protected static final String TROFEO_SOFIA_TESTFILE_XRR_IQ_FOIL = "20220318-1003_335_IQ_FOIL_Men_SAILTI_v1.3.1.xml";
    protected static final String TROFEO_SOFIA_TESTFILE_XRR_470_MEN = "20220318-1003_13_470_Men_SAILTI_v1.3.1.xml";
    protected static final String TROFEO_SOFIA_TESTFILE_XRR_470_WOMEN = "20220318-1003_14_470_Women_SAILTI_v1.3.1.xml";
    protected static final String TROFEO_SOFIA_EVENT_NAME = "50 Trofeo S.A.R. Princesa Sofía IBEROSTAR";
    
    protected static final String BOAT_CLASS_49ER = "IQFoil Men";
    protected static final String BOAT_CLASS_470_MEN = "470 Men";
    protected static final String BOAT_CLASS_470_WOMEN = "470 Women";
    

    protected ResultDocumentProvider getTestDocumentProvider() {
        return new ResultDocumentProvider() {
            @Override
            public Iterable<ResultDocumentDescriptor> getResultDocumentDescriptors() throws IOException {
                try {
                    List<ResultDocumentDescriptor> result = new ArrayList<ResultDocumentDescriptor>();
                    Date _470MenDate = DatatypeConverter.parseDateTime("2022-03-17T13:14:20.000Z").getTime();
                    Date _470WomenDate = DatatypeConverter.parseDateTime("2022-03-17T13:14:39.000Z").getTime();
                    Date _49erDate = DatatypeConverter.parseDateTime("2022-03-17T13:15:04.000Z").getTime();
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(TROFEO_SOFIA_TESTFILE_XRR_470_MEN),
                            null, new MillisecondsTimePoint(_470MenDate), TROFEO_SOFIA_EVENT_NAME , BOAT_CLASS_470_MEN, BOAT_CLASS_470_MEN));
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(TROFEO_SOFIA_TESTFILE_XRR_470_WOMEN),
                            null, new MillisecondsTimePoint(_470WomenDate), TROFEO_SOFIA_EVENT_NAME , BOAT_CLASS_470_WOMEN, BOAT_CLASS_470_WOMEN));
                    result.add(new ResultDocumentDescriptorImpl(getInputStream(TROFEO_SOFIA_TESTFILE_XRR_IQ_FOIL),
                            null, new MillisecondsTimePoint(_49erDate), TROFEO_SOFIA_EVENT_NAME , BOAT_CLASS_49ER, BOAT_CLASS_49ER));
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
