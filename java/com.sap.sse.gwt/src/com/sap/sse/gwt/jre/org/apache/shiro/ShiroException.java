package org.apache.shiro;

/**
 * Root exception for all Shiro runtime exceptions. This class is used as the root instead of
 * {@link java.lang.SecurityException} to remove the potential for conflicts; many other frameworks and products (such
 * as J2EE containers) perform special operations when encountering {@link java.lang.SecurityException}.
 *
 * @since 0.1
 */
public class ShiroException extends RuntimeException {

    /**
     * Creates a new ShiroException.
     */
    public ShiroException() {
        super();
    }

    /**
     * Constructs a new ShiroException.
     *
     * @param message
     *            the reason for the exception
     */
    public ShiroException(String message) {
        super(message);
    }

    /**
     * Constructs a new ShiroException.
     *
     * @param cause
     *            the underlying Throwable that caused this exception to be thrown.
     */
    public ShiroException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ShiroException.
     *
     * @param message
     *            the reason for the exception
     * @param cause
     *            the underlying Throwable that caused this exception to be thrown.
     */
    public ShiroException(String message, Throwable cause) {
        super(message, cause);
    }

}
