package com.sap.sailing.domain.swisstimingreplayadapter;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;

import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingRaceConfig;

public interface SwissTimingReplayService {

    List<SwissTimingReplayRace> parseJSONObject(InputStream inputStream, String swissTimingUrlText) throws IOException,
            ParseException, org.json.simple.parser.ParseException;

    List<SwissTimingReplayRace> listReplayRaces(String swissTimingUrlText);

    SwissTimingRaceConfig loadRaceConfig(InputStream configDataStream) throws IOException,
            org.json.simple.parser.ParseException;

    DateFormat getStartTimeFormat();

}
