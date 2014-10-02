package com.sap.sse.security.ui.client;


/**
 * Constants for remote services. The path value must fit with the corresponding path of the service in the web.xml The
 * <code>@RemoteServiceRelativePath</code> annotation can't be used to automatically resolve the right path because it
 * uses <code>GWT.getModuleBaseURL()</code> to calculate the path which is different for each EntryPoint.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RemoteServiceMappingConstants {
    /**
     * The hosting bundle's web context path from the OSGi manifest. This is the URL prefix under which all services are
     * registered based on their relative path specification in the <code>web.xml</code> descriptor.
     */
    public static final String WEB_CONTEXT_PATH = "security/ui";
    
    public static final String userManagementServiceRemotePath = "service/usermanagement";
}
