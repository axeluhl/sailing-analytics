package com.sap.sailing.domain.tractracadapter;

public interface TracTracConnectionConstants {
    
    final String HOST_NAME = "germanmaster.traclive.dk";
    
    final int PORT_TUNNEL_LIVE = 1520;                          //For official usage use 4412;
    final int PORT_TUNNEL_STORED = PORT_TUNNEL_LIVE + 1;        //For official usage use 4413;
    
    final int PORT_LIVE = 1520;                                 //For official usage use 4400; for SAP-dedicated server use 1520
    final int PORT_STORED = PORT_LIVE + 1;                      //For official usage use 4401;
    
    final String HIDDEN_STATUS = "HIDDEN";
    final String ONLINE_STATUS = "ONLINE";
    final Object REPLAY_STATUS = "REPLAY";

}
