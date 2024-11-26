package com.sap.sailing.domain.igtimiadapter.riot;

import com.igtimi.IgtimiAPI.APIData;
import com.igtimi.IgtimiData.Data;
import com.igtimi.IgtimiDevice.DeviceManagement;
import com.igtimi.IgtimiStream.AckResponse;
import com.igtimi.IgtimiStream.ChannelManagement;
import com.igtimi.IgtimiStream.Msg;

public interface MsgVisitor {
    static void accept(Msg msg, MsgVisitor visitor) {
        switch (msg.getMsgCase()) {
        case ACK_RESPONSE:
            visitor.handleAckResponse(msg.getAckResponse());
            break;
        case API_DATA:
            visitor.handleApiData(msg.getApiData());
            break;
        case DATA:
            visitor.handleData(msg.getData());
            break;
        case DEVICE_MANAGEMENT:
            visitor.handleDeviceManagement(msg.getDeviceManagement());
            break;
        case CHANNEL_MANAGEMENT:
            visitor.handleChannelManagement(msg.getChannelManagement());
            break;
        default:
            throw new RuntimeException("Unknown message type "+msg.getMsgCase());
        }
    }
    
    default void handleAckResponse(AckResponse ackResponse) {}
    default void handleApiData(APIData apiData) {}
    default void handleChannelManagement(ChannelManagement channelManagement) {}
    default void handleData(Data data) {}
    default void handleDeviceManagement(DeviceManagement deviceManagement) {}
}
