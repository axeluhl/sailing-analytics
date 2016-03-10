package com.sap.sailing.yachtscoring;

import java.io.IOException;
import java.io.InputStream;

public interface YachtscoringEventResultsParser {
    public EventResultDescriptor getEventResult(InputStream is) throws IOException;
}
