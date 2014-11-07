package com.sap.sailing.domain.base;


/**
 * Classes implementing this interface have their instance life cycle managed by a {@link SharedDomainFactory}. This is important
 * particularly during de-serialization when the de-serialized instance needs to be replaced by an instance managed by
 * the domain factory.<p>
 * 
 * If the de-serializing stream is a {@link ObjectInputStreamResolvingAgainstDomainFactory} stream, it will automatically
 * do a {@link #resolve(Object)} for instances whose classes implement this interface. Implementing classes need to
 * delegate accordingly to the domain factory passed as argument.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface IsManagedByCache {
    IsManagedByCache resolve(SharedDomainFactory domainFactory);
}
