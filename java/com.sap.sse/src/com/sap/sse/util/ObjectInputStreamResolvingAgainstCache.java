package com.sap.sse.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import com.sap.sse.common.IsManagedByCache;

/**
 * During de-serialization, resolves objects managed by a cache of type <C> using a cache of that type passed to the
 * constructor. Objects are identified as being managed by such a cache by means of the marker interface
 * {@link IsManagedByCache} that their classes then have to implement, telling how resolving against the cache
 * has to work for instances of that type.
 * <p>
 * 
 * Additionally, class loading is managed using the current thread's context class loader which is required in and works
 * well for OSGi environments.
 * <p>
 * 
 * Create an instance of this class by first creating a subclass in the bundle that has all the dependencies required to
 * resolve all the classes whose instances are to be read from the stream and then instantiating this subclass. Otherwise,
 * the stream won't be able to find the classes. To make this need clear, this class is declared <code>abstract</code>.
 * 
 * @param <C>
 *            the cache type used for resolving objects
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public abstract class ObjectInputStreamResolvingAgainstCache<C> extends ObjectInputStream {
    private final C cache;

    /**
     * Package protected on purpose; instances to be created using a factory method on the {@link Replicable}. This
     * constructor {@link #enableResolveObject(boolean) enables resolving} by default.
     * 
     * @param cache must not be {@code null}
     */
    protected ObjectInputStreamResolvingAgainstCache(InputStream in, C cache) throws IOException {
        super(in);
        assert cache != null;
        this.cache = cache;
        enableResolveObject(true);
    }

    public C getCache() {
        return cache;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        String className = classDesc.getName();
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6554519
        // If using loadClass(...) on the class loader directly, an exception is thrown:
        // StreamCorruptedException: invalid type code 00
        return Class.forName(className, /* initialize */true, Thread.currentThread().getContextClassLoader());
    }

    @Override
    protected Object resolveObject(Object o) {
        Object result;
        if (o instanceof IsManagedByCache) {
            @SuppressWarnings("unchecked")
            IsManagedByCache<C> castResult = ((IsManagedByCache<C>) o).resolve(getCache());
            result = castResult;
        } else {
            result = o;
        }
        return result;
    }
}
