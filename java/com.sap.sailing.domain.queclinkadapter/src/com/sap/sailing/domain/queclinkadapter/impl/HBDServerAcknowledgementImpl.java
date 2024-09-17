package com.sap.sailing.domain.queclinkadapter.impl;

import java.text.ParseException;

import com.sap.sailing.domain.queclinkadapter.HBDServerAcknowledgement;
import com.sap.sailing.domain.queclinkadapter.MessageType.Direction;

public class HBDServerAcknowledgementImpl extends HBDMessageImpl implements HBDServerAcknowledgement {
    public HBDServerAcknowledgementImpl(int protocolVersion, short countNumber) {
        super(Direction.SACK, protocolVersion, countNumber);
    }

    public static HBDServerAcknowledgement createFromParameters(String[] parameterList) throws ParseException {
        return new HBDServerAcknowledgementImpl(QueclinkStreamParserImpl.parseProtocolVersionHex(parameterList[0]),
                QueclinkStreamParserImpl.parseCountNumberHex(parameterList[1]));
    }

    @Override
    public String[] getParameters() {
        return new String[] { QueclinkStreamParserImpl.formatProtocolVersionHex(getProtocolVersion()), QueclinkStreamParserImpl.formatCountNumberHex(getCountNumber()) };
    }
}
