package com.sap.sailing.gwt.ui.client;


/**
 * Constants for remote services
 * The path value must fit with the corresponding path of the service in the web.xml
 * The @RemoteServiceRelativePath annotation can't be used to automatically resolve the right path because
 * it uses  GWT.getModuleBaseURL() to calculate the path which is different of each EntryPoint.
 * @author Frank
 *
 */
public interface RemoteServiceMappingConstants {
    public static final String userManagementServiceRemotePath = "service/usermanagement";
        
    public static final String mediaServiceRemotePath = "service/media";

    public static final String sailingServiceRemotePath = "service/sailing";

    public static final String simulatorServiceRemotePath = "service/simulator";

    public static final String dataMiningServiceRemotePath = "service/datamining";
}
