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
    BSI(new Pair<>(Direction.AT, BSICommand.FACTORY), new Pair<>(Direction.AT, BSIAcknowledgement.FACTORY)),
    CFG(new Pair<>(Direction.AT, CFGCommand.FACTORY), new Pair<>(Direction.AT, CFGAcknowledgement.FACTORY)),
    CMD(new Pair<>(Direction.AT, CMDCommand.FACTORY), new Pair<>(Direction.AT, CMDAcknowledgement.FACTORY)),
    DAT(new Pair<>(Direction.AT, DATCommand.FACTORY), new Pair<>(Direction.AT, DATAcknowledgement.FACTORY), new Pair<>(Direction.AT, DATReport.FACTORY)),
    DIS(new Pair<>(Direction.AT, DISCommand.FACTORY), new Pair<>(Direction.AT, DISAcknowledgement.FACTORY)),
    DOG(new Pair<>(Direction.AT, DOGCommand.FACTORY), new Pair<>(Direction.AT, DOGAcknowledgement.FACTORY), new Pair<>(Direction.AT, DOGReport.FACTORY)),
    FKS(new Pair<>(Direction.AT, FKSCommand.FACTORY), new Pair<>(Direction.AT, FKSAcknowledgement.FACTORY)),
    FRI(new Pair<>(Direction.AT, FRICommand.FACTORY), new Pair<>(Direction.AT, FRIAcknowledgement.FACTORY), new Pair<>(Direction.AT, FRIReport.FACTORY)),
    GAM(new Pair<>(Direction.AT, GAMCommand.FACTORY), new Pair<>(Direction.AT, GAMAcknowledgement.FACTORY)),
    GEO(new Pair<>(Direction.AT, GEOCommand.FACTORY), new Pair<>(Direction.AT, GEOAcknowledgement.FACTORY), new Pair<>(Direction.AT, GEOReport.FACTORY)),
    GLM(new Pair<>(Direction.AT, GLMCommand.FACTORY), new Pair<>(Direction.AT, GLMAcknowledgement.FACTORY)),
    HBM(new Pair<>(Direction.AT, HBMCommand.FACTORY), new Pair<>(Direction.AT, HBMAcknowledgement.FACTORY), new Pair<>(Direction.AT, HBMReport.FACTORY)),
    JDC(new Pair<>(Direction.AT, JDCCommand.FACTORY), new Pair<>(Direction.AT, JDCAcknowledgement.FACTORY)),
    NMD(new Pair<>(Direction.AT, NMDCommand.FACTORY), new Pair<>(Direction.AT, NMDAcknowledgement.FACTORY)),
    NTS(new Pair<>(Direction.AT, NTSCommand.FACTORY), new Pair<>(Direction.AT, NTSAcknowledgement.FACTORY)),
    OWN(new Pair<>(Direction.AT, OWNCommand.FACTORY), new Pair<>(Direction.AT, OWNAcknowledgement.FACTORY)),
    PDS(new Pair<>(Direction.AT, PDSCommand.FACTORY), new Pair<>(Direction.AT, PDSAcknowledgement.FACTORY)),
    PIN(new Pair<>(Direction.AT, PINCommand.FACTORY), new Pair<>(Direction.AT, PINAcknowledgement.FACTORY)),
    QSS(new Pair<>(Direction.AT, QSSCommand.FACTORY), new Pair<>(Direction.AT, QSSAcknowledgement.FACTORY)),
    RMD(new Pair<>(Direction.AT, RMDCommand.FACTORY), new Pair<>(Direction.AT, RMDAcknowledgement.FACTORY), new Pair<>(Direction.AT, RMDReport.FACTORY)),
    RTO(new Pair<>(Direction.AT, RTOCommand.FACTORY), new Pair<>(Direction.AT, RTOAcknowledgement.FACTORY)),
    RVC(new Pair<>(Direction.AT, RVCCommand.FACTORY), new Pair<>(Direction.AT, RVCAcknowledgement.FACTORY)),
    SPD(new Pair<>(Direction.AT, SPDCommand.FACTORY), new Pair<>(Direction.AT, SPDAcknowledgement.FACTORY), new Pair<>(Direction.AT, SPDReport.FACTORY)),
    SRI(new Pair<>(Direction.AT, SRICommand.FACTORY), new Pair<>(Direction.AT, SRIAcknowledgement.FACTORY)),
    TEM(new Pair<>(Direction.AT, TEMCommand.FACTORY), new Pair<>(Direction.AT, TEMAcknowledgement.FACTORY), new Pair<>(Direction.AT, TEMReport.FACTORY)),
    TMA(new Pair<>(Direction.AT, TMACommand.FACTORY), new Pair<>(Direction.AT, TMAAcknowledgement.FACTORY)),
    UPC(new Pair<>(Direction.AT, UPCCommand.FACTORY), new Pair<>(Direction.AT, UPCAcknowledgement.FACTORY), new Pair<>(Direction.AT, UPCReport.FACTORY)),
    WLT(new Pair<>(Direction.AT, WLTCommand.FACTORY), new Pair<>(Direction.AT, WLTAcknowledgement.FACTORY)),
    // position-related reports:
    GCR(new Pair<>(Direction.AT, GCRReport.FACTORY)),
    IGL(new Pair<>(Direction.AT, IGLReport.FACTORY)),
    LBC(new Pair<>(Direction.AT, LBCReport.FACTORY)),
    NMR(new Pair<>(Direction.AT, NMRReport.FACTORY)),
    PNL(new Pair<>(Direction.AT, PNLReport.FACTORY)),
    RTL(new Pair<>(Direction.AT, RTLReport.FACTORY)),
    SOS(new Pair<>(Direction.AT, SOSReport.FACTORY)),
    // device information report:
    INF(new Pair<>(Direction.AT, INFReport.FACTORY)),
    // reports for querying:
    ALL(new Pair<>(Direction.AT, ALLReport.FACTORY)),
    ALM(new Pair<>(Direction.AT, ALMReport.FACTORY)),
    ALS(new Pair<>(Direction.AT, ALSReport.FACTORY)),
    BAT(new Pair<>(Direction.AT, BATReport.FACTORY)),
    BPL(new Pair<>(Direction.AT, BPLReport.FACTORY)),
    BTC(new Pair<>(Direction.AT, BTCReport.FACTORY)),
    CID(new Pair<>(Direction.AT, CIDReport.FACTORY)),
    CSQ(new Pair<>(Direction.AT, CSQReport.FACTORY)),
    EPF(new Pair<>(Direction.AT, EPFReport.FACTORY)),
    EPN(new Pair<>(Direction.AT, EPNReport.FACTORY)),
    EUC(new Pair<>(Direction.AT, EUCReport.FACTORY)),
    GPS(new Pair<>(Direction.AT, GPSReport.FACTORY)),
    GSM(new Pair<>(Direction.AT, GSMReport.FACTORY)),
    IGF(new Pair<>(Direction.AT, IGFReport.FACTORY)),
    IGN(new Pair<>(Direction.AT, IGNReport.FACTORY)),
    JDR(new Pair<>(Direction.AT, JDRReport.FACTORY)),
    JDS(new Pair<>(Direction.AT, JDSReport.FACTORY)),
    PDP(new Pair<>(Direction.AT, PDPReport.FACTORY)),
    PFA(new Pair<>(Direction.AT, PFAReport.FACTORY)),
    PNA(new Pair<>(Direction.AT, PNAReport.FACTORY)),
    STC(new Pair<>(Direction.AT, STCReport.FACTORY)),
    STT(new Pair<>(Direction.AT, STTReport.FACTORY)),
    SWG(new Pair<>(Direction.AT, SWGReport.FACTORY)),
    TMZ(new Pair<>(Direction.AT, TMZReport.FACTORY)),
    USW(new Pair<>(Direction.AT, USWReport.FACTORY)),
    VER(new Pair<>(Direction.AT, VERReport.FACTORY)),
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
