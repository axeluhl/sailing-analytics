package com.sap.sailing.domain.tractracadapter;

public interface TracTracConnectionConstants {
    
    final String HOST_NAME = "germanmaster.traclive.dk";
    
    final int PORT_TUNNEL_LIVE = 1520;                          //For official usage use 4412;
    final int PORT_TUNNEL_STORED = PORT_TUNNEL_LIVE + 1;        //For official usage use 4413;
    
    final int PORT_LIVE = 1520;                                 //For official usage use 4400; for SAP-dedicated server use 1520
    final int PORT_STORED = PORT_LIVE + 1;                      //For official usage use 4401; for SAP-dedicated server use 1521
   
    /*
     * "Status" is the one that we used originally and is an enumerate with the values:
     * - HIDDEN: the race is hidden
     * - OFFLINE: the race is visible but it can not be loaded
     * - ONLINE: the race is visible and online
     * - REPLAY: the race is online and the mtb-file has been created (this value is no longer being used as we no longer display information about whether data are being retrieved from data base or file)

     * "Visibility" was introduced this year to improve some short comings of the "status" attribute and is an enumerate with the values:
     * - HIDDEN: the race is hidden (same meaning as above)
     * - OFFLINE: the race is visible but it can not be loaded (same meaning as above)
     * - ONLINE: the race can be loaded and it is in a live status (live or future)
     * - REPLAY: the race can be loaded and it is over (a race in the past)
     *   In addition to these we would like to add a new value for the visibility attribute called "UPCOMING" that means that the race is online (can be loaded) and it is in the future. 
     */
    
    final String HIDDEN_STATUS = "HIDDEN";
    final String ONLINE_STATUS = "ONLINE";
    final Object REPLAY_STATUS = "REPLAY";

    final String HIDDEN_VISIBILITY = "HIDDEN";
    final String ONLINE_VISIBILITY = "ONLINE";
    final Object REPLAY_VISIBILITY = "REPLAY";
}
