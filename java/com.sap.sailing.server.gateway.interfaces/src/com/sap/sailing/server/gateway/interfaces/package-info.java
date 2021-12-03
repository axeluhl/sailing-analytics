/**
 * Java interfaces for functionality from {@code com.sap.sailing.server.gateway}, particularly for accessing
 * functionality on remote server process instances exposed through the REST API implemented by the gateway
 * bundle.<p>
 * 
 * The gateway bundle can register service implementations using the interfaces exposed by this bundle here, so
 * that when the gateway bundle needs a refresh / reload, services specified in this bundle can still be discovered
 * and tracked and be bound and resolved again, e.g., after a restart of the gateway bundle.<p>
 * 
 * Furthermore, other bundles who would like to use these services won't need a dependency on the
 * {@code com.sap.sailing.server.gateway} bundle.
 * 
 * @author Axel Uhl (d043530)
 */
package com.sap.sailing.server.gateway.interfaces;