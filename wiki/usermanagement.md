# User Management and Security

As a feature of the Sports Sponsorships Engine (SSE) which underlies the SAP Sailing Analytics, our Tennis engagements, parts of the Equestrian contributions and in the future perhaps more, we are about to introduce user management to the platform. Based on [Benjamin Ebling's Bachelor thesis](/doc/theses/20140915_Ebling_Authentication_and_Authorization_for_SAP_Sailing_Analytics.pdf) we are introducing [Apache Shiro](http://shiro.apache.org) to the platform.

[[_TOC_]]

## Shiro Integration into SSE

### Bundle Structure

The following bundles implement the Shiro-based security features for SSE:

#### com.sap.sse.security

This bundle contains the core Shiro libraries which so far are not yet part of the target platform. It provides basic services such as the `SecurityService` and utilities such as `SessionUtils` and `ClientUtils`. The `SecurityService` instance is created by the bundle activator and registered with the OSGi service registry.

`UsernamePasswordRealm` and `OAuthRealm` are two realm implementations provided by the bundle that can be used in `shiro.ini` configuration files.

A typical `shiro.ini` configuration file using the `com.sap.sse.security` bundle could look like this:
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

#### com.sap.sse.security.userstore.mongodb
#### com.sap.sse.security.ui


### Using Shiro in SSE-Based Applications

## Security and User Management-Related Entry Points

## Sample Session