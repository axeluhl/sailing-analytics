package com.sap.sailing.domain.queclinkadapter;

/**
 * A Queclink GL 300 message type.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum MessageType {
    // commands to terminal with acknowledgement:
    BSI(Direction.AT, Direction.ACK),
    CFG(Direction.AT, Direction.ACK),
    CMD(Direction.AT, Direction.ACK),
    DAT(Direction.AT, Direction.ACK, Direction.RESP),
    DIS(Direction.AT, Direction.ACK),
    DOG(Direction.AT, Direction.ACK, Direction.RESP),
    FKS(Direction.AT, Direction.ACK),
    FRI(Direction.AT, Direction.ACK, Direction.RESP),
    GAM(Direction.AT, Direction.ACK),
    GEO(Direction.AT, Direction.ACK, Direction.RESP),
    GLM(Direction.AT, Direction.ACK),
    HBM(Direction.AT, Direction.ACK, Direction.RESP),
    JDC(Direction.AT, Direction.ACK),
    NMD(Direction.AT, Direction.ACK),
    NTS(Direction.AT, Direction.ACK),
    OWN(Direction.AT, Direction.ACK),
    PDS(Direction.AT, Direction.ACK),
    PIN(Direction.AT, Direction.ACK),
    QSS(Direction.AT, Direction.ACK),
    RMD(Direction.AT, Direction.ACK, Direction.RESP),
    RTO(Direction.AT, Direction.ACK),
    RVC(Direction.AT, Direction.ACK),
    SPD(Direction.AT, Direction.ACK, Direction.RESP),
    SRI(Direction.AT, Direction.ACK),
    TEM(Direction.AT, Direction.ACK, Direction.RESP),
    TMA(Direction.AT, Direction.ACK),
    UPC(Direction.AT, Direction.ACK, Direction.RESP),
    WLT(Direction.AT, Direction.ACK),
    // position-related reports:
    GCR(Direction.RESP),
    IGL(Direction.RESP),
    LBC(Direction.RESP),
    NMR(Direction.RESP),
    PNL(Direction.RESP),
    RTL(Direction.RESP),
    SOS(Direction.RESP),
    // device information report:
    INF(Direction.RESP),
    // reports for querying:
    ALL(Direction.RESP),
    ALM(Direction.RESP),
    ALS(Direction.RESP),
    BAT(Direction.RESP),
    BPL(Direction.RESP),
    BTC(Direction.RESP),
    CID(Direction.RESP),
    CSQ(Direction.RESP),
    EPF(Direction.RESP),
    EPN(Direction.RESP),
    EUC(Direction.RESP),
    GPS(Direction.RESP),
    GSM(Direction.RESP),
    IGF(Direction.RESP),
    IGN(Direction.RESP),
    JDR(Direction.RESP),
    JDS(Direction.RESP),
    PDP(Direction.RESP),
    PFA(Direction.RESP),
    PNA(Direction.RESP),
    STC(Direction.RESP),
    STT(Direction.RESP),
    SWG(Direction.RESP),
    TMZ(Direction.RESP),
    USW(Direction.RESP),
    VER(Direction.RESP),
    // heartbeat
    HBD(Direction.ACK, Direction.SACK)
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
    }

    private final Direction[] directions;
    
    private MessageType(Direction...directions) {
        this.directions = directions;
    }

    public Direction[] getDirections() {
        return directions;
    }
}
