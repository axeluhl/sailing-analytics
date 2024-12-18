package com.sap.sailing.domain.igtimiadapter;

import java.net.URL;
import java.util.function.Supplier;

import com.sap.sse.security.SecurityService;

/**
 * Used to create {@link IgtimiConnection}s. During bundle start-up, this factory may optionally be equipped with
 * explicit authentication credentials that can then be used for contacting an instance of the Igtimi REST API that may
 * have a separate user base than the server running this factory, or for choosing a user explicitly and different from
 * the user tracking or owning a race into which to load data from the Igtimi REST API.
 * <p>
 * 
 * Clients can use the {@link #hasCredentials()} method to determine if such default credentials were provided to this
 * factory. Based on the result they can then choose how to authenticate API requests: either with these default
 * credentials by using {@link #createConnection()}, or---if an explicit authentication is preferred---by using
 * {@link #createConnection(String)}, providing a user's bearer token that will work on the remote server whose base URL
 * has been provided to this factory at bundle start-up time. See {@link #getBaseUrl}.
 * <p>
 * 
 * If no default credentials are provided during bundle start-up ({@link #hasCredentials()} returning {@code false}),
 * explicit authentication through the {@link #createConnection(String)} method is recommended. Typically, clients would
 * obtain these credentials from an existing session of an authenticated user (e.g., the user starting the tracking for
 * a race), or from the user owning the race for which to obtain data from the Igtimi API, always assuming that the
 * application server running this factory shares its user base ({@link SecurityService}) with the remote REST
 * API-providing server.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface IgtimiConnectionFactory {
    URL getBaseUrl();
    
    boolean hasCredentials();
    
    /**
     * @param bearerToken
     *            if not {@code null}, this bearer token will be used to authenticate requests made through the
     *            {@link IgtimiConnection} returned; otherwise, if {@link #hasCredentials() credentials have been
     *            provided} to this factory at bundle start-up, those credentials will be used. Else, API requests will
     *            be made without authentication, which may lead to insufficient permissions and hence empty results.
     */
    IgtimiConnection createConnection(String bearerToken);

    /**
     * If {@link #hasCredentials() default credentials} were provided to this factory during bundle start-up, those will
     * be used for the connection created; otherwise, API requests will be made without authentication, which may lead
     * to insufficient permissions and hence empty results.
     */
    IgtimiConnection createConnection();
    
    /**
     * Use this method to create a connection if you'd like to prefer any {@link #hasCredentials() default credentials}
     * provided at bundle start-up time over any explicit credentials you may be able to supply otherwise. The supplier
     * will only be invoked if no {@link #hasCredentials() default credentials} exist for this factory.<p>
     * 
     * This is equivalent to calling:
     * <pre>
     *   hasCredentials()
     *       ? createConnection()
     *       : createConnection(bearerTokenSupplierIfHasNoCredentials.get())
     * </pre>
     */
    default IgtimiConnection createConnection(Supplier<String> bearerTokenSupplierIfHasNoCredentials) {
        return hasCredentials() ? createConnection() : createConnection(bearerTokenSupplierIfHasNoCredentials.get());
    }
}
