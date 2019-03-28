package org.apache.shiro.authz;

import org.apache.shiro.ShiroException;

/**
 * Exception thrown if there is a problem during authorization (access control check).
 *
 * @since 0.1
 */
public class AuthorizationException extends ShiroException {

    /**
     * Creates a new AuthorizationException.
     */
    public AuthorizationException() {
        super();
    }

    /**
     * Constructs a new AuthorizationException.
     *
     * @param message
     *            the reason for the exception
     */
    public AuthorizationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuthorizationException.
     *
     * @param cause
     *            the underlying Throwable that caused this exception to be thrown.
     */
    public AuthorizationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new AuthorizationException.
     *
     * @param message
     *            the reason for the exception
     * @param cause
     *            the underlying Throwable that caused this exception to be thrown.
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
