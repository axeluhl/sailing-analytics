package com.sap.sse.security;

/**
 * A cache used to resolve objects during replication-triggered de-serialization. For example, when a {@link User} object is sent
 * in an {@link ObjectOutputStream}, the object shall not be created a second time on the receiving side when an object representing
 * the same user already exists there.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Cache {

}
