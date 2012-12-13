package com.sap.sailing.domain.swisstimingreplayadapter;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SwissTimingReplayService {

    static final String RACE_CONFIG_URL_TEMPLATE = "http://live.ota.st-sportservice.com/configuration?_race={0}&effective=1&additional=config";

    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int EOT = 0x04;

    private static final Charset CHARSET = Charset.forName("ISO-8859-1");

    public static final String SWISSTIMING_DATEFORMAT_PATTERN = "dd.MM.yyyy HH:mm";
    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT");

    public static List<SwissTimingReplayRace> listReplayRaces(String swissTimingUrlText) {
        URL raceListUrl;
        try {
            raceListUrl = new URL(swissTimingUrlText);
            List<SwissTimingReplayRace> races = parseJSONObject(raceListUrl.openStream(), swissTimingUrlText);
            // loadRaceConfigs(races);
            return races;
        } catch (Exception e) { // MalformedURLException | IOException | ParseException |
                                // org.json.simple.parser.ParseException)
            throw new RuntimeException(e);
        }
    }

    public static SwissTimingRaceConfig loadRaceConfig(String raceId) {
        try {
            URL configUrl = new URL(MessageFormat.format(RACE_CONFIG_URL_TEMPLATE, raceId));
            InputStream configDataStream = configUrl.openStream();
            return loadRaceConfig(configDataStream);
        } catch (Exception e) { // MalformedURLException | ParseException | org.json.simple.parser.ParseException)
            throw new RuntimeException(e);
        }
    }

    static SwissTimingRaceConfig loadRaceConfig(InputStream configDataStream) throws IOException,
            org.json.simple.parser.ParseException {
        JSONObject jsonRaceConfig = (JSONObject) new JSONParser().parse(new InputStreamReader(configDataStream));
        JSONObject jsonConfigEntry = (JSONObject) jsonRaceConfig.get("config");
        String latitude = (String) jsonRaceConfig.get("latitude");
        String longitude = (String) jsonRaceConfig.get("longitude");
        String country_code = (String) jsonRaceConfig.get("country_code");
        String gmt_offset = (String) jsonRaceConfig.get("gmt_offset");
        String location = (String) jsonRaceConfig.get("location");
        String event_name = (String) (jsonConfigEntry != null ? jsonConfigEntry.get("event_name") : null);
        String race_start_ts = (String) (jsonConfigEntry != null ? jsonConfigEntry.get("race_start_ts") : null);
        SwissTimingRaceConfig raceConfig = new SwissTimingRaceConfig(latitude, longitude, country_code, gmt_offset,
                location, event_name, race_start_ts);
        return raceConfig;
    }

    /**
     * 
     * @param inputStream
     *            The stream to read the JSON content from
     * @param swissTimingUrlText
     *            The URL where the stream has been taken from. Only used as information for later reference.
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws org.json.simple.parser.ParseException
     */
    static List<SwissTimingReplayRace> parseJSONObject(InputStream inputStream, String swissTimingUrlText)
            throws IOException, ParseException, org.json.simple.parser.ParseException {
        JSONArray json = (JSONArray) new JSONParser().parse(new InputStreamReader(inputStream));
        List<SwissTimingReplayRace> result = new ArrayList<SwissTimingReplayRace>();
        DateFormat startTimeFormat = getStartTimeFormat();
        for (Object raceEntry : json) {
            JSONObject jsonRaceEntry = (JSONObject) raceEntry;
            String startTimeText = (String) jsonRaceEntry.get("start");
            Date startTime = startTimeText == null ? null : startTimeFormat.parse(startTimeText);
            
            SwissTimingReplayRace replayRace = new SwissTimingReplayRace(swissTimingUrlText,
                    (String) jsonRaceEntry.get("flight_number"), (String) jsonRaceEntry.get("race_id"),
                    (String) jsonRaceEntry.get("rsc"), (String) jsonRaceEntry.get("name"),
                    (String) jsonRaceEntry.get("class"), startTime,
                    (String) jsonRaceEntry.get("link"));
            result.add(replayRace);
        }
        return result;
    }

    public static DateFormat getStartTimeFormat() {
        DateFormat startTimeFormat = new SimpleDateFormat(SWISSTIMING_DATEFORMAT_PATTERN);
        startTimeFormat.setTimeZone(DEFAULT_TIMEZONE);
        return startTimeFormat;
    }

    public static void loadRaceData(String link, SwissTimingReplayListener replayListener) {
        URL raceDataUrl;
        try {
            raceDataUrl = new URL("http://" + link);
            InputStream urlInputStream = raceDataUrl.openStream();
            readData(urlInputStream, replayListener);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    static void readData(InputStream urlInputStream, SwissTimingReplayListener replayListener) throws IOException {
        DataInputStream data = new DataInputStream(urlInputStream);

        byte[] startByteBuffer = new byte[1];
        int readSuccess = data.read(startByteBuffer);

        while (readSuccess != -1) {

            byte startByte = startByteBuffer[0];
            if (startByte != STX) {
                replayListener.unexpectedStartByte(startByte);
            }

            byte messageIdentificationCode = data.readByte();
            short messageLength = data.readShort();

            if (messageLength > 0) {
                short payloadSize = (short) (messageLength - 5); // payload is message minus STX, MIC, message length
                                                                 // and ETX/EOT --> = 5 byte

                switch (messageIdentificationCode) {
                case MessageIdentificationCodes.MIC_00_Reference_Timestamp:
                    while (payloadSize > 0) {
                        long referenceTimestamp = data.readLong();

                        replayListener.referenceTimestamp(referenceTimestamp);
                        payloadSize -= 8;
                    }
                    break;
                case MessageIdentificationCodes.MIC_01_Reference_Location:
                    while (payloadSize > 0) {
                        int latitude = data.readInt();
                        int longitude = data.readInt();

                        replayListener.referenceLocation(latitude, longitude);
                        payloadSize -= 8;
                    }
                    break;
                case MessageIdentificationCodes.MIC_09_Keyframe_Index:
                    int keyFrameIndex = data.readInt();

                    replayListener.keyFrameIndex(keyFrameIndex);
                    payloadSize -= 4;

                    while (payloadSize > 0) {
                        int keyFrameIndexPosition = data.readInt();

                        replayListener.keyFrameIndexPosition(keyFrameIndexPosition);
                        payloadSize -= 4;
                    }
                    break;
                case MessageIdentificationCodes.MIC_10_RSC_CID:
                    while (payloadSize > 0) {
                        int length = 9;
                        String text = readString(data, length);

                        replayListener.rsc_cid(text);
                        payloadSize -= length;
                    }
                    break;
                case MessageIdentificationCodes.MIC_113_Marks:
                    while (payloadSize > 0) {
                        byte markType = data.readByte();
                        String identifier = readString(data, 8);
                        byte index = data.readByte();
                        String id1 = readString(data, 8);
                        String id2 = readString(data, 8);
                        short windSpeed = data.readShort();
                        short windDirection = data.readShort();

                        replayListener.mark(new MarkType(markType), identifier, index, id1, id2, windSpeed,
                                windDirection);
                        payloadSize -= 1 + 8 + 1 + 8 + 8 + 2 + 2;
                    }
                    break;
                case MessageIdentificationCodes.MIC_121_Competitor:
                    short competitorsCount = data.readShort();
                    replayListener.competitorsCount(competitorsCount);
                    payloadSize -= 2;
                    while (payloadSize > 0) {
                        int hashValue = data.readInt();
                        String nation = readString(data, 3);
                        byte sailNumberLength = data.readByte();
                        String sailNumber = readString(data, sailNumberLength);
                        byte nameLength = data.readByte();
                        String name = readString(data, nameLength);
                        byte competitorStatus = data.readByte();
                        byte boatType = data.readByte();
                        short cRank_Bracket = data.readShort();
                        short cnPoints_x10_Bracket = data.readShort();
                        short ctPoints_x10_Winner = data.readShort();

                        replayListener.competitor(hashValue, nation, sailNumber, name,
                                CompetitorStatus.byCode(competitorStatus), BoatType.byCode(boatType), cRank_Bracket,
                                cnPoints_x10_Bracket, ctPoints_x10_Winner);
                        payloadSize -= 4 + 3 + 1 + sailNumberLength + 1 + nameLength + 1 + 1 + 2 + 2 + 2;
                    }
                    break;
                case MessageIdentificationCodes.MIC_122_Frame_Meta:
                    while (payloadSize > 0) {
                        byte cid = data.readByte();
                        int raceTime = read3ByteInt(data);
                        int startTime = read3ByteInt(data);
                        int estimatedStartTime = read3ByteInt(data);
                        byte raceStatus = data.readByte();
                        short distanceToNextMark = data.readShort();
                        byte weather = data.readByte();
                        short humidity = data.readShort();
                        short temperature = data.readShort();
                        byte messageTextLength = data.readByte();
                        String messageText = readString(data, messageTextLength);
                        byte cFlag = data.readByte();
                        byte rFlag = data.readByte();
                        byte duration = data.readByte();
                        short nm = data.readShort();

                        replayListener.frameMetaData(cid, raceTime, startTime, estimatedStartTime,
                                RaceStatus.byCode(raceStatus), distanceToNextMark, Weather.byCode(weather), humidity,
                                temperature, messageText, cFlag, rFlag, duration, nm);
                        payloadSize -= 1 + 3 + 3 + 3 + 1 + 2 + 1 + 2 + 2 + 1 + messageTextLength + 1 + 1 + 1 + 2;
                    }
                    break;
                case MessageIdentificationCodes.MIC_123_Ranking_table:
                    short entriesCount = data.readShort();
                    replayListener.rankingsCount(entriesCount);
                    payloadSize -= 2;
                    while (payloadSize > 0) {
                        int hashValue = data.readInt();
                        short rank = data.readShort();
                        short rankIndex = data.readShort();
                        short racePoints = data.readShort();
                        byte competitorStatus = data.readByte();
                        short finishRank = data.readShort();
                        short finishRankIndex = data.readShort();
                        int gap = read3ByteInt(data);
                        int raceTime = read3ByteInt(data);
                        short marksCount = data.readShort();
                        replayListener.ranking(hashValue, rank, rankIndex, racePoints,
                                CompetitorStatus.byCode(competitorStatus), finishRank, finishRankIndex, gap, raceTime);
                        payloadSize -= 4 + 2 + 2 + 2 + 1 + 2 + 2 + 3 + 3 + 2;
                        for (int i = 0; i < marksCount; i++) {
                            short marksRank = data.readShort();
                            short marksRankIndex = data.readShort();
                            int marksGap = read3ByteInt(data);
                            int marksRaceTime = read3ByteInt(data);
                            replayListener.rankingMark(marksRank, marksRankIndex, marksGap, marksRaceTime);
                            payloadSize -= 2 + 2 + 3 + 3;
                        }

                    }
                    break;
                case MessageIdentificationCodes.MIC_124_Trackers:
                    short trackersCount = data.readShort();
                    replayListener.trackersCount(trackersCount);
                    payloadSize -= 2;
                    while (payloadSize > 0) {
                        int hashValue = data.readInt();
                        int latitude = data.readInt();
                        int longitude = data.readInt();
                        short cog = data.readShort();
                        short sog = data.readShort();
                        short average_sog = data.readShort();
                        short vmg = data.readShort();
                        byte competitorStatus = data.readByte();
                        short rank = data.readShort();
                        short dtl = data.readShort();
                        short dtnm = data.readShort();
                        short nm = data.readShort();
                        short pRank = data.readShort();
                        short ptPoints = data.readShort();
                        short pnPoints = data.readShort();

                        replayListener.trackers(hashValue, latitude, longitude, cog, sog, average_sog, vmg,
                                CompetitorStatus.byCode(competitorStatus), rank, dtl, dtnm, nm, pRank, ptPoints,
                                pnPoints);

                        payloadSize -= 4 + 4 + 4 + 2 + 2 + 2 + 2 + 1 + 2 + 2 + 2 + 2 + 2 + 2 + 2;
                    }
                    break;
                default:
                    replayListener.unknownMessageIdentificationCode(messageIdentificationCode);
                    break;
                }

                if (payloadSize != 0) {
                    replayListener.payloadMismatch(payloadSize);
                }
            }

            byte endByte = data.readByte();
            if (endByte == EOT) {
                replayListener.eot();
            } else if (endByte == ETX) {
                // observe whether ETX is interesting for anybody.
            } else {
                replayListener.prematureEndOfData(endByte);
            }

            readSuccess = data.read(startByteBuffer);

        }
    }

    private static int read3ByteInt(DataInputStream data) throws IOException {
        byte highByte = data.readByte();
        byte midByte = data.readByte();
        byte lowByte = data.readByte();

        // turn signed to unsigned, then shift left
        int highInt = (highByte & 0xFF) << 16;
        int midInt = (midByte & 0xFF) << 8;
        int lowInt = lowByte & 0xFF;

        return highInt | midInt | lowInt;
    }

    private static String readString(DataInputStream data, int length) throws IOException {
        byte[] chars = new byte[length];
        data.read(chars);
        String text = new String(chars, CHARSET);
        return text;
    }

}
