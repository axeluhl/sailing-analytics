package com.sap.sailing.domain.igtimiadapter.riot;

import com.igtimi.IgtimiData.Acceleration;
import com.igtimi.IgtimiData.Angle;
import com.igtimi.IgtimiData.ApparentWindAngle;
import com.igtimi.IgtimiData.ApparentWindSpeed;
import com.igtimi.IgtimiData.Boolean;
import com.igtimi.IgtimiData.Command;
import com.igtimi.IgtimiData.Corrections;
import com.igtimi.IgtimiData.CourseOverGround;
import com.igtimi.IgtimiData.DataPoint;
import com.igtimi.IgtimiData.ElectricalCurrent;
import com.igtimi.IgtimiData.ElectricalPotential;
import com.igtimi.IgtimiData.Event;
import com.igtimi.IgtimiData.File;
import com.igtimi.IgtimiData.Force;
import com.igtimi.IgtimiData.Frequency;
import com.igtimi.IgtimiData.GNSS_Position;
import com.igtimi.IgtimiData.GNSS_Quality;
import com.igtimi.IgtimiData.GNSS_Sat_Count;
import com.igtimi.IgtimiData.Heading;
import com.igtimi.IgtimiData.HeadingMagnetic;
import com.igtimi.IgtimiData.HeartRate;
import com.igtimi.IgtimiData.Integer;
import com.igtimi.IgtimiData.JSON;
import com.igtimi.IgtimiData.Length;
import com.igtimi.IgtimiData.Log;
import com.igtimi.IgtimiData.Number;
import com.igtimi.IgtimiData.Orientation;
import com.igtimi.IgtimiData.Power;
import com.igtimi.IgtimiData.Pressure;
import com.igtimi.IgtimiData.SessionLogEntry;
import com.igtimi.IgtimiData.Speed;
import com.igtimi.IgtimiData.SpeedOverGround;
import com.igtimi.IgtimiData.SpeedThroughWater;
import com.igtimi.IgtimiData.Temperature;
import com.igtimi.IgtimiData.Text;
import com.igtimi.IgtimiData.TimeInterval;
import com.igtimi.IgtimiData.Torque;
import com.igtimi.IgtimiData.TrueWindDirection;
import com.igtimi.IgtimiData.TrueWindSpeed;

public interface DataPointVisitor {
    static void accept(DataPoint dataPoint, DataPointVisitor visitor) {
        switch (dataPoint.getDataCase()) {
        case ACC:
            visitor.handleAcc(dataPoint.getAcc());
            break;
        case AMP:
            visitor.handleAmp(dataPoint.getAmp());
            break;
        case ANG:
            visitor.handleAng(dataPoint.getAng());
            break;
        case AWA:
            visitor.handleAwa(dataPoint.getAwa());
            break;
        case AWS:
            visitor.handleAws(dataPoint.getAws());
            break;
        case BOOL:
            visitor.handleBool(dataPoint.getBool());
            break;
        case CMD:
            visitor.handleCmd(dataPoint.getCmd());
            break;
        case COG:
            visitor.handleCog(dataPoint.getCog());
            break;
        case COR:
            visitor.handleCor(dataPoint.getCor());
            break;
        case DATA_NOT_SET: // ignore message
            break;
        case EVENT:
            visitor.handleEvent(dataPoint.getEvent());
            break;
        case FILE:
            visitor.handleFile(dataPoint.getFile());
            break;
        case FOR:
            visitor.handleFor(dataPoint.getFor());
            break;
        case FREQ:
            visitor.handleFreq(dataPoint.getFreq());
            break;
        case HDG:
            visitor.handleHdg(dataPoint.getHdg());
            break;
        case HDGM:
            visitor.handleHdgm(dataPoint.getHdgm());
            break;
        case HR:
            visitor.handleHr(dataPoint.getHr());
            break;
        case INT:
            visitor.handleInt(dataPoint.getInt());
            break;
        case JSON:
            visitor.handleJson(dataPoint.getJson());
            break;
        case LEN:
            visitor.handleLen(dataPoint.getLen());
            break;
        case LOG:
            visitor.handleLog(dataPoint.getLog());
            break;
        case NUM:
            visitor.handleNum(dataPoint.getNum());
            break;
        case ORI:
            visitor.handleOri(dataPoint.getOri());
            break;
        case POS:
            visitor.handlePos(dataPoint.getPos());
            break;
        case PRESS:
            visitor.handlePress(dataPoint.getPress());
            break;
        case PWR:
            visitor.handlePwr(dataPoint.getPwr());
            break;
        case SATC:
            visitor.handleSatc(dataPoint.getSatc());
            break;
        case SATQ:
            visitor.handleSatq(dataPoint.getSatq());
            break;
        case SLE:
            visitor.handleSle(dataPoint.getSle());
            break;
        case SOG:
            visitor.handleSog(dataPoint.getSog());
            break;
        case SPD:
            visitor.handleSpd(dataPoint.getSpd());
            break;
        case STW:
            visitor.handleStw(dataPoint.getStw());
            break;
        case TEMP:
            visitor.handleTemp(dataPoint.getTemp());
            break;
        case TIME:
            visitor.handleTime(dataPoint.getTime());
            break;
        case TORQ:
            visitor.handleTorq(dataPoint.getTorq());
            break;
        case TWD:
            visitor.handleTwd(dataPoint.getTwd());
            break;
        case TWS:
            visitor.handleTws(dataPoint.getTws());
            break;
        case TXT:
            visitor.handleTxt(dataPoint.getTxt());
            break;
        case VOLT:
            visitor.handleVolt(dataPoint.getVolt());
            break;
        default:
            throw new RuntimeException("Unknown data type " + dataPoint.getDataCase());
        }
    }

    default void handleAcc(Acceleration acc) {
    }

    default void handleAmp(ElectricalCurrent amp) {
    }

    default void handleAng(Angle ang) {
    }

    default void handleAwa(ApparentWindAngle awa) {
    }

    default void handleAws(ApparentWindSpeed aws) {
    }

    default void handleBool(Boolean bool) {
    }

    default void handleCmd(Command cmd) {
    }

    default void handleCog(CourseOverGround cog) {
    }

    default void handleCor(Corrections cor) {
    }

    default void handleEvent(Event event) {
    }

    default void handleFile(File file) {
    }

    default void handleFor(Force for1) {
    }

    default void handleFreq(Frequency freq) {
    }

    default void handleHdg(Heading hdg) {
    }

    default void handleHdgm(HeadingMagnetic hdgm) {
    }

    default void handleHr(HeartRate hr) {
    }

    default void handleInt(Integer int1) {
    }

    default void handleJson(JSON json) {
    }

    default void handleLen(Length len) {
    }

    default void handleLog(Log log) {
    }

    default void handleNum(Number num) {
    }

    default void handleOri(Orientation ori) {
    }

    default void handlePos(GNSS_Position pos) {
    }

    default void handlePress(Pressure press) {
    }

    default void handlePwr(Power pwr) {
    }

    default void handleSatc(GNSS_Sat_Count satc) {
    }

    default void handleSatq(GNSS_Quality satq) {
    }

    default void handleSle(SessionLogEntry sle) {
    }

    default void handleSog(SpeedOverGround sog) {
    }

    default void handleSpd(Speed spd) {
    }

    default void handleStw(SpeedThroughWater stw) {
    }

    default void handleTemp(Temperature temp) {
    }

    default void handleTime(TimeInterval time) {
    }

    default void handleTorq(Torque torq) {
    }

    default void handleTwd(TrueWindDirection twd) {
    }

    default void handleTws(TrueWindSpeed tws) {
    }

    default void handleTxt(Text txt) {
    }

    default void handleVolt(ElectricalPotential volt) {
    }

}
