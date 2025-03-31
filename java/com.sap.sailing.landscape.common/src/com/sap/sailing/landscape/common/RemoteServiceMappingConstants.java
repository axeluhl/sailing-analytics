package com.sap.sailing.landscape.common;

/**
 * Constants for remote services. The path value must fit with the corresponding path of the service in the web.xml The
 * <code>@RemoteServiceRelativePath</code> annotation can't be used to automatically resolve the right path because it
 * uses <code>GWT.getModuleBaseURL()</code> to calculate the path which is different for each EntryPoint.
 * 
 * @author Frank
 *
 */
public interface RemoteServiceMappingConstants {
    /**
     * The hosting bundle's web context path from the OSGi manifest's {@code Web-ContextPath} declaration. This is the
     * URL prefix under which all services are registered based on their relative path specification in the
     * <code>web.xml</code> descriptor.
     */
    String WEB_CONTEXT_PATH = "gwt";

    String mediaServiceRemotePath = "service/media";
    
    String sailingServiceRemotePath = "service/sailing";
    
    /**
     * the prefix to use when constructing a load balancer listener rule's path condition from a sharding key.
     * See also {@link ShardProcedure#getPathConditionForShardingKey(Object, String)} and
     * {@link ShardProcedure#getShardingKeyFromPathCondition(String, String)}.
     */
    String pathPrefixForShardingKey = "/"+WEB_CONTEXT_PATH+"/"+sailingServiceRemotePath;
    
    String serverConfigurationServiceRemotePath = "service/serverconfiguration";

    String simulatorServiceRemotePath = "service/simulator";

    String dataMiningServiceRemotePath = "service/datamining";

    String dispatchServiceRemotePath = "service/dispatch";
}
