package com.sap.sailing.domain.igtimiadapter;

/**
 * When a device is shared to you, all that happens is that a DAW (Data Access Window) is being created for the time
 * interval that the device owner wants you to have access to the data from the device.<p>
 * 
 * So you’re not actually sharing a device per-se, rather you are granting access to data that it creates. This means
 * that you don’t see shared devices in the devices::list call – that’s only for devices that you own at the instant in
 * time the call is made. DAW’s allow granting access to data from a device for any interval in time; past, present or
 * future.<p>
 * 
 * So if you need to get a complete list of serials that you can request data for, whether or not you are the owner,
 * then you need to use the data_access_windows:: call.<p>
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface DataAccessWindow extends HasId, HasStartAndEndTime, HasPermissions {
    String getDeviceSerialNumber();

    SecurityEntity getRecipient();
}
