# User Management and Security

As a feature of the Sports Sponsorships Engine (SSE) which underlies the SAP Sailing Analytics, our Tennis engagements, parts of the Equestrian contributions and in the future perhaps more, we are about to introduce user management to the platform. Based on [Benjamin Ebling's Bachelor thesis](/doc/theses/20140915_Ebling_Authentication_and_Authorization_for_SAP_Sailing_Analytics.pdf) we are introducing [Apache Shiro](http://shiro.apache.org) to the platform.

[[_TOC_]]

## Shiro Integration into SSE

### Bundle Structure

The following bundles implement the Shiro-based security features for SSE:

#### com.sap.sse.security

This bundle contains the core Shiro libraries which so far are not yet part of the target platform. It provides basic services such as the `SecurityService` and utilities such as `SessionUtils` and `ClientUtils`. The `SecurityService` instance is created by the bundle activator and registered with the OSGi service registry.

`UsernamePasswordRealm` and `OAuthRealm` are two realm implementations provided by the bundle that can be used in `shiro.ini` configuration files. Both realms store and obtain user-specific data including the roles and permissions in a `UserStore` (see the [com.sap.sse.security.userstore.mongodb](/wiki/usermanagement#com.sap.sse.security.userstore.mongodb) section) which is an instance shared by the realm objects as well as the `SecurityService`.

A web bundle that wants to use Shiro-based security and user management features should declare the following in its `WEB-INF/web.xml` descriptor:

	<context-param>
		<param-name>shiroEnvironmentClass</param-name>
		<param-value>org.apache.shiro.web.env.IniWebEnvironment</param-value>
	</context-param>
	<listener>
		<listener-class>org.apache.shiro.web.env.EnvironmentLoaderListener</listener-class>
	</listener>
	<filter>
		<filter-name>ShiroFilter</filter-name>
		<filter-class>org.apache.shiro.web.servlet.ShiroFilter</filter-class>
	</filter>
	<!--	Make sure any request you want accessible to Shiro is filtered. "/*" 
		catches all requests. Usually this filter mapping is defined first (before all 
		others) to ensure that Shiro works in subsequent filters in the filter chain: -->
	<filter-mapping>
		<filter-name>ShiroFilter</filter-name>
		<url-pattern>/*</url-pattern>
		<dispatcher>REQUEST</dispatcher>
		<dispatcher>FORWARD</dispatcher>
		<dispatcher>INCLUDE</dispatcher>
		<dispatcher>ERROR</dispatcher>
	</filter-mapping>

It is then the `shiro.ini` configuration file which needs to be in the using bundle's classpath root that configures Shiro to use the elements provided by the `com.sap.sse.security` bundle. A typical `shiro.ini` configuration file using the `com.sap.sse.security` bundle could look like this:
<pre>

[main]
shiro.loginUrl = /security/ui/Login.html
shiro.successUrl = /UserManagement.html
anyofroles = com.sap.sse.security.AnyOfRolesFilter
anyofroles.loginUrl = ../security/ui/Login.html

credentialsMatcher = org.apache.shiro.authc.credential.Sha256CredentialsMatcher
# base64 encoding, not hex in this example:
credentialsMatcher.storedCredentialsHexEncoded = false
credentialsMatcher.hashIterations = 1024

# configure the username/password realm:
upRealm = com.sap.sse.security.UsernamePasswordRealm
upRealm.credentialsMatcher = $credentialsMatcher

# configure the OAuth realm:
oauthRealm = com.sap.sse.security.OAuthRealm

securityManager.realms = $upRealm, $oauthRealm

sessionManager = com.sap.sse.security.SecurityWebSessionManager
securityManager.sessionManager = $sessionManager

authc = com.sap.sse.security.CustomFilter
authc.loginUrl = ../security/ui/Login.html
authc.successUrl  = /security/ui/UserManagement.html

roles.loginUrl = ../security/ui/Login.html

sessionDAO = org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO
securityManager.sessionManager.sessionDAO = $sessionDAO

cacheManager = com.sap.sse.security.SessionCacheManager
securityManager.cacheManager = $cacheManager

[urls]
/security/ui/UserManagement.html = roles[admin]
/YourFantasyURL.html = anyofroles[admin,eventmanager]
</pre>

In addition to URL-based security that is configured in `shiro.ini`, using bundles can do two more things:

* Use `SecurityUtils.getSubject()` in server-side code to obtain the current subject on whose behalf the call is being executed. This allows the application to check for roles and permissions, as in
<pre>
    if (SecurityUtils.getSubject().checkRole("some-role")) {
        ... // do something for which the subject must have role "some-role"
    } else {
        ... // throw some security exception or simply don't carry out the transaction
    }
</pre>

* Use the `SecurityService` API to store and retrieve data such as preferences or settings and work with the user base, including creating, modifying and deleting user accounts and manipulating their roles. The `SecurityService` registers itself with the OSGi registry upon bundle activation. 
<pre>
    ServiceTracker<SecurityService, SecurityService> tracker = new ServiceTracker<>(context, SecurityService.class, /* customizer */ null);
    tracker.open();
    SecurityService securityService = tracker.waitForService(0);
</pre>
The security service offers methods such as `addSetting`, `setSetting` and `getSetting` to manage name/value pairs. The settings API is typed in the sense that when registering a setting 

#### com.sap.sse.security.userstore.mongodb
#### com.sap.sse.security.ui


### Using Shiro in SSE-Based Applications

## Security and User Management-Related Entry Points

## Sample Session