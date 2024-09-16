package com.sap.sailing.domain.queclinkadapter.impl;

import java.text.ParseException;

import com.sap.sailing.domain.queclinkadapter.HeartbeatAcknowledgement;
import com.sap.sailing.domain.queclinkadapter.MessageType.Direction;
import com.sap.sse.common.TimePoint;

public class HeartbeatAcknowledgementImpl extends HeartbeatMessageImpl implements HeartbeatAcknowledgement {
    private final String imei;
    private final String deviceName;
    private final TimePoint sendTime;

    public HeartbeatAcknowledgementImpl(int protocolVersion, String imei, String deviceName, TimePoint sendTime, short countNumber) {
        super(Direction.ACK, protocolVersion, countNumber);
        this.imei = imei;
        this.deviceName = deviceName;
        this.sendTime = sendTime;
    }
    
    @Override
    public String[] getParameters() {
        return new String[] {
                QueclinkStreamParserImpl.formatProtocolVersionHex(getProtocolVersion()),
                getImei(),
                getDeviceName(),
                getSendTime() == null ? "" : QueclinkStreamParserImpl.formatAsYYYYMMDDHHMMSS(getSendTime()),
                QueclinkStreamParserImpl.formatCountNumberHex(getCountNumber())
        };
    }
    
    public static HeartbeatAcknowledgement createFromParameters(String[] parameterList) throws ParseException {
        return new HeartbeatAcknowledgementImpl(QueclinkStreamParserImpl.parseProtocolVersionHex(parameterList[0]),
                /* imei */ parameterList[1],
                /* deviceName */ parameterList[2],
                /* sendTime */ QueclinkStreamParserImpl.parseTimeStamp(parameterList[3]),
                QueclinkStreamParserImpl.parseCountNumberHex(parameterList[4]));
    }

    @Override
    public TimePoint getSendTime() {
        return sendTime;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public String getImei() {
        return imei;
    }
}
