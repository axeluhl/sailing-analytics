package com.sap.sailing.domain.queclinkadapter;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.Util.Pair;

/**
 * A Queclink GL 300 message type.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum MessageType {
    // commands to terminal with acknowledgement:
    BSI(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    CFG(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    CMD(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    DAT(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null), new Pair<>(Direction.RESP, /* TODO */ null)),
    DIS(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    DOG(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null), new Pair<>(Direction.RESP, /* TODO */ null)),
    FKS(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    FRI(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null), new Pair<>(Direction.RESP, /* TODO */ null)),
    GAM(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    GEO(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null), new Pair<>(Direction.RESP, /* TODO */ null)),
    GLM(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    HBM(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null), new Pair<>(Direction.RESP, /* TODO */ null)),
    JDC(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    NMD(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    NTS(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    OWN(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    PDS(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    PIN(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    QSS(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    RMD(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null), new Pair<>(Direction.RESP, /* TODO */ null)),
    RTO(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    RVC(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    SPD(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null), new Pair<>(Direction.RESP, /* TODO */ null)),
    SRI(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    TEM(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null), new Pair<>(Direction.RESP, /* TODO */ null)),
    TMA(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    UPC(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null), new Pair<>(Direction.RESP, /* TODO */ null)),
    WLT(new Pair<>(Direction.AT, /* TODO */ null), new Pair<>(Direction.ACK, /* TODO */ null)),
    // position-related reports:
    GCR(new Pair<>(Direction.RESP, /* TODO */ null)),
    IGL(new Pair<>(Direction.RESP, /* TODO */ null)),
    LBC(new Pair<>(Direction.RESP, /* TODO */ null)),
    NMR(new Pair<>(Direction.RESP, /* TODO */ null)),
    PNL(new Pair<>(Direction.RESP, /* TODO */ null)),
    RTL(new Pair<>(Direction.RESP, /* TODO */ null)),
    SOS(new Pair<>(Direction.RESP, /* TODO */ null)),
    // device information report:
    INF(new Pair<>(Direction.RESP, /* TODO */ null)),
    // reports for querying:
    ALL(new Pair<>(Direction.RESP, /* TODO */ null)),
    ALM(new Pair<>(Direction.RESP, /* TODO */ null)),
    ALS(new Pair<>(Direction.RESP, /* TODO */ null)),
    BAT(new Pair<>(Direction.RESP, /* TODO */ null)),
    BPL(new Pair<>(Direction.RESP, /* TODO */ null)),
    BTC(new Pair<>(Direction.RESP, /* TODO */ null)),
    CID(new Pair<>(Direction.RESP, /* TODO */ null)),
    CSQ(new Pair<>(Direction.RESP, /* TODO */ null)),
    EPF(new Pair<>(Direction.RESP, /* TODO */ null)),
    EPN(new Pair<>(Direction.RESP, /* TODO */ null)),
    EUC(new Pair<>(Direction.RESP, /* TODO */ null)),
    GPS(new Pair<>(Direction.RESP, /* TODO */ null)),
    GSM(new Pair<>(Direction.RESP, /* TODO */ null)),
    IGF(new Pair<>(Direction.RESP, /* TODO */ null)),
    IGN(new Pair<>(Direction.RESP, /* TODO */ null)),
    JDR(new Pair<>(Direction.RESP, /* TODO */ null)),
    JDS(new Pair<>(Direction.RESP, /* TODO */ null)),
    PDP(new Pair<>(Direction.RESP, /* TODO */ null)),
    PFA(new Pair<>(Direction.RESP, /* TODO */ null)),
    PNA(new Pair<>(Direction.RESP, /* TODO */ null)),
    STC(new Pair<>(Direction.RESP, /* TODO */ null)),
    STT(new Pair<>(Direction.RESP, /* TODO */ null)),
    SWG(new Pair<>(Direction.RESP, /* TODO */ null)),
    TMZ(new Pair<>(Direction.RESP, /* TODO */ null)),
    USW(new Pair<>(Direction.RESP, /* TODO */ null)),
    VER(new Pair<>(Direction.RESP, /* TODO */ null)),
    // heartbeat
    HBD(new Pair<>(Direction.ACK, HBDAcknowledgement.FACTORY), new Pair<>(Direction.SACK, HBDServerAcknowledgement.FACTORY))
    // there is a special "SACK" message for general server acknowledgement which differs from all other message formats
    // in that it doesn't contain the actual message type after the colon (":") but instead only has the count number as
    // a four-character HEX value after the colon. We don't provide a message type enum literal for this type of message
    // here.
    ;
    
    public enum Direction {
        /**
         * command sent to the "terminal" (tracking device)
         */
        AT,
        
        /**
         * message sent from the "terminal" (tracking device) to the server; in most cases an acknowledgement
         * of an {@link #AT} command, but in some cases a spontaneous emission, such as for a heart beat message;
         * see {@link MessageType#HBD}.
         */
        ACK,
        
        /**
         * response sent by the "terminal" (tracking device) to the server; for example, a single response to a request,
         * or a repetitive response sent by the device on a regular basis, such as position report.
         */
        RESP,
        
        /**
         * a server acknowledgement, sent from the server to the "terminal" (tracking device); without a message type
         * identifier, this 
         */
        SACK;
        
        public static Direction fromMessageStart(String messageStart) {
            return valueOf(messageStart.replace("+", "").replace(":", ""));
        }
    }

    private static Map<Pair<Direction, MessageType>, MessageFactory> messageFactories;
    
    private synchronized static Map<Pair<Direction, MessageType>, MessageFactory> getMessageFactories() {
        if (messageFactories == null) {
            messageFactories = new HashMap<>();
        }
        return messageFactories;
    }
    
    static {
        // support special case: a server acknowledgement with no message type:
        getMessageFactories().put(new Pair<>(Direction.SACK, null), ServerAcknowledgement.FACTORY);
    }
    
    @SafeVarargs
    private MessageType(Pair<Direction, MessageFactory>... factoryForDirection) {
        for (final Pair<Direction, MessageFactory> directionAndFactory : factoryForDirection) {
//            assert directionAndFactory.getB() != null; // TODO uncomment when all message types have their factories
            putMessageFactory(directionAndFactory.getA(), directionAndFactory.getB());
        }
    }
    
    private void putMessageFactory(Direction direction, MessageFactory factory) {
        getMessageFactories().put(new Pair<>(direction, this), factory);
    }
    
    public static MessageFactory getMessageFactory(Direction direction, MessageType messageType) {
        return getMessageFactories().get(new Pair<>(direction, messageType));
    }
}
