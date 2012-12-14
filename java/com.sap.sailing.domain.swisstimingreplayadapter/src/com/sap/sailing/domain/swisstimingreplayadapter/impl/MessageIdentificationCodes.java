package com.sap.sailing.domain.swisstimingreplayadapter.impl;

public class MessageIdentificationCodes {

    //Only message types 0, 1, 9, 10, 113, 121, 122, 123, 124 need to be supported
    
    public static final byte MIC_00_Reference_Timestamp = 0;
    public static final byte MIC_01_Reference_Location = 1;
    public static final byte DEPRECATED_MIC_02_Protocol_Version = 2;
    public static final byte DEPRECATED_MIC_03_Reserved = 3;
    public static final byte DEPRECATED_MIC_04_Reserved = 4;
    public static final byte DEPRECATED_MIC_05_Reserved = 5;
    public static final byte DEPRECATED_MIC_06_Reserved = 6;
    public static final byte DEPRECATED_MIC_07_Reserved = 7;
    public static final byte DEPRECATED_MIC_08_Reserved = 8;
    public static final byte MIC_09_Keyframe_Index = 9;
    public static final byte MIC_10_RSC_CID = 10;
    public static final byte DEPRECATED_MIC_11_Competitors = 11;
    public static final byte DEPRECATED_MIC_112_Frame = 112;
    public static final byte MIC_113_Marks = 113;
    public static final byte DEPRECATED_MIC_115_Trackers = 115;
    public static final byte DEPRECATED_MIC_115_Trackers_if_gt_tracker_inactive = -41;
    public static final byte DEPRECATED_MIC_116_mark_crossover = 116;
    public static final byte DEPRECATED_MIC_12_Frame = 12;
    public static final byte MIC_121_Competitor = 121;
    public static final byte MIC_122_Frame_Meta = 122;
    public static final byte MIC_123_Ranking_table = 123;
    public static final byte MIC_124_Trackers = 124;
    public static final byte DEPRECATED_MIC_129_Bracket = -127;
    public static final byte DEPRECATED_MIC_13_Marks = 13;
    public static final byte DEPRECATED_MIC_130_Final_Ranking_Match_Races = -126;
    public static final byte DEPRECATED_MIC_131_Round_Robin = -125;
    public static final byte DEPRECATED_MIC_15_Trackers = 15;
    public static final byte DEPRECATED_MIC_25_Analogous_to_15_but_Indexing_like_215= 25;

}
