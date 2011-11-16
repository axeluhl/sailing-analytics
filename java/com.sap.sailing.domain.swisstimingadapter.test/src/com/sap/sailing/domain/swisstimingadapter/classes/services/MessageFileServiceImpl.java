package com.sap.sailing.domain.swisstimingadapter.classes.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.swisstimingadapter.Competitor;
import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.classes.messages.CAMMessage;
import com.sap.sailing.domain.swisstimingadapter.classes.messages.CCGMessage;
import com.sap.sailing.domain.swisstimingadapter.classes.messages.ClockAtMarkElement;
import com.sap.sailing.domain.swisstimingadapter.classes.messages.RACMessage;
import com.sap.sailing.domain.swisstimingadapter.classes.messages.RPDMessage;
import com.sap.sailing.domain.swisstimingadapter.classes.messages.RacePositionDataElement;
import com.sap.sailing.domain.swisstimingadapter.classes.messages.STLMessage;
import com.sap.sailing.domain.swisstimingadapter.classes.messages.TMDMessage;
import com.sap.sailing.domain.swisstimingadapter.classes.messages.TimingDataElement;
import com.sap.sailing.domain.swisstimingadapter.classes.services.Exceptions.MessageScriptParsingException;

import com.sap.sailing.domain.swisstimingadapter.impl.CompetitorImpl;
import com.sap.sailing.domain.swisstimingadapter.impl.MarkImpl;
import com.sap.sailing.domain.swisstimingadapter.impl.RaceImpl;
import com.sap.sailing.domain.swisstimingadapter.impl.SailMasterMessageImpl;

public class MessageFileServiceImpl implements MessageFileService {

    private File file;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String lastTimeZoneSuffix;
    private final DateFormat dateFormat;

    public MessageFileServiceImpl() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
    }

    @Override
    public void writeListToFile(File path, List<Object> msgList) throws IOException {
        writer = new BufferedWriter(new FileWriter(path));
        String tmp = "";
        for (Object object : msgList) {
            tmp = tmp + object.toString() + "\n";
        }
        System.out.println(tmp);
        writer.write(tmp);
        writer.flush();
    }

    @Override
    public List<Object> readListFromFile(File path) throws MessageScriptParsingException, IOException, ParseException {
        List<Object> resultList = new ArrayList<Object>();

        reader = new BufferedReader(new FileReader(path));
        List<String> strList = getFileIntoArrayListString(reader);
        List<SailMasterMessage> sailMasterMessageList = getSailMasterMessageList(strList);

        // Iterate throug sailmastermessagelist
        for (SailMasterMessage sailMasterMessage : sailMasterMessageList) {
            // Switch Case for all kinds of actions, just used
            switch (sailMasterMessage.getType()) {
            case RPD:
                resultList.add(getRPDMessage(sailMasterMessage));
                break;
            case RAC: // /
                resultList.add(getRACMessage(sailMasterMessage));
                break;
            case CCG: // /
                resultList.add(getCCGMessage(sailMasterMessage));
                break;
            case STL: // /
                resultList.add(getSTLMessage(sailMasterMessage));
                break;
            case CAM: // /
                resultList.add(getCAMMessage(sailMasterMessage));
                break;
            case TMD:
                resultList.add(getTMDMessage(sailMasterMessage));
                break;
            default:
                throw new MessageScriptParsingException(sailMasterMessage.getMessage(), file);
            }
        }
        return resultList;
    }

    private RPDMessage getRPDMessage(SailMasterMessage message) throws ParseException {
        String[] sections = message.getSections();

        String raceID = sections[1];
        int status = Integer.valueOf(sections[2]);
        Date timePoint = parseTimeAndDateISO(sections[3]);
        Date startTimeEstimatedStartTime = sections[4].trim().length() == 0 ? null
                : parseTimePrefixedWithISOToday(sections[4]);
        Long millisecondsSinceRaceStart = sections[5].trim().length() == 0 ? null
                : parseHHMMSSToMilliseconds(sections[5]);
        Integer nextMarkIndexForLeader = sections[6].trim().length() == 0 ? null : Integer.valueOf(sections[6]);
        Distance distanceToNextMarkForLeader = sections[7].trim().length() == 0 ? null : new MeterDistance(
                Double.valueOf(sections[7]));
        int count = Integer.valueOf(sections[8]);

        List<RacePositionDataElement> racePositionElements = new ArrayList<RacePositionDataElement>();

        for (int i = 0; i < count; i++) {
            int fixDetailIndex = 0;
            String[] fixSections = sections[9 + i].split(";");
            if (fixSections.length > 2) {
                String boatID = fixSections[fixDetailIndex++];
                int trackerTypeInt = Integer.valueOf(fixSections[fixDetailIndex++]);
                Long ageOfDataInMilliseconds = 1000l * Long.valueOf(fixSections[fixDetailIndex++]);
                double longitude = Double.valueOf(fixSections[fixDetailIndex++]);
                double latitude = Double.valueOf(fixSections[fixDetailIndex++]);
                Double speedOverGroundInKnots = Double.valueOf(fixSections[fixDetailIndex++]);
                Speed averageSpeedOverGround = fixSections[fixDetailIndex].trim().length() == 0 ? null
                        : new KnotSpeedImpl(Double.valueOf(fixSections[fixDetailIndex]));
                fixDetailIndex++;
                Speed velocityMadeGood = fixSections[fixDetailIndex].trim().length() == 0 ? null : new KnotSpeedImpl(
                        Double.valueOf(fixSections[fixDetailIndex]));
                fixDetailIndex++;
                SpeedWithBearing speed = new KnotSpeedWithBearingImpl(speedOverGroundInKnots, new DegreeBearingImpl(
                        Double.valueOf(fixSections[fixDetailIndex++])));

                Integer nextMarkIndex = fixSections.length <= fixDetailIndex
                        || fixSections[fixDetailIndex].trim().length() == 0 ? null : Integer
                        .valueOf(fixSections[fixDetailIndex]);
                fixDetailIndex++;
                Integer rank = fixSections.length <= fixDetailIndex || fixSections[fixDetailIndex].trim().length() == 0 ? null
                        : Integer.valueOf(fixSections[fixDetailIndex]);
                fixDetailIndex++;
                Distance distanceToLeader = fixSections.length <= fixDetailIndex
                        || fixSections[fixDetailIndex].trim().length() == 0 ? null : new MeterDistance(
                        Double.valueOf(fixSections[fixDetailIndex]));
                fixDetailIndex++;
                Distance distanceToNextMark = fixSections.length <= fixDetailIndex
                        || fixSections[fixDetailIndex].trim().length() == 0 ? null : new MeterDistance(
                        Double.valueOf(fixSections[fixDetailIndex]));
                fixDetailIndex++;

                racePositionElements.add(new RacePositionDataElement(boatID, trackerTypeInt, ageOfDataInMilliseconds
                        .longValue(), latitude, longitude, speedOverGroundInKnots.doubleValue(), velocityMadeGood
                        .getKnots(), averageSpeedOverGround.getKnots(), speed.getBearing().getDegrees(), nextMarkIndex
                        .intValue(), rank.intValue(), distanceToLeader.getMeters(), distanceToNextMark.getMeters()));
            }
        }
        return new RPDMessage(raceID, status, timePoint, startTimeEstimatedStartTime, new Date(
                millisecondsSinceRaceStart), nextMarkIndexForLeader.intValue(),
                distanceToNextMarkForLeader.getMeters(), racePositionElements);
    }

    private RACMessage getRACMessage(SailMasterMessage message) {
        int count = Integer.valueOf(message.getSections()[1]);
        List<Race> result = new ArrayList<Race>();
        for (int i = 0; i < count; i++) {
            String[] idAndDescription = message.getSections()[2 + i].split(";");
            result.add(new RaceImpl(idAndDescription[0], idAndDescription[1]));
        }
        return new RACMessage(result);
    }

    private CCGMessage getCCGMessage(SailMasterMessage message) {
        String raceId = message.getSections()[1];
        int count = Integer.valueOf(message.getSections()[2]);
        List<Mark> marks = new ArrayList<Mark>();
        for (int i = 0; i < count; i++) {
            String[] markDetails = message.getSections()[3 + i].split(";");
            marks.add(new MarkImpl(markDetails[1], Integer.valueOf(markDetails[0]), Arrays.asList(markDetails).subList(
                    2, markDetails.length)));
        }
        return new CCGMessage(raceId, marks);
    }

    private STLMessage getSTLMessage(SailMasterMessage message) {
        String raceId = message.getSections()[1];
        ArrayList<Competitor> competitors = new ArrayList<Competitor>();
        int count = Integer.valueOf(message.getSections()[2]);
        for (int i = 0; i < count; i++) {
            String[] competitorDetails = message.getSections()[3 + i].split(";");
            competitors.add(new CompetitorImpl(competitorDetails[0], competitorDetails[1], competitorDetails[2]));
        }
        return new STLMessage(raceId, competitors);
    }

    private CAMMessage getCAMMessage(SailMasterMessage message) throws ParseException {
        String raceId = message.getSections()[1];
        List<ClockAtMarkElement> result = new ArrayList<ClockAtMarkElement>();
        int count = Integer.valueOf(message.getSections()[2]);
        for (int i = 0; i < count; i++) {
            String[] clockAtMarkDetail = message.getSections()[3 + i].split(";");
            int markIndex = Integer.valueOf(clockAtMarkDetail[0]);
            Date timePoint = clockAtMarkDetail.length <= 1 || clockAtMarkDetail[1].trim().length() == 0 ? null
                    : parseHHMMSSToDate(clockAtMarkDetail[1]);
            result.add(new ClockAtMarkElement(markIndex, timePoint, clockAtMarkDetail.length <= 2 ? null
                    : clockAtMarkDetail[2]));
        }
        return new CAMMessage(raceId, result);
    }

    private TMDMessage getTMDMessage(SailMasterMessage message) throws ParseException {
        String raceID = message.getSections()[1];
        String boatID = message.getSections()[2];
        int count = Integer.valueOf(message.getSections()[3]);
        List<TimingDataElement> timingDataelementList = new ArrayList<TimingDataElement>();
        for (int i = 0; i < count; i++) {
            String[] details = message.getSections()[4 + i].split(";");
            Integer markIndex = details.length <= 0 || details[0].trim().length() == 0 ? null : Integer
                    .valueOf(details[0]);
            Integer rank = details.length <= 1 || details[1].trim().length() == 0 ? null : Integer.valueOf(details[1]);
            Date timeSinceStart = details[4].trim().length() == 0 ? null : parseTimePrefixedWithISOToday(details[4]);
            timingDataelementList.add(new TimingDataElement(markIndex, rank, timeSinceStart));
        }
        return new TMDMessage(raceID, boatID, timingDataelementList);
    }

    private Date parseTimeAndDateISO(String timeAndDateISO) throws ParseException {
        char timeZoneIndicator = timeAndDateISO.charAt(timeAndDateISO.length() - 6);
        if ((timeZoneIndicator == '+' || timeZoneIndicator == '-')
                && timeAndDateISO.charAt(timeAndDateISO.length() - 3) == ':') {
            timeAndDateISO = timeAndDateISO.substring(0, timeAndDateISO.length() - 3)
                    + timeAndDateISO.substring(timeAndDateISO.length() - 2);
            lastTimeZoneSuffix = timeAndDateISO.substring(timeAndDateISO.length() - 5);
        }
        synchronized (dateFormat) {
            return dateFormat.parse(timeAndDateISO);
        }
    }

    private Date parseTimePrefixedWithISOToday(String timeHHMMSS) throws ParseException {
        synchronized (dateFormat) {
            return dateFormat.parse(prefixTimeWithISOTodayAndSuffixWithTimezoneIndicator(timeHHMMSS));
        }
    }

    private String prefixTimeWithISOTodayAndSuffixWithTimezoneIndicator(String time) {
        synchronized (dateFormat) {
            return dateFormat.format(new Date()).substring(0, "yyyy-mm-ddT".length()) + time + lastTimeZoneSuffix;
        }
    }

    private long parseHHMMSSToMilliseconds(String hhmmss) {
        String[] timeDetail = hhmmss.split(":");
        long millisecondsSinceStart = 1000 * (Long.valueOf(timeDetail[2]) + 60 * Long.valueOf(timeDetail[1]) + 3600 * Long
                .valueOf(timeDetail[0]));
        return millisecondsSinceStart;
    }
    
    private Date parseHHMMSSToDate(String hhmmss){
        String[] timeDetail = hhmmss.split(":");
        long millisecondsSinceStart = 1000 * (Long.valueOf(timeDetail[2]) + 60 * Long.valueOf(timeDetail[1]) + 3600 * Long
                .valueOf(timeDetail[0]));
        return new Date(millisecondsSinceStart);
    }

    private List<SailMasterMessage> getSailMasterMessageList(List<String> strList) {
        // Add Strings of stringmessageList into a ArrayList of type SailMasterMessage
        List<SailMasterMessage> sailMasterMessageList = new ArrayList<SailMasterMessage>();
        for (String s : strList) {
            sailMasterMessageList.add(new SailMasterMessageImpl(s, new Long(0)));
        }
        return sailMasterMessageList;
    }

    private List<String> getFileIntoArrayListString(BufferedReader reader) throws IOException {
        List<String> strList = new ArrayList<String>();
        String tmp = "";
        while ((tmp = reader.readLine()) != null) {
            strList.add(tmp);
        }
        return strList;
    }

}
