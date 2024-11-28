package com.sap.sailing.domain.igtimiadapter;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface Device extends HasId, WithQualifiedObjectIdentifier {
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

    User getOwner() throws IllegalStateException, ClientProtocolException, IOException, ParseException;
}
