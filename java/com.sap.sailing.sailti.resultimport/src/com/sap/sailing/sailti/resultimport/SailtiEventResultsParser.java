package com.sap.sailing.sailti.resultimport;

import java.io.IOException;
import java.io.InputStream;

public interface SailtiEventResultsParser {
    public EventResultDescriptor getEventResult(InputStream is) throws IOException;
}
