package com.sap.sailing.domain.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

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
     * {@link DomainFactory#createObjectInputStreamResolvingAgainstThisFactory(InputStream)}.
     */
    protected ObjectInputStreamResolvingAgainstDomainFactory(InputStream in, DomainFactory domainFactory) throws IOException {
        super(in);
        this.domainFactory = domainFactory;
    }

    @Override
    public DomainFactory getDomainFactory() {
        return domainFactory;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        String className = classDesc.getName();
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }
}
