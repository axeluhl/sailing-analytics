package com.sap.sailing.domain.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import com.sap.sse.common.IsManagedByCache;

/**
 * During de-serialization, resolves objects managed by a {@link DomainFactory} using the domain factory instance passed
 * to the constructor. Additionally, class loading is managed using the current thread's context class loader which is
 * required in and works well for OSGi environments.
 * <p>
 * 
 * Create an instance of this class using
 * {@link DomainFactory#createObjectInputStreamResolvingAgainstThisFactory(InputStream)}.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public abstract class ObjectInputStreamResolvingAgainstDomainFactory extends ObjectInputStream implements HasDomainFactory {
    private final DomainFactory domainFactory;
    
    /**
     * Package protected on purpose; instances to be created using
     * {@link DomainFactory#createObjectInputStreamResolvingAgainstThisFactory(InputStream)}. This constructor
     * {@link #enableResolveObject(boolean) enables resolving} by default.
     */
    protected ObjectInputStreamResolvingAgainstDomainFactory(InputStream in, DomainFactory domainFactory) throws IOException {
        super(in);
        this.domainFactory = domainFactory;
        enableResolveObject(true);
    }

    @Override
    public DomainFactory getDomainFactory() {
        return domainFactory;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        String className = classDesc.getName();
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6554519
        // If using loadClass(...) on the class loader directly, an exception is thrown:
        // StreamCorruptedException: invalid type code 00
        return Class.forName(className, /* initialize */ true, Thread.currentThread().getContextClassLoader());
    }
    
    @Override
    protected Object resolveObject(Object o) {
        Object result;
        if (o instanceof IsManagedByCache) {
            @SuppressWarnings("unchecked")
            IsManagedByCache<SharedDomainFactory> castResult = ((IsManagedByCache<SharedDomainFactory>) o).resolve(getDomainFactory());
            result = castResult;
        } else {
            result = o;
        }
        return result;
    }
}
