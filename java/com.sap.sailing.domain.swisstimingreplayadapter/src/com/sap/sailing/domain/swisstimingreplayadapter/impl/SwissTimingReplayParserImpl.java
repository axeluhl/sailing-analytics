package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.sap.sailing.domain.swisstimingreplayadapter.CompetitorStatus;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayListener;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayParser;

public class SwissTimingReplayParserImpl implements SwissTimingReplayParser {

    public static class UnknownMessageIdentificationCode extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public UnknownMessageIdentificationCode(byte messageIdentificationCode, long position) {
            super("Unknown message identification code: " +  messageIdentificationCode+" at position "+position);
        }
    }

    public static class UnexpectedStartByte extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
        public UnexpectedStartByte(byte unexpectedStartByte, long position) {
            super("Unexpected start byte: " + unexpectedStartByte+" at position "+position);
        }
    }

    public static class PayloadMismatch extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public PayloadMismatch(short remainingPayloadSize, long position) {
            super("Payload mismatch. Remaining payload: " + remainingPayloadSize+" at position "+position);
            
        }
    }

    public static class PrematureEndOfData extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public PrematureEndOfData(byte endByte, long position) {
            super("Premature end of data: " + endByte+" at position "+position);
            
        }
    }

    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int EOT = 0x04;

    private static final Charset CHARSET = Charset.forName("ISO-8859-1");

    /**
     * Parses a binary file containing Swiss Timing replay data.
     * @param urlInputStream
     * @param replayListener
     * @throws IOException For any stream read issues.
     * @throws UnknownMessageIdentificationCode When an unknown message identification codes has been encountered. See {@link MessageIdentificationCodes} for supported codes.
     * @throws UnexpectedStartByte When STX was expected but startByte has been encountered.
     * @throws PayloadMismatch When the actual payload was smaller than announced in the message header. 
     * @throws PrematureEndOfData When the end of a message was expected but a non ETX/EOT byte has been encountered. 
     */
    @Override
    public void readData(InputStream urlInputStream, SwissTimingReplayListener replayListener) throws IOException,
            UnknownMessageIdentificationCode, UnexpectedStartByte, PayloadMismatch, PrematureEndOfData {
        replayListener.progress(0.0);
        long numberOfBytesRead = 0;
        long initiallyAvailable = urlInputStream.available();
        DataInputStream data = new DataInputStream(urlInputStream);
        byte[] startByteBuffer = new byte[1];
        int readSuccess = data.read(startByteBuffer);
        while (readSuccess != -1) {
            numberOfBytesRead += readSuccess;
            replayListener.progress((double) (initiallyAvailable-urlInputStream.available())/(double) initiallyAvailable);
            byte startByte = startByteBuffer[0];
            if (startByte != STX) {
                throw new UnexpectedStartByte(startByte, numberOfBytesRead-1);
            }
            byte messageIdentificationCode = data.readByte();
            numberOfBytesRead++;
            short messageLength = data.readShort();
            numberOfBytesRead += 2;
            if (messageLength > 0) {
                short payloadSize = (short) (messageLength - 5); // payload is message minus STX, MIC, message length
                                                                 // and ETX/EOT --> = 5 byte
                short remainingPayloadSize = payloadSize;
                switch (messageIdentificationCode) {
                case MessageIdentificationCodes.MIC_00_Reference_Timestamp:
                    while (remainingPayloadSize > 0) {
                        long referenceTimestamp = data.readLong();
                        replayListener.referenceTimestamp(referenceTimestamp);
                        remainingPayloadSize -= 8;
                    }
                    break;
                case MessageIdentificationCodes.MIC_01_Reference_Location:
                    while (remainingPayloadSize > 0) {
                        int latitude = data.readInt();
                        int longitude = data.readInt();
                        replayListener.referenceLocation(latitude, longitude);
                        remainingPayloadSize -= 8;
                    }
                    break;
                case MessageIdentificationCodes.MIC_09_Keyframe_Index:
                    int keyFrameIndex = data.readInt();
                    replayListener.keyFrameIndex(keyFrameIndex);
                    remainingPayloadSize -= 4;
                    while (remainingPayloadSize > 0) {
                        int keyFrameIndexPosition = data.readInt();
                        replayListener.keyFrameIndexPosition(keyFrameIndexPosition);
                        remainingPayloadSize -= 4;
                    }
                    break;
                case MessageIdentificationCodes.MIC_10_RSC_CID:
                    while (remainingPayloadSize > 0) {
                        int length = 9;
                        String text = readString(data, length);
                        replayListener.raceID(text);
                        remainingPayloadSize -= length;
                    }
                    break;
                case MessageIdentificationCodes.MIC_113_Marks:
                    while (remainingPayloadSize > 0) {
                        byte markType = data.readByte();
                        String identifier = readString(data, 8);
                        byte index = data.readByte();
                        String id1 = readString(data, 8);
                        String id2 = readString(data, 8);
                        short windSpeed = data.readShort();
                        short windDirection = data.readShort();
                        replayListener.mark(new MarkType(markType), identifier, index, id1, id2, windSpeed, windDirection);
                        remainingPayloadSize -= 1 + 8 + 1 + 8 + 8 + 2 + 2;
                    }
                    break;
                case MessageIdentificationCodes.MIC_121_Competitor:
                    short competitorsCount = data.readShort();
                    replayListener.competitorsCount(competitorsCount);
                    remainingPayloadSize -= 2;
                    while (remainingPayloadSize > 0) {
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
                        remainingPayloadSize -= 4 + 3 + 1 + sailNumberLength + 1 + nameLength + 1 + 1 + 2 + 2 + 2;
                    }
                    break;
                case MessageIdentificationCodes.MIC_122_Frame_Meta:
                    while (remainingPayloadSize > 0) {
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
                        remainingPayloadSize -= 1 + 3 + 3 + 3 + 1 + 2 + 1 + 2 + 2 + 1 + messageTextLength + 1 + 1 + 1 + 2;
                    }
                    break;
                case MessageIdentificationCodes.MIC_123_Ranking_table:
                    short entriesCount = data.readShort();
                    replayListener.rankingsCount(entriesCount);
                    remainingPayloadSize -= 2;
                    while (remainingPayloadSize > 0) {
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
                        remainingPayloadSize -= 4 + 2 + 2 + 2 + 1 + 2 + 2 + 3 + 3 + 2;
                        for (int i = 0; i < marksCount; i++) {
                            short marksRank = data.readShort();
                            short marksRankIndex = data.readShort();
                            int marksGap = read3ByteInt(data);
                            int marksRaceTime = read3ByteInt(data);
                            replayListener.rankingMark(marksRank, marksRankIndex, marksGap, marksRaceTime);
                            remainingPayloadSize -= 2 + 2 + 3 + 3;
                        }
                    }
                    break;
                case MessageIdentificationCodes.MIC_124_Trackers:
                    short trackersCount = data.readShort();
                    replayListener.trackersCount(trackersCount);
                    remainingPayloadSize -= 2;
                    while (remainingPayloadSize > 0) {
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
                        remainingPayloadSize -= 4 + 4 + 4 + 2 + 2 + 2 + 2 + 1 + 2 + 2 + 2 + 2 + 2 + 2 + 2;
                    }
                    break;
                default:
                    throw new UnknownMessageIdentificationCode(messageIdentificationCode, numberOfBytesRead-3);
                }
                if (remainingPayloadSize != 0) {
                    throw new PayloadMismatch(remainingPayloadSize, numberOfBytesRead);
                } else {
                    numberOfBytesRead += payloadSize;
                }
                replayListener.progress((double) (initiallyAvailable-urlInputStream.available())/(double) initiallyAvailable);
            }
            byte endByte = data.readByte();
            numberOfBytesRead++;
            if (endByte == EOT) {
                replayListener.eot();
            } else if (endByte == ETX) {
                // observe whether ETX is interesting for anybody.
            } else {
                throw new PrematureEndOfData(endByte, numberOfBytesRead-1);
            }
            readSuccess = data.read(startByteBuffer);
        }
        replayListener.progress(1.0);
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
        byte[] bytes = new byte[length];
        data.read(bytes);
        String text = new String(bytes, CHARSET);
        return text;
    }

}
