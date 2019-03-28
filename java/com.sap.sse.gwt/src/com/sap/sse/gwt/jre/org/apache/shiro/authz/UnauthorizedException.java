package org.apache.shiro.authz;

/**
 * Thrown to indicate a requested operation or access to a requested resource is not allowed.
 *
 * @since 0.1
 */
public class UnauthorizedException extends AuthorizationException {

    /**
     * Creates a new UnauthorizedException.
     */
    public UnauthorizedException() {
        super();
    }

    /**
     * Constructs a new UnauthorizedException.
     *
     * @param message
     *            the reason for the exception
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnauthorizedException.
     *
     * @param cause
     *            the underlying Throwable that caused this exception to be thrown.
     */
    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new UnauthorizedException.
     *
     * @param message
     *            the reason for the exception
     * @param cause
     *            the underlying Throwable that caused this exception to be thrown.
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}