# User Management and Security

As a feature of the Sports Sponsorships Engine (SSE) which underlies the SAP Sailing Analytics, our Tennis engagements, parts of the Equestrian contributions and in the future perhaps more, we are about to introduce user management to the platform. Based on [Benjamin Ebling's Bachelor thesis](/doc/theses/20140915_Ebling_Authentication_and_Authorization_for_SAP_Sailing_Analytics.pdf) we are introducing [Apache Shiro](http://shiro.apache.org) to the platform.

[[_TOC_]]

## Shiro Integration into SSE

### Bundle Structure

The following bundles implement the Shiro-based security features for SSE:

#### com.sap.sse.security

This bundle contains the core Shiro libraries which so far are not yet part of the target platform. It provides basic services such as the `SecurityService` and utilities such as `SessionUtils` and `ClientUtils`. The `SecurityService` instance is created by the bundle activator and registered with the OSGi service registry.


#### com.sap.sse.security.userstore.mongodb
#### com.sap.sse.security.ui


### Using Shiro in SSE-Based Applications

## Security and User Management-Related Entry Points

## Sample Session