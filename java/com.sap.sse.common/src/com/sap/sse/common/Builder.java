package com.sap.sse.common;

/**
 * A builder interface for the "Builder" pattern. In order to chain setter calls on the builder, they all shall return
 * {@code this} object, typed as {@code BuilderT}. For this purpose, the {@link #self} method is available.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <BuilderT>
 *            the concrete builder type, defining the chaining-enabled return type for all setters
 * @param <T>
 *            the type of object built by this builder
 */
public interface Builder<BuilderT extends Builder<BuilderT, T>, T> {
    default BuilderT self() {
        @SuppressWarnings("unchecked")
        final BuilderT self = (BuilderT) this;
        return self;
    }
    
    T build() throws Exception;
}
