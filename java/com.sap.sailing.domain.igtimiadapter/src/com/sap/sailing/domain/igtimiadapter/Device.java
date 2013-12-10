package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

public interface Device extends HasId {
    /**
     * @return a string identifying the device, such as "AA-AA-AAAA"
     */
    String getSerialNumber();

    String getServiceTag();

    Long getOwnerId();

    Long getDeviceUserGroupId();

    Long getAdminDeviceUserGroupId();

    Iterable<Permission> getPermissions();

    Boolean getBlob();

    String getName();

    User getOwner() throws IllegalStateException, ClientProtocolException, IOException, ParseException;
}
