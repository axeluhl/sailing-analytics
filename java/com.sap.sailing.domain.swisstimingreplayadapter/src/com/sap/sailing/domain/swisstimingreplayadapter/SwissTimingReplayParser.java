package com.sap.sailing.domain.swisstimingreplayadapter;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingReplayParserImpl.PayloadMismatch;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingReplayParserImpl.PrematureEndOfData;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingReplayParserImpl.UnexpectedStartByte;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingReplayParserImpl.UnknownMessageIdentificationCode;

public interface SwissTimingReplayParser {

    void readData(InputStream urlInputStream, SwissTimingReplayListener replayListener) throws IOException,
            UnknownMessageIdentificationCode, UnexpectedStartByte, PayloadMismatch, PrematureEndOfData;
    
}
